# Monitoreo y gestión de eventos (Sesión 30)

El backend publica sus métricas, Prometheus las recolecta cada 15 segundos, Grafana las muestra y Alertmanager avisa cuando una
regla se cumple de forma sostenida. Si la alerta es crítica, el backend abre solo un ticket P1 en el
[módulo de incidentes](incidentes.md).

```
Backend (/actuator/prometheus) <-- scrape 15 s -- Prometheus --reglas--> Alertmanager --webhook--> /api/monitoreo/alertas --> Ticket P1
                                                     |
                                                  Grafana (dashboard de las 4 señales doradas)
```

## Puesta en marcha

```bash
cp .env.example .env            # completa DB_PASSWORD, JWT_SECRET, APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD
./scripts/monitoreo-preparar.sh # genera MONITORING_TOKEN en .env y monitoring/secrets/monitoring_token
echo 'MONITORING_SIMULATION=true' >> .env      # solo laboratorio: habilita el simulador de incidentes
docker compose --profile app up -d --build     # backend + MySQL Master/Replica
docker compose -f docker-compose.monitoring.yml up -d
```

| Servicio | URL | Acceso |
| --- | --- | --- |
| Backend, métricas | http://localhost:8080/actuator/prometheus | `Authorization: Bearer <MONITORING_TOKEN>` |
| Prometheus | http://localhost:9090 (Status → Targets, Alerts) | sin clave, solo `127.0.0.1` |
| Alertmanager | http://localhost:9093 | sin clave, solo `127.0.0.1` |
| Grafana | http://localhost:3000 | `admin` / `GRAFANA_ADMIN_PASSWORD` (por defecto `utp_admin`) |

Si ejecutas el backend fuera de Docker (`mvn spring-boot:run`), define `MONITORING_TOKEN` en tu entorno y cambia el target de
`monitoring/prometheus.yml` a `host.docker.internal:8080`.

## Qué mide y cómo

| Señal dorada | Métrica de Spring Boot / Micrometer | Panel y umbral |
| --- | --- | --- |
| Saturación | `process_cpu_usage`, `jvm_memory_used_bytes`, `hikaricp_connections_active` | Gauges: verde < 70 %, ámbar 70–85 %, rojo > 85 % |
| Tráfico | `http_server_requests_seconds_count` | RPS = `sum(rate(...[1m]))` |
| Latencia | `http_server_requests_seconds_bucket` | **p95** con `histogram_quantile` (nunca el promedio): verde < 0,3 s, ámbar hasta 1 s, rojo > 1 s |
| Errores | `http_server_requests_seconds_count{status=~"5.."}` | Porcentaje 5xx sobre el total; rojo desde 5 % |
| Disponibilidad | `up{job="inmobiliaria_api"}` | State timeline: 1 = UP, 0 = DOWN |

La ficha del curso usa nombres de Node.js (`http_requests_total`, `/metrics`). En Spring Boot los nombres son los de la tabla y la
ruta es `/actuator/prometheus`. Además, `rate(...) > 0.05` mide más de 0,05 errores por segundo, no el 5 %; por eso la regla de este
proyecto divide los errores 5xx entre el total de peticiones.

## Reglas de alerta (`monitoring/alert_rules.yml`)

| Alerta | Condición | Ventana (`for`) | Prioridad |
| --- | --- | --- | --- |
| `TasaErroresCritica5xx` | más del 5 % de respuestas 5xx y más de 0,1 petición/s | 1 min (laboratorio; 5 min en producción) | P1 |
| `ApiCaida` | `up == 0` | 1 min | P1 |
| `LatenciaP95Alta` | p95 > 1 s | 2 min | P3 |
| `CpuAlta` | CPU del proceso > 85 % | 5 min | P3 |
| `PoolBaseDatosSaturado` | conexiones activas > 80 % del máximo | 2 min | P2 |
| `MemoriaHeapAlta` | heap > 90 % del máximo | 5 min | P3 |

La ventana sostenida evita la fatiga de alertas: un pico de un segundo no avisa a nadie, un problema de minutos sí.

## De la alerta al ticket

Alertmanager envía cada alerta a `POST /api/monitoreo/alertas` con el token Bearer. El backend:

1. crea un ticket `[ALERTA] <nombre>: <resumen>` con la prioridad de la etiqueta `priority` (o P1 si la severidad es `critical`);
2. calcula el SLA con la matriz del módulo de incidentes (P1 = 2 h);
3. no crea otro ticket mientras haya uno Abierto o En Proceso con el mismo título;
4. ignora las alertas `resolved`: el cierre del ticket lo confirma una persona.

Para avisar también por Discord: guarda la URL del webhook en `monitoring/secrets/discord_webhook` y levanta con
`ALERTMANAGER_CONFIG=alertmanager.discord.yml`. Otra opción es crear el punto de contacto desde Grafana (Alerting → Contact points).

## Simulación de incidentes (Ficha 04)

- **Errores 5xx sin romper nada:** con `MONITORING_SIMULATION=true`, `POST /api/monitoreo/simular?tipo=error500` responde 500 y
  `?tipo=lento&ms=2000` responde con demora. Exige el token y responde 404 si la variable no está activa.
- **Carga con k6:** `k6 run --vus 50 --duration 60s -e MONITORING_TOKEN=$(cat monitoring/secrets/monitoring_token) scripts/script_carga.js`.
- **Caída real:** `docker stop inmobiliaria-backend` dispara `ApiCaida`; `docker start inmobiliaria-backend` la recupera.
- **Todo automatizado:** `./scripts/prueba-monitoreo.sh` (lo ejecuta también el CI) verifica métricas, target UP, reglas, Grafana,
  alerta en FIRING, ticket P1, ausencia de duplicados y recuperación.

## Seguridad

- Sin `MONITORING_TOKEN`, `/actuator/prometheus` y `/api/monitoreo/**` rechazan toda petición (401).
- Solo se exponen `health` y `prometheus`; `env`, `beans`, `heapdump` y el resto de endpoints de Actuator no están disponibles.
- Prometheus, Alertmanager y Grafana publican su puerto únicamente en `127.0.0.1`.
- En producción: `MANAGEMENT_ENDPOINTS=health`, `MONITORING_SIMULATION=false` y una clave propia para Grafana. En Render, monta Prometheus
  en un servicio privado o usa Grafana Cloud en lugar de publicar las métricas en Internet.
- `monitoring/secrets/` está en `.gitignore`.
