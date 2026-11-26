# Alta disponibilidad y automatización de respaldos (Sesión 32)

Complementa el plan de [continuidad](continuidad.md) (Sesión 31). Allí se definió **qué** se debe recuperar y en cuánto tiempo (RTO/RPO);
aquí se reduce la probabilidad de que la caída llegue a ser un desastre (alta disponibilidad) y se quita la intervención humana de los
respaldos. Alta disponibilidad (HA) y recuperación ante desastres (DRP) no son lo mismo: HA evita que un fallo de un componente se note;
DRP recupera cuando ya hubo pérdida. Un sistema serio necesita las dos.

Todo lo que se afirma como "medido" proviene de la ejecución real del CI:
[run 45906572430](https://github.com/ciscotramos/sistema-gestion-inmobiliaria/actions/runs/45906572430), jobs `alta-disponibilidad` y
`respaldo-automatico` (cada uno guarda su registro como artefacto `evidencia-failover` y `evidencia-respaldo-automatico`).

## 1. Qué había y dónde estaban los puntos únicos de falla (SPOF)

| Componente | Antes de la Sesión 32 | Riesgo |
| --- | --- | --- |
| API Spring Boot | 1 contenedor | Si cae, el sistema se detiene (SPOF) |
| Entrada al sistema | La API publicada directamente en `:8080` | Sin balanceo ni reintento |
| MySQL | Master + Replica asíncrona (GTID) | La Replica protege de la caída del Master, pero la promoción es manual |
| Respaldos | Scripts de la Sesión 31 que alguien debía ejecutar | Depende de una persona; la copia vive en el mismo servidor |

## 2. Alta disponibilidad de la API: dos nodos detrás de Nginx

```
Usuario / Frontend ──► Nginx (:8080) ──► api-node-1 ┐
                       balanceador   └─► api-node-2 ┴──► MySQL Master (escritura) / Replica (lectura)
```

- La API es **sin estado** (la sesión viaja en un JWT firmado con el mismo `JWT_SECRET`), por eso se puede replicar sin sesiones
  compartidas.
- `nginx/nginx.conf`: `upstream` con los dos nodos, `max_fails=2 fail_timeout=10s` (verificación pasiva: tras 2 fallos el nodo se omite
  10 s), `proxy_connect_timeout 1s` y `proxy_next_upstream error timeout http_502 http_503 http_504` para **reintentar la misma petición en
  el otro nodo**. La cabecera `X-Served-By` indica qué nodo respondió.
- `docker-compose.ha.yml` (se usa junto al compose base, reemplaza al perfil `app`):
  `docker compose -f docker-compose.yml -f docker-compose.ha.yml up -d --build`. Cada nodo tiene *healthcheck* sobre
  `/actuator/health`; el balanceador no arranca hasta que ambos están sanos. El frontend no cambia: sigue llamando a `localhost:8080`.

### Resultado medido (job `alta-disponibilidad`)

Se envía tráfico continuo (10 peticiones/s) a `GET /api/caracteristicas`, se detiene el nodo 1 con `docker stop` y 12 s después se reinicia.

| Medida | Resultado |
| --- | --- |
| Reparto de 40 peticiones con ambos nodos | nodo 1 = 21, nodo 2 = 19 |
| Peticiones totales durante la prueba | 316 |
| Errores visibles para el usuario | **0** |
| Peticiones durante la caída | 42, todas con respuesta 200: 41 las atendió el nodo 2 y 1 el nodo 1 en el mismo instante de la orden de detenerlo (0,0 s), cuando aún se estaba cerrando |
| Latencia máxima | 1008 ms (8 peticiones > 900 ms: Nginx esperó 1 s al nodo caído y reintentó en el sano) |
| Intentos fallidos de Nginx hacia el nodo caído | 11 (luego lo omite 10 s) |
| Nodo 1 reintegrado | 43 peticiones atendidas por él tras reiniciarlo; 12 s hasta estar sano |

Lectura: la caída de un nodo **no interrumpió el servicio**; el costo fue un pico de ~1 s en algunas peticiones mientras Nginx detectaba el fallo.

### Disponibilidad teórica (SLA)

Referencia de la sesión: 99 % = 3,65 días/año de caída; 99,9 % = 8,76 h; 99,99 % = 52,56 min; 99,999 % = 5,26 min.
Dos nodos independientes de 99,9 % cada uno dan, en teoría, 1 − 0,001² = 99,9999 % para la capa de API. Pero la disponibilidad total es
la **multiplicación** de la cadena (Nginx × API × base de datos): con Nginx y MySQL de un solo servidor cada uno, el sistema queda
limitado por ellos. Con supuestos de 99,95 % (Nginx) y 99,9 % (MySQL) el total es ≈ 99,85 %, es decir, cerca de **99,9 % (≈ 8,76 h/año)**.
Es el objetivo realista de este proyecto; "cuatro nueves" exigiría duplicar también el balanceador y automatizar la promoción de MySQL.
Estos porcentajes son supuestos de cálculo, no mediciones.

## 3. Respaldos automáticos, cifrados e inmutables

`docker-compose.backup.yml` agrega dos servicios (se usa con el compose base):

```
mysql-master ──mysqldump──► contenedor "respaldo" ──gzip──► SHA-256 ──► AES-256 ──► bucket S3 con Object Lock (COMPLIANCE)
                                   │                                                     │
                                   └─ copia local cifrada (3 últimas)                    └─ se vuelve a descargar y se compara el SHA-256
```

| Pieza | Qué hace |
| --- | --- |
| `scripts/respaldo/programador.sh` | Repite el respaldo cada `BACKUP_INTERVALO_SEGUNDOS` (86400 = diario) o a la hora fija `BACKUP_HORA_UTC`. Sin intervención humana. |
| `scripts/respaldo/ciclo.sh` | `mysqldump --single-transaction --routines --triggers --events --source-data=2` → gzip → SHA-256 → `openssl enc -aes-256-cbc -pbkdf2` → subida con `--object-lock-mode COMPLIANCE` → verificación (modo de bloqueo y descarga con SHA-256 idéntico) → retención local. |
| `scripts/respaldo/preparar-bucket.sh` | Crea el bucket con Object Lock y una retención por defecto COMPLIANCE (aunque alguien suba un objeto sin indicar el bloqueo, queda protegido). |
| *healthcheck* del contenedor | Pasa a `unhealthy` si no hubo un respaldo exitoso en los últimos 2 periodos: es la señal de que la automatización dejó de funcionar. |
| `almacen-s3` | Servidor compatible con S3 (Scality CloudServer) **solo para laboratorio y CI**. En producción se reemplaza por un bucket real de otra región o proveedor. Las imágenes comunitarias de MinIO se retiraron de Docker Hub, por eso no se usa. |

Variables (en `.env`, ver `.env.example`): `BACKUP_PASS` (clave AES-256, **guardarla fuera del servidor**: sin ella no hay restauración),
`S3_ACCESS_KEY`, `S3_SECRET_KEY`, `BACKUP_BUCKET`, `BACKUP_RETENCION_DIAS`, `BACKUP_INTERVALO_SEGUNDOS`, `BACKUP_HORA_UTC`.

### Resultado medido (job `respaldo-automatico`, ciclo cada 20 s para poder observarlo)

| Verificación | Resultado |
| --- | --- |
| Ejecución sin intervención | 4 ciclos automáticos; intervalo máximo entre respaldos 22 s; cada ciclo tarda 1-2 s |
| Cifrado AES-256 | Cabecera `Salted__` (formato OpenSSL); sin clave el objeto no es un gzip válido; con clave incorrecta el descifrado falla; con la correcta el SHA-256 coincide con el registrado al crearlo |
| Inmutabilidad (WORM) | Modo COMPLIANCE; borrar la versión, borrar con `--bypass-governance-retention` y acortar la retención a 1 hora fueron **rechazados** (`AccessDenied`) con las mismas credenciales que usa el sistema |
| Restauración desde la copia externa | Descarga → descifrado → servidor MySQL nuevo → 17 tablas, `mysqlcheck` sin errores y **las mismas filas por tabla que el Master** |
| Tiempo (descarga + descifrado + restauración + verificación) | **12 s** con la base de ejemplo (170 filas) |
| Rotación local | Se conservan 3 copias cifradas locales |

## 4. Cómo queda frente a los objetivos de la Sesión 31

| Objetivo | Meta | Estado |
| --- | --- | --- |
| API (Tier 1) RTO | 15 min | Cumplido y mejorado: la caída de un nodo no interrumpe el servicio (0 errores en 316 peticiones). Reponer el nodo caído: 12 s |
| Base de datos, restauración (RTO) | 60 min | 12 s medidos con 170 filas: **no es representativo** de un volumen real, hay que repetir la prueba al crecer la base |
| Base de datos RPO | 5 min | **No cubierto por el respaldo diario por sí solo.** Se cumple al programar además los binlogs incrementales de la Sesión 31 (`scripts/respaldar.sh incremental`) cada 5 min o al bajar `BACKUP_INTERVALO_SEGUNDOS` |
| Regla 3-2-1-1-0 | 3 copias, 2 medios, 1 externa, 1 inmutable, 0 errores | Inmutable: verificada. 0 errores: verificada con restauración real. Externa y 2 medios: en el laboratorio el "almacén" corre en el mismo host; en producción debe ser un bucket real de otra región |

## 5. Runbook

1. **Un nodo de la API cae:** no hay que hacer nada para que el servicio siga. `docker compose -f docker-compose.yml -f docker-compose.ha.yml ps`
   para ver cuál; `docker start inmobiliaria-api-1` (o `logs api-node-1` para la causa). Si el nodo cambió de IP y Nginx no lo ve:
   `docker exec inmobiliaria-lb nginx -s reload`.
2. **Los respaldos dejaron de salir:** el contenedor `inmobiliaria-respaldo` figura `unhealthy`; ver `docker logs inmobiliaria-respaldo`
   (busca `CICLO_FALLO`).
3. **Restaurar desde la copia externa:** descargar el objeto del bucket, `openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass env:BACKUP_PASS`,
   y `./scripts/probar-restauracion.sh respaldo.sql.gz` (acompañado de su `.sha256`). Es exactamente lo que automatiza
   `scripts/prueba-respaldo-automatico.sh`.

## 6. Límites conocidos (lo que esto todavía no resuelve)

- **Nginx es un único punto de falla.** Duplicarlo exige una IP virtual (keepalived) o un balanceador administrado de la nube.
- **MySQL sigue con un solo Master.** La Replica es asíncrona y su promoción es manual (`scripts/promover-replica.sh`); un Master caído
  sigue siendo una interrupción.
- **Las peticiones POST no se reintentan** en el otro nodo (Nginx no reintenta métodos no idempotentes por defecto): si un nodo cae
  justo en medio de una escritura, ese usuario verá un error y deberá repetir. Es intencional: evita duplicar operaciones.
- **Prometheus sigue apuntando a un solo destino** (`inmobiliaria-backend`); el monitoreo por nodo y la alerta `ApiCaida` por instancia son una mejora pendiente.
- Los nodos corren en el **mismo host Docker**: protege de la caída de un proceso o contenedor, no de la caída del servidor completo.
- El respaldo usa el usuario `root` de MySQL por simplicidad de laboratorio; en producción conviene un usuario dedicado con privilegios mínimos de lectura.
- La clave `BACKUP_PASS` es un secreto crítico: si se pierde, los respaldos cifrados son irrecuperables.
- El `awscli` empaquetado en Debian intentaba averiguar la región consultando el servicio de metadatos de la nube y fallaba de forma
  intermitente en CI; se corrigió fijando `AWS_DEFAULT_REGION` y `AWS_EC2_METADATA_DISABLED`.
