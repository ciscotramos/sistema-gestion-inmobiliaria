#!/bin/bash
# Funciones comunes de respaldo y restauración (Taller Sesión 31). Se carga con `source`.
source .env 2>/dev/null || true
DB=${DB_NAME:-inmobiliaria_db}
MASTER=${CONTENEDOR_MASTER:-inmobiliaria-master}
REPLICA=${CONTENEDOR_REPLICA:-inmobiliaria-replica}
DEST=${RESPALDO_DIR:-respaldos}

# Ejecuta SQL en un contenedor MySQL: sql <contenedor> <clave> [opciones de mysql]
# La clave viaja por variable de entorno (MYSQL_PWD), no por la línea de comandos.
sql() { local c=$1 p=$2; shift 2; docker exec -i -e MYSQL_PWD="$p" "$c" mysql -uroot "$@"; }

# Filas por tabla, ordenadas: conteos_tablas <contenedor> <clave>
conteos_tablas() {
  sql "$1" "$2" -N "$DB" 2>/dev/null <<'SQL' | sort
SET SESSION group_concat_max_len = 1000000;
SELECT GROUP_CONCAT(CONCAT('SELECT ''', table_name, ''' AS t, COUNT(*) AS n FROM `', table_name, '`') SEPARATOR ' UNION ALL ')
  INTO @q FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';
PREPARE s FROM @q;
EXECUTE s;
SQL
}

# Archivo y posición del binlog que mysqldump guarda en el encabezado de un respaldo completo.
posicion_binlog() {
  { gunzip -c "$1" | head -80 | sed -nE "s/.*(SOURCE|MASTER)_LOG_FILE='([^']+)', *(SOURCE|MASTER)_LOG_POS=([0-9]+).*/\2 \4/p" | head -1; } || true
}
