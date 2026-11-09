#!/bin/bash
# Conecta la Replica al Master con GTID y espera a que quede sincronizada.
set -e
M="mysql -h mysql-master -uroot -p$MASTER_ROOT_PASSWORD"
R="mysql -h mysql-replica -uroot -p$REPLICA_ROOT_PASSWORD"

estado=$($R -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -c Replica_IO_Running || true)
if [ "$estado" -gt 0 ]; then
  echo "La replicación ya estaba configurada."
else
  $R -e "
    CHANGE REPLICATION SOURCE TO
      SOURCE_HOST='mysql-master', SOURCE_PORT=3306,
      SOURCE_USER='replicador', SOURCE_PASSWORD='$REPLICATION_PASSWORD',
      SOURCE_AUTO_POSITION=1, GET_SOURCE_PUBLIC_KEY=1;
    START REPLICA;"
fi

# Solo lectura persistente (se aplica aquí y no en el .cnf para no bloquear el arranque inicial de la imagen).
$R -e "SET PERSIST super_read_only=ON; SET PERSIST read_only=ON;"

for i in $(seq 1 60); do
  io=$($R -e "SHOW REPLICA STATUS\G" | awk '/Replica_IO_Running:/{print $2}')
  sql=$($R -e "SHOW REPLICA STATUS\G" | awk '/Replica_SQL_Running:/{print $2}')
  if [ "$io" = "Yes" ] && [ "$sql" = "Yes" ]; then
    echo "Replicación activa (IO=$io, SQL=$sql)."
    exit 0
  fi
  sleep 2
done
echo "La replicación no se activó:"; $R -e "SHOW REPLICA STATUS\G" | grep -E "Last_(IO|SQL)_Error|Running"
exit 1
