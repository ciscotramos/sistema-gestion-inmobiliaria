# Continuidad del servicio y plan de recuperación ante desastres (Sesión 31)

Plan de continuidad (ITSCM) del Sistema Web de Gestión Inmobiliaria, alineado con ISO/IEC 20000-1:2018 (cláusula 8.7.2, gestión de la
continuidad del servicio). La obligación de **probar** las copias de seguridad viene de ISO/IEC 27001:2022, control A.8.13
(*Información de respaldo*): las copias se mantienen y se prueban con regularidad según la política de respaldo.

Este documento se apoya en lo ya construido: la replicación Master/Replica de [base-de-datos.md](base-de-datos.md), las alertas de
[monitoreo.md](monitoreo.md) (Sesión 30) y el módulo de [incidentes](incidentes.md) (Sesión 28).

## 1. Matriz de análisis de impacto al negocio (BIA)

RPO = pérdida máxima de datos aceptable; RTO = tiempo máximo para volver a operar. Los Tiers siguen la tabla de la sesión
(Tier 1: RTO < 15 min y RPO de 0 a 5 min; Tier 2: RTO de 1 a 4 h y RPO de 15 a 60 min; Tier 3: RTO de 4 a 24 h y RPO de 24 h;
Tier 4: RTO > 48 h y RPO semanal). Son **objetivos**; lo medido hasta hoy está en la sección 7.

| Componente | Tier | RPO | RTO | Estrategia | Justificación del impacto |
| --- | --- | --- | --- | --- | --- |
| Base de datos MySQL (Master + Replica) | 1 | 5 min | 15 min si falla el servidor (promover la Replica); 60 min si el daño es lógico o se pierde el sitio (restaurar) | Backup & Restore con binlogs, más la Replica como alta disponibilidad | Guarda propiedades, clientes, alquileres, operaciones, comisiones, usuarios y tickets. Sin ella nada funciona y los datos no se pueden recrear. |
| API Backend Spring Boot (incluye autenticación JWT y control de roles) | 1 | 0 (sin estado) | 15 min | Backup & Restore: redespliegue desde Git y la imagen Docker | Todas las operaciones y el inicio de sesión pasan por ella. No guarda datos: el código y la configuración están en Git y en variables de entorno. |
| Frontend (React) | 2 | 0 (sin estado) | 1 h | Backup & Restore: reconstrucción desde Git | Sin él los usuarios no pueden operar desde el navegador, pero la API y los datos siguen a salvo. |
| Módulo de incidentes y monitoreo (Prometheus, Alertmanager, Grafana) | 3 | 24 h | 8 h | Backup & Restore: configuración versionada en `monitoring/` | Sin monitoreo se pierde visibilidad, no se detiene la operación. Los dashboards y reglas se recrean con el aprovisionamiento. |
| Reportes y dashboard | 3 | 24 h | 8 h | Backup & Restore | Se calculan a partir de la base de datos: vuelven al recuperarla. |
| Históricos y registros antiguos (logs, respaldos viejos, evidencias) | 4 | 7 días | más de 48 h | Backup & Restore | Útiles para auditoría, no para operar. |

## 2. Estrategia de recuperación (DRP)

| Estrategia | Decisión | Motivo |
| --- | --- | --- |
| Backup & Restore | **Adoptada** | Es la más barata y suficiente para un sistema de este tamaño. Se recupera desde copias de seguridad; el RTO se mide en minutos u horas. |
| Pilot Light | Mejora futura | Mantener una réplica de la base en otra región reduciría el RTO por pérdida del sitio. Se evaluará cuando existan datos reales de costos en la nube. |
| Warm Standby | Descartada | Exige mantener un entorno reducido funcionando todo el tiempo; el costo no se justifica para el volumen de operaciones del proyecto. |

La Replica local no es una estrategia de recuperación ante desastres: protege de la caída de un servidor, **no** del borrado de datos,
porque copia los errores casi al instante (se comprobó en el simulacro, sección 7).

## 3. Escenario crítico y runbook

**Escenario simulado:** ataque de ransomware o borrado masivo en la base de datos. La Replica copia el daño y la API deja de servir datos
correctos.

**Disparador (Sesión 30):** Alertmanager envía `TasaErroresCritica5xx` (más del 5 % de respuestas 5xx durante 5 min en producción, 1 min en
laboratorio) o `ApiCaida` (`up == 0`). El backend abre solo un ticket **P1** (SLA 2 h) en el módulo de incidentes y el líder de turno
declara el desastre.

