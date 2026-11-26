#!/bin/bash
# Programador: ejecuta ciclo.sh de forma periódica sin intervención humana.
#   INTERVALO_SEGUNDOS  periodo entre respaldos (86400 = diario; el laboratorio usa 20 s para probarlo)
#   HORA_UTC            si se define (HH:MM), el respaldo diario se hace a esa hora (ej. 02:00)
source /opt/respaldo/comun.sh
: "${INTERVALO_SEGUNDOS:=86400}"

parar=0; trap 'parar=1; kill "${dormir:-0}" 2>/dev/null || true' TERM INT

/opt/respaldo/preparar-bucket.sh || { log "FALLO: no se pudo preparar el bucket"; exit 1; }
log "Programador activo: cada ${INTERVALO_SEGUNDOS}s${HORA_UTC:+ (diario a las $HORA_UTC UTC)}; retención WORM ${RETENCION_DIAS} día(s)."

espera() {
  if [ -n "${HORA_UTC:-}" ]; then
    local ahora objetivo; ahora=$(date -u +%s); objetivo=$(date -u -d "today $HORA_UTC" +%s)
    [ "$objetivo" -le "$ahora" ] && objetivo=$(date -u -d "tomorrow $HORA_UTC" +%s)
    echo $((objetivo - ahora))
  else
    echo "$INTERVALO_SEGUNDOS"
  fi
}

[ -n "${HORA_UTC:-}" ] || primera=1   # sin hora fija, el primer respaldo se hace al arrancar
while [ "$parar" = 0 ]; do
  if [ -n "${primera:-}" ]; then primera=; else
    s=$(espera); sleep "$s" & dormir=$!; wait "$dormir" || true
    [ "$parar" = 0 ] || break
  fi
  /opt/respaldo/ciclo.sh || log "CICLO_FALLO: el respaldo de este ciclo no se completó (revisa el registro)"
done
log "Programador detenido."
