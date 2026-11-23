#!/bin/bash
# Restaura un respaldo en un MySQL nuevo y lo verifica: es el "0 errores" de la regla 3-2-1-1-0
# (ISO/IEC 27001:2022 A.8.13: las copias deben probarse con regularidad).
#
#   ./scripts/probar-restauracion.sh [respaldo.sql.gz]     usa el último respaldo diario si no se indica
#
# Variables opcionales:
#   HASTA_ARCHIVO y HASTA_POS  recuperación a un punto en el tiempo: reaplica los binlogs hasta esa posición
#   CONTEOS_ESPERADOS          archivo con filas por tabla que el resultado debe igualar
#   CONSERVAR=1                no borra el servidor restaurado (restauración real, no solo prueba)
#   DESTINO                    nombre del contenedor (por defecto inmobiliaria-restauracion)
#   CLAVE_DESTINO              clave root del servidor restaurado (si no, se genera una al azar)
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/lib-respaldo.sh

COMPLETO=${1:-$(ls -1 "$DEST"/diario/*.sql.gz 2>/dev/null | sort | tail -1)}
[ -n "$COMPLETO" ] && [ -f "$COMPLETO" ] || { echo "FALLO: no hay respaldo completo en $DEST/diario."; exit 1; }
DESTINO=${DESTINO:-inmobiliaria-restauracion}
PW=${CLAVE_DESTINO:-$(openssl rand -hex 12)}
fallo() { echo "FALLO: $1"; exit 1; }

inicio=$(date +%s)
echo "Respaldo a restaurar: $COMPLETO"
(cd "$(dirname "$COMPLETO")" && sha256sum -c "$(basename "$COMPLETO").sha256") || fallo "la suma SHA-256 no coincide (respaldo alterado o corrupto)"

docker rm -f "$DESTINO" >/dev/null 2>&1 || true
[ "${CONSERVAR:-0}" = 1 ] || trap 'docker rm -f "$DESTINO" >/dev/null 2>&1 || true' EXIT
docker run -d --name "$DESTINO" -e MYSQL_ROOT_PASSWORD="$PW" mysql:8.0 \
  --skip-log-bin --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci >/dev/null
for i in $(seq 1 60); do
  docker exec -e MYSQL_PWD="$PW" "$DESTINO" mysqladmin ping -h127.0.0.1 -uroot --silent 2>/dev/null && break
  sleep 2
done
docker exec -e MYSQL_PWD="$PW" "$DESTINO" mysqladmin ping -h127.0.0.1 -uroot --silent >/dev/null 2>&1 || fallo "el servidor temporal no arrancó"
echo "OK: servidor nuevo '$DESTINO' disponible ($(( $(date +%s) - inicio )) s)."

gunzip -c "$COMPLETO" | sql "$DESTINO" "$PW"
echo "OK: respaldo completo cargado."

# Recuperación a un punto en el tiempo: Full + binlogs (incrementales) hasta justo antes del daño
if [ -n "${HASTA_ARCHIVO:-}" ] && [ -n "${HASTA_POS:-}" ]; then
  read -r f0 p0 < <(posicion_binlog "$COMPLETO")
  docker exec "$DESTINO" mkdir -p /tmp/binlogs
  archivos=()
  for b in $(ls -1 "$DEST/binlogs" | sort); do
    [[ "$b" < "$f0" ]] && continue
    [[ "$b" > "$HASTA_ARCHIVO" ]] && continue
    docker cp "$DEST/binlogs/$b" "$DESTINO:/tmp/binlogs/$b"
    archivos+=("/tmp/binlogs/$b")
  done
  [ "${#archivos[@]}" -gt 0 ] || fallo "no hay binlogs entre $f0 y $HASTA_ARCHIVO"
  docker exec -e MYSQL_PWD="$PW" "$DESTINO" sh -c \
    "mysqlbinlog --skip-gtids --start-position=$p0 --stop-position=$HASTA_POS ${archivos[*]} | mysql -uroot"
  echo "OK: binlogs reaplicados desde $f0:$p0 hasta $HASTA_ARCHIVO:$HASTA_POS (${#archivos[@]} archivo(s))."
fi

# Verificaciones: estructura, integridad y datos
tablas=$(sql "$DESTINO" "$PW" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB' AND table_type='BASE TABLE'")
[ "$tablas" = 17 ] || fallo "se esperaban 17 tablas y hay $tablas"
echo "OK: $tablas tablas restauradas."

revision=$(docker exec -e MYSQL_PWD="$PW" "$DESTINO" mysqlcheck -uroot --databases "$DB" 2>&1 || true)
revisadas=$(echo "$revision" | grep -cE "^$DB\." || true)
malas=$(echo "$revision" | grep -E "^$DB\." | grep -v 'OK$' || true)
if [ -n "$malas" ] || [ "$revisadas" != 17 ]; then
  echo "Salida de mysqlcheck:"; echo "$revision" | sed 's/^/  /'
  fallo "mysqlcheck: $revisadas tablas revisadas (se esperaban 17) o con problemas"
fi
echo "OK: mysqlcheck revisó $revisadas tablas sin errores de integridad."

conteos_tablas "$DESTINO" "$PW" > "${TMPDIR:-/tmp}/conteos_restaurado.txt"
echo "Filas por tabla en el servidor restaurado:"; sed 's/^/  /' "${TMPDIR:-/tmp}/conteos_restaurado.txt"
if [ -n "${CONTEOS_ESPERADOS:-}" ]; then
  diff -u "$CONTEOS_ESPERADOS" "${TMPDIR:-/tmp}/conteos_restaurado.txt" || fallo "los datos restaurados no coinciden con los esperados"
  echo "OK: las filas de las 17 tablas coinciden con las del momento previo al desastre."
fi

fin=$(( $(date +%s) - inicio ))
echo "RESTAURACION_OK  tiempo_total_s=$fin"
[ "${CONSERVAR:-0}" = 1 ] && echo "Servidor conservado: $DESTINO (usuario root, clave = CLAVE_DESTINO)."
exit 0
