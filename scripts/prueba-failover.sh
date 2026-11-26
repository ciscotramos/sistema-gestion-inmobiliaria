#!/bin/bash
# Prueba de failover (Taller Sesión 32, Actividad 1 y Prueba 3.1).
# Con tráfico continuo, detiene el nodo 1 de la API y mide qué ve el usuario; luego lo reinicia.
# Requiere:  docker compose -f docker-compose.yml -f docker-compose.ha.yml up -d --build
set -euo pipefail
cd "$(dirname "$0")/.."

URL=${URL:-http://localhost:8080}
ENDPOINT="$URL/api/caracteristicas"
N1=inmobiliaria-api-1; N2=inmobiliaria-api-2; LB=inmobiliaria-lb
RED=${RED_DOCKER:-inmobiliaria_default}
T=$(mktemp -d)
paso() { echo; echo "== $1"; }
fallo() { echo "FALLO: $1"; exit 1; }
ahora() { date +%s.%N; }
ip_de() { docker inspect -f "{{(index .NetworkSettings.Networks \"$RED\").IPAddress}}" "$1"; }
salud() { docker inspect -f '{{.State.Health.Status}}' "$1"; }

paso "1. Estado inicial: 2 nodos activos detrás del balanceador"
for c in $N1 $N2 $LB; do echo "  $c: $(salud $c)"; [ "$(salud $c)" = healthy ] || fallo "$c no está sano"; done
IP1=$(ip_de $N1); IP2=$(ip_de $N2)
echo "  nodo 1 = $IP1   nodo 2 = $IP2"

paso "2. Distribución de carga: 40 peticiones (cabecera X-Served-By)"
: > "$T/reparto.txt"
for i in $(seq 1 40); do
  curl -s -o /dev/null -D - "$ENDPOINT" | tr -d '\r' | awk -F': ' 'tolower($1)=="x-served-by"{print $2}' >> "$T/reparto.txt"
done
n1=$(grep -c "^$IP1:" "$T/reparto.txt" || true); n2=$(grep -c "^$IP2:" "$T/reparto.txt" || true)
echo "  atendidas por el nodo 1: $n1   por el nodo 2: $n2"
[ "$n1" -gt 0 ] && [ "$n2" -gt 0 ] || fallo "el balanceador no reparte entre los dos nodos"

paso "3. Tráfico continuo (10 peticiones/s) y caída del nodo 1"
rm -f "$T/parar"; : > "$T/trafico.csv"
(
  while [ ! -f "$T/parar" ]; do
    ini=$(ahora)
    r=$(curl -s -o /dev/null -D "$T/h" -w '%{http_code} %{time_total}' "$ENDPOINT" || echo "000 0")
    srv=$(tr -d '\r' < "$T/h" | awk -F': ' 'tolower($1)=="x-served-by"{print $2}' | awk -F', ' '{print $NF}')
    echo "$ini $r ${srv:-none}" >> "$T/trafico.csv"
    sleep 0.1
  done
) &
GEN=$!
sleep 8
t_stop=$(ahora)
docker stop "$N1" >/dev/null
echo "  docker stop $N1 ejecutado ($(( $(date +%s) )) UTC epoch)"
sleep 12
t_start=$(ahora)
docker start "$N1" >/dev/null
for i in $(seq 1 60); do [ "$(salud $N1)" = healthy ] && break; sleep 2; done
t_sano=$(ahora)
[ "$(salud $N1)" = healthy ] || { touch "$T/parar"; fallo "el nodo 1 no volvió a estar sano"; }
IP1_NUEVA=$(ip_de $N1)
if [ "$IP1_NUEVA" != "$IP1" ]; then
  echo "  AVISO: el nodo 1 cambió de IP ($IP1 -> $IP1_NUEVA); se recarga Nginx para que lo vea."
  docker exec $LB nginx -s reload >/dev/null
fi
sleep 15
touch "$T/parar"; wait $GEN 2>/dev/null || true

paso "4. Resultados"
read -r total errores < <(awk '{t++; if($2!=200)e++} END{print t+0, e+0}' "$T/trafico.csv")
# 'solo nodo sano': desde que instante todas las respuestas vienen del nodo 2 mientras el nodo 1 está caído
ultima_n1=$(awk -v ip="$IP1:" -v ts="$t_stop" '$1>=ts && index($4,ip)==1 {u=$1} END{print u+0}' "$T/trafico.csv")
tras_stop=$(awk -v ts="$t_stop" -v tr="$t_start" '$1>=ts && $1<tr {t++} END{print t+0}' "$T/trafico.csv")
n2_caida=$(awk -v ts="$t_stop" -v tr="$t_start" -v ip="$IP2:" '$1>=ts && $1<tr && index($4,ip)==1 {t++} END{print t+0}' "$T/trafico.csv")
lat_max=$(awk '{if($3>m)m=$3} END{printf "%d", m*1000}' "$T/trafico.csv")
lentas=$(awk '$3>0.9{n++} END{print n+0}' "$T/trafico.csv")
n1_despues=$(awk -v ts="$t_sano" -v a="$IP1:" -v b="$IP1_NUEVA:" '$1>=ts && (index($4,a)==1 || index($4,b)==1){t++} END{print t+0}' "$T/trafico.csv")
conmutacion=$(awk -v u="$ultima_n1" -v s="$t_stop" 'BEGIN{ if(u<s) printf "0.0"; else printf "%.1f", u-s }')
intentos=$(docker logs "$LB" --since 5m 2>&1 | grep -cE "connect\(\) failed|upstream timed out|no route to host" || true)

echo "  Peticiones totales: $total   con error visible para el usuario: $errores"
echo "  Durante la caída: $tras_stop peticiones, $n2_caida atendidas por el nodo 2 (100 % = $tras_stop)"
echo "  Tiempo hasta que el nodo 1 dejó de recibir tráfico tras docker stop: ${conmutacion} s"
echo "  Latencia máxima observada: ${lat_max} ms   (peticiones > 900 ms: $lentas, reintentadas por Nginx hacia el nodo sano)"
echo "  Intentos fallidos registrados por Nginx hacia el nodo caído: $intentos (max_fails=2, luego lo omite 10 s)"
echo "  Nodo 1 de vuelta en la rotación: $n1_despues peticiones atendidas después de reiniciarlo"
echo "  Tiempo en volver a estar sano tras docker start: $(awk -v a="$t_start" -v b="$t_sano" 'BEGIN{printf "%.0f", b-a}') s"

echo; echo "  Extracto del registro de Nginx durante la caída:"
docker logs "$LB" --since 3m 2>&1 | grep -E "connect\(\) failed|upstream timed out|no route" | head -4 | sed 's/^/    /' || true

[ "$errores" = 0 ] || fallo "hubo $errores peticiones con error visible para el usuario"
[ "$n2_caida" = "$tras_stop" ] || fallo "durante la caída no todo el tráfico fue al nodo sano"
[ "$n1_despues" -gt 0 ] || fallo "el nodo 1 no volvió a recibir tráfico tras reiniciarlo"

echo
echo "RESULTADO FAILOVER"
echo "  Interrupción percibida por el usuario: 0 s ($errores errores en $total peticiones)"
echo "  Latencia máxima durante la detección: ${lat_max} ms"
echo "  Reparto 40 peticiones antes de la caída: nodo 1 = $n1, nodo 2 = $n2"
echo "  Reincorporación del nodo 1: $n1_despues peticiones atendidas tras reiniciarlo"