| Fase | Acción técnica | Responsable | Min. | Verificación |
| --- | --- | --- | --- | --- |
| 1. Detección y aislamiento | Atender el ticket P1 abierto por la alerta. Detener el backend para frenar escrituras: `docker compose stop backend` (en Render, *Suspend*). Congelar la base dañada: `docker exec -e MYSQL_PWD="$DB_PASSWORD" inmobiliaria-master mysql -uroot -e "SET GLOBAL super_read_only=ON"`. Conservar los binlogs: `./scripts/respaldar.sh incremental`. Ubicar el inicio de la transacción dañina con `SHOW BINLOG EVENTS IN '<archivo>'` y anotar archivo y posición. | Help Desk y líder (comandante del incidente); Especialista DB | 10 | Ticket P1 abierto; backend detenido; binlogs copiados en `respaldos/binlogs`; archivo y posición anotados. |
| 2. Aprovisionamiento alterno | Crear un servidor MySQL nuevo, sin tocar el dañado. Lo hace `probar-restauracion.sh` con `mysql:8.0-debian`; en Render, crear una instancia de base administrada. | Especialista DB | 10 | `mysqladmin ping` responde `mysqld is alive`. |
| 3. Restauración de datos | `CONSERVAR=1 RED_DOCKER=inmobiliaria_default CLAVE_DESTINO="$DB_PASSWORD" HASTA_ARCHIVO=<archivo> HASTA_POS=<posición> ./scripts/probar-restauracion.sh` (Full + binlogs hasta justo antes del daño). | Especialista DB | 15 | Aparece `RESTAURACION_OK`: suma SHA-256 correcta, 17 tablas, `mysqlcheck` sin errores y filas iguales a las del punto previo. |
| 4. Despliegue Backend/Front | Apuntar el backend al servidor restaurado: `DB_HOST_BACKEND=inmobiliaria-restauracion DB_HOST_REPLICA_BACKEND=inmobiliaria-restauracion docker compose --profile app up -d --build backend`. En Render, cambiar `DB_HOST` y `DB_HOST_REPLICA` y redesplegar. El frontend no cambia. | Equipo de Desarrollo | 15 | `GET /actuator/health` responde `UP`. |
| 5. Validación y apertura | `GET /api/caracteristicas` devuelve los datos recuperados; iniciar sesión y consultar `/api/tickets`; en Prometheus, `up{job="inmobiliaria_api"} == 1` y la alerta pasa a *resolved*. Registrar la solución y cerrar el ticket; reconstruir la Replica y redactar el post-mortem. | Líder y Help Desk | 10 | Datos correctos en la API, alerta resuelta, ticket Resuelto con la solución registrada. |

Tiempo total planificado: 60 min, dentro del SLA de 2 h de un ticket P1 y del RTO de la base de datos.

**Si falla solo el servidor** (disco o proceso, sin daño lógico), no se restaura: `./scripts/promover-replica.sh` convierte la Replica en
Master (RTO objetivo 15 min). El backend se apunta a ese servidor con las mismas variables de la fase 4.

## 4. Política de respaldos y regla 3-2-1-1-0

| Criterio | Configuración | Herramienta y evidencia |
| --- | --- | --- |
| Esquema, frecuencia y retención | **Full** diario (02:00) con `mysqldump --single-transaction`, que guarda la posición del binlog. **Incremental** cada 5 min con los *binary logs* de MySQL (esto fija el RPO de 5 min). Diferencial: no se usa, porque el binlog ya cubre lo que cambia entre dos Full. Retención: 7 diarios, 4 semanales (domingo), 12 mensuales (día 1) y binlogs 30 días. | `scripts/respaldar.sh {completo, incremental, rotar}`; cron o tarea programada. Prueba de la rotación en el simulacro. |
| 3 copias en 2 medios distintos | Datos en producción, copia local en disco del servidor (medio 1) y copia externa en almacenamiento de objetos o artefacto en la nube (medio 2). | Directorio `respaldos/` y artefacto `respaldo-externo` del CI. |
| 1 copia externa (off-site) | Cada ejecución del CI sube el respaldo a un almacenamiento ajeno al servidor, con retención de 30 días. En producción: un bucket de otro proveedor o región. | Artefacto `respaldo-externo` de GitHub Actions. |
| 1 copia inmutable (anti-ransomware) | **Diseño:** bucket con bloqueo de objetos (*Object Lock*, modo COMPLIANCE) y retención mínima de 30 días, de modo que ni un atacante con credenciales pueda borrar o cifrar la copia. **Estado:** el artefacto del CI no se puede modificar, pero un administrador del repositorio sí puede borrarlo; la inmutabilidad completa queda **pendiente** hasta contratar el bucket. | Configuración del bucket (pendiente). |
| 0 errores en la prueba de restauración | Cada respaldo se restaura en un servidor nuevo y se verifica: SHA-256, 17 tablas, `mysqlcheck` y filas por tabla. Se ejecuta en cada cambio y debe dar cero errores. | `scripts/probar-restauracion.sh` y el job `Continuidad` del CI (registro `evidencia-continuidad`). |

