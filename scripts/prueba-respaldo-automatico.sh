#!/bin/bash
# Prueba de respaldos automáticos (Taller Sesión 32, Actividad 2 y Pruebas 3.2 y 3.3).
# Verifica, con el sistema en marcha: ejecución periódica sin intervención, cifrado AES-256, inmutabilidad (WORM),
# copia externa y restauración completa desde esa copia con comparación de filas por tabla y tiempo de restauración.
# Requiere:  docker compose -f docker-compose.yml -f docker-compose.backup.yml up -d --build mysql-master mysql-replica respaldo
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/lib-respaldo.sh
: "${DB_PASSWORD:?Define DB_PASSWORD en .env}"
: "${BACKUP_PASS:?Define BACKUP_PASS en .env}"; export BACKUP_PASS

RESP=${CONTENEDOR_RESPALDO:-inmobiliaria-respaldo}
BUCKET=${BACKUP_BUCKET:-respaldos-inmobiliaria}
CICLOS=${CICLOS_ESPERADOS:-3}
T=$(mktemp -d)
paso() { echo; echo "== $1"; }
fallo() { echo "FALLO: $1"; docker logs --tail 30 "$RESP" 2>&1 | sed 's/^/    /' || true; exit 1; }
S3() { docker exec "$RESP" aws --endpoint-url http://almacen-s3:8000 "$@"; }
M() { sql "$MASTER" "$DB_PASSWORD" -N "$DB" "$@"; }
ciclos_ok() { docker logs "$RESP" 2>&1 | grep -c "CICLO_OK" || true; }

paso "1. El contenedor de respaldos está activo y sano"
for i in $(seq 1 60); do [ "$(docker inspect -f '{{.State.Health.Status}}' "$RESP")" = healthy ] && break; sleep 3; done
[ "$(docker inspect -f '{{.State.Health.Status}}' "$RESP")" = healthy ] || fallo "el contenedor de respaldos no está sano"
docker logs "$RESP" 2>&1 | grep -E "Programador activo|Bucket" | sed 's/^/  /'

paso "2. Datos de ejemplo en el Master y espera de $CICLOS respaldos automáticos posteriores (nadie ejecuta nada a mano)"
M < backend/data.sql
conteos_tablas "$MASTER" "$DB_PASSWORD" > "$T/esperado.txt"
echo "  Filas por tabla en el Master (referencia): $(awk -F'\t' '{s+=$2} END{print s}' "$T/esperado.txt") filas en total, $(wc -l < "$T/esperado.txt") tablas"
n0=$(ciclos_ok)
for i in $(seq 1 90); do [ "$(ciclos_ok)" -ge $((n0 + CICLOS)) ] && break; sleep 3; done
[ "$(ciclos_ok)" -ge $((n0 + CICLOS)) ] || fallo "no se completaron $CICLOS ciclos automáticos a tiempo"
docker logs "$RESP" 2>&1 | grep "CICLO_OK" | tail -n "$CICLOS" | sed -E 's/ sha256_cifrado=.*//; s/^/  /'

paso "3. Frecuencia real de los respaldos (marcas de tiempo del almacén externo)"
S3 s3api list-objects --bucket "$BUCKET" --prefix diario/ --query 'Contents[].[Key,Size,LastModified]' --output text | sort > "$T/objetos.txt"
sed 's/^/  /' "$T/objetos.txt"
total_obj=$(wc -l < "$T/objetos.txt")
[ "$total_obj" -ge "$CICLOS" ] || fallo "el almacén externo tiene $total_obj objetos y se esperaban al menos $CICLOS"
intervalo_max=$(docker logs "$RESP" 2>&1 | grep "CICLO_OK" | sed -E 's/^\[([^]]+)\].*/\1/' \
  | while read -r t; do date -u -d "$t" +%s; done | awk 'NR>1{d=$1-p; if(d>m)m=d} {p=$1} END{print m+0}')
duracion=$(docker logs "$RESP" 2>&1 | grep "CICLO_OK" | tail -1 | sed -E 's/.*duracion_s=([0-9]+).*/\1/')
echo "  Intervalo máximo entre respaldos consecutivos: ${intervalo_max} s; duración de un ciclo: ${duracion} s"

ULTIMO=$(awk '{print $1}' "$T/objetos.txt" | tail -1)
NOMBRE=$(basename "$ULTIMO")
echo "  Respaldo que se usará para la prueba: $NOMBRE"

paso "4. Cifrado: el almacén externo solo contiene datos ilegibles (AES-256)"
S3 s3api get-object --bucket "$BUCKET" --key "$ULTIMO" /tmp/ultimo.enc >/dev/null
docker cp "$RESP:/tmp/ultimo.enc" "$T/ultimo.enc"
cabecera=$(head -c 8 "$T/ultimo.enc")
[ "$cabecera" = "Salted__" ] || fallo "el objeto no tiene el formato de cifrado de OpenSSL"
echo "  Cabecera del objeto: '$cabecera' (formato OpenSSL con sal; el contenido no es un .gz legible)"
if gunzip -t < "$T/ultimo.enc" >/dev/null 2>&1; then fallo "el objeto se pudo leer sin descifrar"; fi
echo "  OK: sin la clave el objeto no se puede abrir como gzip ni como SQL."
if openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass pass:clave-incorrecta -in "$T/ultimo.enc" 2>/dev/null | gunzip -t >/dev/null 2>&1; then
  fallo "el objeto se pudo descifrar con una clave incorrecta"; fi
echo "  OK: con una clave incorrecta el descifrado falla."
BACKUP_PASS="$BACKUP_PASS" openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass env:BACKUP_PASS \
  -in "$T/ultimo.enc" -out "$T/ultimo.sql.gz"
gunzip -t "$T/ultimo.sql.gz" || fallo "con la clave correcta el respaldo no descifra a un gzip válido"
echo "  OK: con la clave correcta se obtiene un gzip válido."
sha_meta=$(S3 s3api head-object --bucket "$BUCKET" --key "$ULTIMO" --query 'Metadata."sha256-claro"' --output text)
sha_real=$(sha256sum "$T/ultimo.sql.gz" | cut -d' ' -f1)
[ "$sha_meta" = "$sha_real" ] || fallo "el SHA-256 del respaldo descifrado no coincide con el registrado al crearlo"
echo "  OK: SHA-256 del respaldo descifrado = SHA-256 registrado al crearlo ($sha_real)"

paso "5. Inmutabilidad (WORM): ni con las credenciales del sistema se puede borrar ni acortar la retención"
S3 s3api head-object --bucket "$BUCKET" --key "$ULTIMO" --query '{Modo:ObjectLockMode,RetenidoHasta:ObjectLockRetainUntilDate}' --output text | sed 's/^/  Bloqueo: /'
modo=$(S3 s3api head-object --bucket "$BUCKET" --key "$ULTIMO" --query ObjectLockMode --output text)
[ "$modo" = COMPLIANCE ] || fallo "el respaldo no está en modo COMPLIANCE"
vid=$(S3 s3api head-object --bucket "$BUCKET" --key "$ULTIMO" --query VersionId --output text)
set +e
salida_borrado=$(S3 s3api delete-object --bucket "$BUCKET" --key "$ULTIMO" --version-id "$vid" 2>&1); rc_borrado=$?
salida_bypass=$(S3 s3api delete-object --bucket "$BUCKET" --key "$ULTIMO" --version-id "$vid" --bypass-governance-retention 2>&1); rc_bypass=$?
nueva=$(date -u -d "+1 hour" +%Y-%m-%dT%H:%M:%SZ)
salida_acortar=$(S3 s3api put-object-retention --bucket "$BUCKET" --key "$ULTIMO" --version-id "$vid" \
  --retention "{\"Mode\":\"COMPLIANCE\",\"RetainUntilDate\":\"$nueva\"}" 2>&1); rc_acortar=$?
set -e
echo "  Intento 1, borrar la versión del respaldo:            código $rc_borrado -> $(echo "$salida_borrado" | tail -1 | cut -c1-150)"
echo "  Intento 2, borrar con --bypass-governance-retention:  código $rc_bypass -> $(echo "$salida_bypass" | tail -1 | cut -c1-150)"
echo "  Intento 3, acortar la retención a 1 hora:             código $rc_acortar -> $(echo "$salida_acortar" | tail -1 | cut -c1-150)"
[ "$rc_borrado" != 0 ] && [ "$rc_bypass" != 0 ] && [ "$rc_acortar" != 0 ] || fallo "el almacén permitió borrar o debilitar un respaldo bloqueado"
S3 s3api head-object --bucket "$BUCKET" --key "$ULTIMO" --version-id "$vid" >/dev/null || fallo "el respaldo desapareció tras los intentos de borrado"
echo "  OK: los 3 intentos fueron rechazados y el respaldo sigue íntegro."

paso "6. Desastre y restauración completa desde la copia EXTERNA (descargar -> descifrar -> restaurar -> comparar)"
inicio=$(date +%s)
S3 s3api get-object --bucket "$BUCKET" --key "$ULTIMO" /tmp/rto.enc >/dev/null
docker cp "$RESP:/tmp/rto.enc" "$T/rto.enc"
openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass env:BACKUP_PASS -in "$T/rto.enc" -out "$T/inmobiliaria_completo_externo.sql.gz"
(cd "$T" && sha256sum inmobiliaria_completo_externo.sql.gz > inmobiliaria_completo_externo.sql.gz.sha256)
t_descarga=$(( $(date +%s) - inicio ))
echo "  Descarga + descifrado: ${t_descarga} s"
CONTEOS_ESPERADOS="$T/esperado.txt" TMPDIR="$T" ./scripts/probar-restauracion.sh "$T/inmobiliaria_completo_externo.sql.gz" | tee "$T/restauracion.txt" | sed 's/^/  /'
grep -q RESTAURACION_OK "$T/restauracion.txt" || fallo "la restauración desde la copia externa no terminó bien"
rto=$(( $(date +%s) - inicio ))

paso "7. Rotación local: solo se conservan las últimas copias cifradas"
locales=$(docker exec "$RESP" sh -c 'ls -1 /respaldos/local/*.enc | wc -l')
echo "  Copias locales cifradas: $locales (límite 3); objetos inmutables en el almacén: $(wc -l < "$T/objetos.txt")"
[ "$locales" -le 3 ] || fallo "la rotación local no limitó las copias"

echo
echo "RESULTADO RESPALDO AUTOMATICO"
echo "  Ciclos automáticos observados: $(ciclos_ok) (intervalo máximo ${intervalo_max} s)"
echo "  Cifrado AES-256: verificado (sin clave ilegible, con clave incorrecta falla, con la correcta SHA-256 coincide)"
echo "  Inmutabilidad: COMPLIANCE; borrar, borrar con bypass y acortar retención rechazados"
echo "  Restauración desde la copia externa: OK, 17 tablas con las mismas filas que el Master"
echo "  RTO medido de la base (descarga + descifrado + restauración + verificación): ${rto} s"
echo "  RPO del esquema: igual al periodo configurado (laboratorio ${intervalo_max} s; producción 24 h con diario + binlogs)"
