#!/bin/bash
# Respaldos del Master (Taller Sesión 31, Parte 3: política 3-2-1-1-0).
#   ./scripts/respaldar.sh completo     Full: mysqldump consistente + posición del binlog + SHA-256
#   ./scripts/respaldar.sh incremental  Cierra el binlog activo y copia los binlogs cerrados (cambios desde el Full)
#   ./scripts/respaldar.sh rotar        Retención: 7 diarios, 4 semanales, 12 mensuales y binlogs de 30 días
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/lib-respaldo.sh

rotar_dir() { # conserva los $2 más recientes de $1 (el nombre lleva la fecha UTC) y borra el resto
  { ls -1 "$1"/*.sql.gz 2>/dev/null | sort -r | tail -n +$(($2 + 1)) | while read -r f; do rm -f "$f" "$f.sha256" "$f.meta"; done; } || true
}

completo() {
  : "${DB_PASSWORD:?Define DB_PASSWORD en .env}"
  mkdir -p "$DEST"/{diario,semanal,mensual,binlogs}
  local f="$DEST/diario/inmobiliaria_completo_$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
  docker exec -e MYSQL_PWD="$DB_PASSWORD" "$MASTER" mysqldump -uroot \
    --single-transaction --routines --triggers --events --hex-blob \
    --set-gtid-purged=OFF --source-data=2 --databases "$DB" | gzip -9 > "$f"
  gzip -t "$f"
  read -r bl_archivo bl_pos < <(posicion_binlog "$f")
  [ -n "${bl_archivo:-}" ] || { echo "FALLO: el respaldo no trae la posición del binlog."; exit 1; }
  printf 'binlog_archivo=%s\nbinlog_posicion=%s\n' "$bl_archivo" "$bl_pos" > "$f.meta"
  (cd "$(dirname "$f")" && sha256sum "$(basename "$f")" > "$(basename "$f").sha256")
  # Niveles abuelo-padre-hijo: el del domingo pasa a semanal y el del día 1 a mensual
  [ "$(date -u +%u)" = 7 ] && cp "$f" "$f.sha256" "$f.meta" "$DEST/semanal/"
  [ "$(date -u +%d)" = 01 ] && cp "$f" "$f.sha256" "$f.meta" "$DEST/mensual/"
  echo "OK: respaldo completo $f ($(du -h "$f" | cut -f1)), binlog $bl_archivo:$bl_pos"
  return 0
}

incremental() {
  : "${DB_PASSWORD:?Define DB_PASSWORD en .env}"
  mkdir -p "$DEST/binlogs"
  sql "$MASTER" "$DB_PASSWORD" -e "FLUSH BINARY LOGS"
  mapfile -t logs < <(docker exec "$MASTER" sh -c "ls -1 /var/lib/mysql | grep -E '^mysql-bin\.[0-9]{6}$' | sort")
  local activo=${logs[${#logs[@]}-1]} n=0 b
  for b in "${logs[@]}"; do
    [ "$b" = "$activo" ] && continue
    [ -s "$DEST/binlogs/$b" ] && continue
    docker exec "$MASTER" cat "/var/lib/mysql/$b" > "$DEST/binlogs/$b"
    n=$((n + 1))
  done
  echo "OK: incremental, $n binlog(s) nuevo(s) copiado(s); el activo ($activo) se copia en el próximo ciclo."
}

rotar() {
  rotar_dir "$DEST/diario" 7
  rotar_dir "$DEST/semanal" 4
  rotar_dir "$DEST/mensual" 12
  find "$DEST/binlogs" -type f -mtime +30 -delete 2>/dev/null || true
  echo "OK: retención aplicada (7 diarios, 4 semanales, 12 mensuales)."
}

case "${1:-}" in
  completo) completo ;;
  incremental) incremental ;;
  rotar) rotar ;;
  *) echo "Uso: $0 {completo|incremental|rotar}"; exit 2 ;;
esac
