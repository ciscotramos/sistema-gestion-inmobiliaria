# Sistema Web de Gestión Inmobiliaria (MVP) para la Trazabilidad Comercial

Sistema web para la gestión comercial, alquileres y control de comisiones de una inmobiliaria.
Proyecto del Curso Integrador II: Sistemas (UTP sede Arequipa).

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Frontend | React + Vite (`front/`) |
| Backend | Java 21, Spring Boot 3.3.4, Spring Security, JWT (HS256), BCrypt (`backend/`) |
| Persistencia | Spring Data JPA / Hibernate, HikariCP |
| Base de datos | MySQL 8.0 con Master (3306) y Replica (3307), 13 tablas |
| Documentación de la API | SpringDoc OpenAPI 2.6.0, Swagger UI en `/swagger-ui.html` |
| Despliegue | Docker multi-stage, Render, HTTPS |

## Funcionalidad destacada

- **Seguridad:** login con JWT, roles `ADMIN` y `AGENTE`, y verificación de que un agente solo modifique sus propiedades.
- **Concurrencia:** `@Version` en `Propiedad` (bloqueo optimista), `PESSIMISTIC_WRITE` al registrar ventas y alquileres,
  aislamiento `READ_COMMITTED` con `rollbackFor = Exception.class`, y respuesta `409` ante conflictos.
- **Rutas de datos:** las lecturas van a la Replica y las escrituras al Master (`RoutingDataSource`).
- **API documentada:** Swagger UI con esquema Bearer JWT para probar endpoints protegidos.

## Ejecución local

```bash
# Backend (requiere MySQL en 3306 y 3307, o ambos apuntando a la misma instancia)
export DB_PASSWORD=... JWT_SECRET=$(openssl rand -base64 48)   # obligatorias: sin ellas no arranca
export SEED_DEMO_USERS=true   # solo desarrollo: crea admin y agente de ejemplo
cd backend && mvn spring-boot:run

# Frontend
cd front && npm ci && npm run dev
```

La base se crea con `backend/schema_v1.sql`. Si ya existía, aplicar `backend/db/migracion_v2_propiedades_version.sql`.

## Proceso de trabajo

- [Control de cambios y Git Flow](docs/gestion-cambios.md)
- [Gestión de solicitudes de servicio (ITSM)](docs/solicitudes-servicio.md)
- [Capacidad y rendimiento](docs/capacidad-rendimiento.md)
- [Despliegue](docs/despliegue.md)
- [Seguridad](docs/seguridad.md) y [política de seguridad](SECURITY.md)

## Alcance y pendientes

- Comisiones, clientes y contactos: el esquema SQL los contempla, pero aún no tienen API.
- Las pruebas de integración (`*Tests`) requieren MySQL Master y Replica; el CI ejecuta las pruebas unitarias (`*UnitTest`).
- No hay mediciones de rendimiento validadas (ver `docs/capacidad-rendimiento.md`).
