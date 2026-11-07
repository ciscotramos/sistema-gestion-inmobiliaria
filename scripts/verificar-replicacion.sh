#!/bin/bash
# Comprueba que lo escrito en el Master aparece en la Replica y que la Replica rechaza escrituras.
set -e
source .env 2>/dev/null || true
DB=${DB_NAME:-inmobiliaria_db}
M="docker exec inmobiliaria-master mysql -uroot -p${DB_PASSWORD} -N $DB"
R="docker exec inmobiliaria-replica mysql -uroot -p${DB_PASSWORD_REPLICA:-$DB_PASSWORD} -N $DB"

echo "Tablas en Master:  $($M -e 'SHOW TABLES' 2>/dev/null | wc -l)"
echo "Tablas en Replica: $($R -e 'SHOW TABLES' 2>/dev/null | wc -l)"

$M -e "INSERT INTO caracteristicas (nombre, categoria) VALUES ('__prueba_replicacion__', 'Interior')" 2>/dev/null
sleep 2
n=$($R -e "SELECT COUNT(*) FROM caracteristicas WHERE nombre='__prueba_replicacion__'" 2>/dev/null)
$M -e "DELETE FROM caracteristicas WHERE nombre='__prueba_replicacion__'" 2>/dev/null
[ "$n" = "1" ] && echo "OK: la escritura en el Master llegó a la Replica." || { echo "FALLO: no se replicó."; exit 1; }

if $R -e "DELETE FROM caracteristicas WHERE 1=0" 2>/dev/null && $R -e "INSERT INTO caracteristicas (nombre, categoria) VALUES ('x','Interior')" 2>/dev/null; then
  echo "FALLO: la Replica aceptó una escritura."; exit 1
else
  echo "OK: la Replica es de solo lectura."
fi
