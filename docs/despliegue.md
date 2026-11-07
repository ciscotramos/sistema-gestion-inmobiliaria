# Despliegue (versión V1)

El backend se empaqueta con Docker en dos etapas y se publica como servicio web (Render) detrás de HTTPS.

## Imagen Docker (`backend/Dockerfile`)

| Etapa | Imagen base | Función |
| --- | --- | --- |
| build | `maven:3.9.6-eclipse-temurin-21` | Compila con `mvn clean package -DskipTests` |
| runtime | `eclipse-temurin:21-jre-alpine` | Ejecuta solo el JAR con JRE 21 |

Construcción local:

```bash
docker build -t inmobiliaria-backend ./backend
docker run -p 8080:8080 --env-file backend.env inmobiliaria-backend
```

## Variables de entorno

Ningún secreto va en el repositorio. Todo se entrega por variables de entorno:

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `DB_HOST`, `DB_NAME` | Host y nombre de la base | `localhost`, `inmobiliaria_db` |
| `DB_PORT_MASTER` / `DB_PORT_REPLICA` | Puertos de escritura y lectura | `3306` / `3307` |
| `DB_USER`, `DB_PASSWORD` | Usuario y clave del Master | `root`, sin definir |
| `DB_USER_REPLICA`, `DB_PASSWORD_REPLICA` | Usuario y clave de la Replica | los del Master |
| `JWT_SECRET` | Clave de firma HS256 del JWT | sin definir (obligatoria) |
| `JWT_EXPIRATION` | Duración del token en milisegundos | `86400000` (24 horas) |
| `SERVER_PORT` | Puerto HTTP | definido en `application.properties` |

## Base de datos

1. Crear el esquema con `backend/schema_v1.sql` (13 tablas).
2. Si la base ya existía, aplicar `backend/db/migracion_v2_propiedades_version.sql`
   **antes** de desplegar: `ddl-auto=validate` exige la columna `version` de `propiedades`.
3. Cargar datos de ejemplo con `backend/data.sql` solo en entornos de prueba.

El enrutamiento es automático: las transacciones de solo lectura van a la Replica y
las de escritura al Master (`RoutingDataSource`).

## Verificación posterior al despliegue

- `GET /swagger-ui.html` carga la documentación interactiva.
- Una ruta protegida sin token responde `401` en JSON.
- Iniciar sesión por `/api/auth/**` y autorizar en Swagger con el botón *Authorize*.
