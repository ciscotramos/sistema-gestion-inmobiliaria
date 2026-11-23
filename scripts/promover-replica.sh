#!/bin/bash
# Falla del servidor Master: convierte la Replica en el nuevo Master (RTO objetivo: 15 min).
# Usar solo si la Replica está sana. Si el daño es lógico (borrado, ransomware) la Replica ya lo copió:
# en ese caso se restaura un respaldo (scripts/probar-restauracion.sh), no se promueve.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/lib-respaldo.sh
CLAVE=${DB_PASSWORD_REPLICA:-${DB_PASSWORD:?Define DB_PASSWORD en .env}}

inicio=$(date +%s)
sql "$REPLICA" "$CLAVE" -e "STOP REPLICA; RESET REPLICA ALL;"
sql "$REPLICA" "$CLAVE" -e "SET PERSIST super_read_only=OFF; SET PERSIST read_only=OFF;"
escribible=$(sql "$REPLICA" "$CLAVE" -N -e "SELECT @@super_read_only + @@read_only")
[ "$escribible" = 0 ] || { echo "FALLO: la Replica sigue en solo lectura."; exit 1; }
echo "OK: $REPLICA ahora acepta escrituras ($(( $(date +%s) - inicio )) s)."
echo "Siguiente paso: apuntar DB_HOST del backend a este servidor y reiniciar el backend."