Programación sugerida (cron del servidor que aloja el Master):

```
0 2 * * *    cd /ruta/proyecto && ./scripts/respaldar.sh completo && ./scripts/respaldar.sh rotar
*/5 * * * *  cd /ruta/proyecto && ./scripts/respaldar.sh incremental
```

## 5. Articulación con la observabilidad (Sesión 30)

| Señal | Qué dispara |
| --- | --- |
| Alerta `TasaErroresCritica5xx` o `ApiCaida` (P1) | Ticket P1 automático y declaración de desastre (fase 1 del runbook). |
| `up{job="inmobiliaria_api"} == 1` y alerta *resolved* | Criterio de cierre del desastre (fase 5). |
| Ticket con módulo afectado "Base de datos e infraestructura" | Registro del incidente, de la solución aplicada y del tiempo de resolución. |

## 6. Cómo ejecutarlo

```bash
docker compose up -d --wait mysql-master mysql-replica && docker compose up replica-init
./scripts/simulacro-desastre.sh        # Full, desastre, restauración, retención, backend y promoción
./scripts/respaldar.sh completo        # o incremental / rotar, solo respaldos
./scripts/probar-restauracion.sh       # prueba de restauración del último respaldo
```

El simulacro destruye los datos del Master de laboratorio: no ejecutarlo contra una base con datos reales. Se ejecuta solo en el CI
(job **Continuidad**).

## 7. Resultado medido en el CI

El job **Continuidad (respaldo, restauración y simulacro DRP)** ejecutó el simulacro de principio a fin con cero errores:

| Prueba | Resultado |
| --- | --- |
| Respaldo Full del Master (12 KB, 17 tablas) | Correcto, con posición del binlog y SHA-256 |
| Borrado masivo de `caracteristicas` | El Master y la Replica quedaron en 0 filas: la replicación copió el daño |
| Restauración Full + binlogs hasta justo antes del daño | 17 tablas, `mysqlcheck` sin errores y filas idénticas en las 17 tablas; incluye los 5 registros posteriores al Full |
| Servidor nuevo disponible y datos cargados | 17 s |
| Backend desplegado contra el servidor restaurado | `GET /actuator/health` = UP; `GET /api/caracteristicas` = 20 elementos (el Master dañado tiene 0) |
| Recuperación completa, del desastre a la API sirviendo datos | 59 s (con la construcción de la imagen del backend) |
| Retención | De 10 respaldos diarios quedan los 7 más recientes |
| Promoción de la Replica | Menos de 1 s; acepta escrituras |

## 8. Riesgo residual y límites

- Las cifras son de un laboratorio con una base pequeña (12 KB). En producción el tiempo de carga crece con el tamaño de los datos y el
  aprovisionamiento de una base administrada en Render puede tardar varios minutos: los RTO de la matriz son objetivos y se deben
  revalidar con un simulacro real en la nube.
- El RPO de 5 min se cumple si los binlogs se copian cada 5 min. Lo que ocurra entre dos copias se pierde si el servidor se destruye por
  completo. Reducirlo exige replicación continua a otra región (Pilot Light).
- La copia inmutable (Object Lock) y la copia en otro proveedor están diseñadas, no contratadas.
- Ante un ransomware o la pérdida de todo el sitio, el RTO real puede superar el de Tier 1 (15 min) porque implica restaurar, no
  promover. Se acepta este riesgo hasta contar con presupuesto para Pilot Light.
