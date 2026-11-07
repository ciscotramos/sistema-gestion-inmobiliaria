# Plan de gestión y control de cambios

Todo cambio entra al código principal solo después de ser evaluado, revisado y validado.

| Paso | Actividad | Cómo se hace en GitHub |
| --- | --- | --- |
| 1 | Solicitud de cambio | Se registra un issue (plantilla *Solicitud de servicio*) |
| 2 | Evaluación de impacto | Se marca el impacto en usuarios, propiedades, autenticación y transacciones en el Pull Request |
| 3 | Asignación y desarrollo | El responsable trabaja en una rama `feature/` o `fix/` con commits descriptivos |
| 4 | Revisión por Pull Request | Revisión cruzada obligatoria: otro integrante aprueba antes del merge |
| 5 | Integración y validación | Merge a `develop`; el CI compila y ejecuta las pruebas; se valida con Postman y Swagger |

## Git Flow

- `main`: versión estable y desplegada.
- `develop`: integración continua del equipo.
- `feature/*`: funcionalidades nuevas, parten de `develop`.
- `fix/*`: correcciones, parten de `develop`.

## Integración continua

`.github/workflows/ci.yml` compila el backend (Maven, Java 21), ejecuta las pruebas unitarias
(`*UnitTest`, sin base de datos) y construye el frontend (React + Vite) en cada push y Pull Request.

Pendiente: automatizar las pruebas de integración, que hoy requieren MySQL Master y Replica.
