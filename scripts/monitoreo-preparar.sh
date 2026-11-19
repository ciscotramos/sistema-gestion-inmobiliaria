#!/bin/bash
# Prepara el token compartido entre el backend, Prometheus y Alertmanager (Taller Sesión 30).
# - Si .env no tiene MONITORING_TOKEN, genera uno y lo agrega.
# - Escribe monitoring/secrets/monitoring_token (ignorado por Git) para montarlo en los contenedores.
set -e
cd "$(dirname "$0")/.."
touch .env
TOKEN=$(grep -E '^MONITORING_TOKEN=' .env | tail -n1 | cut -d= -f2- || true)
if [ -z "$TOKEN" ]; then
  TOKEN=$(openssl rand -hex 24)
  sed -i '/^MONITORING_TOKEN=/d' .env
  printf 'MONITORING_TOKEN=%s\n' "$TOKEN" >> .env
  echo "Se generó MONITORING_TOKEN y se guardó en .env (reinicia el backend para que lo lea)."
fi
mkdir -p monitoring/secrets
printf '%s' "$TOKEN" > monitoring/secrets/monitoring_token
# Los contenedores corren con otro usuario: el archivo debe ser legible. El token solo protege un laboratorio local.
chmod 755 monitoring/secrets
chmod 644 monitoring/secrets/monitoring_token
echo "OK: monitoring/secrets/monitoring_token listo."
