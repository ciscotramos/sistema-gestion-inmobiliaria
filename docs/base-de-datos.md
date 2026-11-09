# Base de datos

MySQL 8.0 en topología **Master/Replica**: el Master recibe las escrituras y la Replica atiende las lecturas
(el backend enruta con `AbstractRoutingDataSource`: transacciones `readOnly` van a la Replica, el resto al Master).

## Levantar en local

```bash
cp .env.example .env          # cambia las tres contraseñas
docker compose up -d          # Master (3306) + Replica (3307)
./scripts/verificar-replicacion.sh
```

Al crear el Master se carga `backend/schema_v1.sql` (16 tablas, InnoDB, utf8mb4, 3FN) y se crea el usuario `replicador`.
El servicio de un solo uso `replica-init` conecta la Replica con replicación por **GTID** (`SOURCE_AUTO_POSITION=1`) y la deja en solo lectura.

Para levantar también el backend: `docker compose --profile app up -d --build` (requiere `JWT_SECRET` en `.env`).

## Datos de ejemplo (solo desarrollo)

```bash
docker exec -i inmobiliaria-master mysql -uroot -p"$DB_PASSWORD" inmobiliaria_db < backend/data.sql
```

Incluye usuarios de ejemplo: no cargarlo en producción. El primer administrador real se crea con `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`.

## Comprobación automática

El job **Base de datos (Master/Replica)** del CI levanta ambos servidores, comprueba que la Replica tenga las 16 tablas,
que una escritura en el Master aparezca en la Replica y que la Replica rechace escrituras.

## Notas

- Los puertos se publican solo en `127.0.0.1`.
- Reiniciar desde cero: `docker compose down -v` (borra los datos).
- En Render u otra nube se usa una base administrada (MySQL con réplica de lectura) y el esquema se aplica con `schema_v1.sql`; las variables de conexión están en [despliegue.md](despliegue.md).
- Si ya existía una base creada antes del bloqueo optimista: `backend/db/migracion_v2_propiedades_version.sql`.
