#!/bin/bash
# Simulacro de desastre (Taller Sesión 31): borrado masivo de datos que la Replica copia al instante.
# Demuestra que la replicación NO es un respaldo y que Full + binlogs recuperan hasta justo antes del daño.
# Requiere MySQL Master y Replica levantados (docker compose up -d --wait mysql-master mysql-replica).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/lib-respaldo.sh
: "${DB_PASSWORD:?Define DB_PASSWORD en .env}"
CLAVE_R=${DB_PASSWORD_REPLICA:-$DB_PASSWORD}
M() { sql "$MASTER" "$DB_PASSWORD" -N "$DB" "$@"; }
R() { sql "$REPLICA" "$CLAVE_R" -N "$DB" "$@"; }
paso() { echo; echo "== $1"; }
fallo() { echo "FALLO: $1"; exit 1; }
T=$(mktemp -d)

paso "1. Estado normal: datos de ejemplo en el Master"
M < backend/data.sql
echo "Características en Master: $(M -e 'SELECT COUNT(*) FROM caracteristicas')"

paso "2. Respaldo completo (Full)"
rm -rf "$DEST"
./scripts/respaldar.sh completo

paso "3. Operación normal después del Full: 5 registros nuevos (solo existen en el binlog)"
for i in 1 2 3 4 5; do M -e "INSERT INTO caracteristicas (nombre, categoria) VALUES ('dr_posterior_$i','Interior')"; done
read -r hasta_archivo hasta_pos < <(M -e "SHOW MASTER STATUS" 2>/dev/null | awk '{print $1, $2}')
conteos_tablas "$MASTER" "$DB_PASSWORD" > "$T/antes_del_desastre.txt"
echo "Punto previo al desastre: $hasta_archivo:$hasta_pos"
echo "Características antes del desastre: $(M -e 'SELECT COUNT(*) FROM caracteristicas')"

paso "4. DESASTRE: alguien (o un ransomware) borra todo el catálogo de características"
M -e "SET FOREIGN_KEY_CHECKS=0; DELETE FROM caracteristicas;"
echo "Master:  $(M -e 'SELECT COUNT(*) FROM caracteristicas') características"

paso "5. La Replica copió el daño (la replicación no protege de errores lógicos)"
for i in $(seq 1 30); do [ "$(R -e 'SELECT COUNT(*) FROM caracteristicas')" = 0 ] && break; sleep 1; done
n_replica=$(R -e 'SELECT COUNT(*) FROM caracteristicas')
echo "Replica: $n_replica características"
[ "$n_replica" = 0 ] || fallo "la Replica debía haber copiado el borrado"

paso "6. Respaldo incremental: se conservan los binlogs (con el daño incluido) para reaplicarlos hasta antes de él"
./scripts/respaldar.sh incremental

paso "7. Restauración: Full + binlogs hasta el punto previo, en un servidor nuevo"
inicio=$(date +%s)
CONTEOS_ESPERADOS="$T/antes_del_desastre.txt" HASTA_ARCHIVO="$hasta_archivo" HASTA_POS="$hasta_pos" \
  ./scripts/probar-restauracion.sh
rto=$(( $(date +%s) - inicio ))

paso "8. Retención: 10 respaldos diarios simulados -> quedan 7"
tmp=$(mktemp -d); mkdir -p "$tmp/diario"
for d in 01 02 03 04 05 06 07 08 09 10; do touch "$tmp/diario/inmobiliaria_completo_202611${d}T020000Z.sql.gz"; done
RESPALDO_DIR="$tmp" ./scripts/respaldar.sh rotar
quedan=$(ls -1 "$tmp"/diario/*.sql.gz | wc -l)
[ "$quedan" = 7 ] || fallo "se esperaban 7 respaldos diarios y quedan $quedan"
[ -f "$tmp/diario/inmobiliaria_completo_20261110T020000Z.sql.gz" ] && [ ! -f "$tmp/diario/inmobiliaria_completo_20261101T020000Z.sql.gz" ] \
  || fallo "la rotación no conservó los más recientes"
echo "OK: quedan $quedan respaldos diarios (se borraron los 3 más antiguos)."

paso "9. Falla del servidor Master: promover la Replica sana"
docker stop "$MASTER" >/dev/null
inicio=$(date +%s)
./scripts/promover-replica.sh
R -e "INSERT INTO caracteristicas (nombre, categoria) VALUES ('dr_promocion','Interior')"
[ "$(R -e "SELECT COUNT(*) FROM caracteristicas WHERE nombre='dr_promocion'")" = 1 ] || fallo "la Replica promovida no aceptó escrituras"
rto_promocion=$(( $(date +%s) - inicio ))
echo "OK: la Replica promovida acepta escrituras."

echo
echo "RESULTADO DEL SIMULACRO"
echo "  Restauración Full + binlogs (17 tablas, filas idénticas al punto previo): OK, $rto s"
echo "  Retención 7 diarios: OK"
echo "  Promoción de la Replica: OK, $rto_promocion s"
echo "  Errores en la prueba de restauración: 0"
