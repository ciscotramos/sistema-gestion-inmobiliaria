#!/bin/bash
# Crea el bucket con Object Lock (inmutable, versionado automático). Es idempotente.
set -euo pipefail
source /opt/respaldo/comun.sh

for i in $(seq 1 60); do S3 s3api list-buckets >/dev/null 2>&1 && break; sleep 2; done
S3 s3api list-buckets >/dev/null || { log "FALLO: el almacén S3 no responde en $S3_ENDPOINT"; exit 1; }

if S3 s3api head-bucket --bucket "$BUCKET" >/dev/null 2>&1; then
  log "El bucket $BUCKET ya existe."
else
  S3 s3api create-bucket --bucket "$BUCKET" --object-lock-enabled-for-bucket >/dev/null
  log "Bucket $BUCKET creado con Object Lock."
fi
# Un bucket con Object Lock ya nace versionado (y el versionado no se puede suspender): solo se comprueba
log "Versionado: $(S3 s3api get-bucket-versioning --bucket "$BUCKET" --query Status --output text)"
# Retención por defecto: aunque alguien suba un objeto sin indicar el bloqueo, queda protegido
S3 s3api put-object-lock-configuration --bucket "$BUCKET" --object-lock-configuration \
  "{\"ObjectLockEnabled\":\"Enabled\",\"Rule\":{\"DefaultRetention\":{\"Mode\":\"COMPLIANCE\",\"Days\":$RETENCION_DIAS}}}"
log "Bucket listo: versionado + Object Lock COMPLIANCE por defecto ($RETENCION_DIAS día(s))."
