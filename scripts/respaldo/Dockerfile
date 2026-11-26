# Contenedor de respaldos automáticos (Taller Sesión 32).
# Parte de la imagen Debian de MySQL 8.0 para que mysqldump sea de la MISMA versión que el servidor
# (el mysqldump de MariaDB no entiende opciones como --source-data ni --set-gtid-purged).
FROM mysql:8.0-debian
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends awscli openssl gzip ca-certificates \
 && rm -rf /var/lib/apt/lists/*
COPY *.sh /opt/respaldo/
RUN chmod +x /opt/respaldo/*.sh
ENTRYPOINT ["/bin/bash", "/opt/respaldo/programador.sh"]
