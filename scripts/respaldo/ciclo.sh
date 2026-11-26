#!/bin/bash
# Un ciclo de respaldo, sin intervención humana:
#   mysqldump consistente -> gzip -> SHA-256 -> AES-256 -> subida inmutable (Object Lock COMPLIANCE)
#   -> verificación (se vuelve a descargar y se compara el SHA-256) -> retención local.
set -euo pipefail
source /opt/respaldo/comun.sh

ts=$(date -u +%Y%m%dT%H%M%SZ)
nombre="inmobiliaria_completo_$ts"
mkdir -p "$DIR/estado" "$DIR/local"
w=$(mktemp -d "$DIR/trabajo.XXXXXX")
trap 'rm -rf "$w"' EXIT
inicio=$(date +%s)

export MYSQL_PWD="$DB_PASSWORD"   # la clave viaja por entorno, no por la línea de comandos
mysqldump -h "$DB_HOST" -uroot --single-transaction --routines --triggers --events --hex-blob \
  --set-gtid-purged=OFF --source-data=2 --databases "$DB_NAME" | gzip -9 > "$w/$nombre.sql.gz"
gzip -t "$w/$nombre.sql.gz"
sha_claro=$(sha256sum "$w/$nombre.sql.gz" | cut -d' ' -f1)

# Cifrado AES-256-CBC con derivación PBKDF2: en el almacén externo solo hay datos ilegibles
openssl enc -aes-256-cbc -salt -pbkdf2 -iter 100000 -pass env:BACKUP_PASS \
  -in "$w/$nombre.sql.gz" -out "$w/$nombre.sql.gz.enc"
sha_cifrado=$(sha256sum "$w/$nombre.sql.gz.enc" | cut -d' ' -f1)
tam=$(stat -c %s "$w/$nombre.sql.gz.enc")

retener_hasta=$(date -u -d "+${RETENCION_DIAS} days" +%Y-%m-%dT%H:%M:%SZ)
S3 s3api put-object --bucket "$BUCKET" --key "diario/$nombre.sql.gz.enc" --body "$w/$nombre.sql.gz.enc" \
  --object-lock-mode COMPLIANCE --object-lock-retain-until-date "$retener_hasta" \
  --metadata "sha256-claro=$sha_claro,sha256-cifrado=$sha_cifrado,cifrado=aes-256-cbc-pbkdf2" >/dev/null

# Verificación: el objeto está bloqueado y, descargado de nuevo, es idéntico al que se subió
modo=$(S3 s3api head-object --bucket "$BUCKET" --key "diario/$nombre.sql.gz.enc" --query ObjectLockMode --output text)
[ "$modo" = COMPLIANCE ] || { log "FALLO: el respaldo quedó sin bloqueo WORM (modo=$modo)"; exit 1; }
S3 s3api get-object --bucket "$BUCKET" --key "diario/$nombre.sql.gz.enc" "$w/verificacion.enc" >/dev/null
[ "$(sha256sum "$w/verificacion.enc" | cut -d' ' -f1)" = "$sha_cifrado" ] \
  || { log "FALLO: la copia externa no coincide con la local (SHA-256)"; exit 1; }

# Copia local cifrada (rápida de usar) y retención de las últimas $COPIAS_LOCALES
cp "$w/$nombre.sql.gz.enc" "$DIR/local/"
{ ls -1 "$DIR"/local/*.enc | sort -r | tail -n +$((COPIAS_LOCALES + 1)) | xargs -r rm -f; } || true

fin=$(date +%s)
date +%s > "$DIR/estado/ultimo_ok"
log "CICLO_OK nombre=$nombre tamano_bytes=$tam duracion_s=$((fin - inicio)) bloqueo=$modo retener_hasta=$retener_hasta sha256_cifrado=$sha_cifrado"
