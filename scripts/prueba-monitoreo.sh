#!/bin/bash
# Prueba de extremo a extremo del monitoreo (Taller Sesión 30):
#  Ficha 01  /actuator/prometheus protegido y con métricas reales
#  Ficha 02  Prometheus (target UP, reglas cargadas, promtool/amtool OK)
#  Ficha 03  Grafana con datasource y dashboard de 4 señales doradas aprovisionados
#  Ficha 04  incidente simulado -> alerta FIRING -> Alertmanager -> ticket P1 -> recuperación
# Requisitos: backend y MySQL ya levantados (docker compose --profile app up -d backend) con
# MONITORING_TOKEN y MONITORING_SIMULATION=true en .env; variables APP_ADMIN_EMAIL/APP_ADMIN_PASSWORD cargadas.
set -e
cd "$(dirname "$0")/.."
BASE=${BASE_URL:-http://localhost:8080}
PROM=${PROM_URL:-http://localhost:9090}
AM=${AM_URL:-http://localhost:9093}
GRAFANA=${GRAFANA_URL:-http://localhost:3000}
EMAIL=${APP_ADMIN_EMAIL:?Define APP_ADMIN_EMAIL}
PASS=${APP_ADMIN_PASSWORD:?Define APP_ADMIN_PASSWORD}
GF_PASS=${GRAFANA_ADMIN_PASSWORD:-utp_admin}
COMPOSE="docker compose -f docker-compose.monitoring.yml"
fallo() { echo "FALLO: $1"; exit 1; }
codigo() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
hora() { date -u '+%Y-%m-%d %H:%M:%S UTC'; }

./scripts/monitoreo-preparar.sh >/dev/null
TOKEN=$(cat monitoring/secrets/monitoring_token)
TOK="Authorization: Bearer $TOKEN"

echo "== Ficha 01: instrumentar el backend ($(hora)) =="
for i in $(seq 1 40); do [ "$(codigo "$BASE/actuator/health")" = "200" ] && break; sleep 3; done
[ "$(codigo "$BASE/actuator/health")" = "200" ] || fallo "/actuator/health no responde 200"
echo "OK: /actuator/health -> $(curl -s "$BASE/actuator/health")"
[ "$(codigo "$BASE/actuator/prometheus")" = "401" ] || fallo "/actuator/prometheus sin token debía dar 401"
echo "OK: /actuator/prometheus sin token -> 401"
FALSO="Authorization: Bearer $(openssl rand -hex 12)"
[ "$(codigo -H "$FALSO" "$BASE/actuator/prometheus")" = "401" ] || fallo "token falso debía dar 401"
echo "OK: /actuator/prometheus con token falso -> 401"
[ "$(codigo "$BASE/actuator/env")" != "200" ] || fallo "/actuator/env no debe estar expuesto"
echo "OK: /actuator/env no está expuesto"
curl -s "$BASE/api/public/status" >/dev/null
METRICAS=$(curl -s -H "$TOK" "$BASE/actuator/prometheus")
for m in jvm_memory_used_bytes process_cpu_usage http_server_requests_seconds_count http_server_requests_seconds_bucket hikaricp_connections_active; do
  echo "$METRICAS" | grep -q "^$m" || fallo "falta la métrica $m en /actuator/prometheus"
done
echo "OK: /actuator/prometheus con token -> 200 y métricas reales. Muestra:"
echo "$METRICAS" | grep -E '^(process_cpu_usage|jvm_memory_used_bytes\{area="heap"|http_server_requests_seconds_count|hikaricp_connections_(active|max))' | head -8

echo "== Ficha 02: Prometheus, reglas y Alertmanager ($(hora)) =="
docker run --rm --entrypoint promtool -v "$PWD/monitoring:/etc/prometheus:ro" -v "$PWD/monitoring/secrets:/etc/prometheus/secrets:ro" \
  prom/prometheus:v2.54.1 check config /etc/prometheus/prometheus.yml
docker run --rm --entrypoint amtool -v "$PWD/monitoring/alertmanager:/cfg:ro" -v "$PWD/monitoring/secrets:/secrets:ro" \
  prom/alertmanager:v0.27.0 check-config /cfg/alertmanager.yml
$COMPOSE up -d
echo "Esperando a que el target pase a UP..."
for i in $(seq 1 40); do
  UP=$(curl -s "$PROM/api/v1/query?query=up%7Bjob%3D%22inmobiliaria_api%22%7D" | jq -r '.data.result[0].value[1] // empty' 2>/dev/null || true)
  [ "$UP" = "1" ] && break; sleep 3
done
[ "$UP" = "1" ] || { curl -s "$PROM/api/v1/targets" | jq '.data.activeTargets[] | {job: .labels.job, health, lastError}'; fallo "el target inmobiliaria_api no está UP"; }
echo "OK: target inmobiliaria_api en estado UP"
curl -s "$PROM/api/v1/targets" | jq -r '.data.activeTargets[] | "  target \(.labels.job) -> \(.scrapeUrl) [\(.health)] intervalo \(.scrapeInterval)"'
REGLAS=$(curl -s "$PROM/api/v1/rules" | jq -r '[.data.groups[].rules[] | select(.type=="alerting")] | length')
[ "$REGLAS" -ge 6 ] || fallo "se esperaban 6 reglas de alerta cargadas y hay $REGLAS"
echo "OK: $REGLAS reglas de alerta cargadas:"
curl -s "$PROM/api/v1/rules" | jq -r '.data.groups[].rules[] | select(.type=="alerting") | "  \(.name)  for=\(.duration)s  \(.labels.severity)/\(.labels.priority)  estado=\(.state)  salud=\(.health)"'
[ "$(curl -s "$PROM/api/v1/rules" | jq '[.data.groups[].rules[] | select(.health!="ok")] | length')" = "0" ] || fallo "alguna regla tiene errores de evaluación"
[ "$(codigo "$AM/-/healthy")" = "200" ] || fallo "Alertmanager no está sano"
echo "OK: Alertmanager sano y conectado a Prometheus: $(curl -s "$PROM/api/v1/alertmanagers" | jq -r '.data.activeAlertmanagers[].url')"

echo "== Ficha 03: Grafana ($(hora)) =="
for i in $(seq 1 40); do [ "$(codigo "$GRAFANA/api/health")" = "200" ] && break; sleep 3; done
[ "$(codigo "$GRAFANA/api/health")" = "200" ] || fallo "Grafana no responde"
DS=$(curl -s -u "admin:$GF_PASS" "$GRAFANA/api/datasources" | jq -r '.[0] | "\(.name) (\(.type)) -> \(.url)"')
echo "$DS" | grep -q "Prometheus (prometheus)" || fallo "datasource Prometheus no aprovisionado ($DS)"
echo "OK: datasource $DS"
DASH=$(curl -s -u "admin:$GF_PASS" "$GRAFANA/api/dashboards/uid/inmobiliaria-observabilidad")
[ "$(echo "$DASH" | jq '.dashboard.panels | length')" -ge 5 ] || fallo "dashboard no aprovisionado"
echo "OK: dashboard '$(echo "$DASH" | jq -r .dashboard.title)' con $(echo "$DASH" | jq '.dashboard.panels | length') paneles, refresco $(echo "$DASH" | jq -r .dashboard.refresh):"
echo "$DASH" | jq -r '.dashboard.panels[] | "  [\(.type)] \(.title)"'
echo "Comprobando que cada consulta del tablero devuelve datos reales..."
echo "$DASH" | jq -r '.dashboard.panels[] | select(.type!="table") | .targets[0].expr' | while read -r EXPR; do
  N=$(curl -s -G "$PROM/api/v1/query" --data-urlencode "query=$EXPR" | jq '.data.result | length')
  echo "  series=$N  $EXPR" | cut -c1-150
done

echo "== Ficha 04: incidente simulado ($(hora)) =="
[ "$(codigo -X POST -H "$TOK" "$BASE/api/monitoreo/simular?tipo=error500")" = "500" ] || fallo "el simulador debía responder 500 (¿MONITORING_SIMULATION=true?)"
[ "$(codigo -X POST "$BASE/api/monitoreo/simular?tipo=error500")" = "401" ] || fallo "el simulador sin token debía dar 401"
echo "OK: simulador activo y protegido por token"
TOKEN_JWT=$(curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\"}" | jq -r '.token // empty')
[ -n "$TOKEN_JWT" ] || fallo "no se pudo iniciar sesión como administrador"
AUTH="Authorization: Bearer $TOKEN_JWT"

cargar() { # 50% de las peticiones fallan con 500 durante $1 segundos
  local fin=$(( $(date +%s) + $1 ))
  while [ "$(date +%s)" -lt "$fin" ]; do
    curl -s -o /dev/null "$BASE/api/public/status" &
    curl -s -o /dev/null -X POST -H "$TOK" "$BASE/api/monitoreo/simular?tipo=error500" &
    curl -s -o /dev/null "$BASE/api/propiedades" &
    curl -s -o /dev/null -X POST -H "$TOK" "$BASE/api/monitoreo/simular?tipo=error500" &
    sleep 0.5
  done
  wait
}
INICIO=$(date +%s)
echo "[$(hora)] Fase 2: provocando errores 5xx (~50% de las peticiones) durante 200 s..."
cargar 200 &
PID_CARGA=$!
FIRING=""
for i in $(seq 1 100); do
  FIRING=$(curl -s "$PROM/api/v1/alerts" | jq -r '.data.alerts[] | select(.labels.alertname=="TasaErroresCritica5xx" and .state=="firing") | .activeAt' | head -1)
  [ -n "$FIRING" ] && break; sleep 3
done
[ -n "$FIRING" ] || { kill $PID_CARGA 2>/dev/null; curl -s "$PROM/api/v1/alerts" | jq .; fallo "la alerta TasaErroresCritica5xx no llegó a FIRING"; }
echo "[$(hora)] OK: alerta TasaErroresCritica5xx en FIRING (activa desde $FIRING, tardó $(( $(date +%s) - INICIO )) s desde el primer error)"
curl -s "$PROM/api/v1/alerts" | jq -r '.data.alerts[] | "  \(.labels.alertname) [\(.state)] severity=\(.labels.severity) priority=\(.labels.priority) valor=\(.value)"'

echo "Esperando a que Alertmanager notifique y el backend cree el ticket..."
TICKET=""
for i in $(seq 1 40); do
  TICKET=$(curl -s "$BASE/api/tickets" -H "$AUTH" | jq -c '[.[] | select(.titulo | startswith("[ALERTA] TasaErroresCritica5xx"))] | first // empty')
  [ -n "$TICKET" ] && break; sleep 3
done
[ -n "$TICKET" ] || { kill $PID_CARGA 2>/dev/null; curl -s "$AM/api/v2/alerts" | jq .; fallo "no apareció el ticket creado por la alerta"; }
echo "[$(hora)] OK: ticket creado automáticamente por la alerta:"
echo "$TICKET" | jq '{codigo, titulo, prioridad, prioridadNombre, estado, moduloAfectado, tiempoSlaHoras, fechaRegistro, fechaLimiteSla}'
[ "$(echo "$TICKET" | jq -r .prioridad)" = "P1" ] || fallo "el ticket debía nacer con prioridad P1"
[ "$(echo "$TICKET" | jq -r .estado)" = "Abierto" ] || fallo "el ticket debía nacer Abierto"
[ "$(echo "$TICKET" | jq -r .tiempoSlaHoras)" = "2" ] || fallo "el SLA de P1 debía ser de 2 horas"
echo "Alertmanager (alertas activas): $(curl -s "$AM/api/v2/alerts" | jq -r '[.[] | "\(.labels.alertname)=\(.status.state)"] | join(", ")')"

echo "[$(hora)] Anti-duplicados: reenvío la misma alerta al webhook; no debe crearse un 2.º ticket..."
REENVIO=$(curl -s -X POST "$BASE/api/monitoreo/alertas" -H "$TOK" -H 'Content-Type: application/json' -d '{"status":"firing","receiver":"prueba","alerts":[{"status":"firing","labels":{"alertname":"TasaErroresCritica5xx","severity":"critical","priority":"P1"},"annotations":{"summary":"Más del 5% de las respuestas de la API son errores 5xx","description":"Reenvío manual de la misma alerta."},"startsAt":"2026-01-01T00:00:00Z"}]}')
echo "  respuesta del webhook: $REENVIO"
[ "$(echo "$REENVIO" | jq -r .duplicadas)" = "1" ] || fallo "el reenvío debía contarse como duplicado"
N=$(curl -s "$BASE/api/tickets" -H "$AUTH" | jq '[.[] | select(.titulo | startswith("[ALERTA] TasaErroresCritica5xx"))] | length')
[ "$N" = "1" ] || fallo "se esperaba 1 ticket por la alerta y hay $N"
echo "OK: sigue habiendo exactamente 1 ticket para la alerta."
[ "$(codigo -X POST "$BASE/api/monitoreo/alertas" -H 'Content-Type: application/json' -d '{"alerts":[]}')" = "401" ] || fallo "el webhook sin token debía dar 401"
echo "OK: el webhook sin token se rechaza (401)."

wait $PID_CARGA 2>/dev/null || true
echo "[$(hora)] Fase 3: errores detenidos; se mantiene tráfico normal para observar la recuperación (RESOLVED)..."
for i in $(seq 1 120); do
  curl -s -o /dev/null "$BASE/api/public/status"; curl -s -o /dev/null "$BASE/api/propiedades"
  ACTIVA=$(curl -s "$PROM/api/v1/alerts" | jq '[.data.alerts[] | select(.labels.alertname=="TasaErroresCritica5xx")] | length')
  [ "$ACTIVA" = "0" ] && break; sleep 3
done
[ "$ACTIVA" = "0" ] || fallo "la alerta no volvió a estado normal tras detener los errores"
echo "[$(hora)] OK: alerta recuperada (ya no figura en /api/v1/alerts). Resolved enviado a Alertmanager."
echo "Resumen: métricas OK, target UP, reglas OK, Grafana OK, alerta FIRING -> ticket P1 -> recuperada."
