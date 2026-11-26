#!/bin/bash
# Variables y funciones comunes del contenedor de respaldos. Se carga con `source`.
: "${DB_HOST:=mysql-master}"
: "${DB_NAME:=inmobiliaria_db}"
: "${DB_PASSWORD:?falta DB_PASSWORD}"
: "${BACKUP_PASS:?falta BACKUP_PASS (clave de cifrado AES-256; guárdala fuera del servidor)}"
: "${S3_ENDPOINT:=http://almacen-s3:8000}"
: "${BUCKET:=respaldos-inmobiliaria}"
: "${RETENCION_DIAS:=1}"          # bloqueo WORM (Object Lock COMPLIANCE) de cada respaldo, en días
: "${COPIAS_LOCALES:=3}"          # copias cifradas que se conservan también en el volumen local
: "${DIR:=/respaldos}"
export AWS_DEFAULT_REGION=${AWS_DEFAULT_REGION:-us-east-1}
export AWS_PAGER=""

log() { echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*"; }
# Cliente S3 con el endpoint del almacén (en producción: el bucket real, sin --endpoint-url)
S3() { aws --endpoint-url "$S3_ENDPOINT" "$@"; }
