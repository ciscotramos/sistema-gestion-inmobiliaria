# Módulo de gestión de incidentes (tickets)

Implementa el registro, la priorización y el seguimiento de incidentes alineado con ITIL / ISO/IEC 20000-1.
Complementa el catálogo de [solicitudes de servicio](solicitudes-servicio.md): el formulario de GitHub sigue siendo la vía
externa y este módulo es la vía dentro del sistema.

## Modelo de datos

| Tabla | Atributos principales | Restricciones |
| --- | --- | --- |
| `prioridades` | `id`, `nivel` (P1–P4), `nombre`, `tiempo_sla_horas` | PK `id`; `nivel` único; SLA > 0 |
| `estados_ticket` | `id`, `nombre` (Abierto, En Proceso, Resuelto, Cerrado) | PK `id`; `nombre` único |
| `asignado_a` | `id`, `nombre` (Help Desk, Especialista DB, Equipo de Desarrollo), `nivel_soporte` (1–3) | PK `id`; `nombre` único; nivel entre 1 y 3 |
| `tickets` | `id`, `codigo`, `tipo`, `modulo_afectado`, `titulo`, `descripcion`, `impacto`, `urgencia`, `prioridad_id`, `estado_id`, `reportado_por`, `asignado_a`, `equipo_asignado_id`, `solucion`, `fecha_registro`, `fecha_limite_sla`, `fecha_resolucion` | PK `id`; `codigo` único; FK a `prioridades`, `estados_ticket`, `asignado_a` (equipo) y dos FK a `usuarios` (quien reporta y responsable) |

Script: `backend/schema_v1.sql` (tablas 14 a 17) o, en una base ya creada, `backend/db/migracion_v3_incidentes.sql` y luego
`backend/db/migracion_v4_asignado_a_modulo.sql`.

El responsable de un ticket puede ser un usuario del sistema (`asignado_a` en `tickets`), un equipo de soporte (`equipo_asignado_id`,
entidad `asignado_a`) o ambos. El módulo afectado se elige de un catálogo: Autenticación y usuarios, Clientes, Propiedades, Alquileres,
Operaciones, Comisiones, Reportes y dashboard, Base de datos e infraestructura y Otro.

## Priorización (impacto × urgencia)

Puntaje: Alto/Alta = 3, Medio/Media = 2, Bajo/Baja = 1. La suma decide el nivel:

| Suma | Prioridad | SLA de resolución |
| --- | --- | --- |
| 6 | P1 Crítico | 2 horas |
| 5 | P2 Alto | 8 horas |
| 4 | P3 Normal | 24 horas |
| 2–3 | P4 Bajo | 72 horas |

La prioridad se **sugiere** con esta matriz, pero el formulario permite elegir P1–P4 (campo opcional `prioridad`; si no llega, el servidor usa
la matriz). El servidor siempre calcula el límite SLA (`fecha_limite_sla = fecha_registro + SLA de la prioridad`) y la marca de tiempo.
Los tiempos SLA son una propuesta del equipo y se ajustan en la tabla `prioridades`, sin tocar código.

## Ciclo de vida

`Abierto → En Proceso → Resuelto → Cerrado` (un ticket Resuelto puede reabrirse a En Proceso).
Reglas: no se puede poner En Proceso sin responsable (usuario) o equipo de soporte asignado, ni Resolver sin registrar la solución aplicada.

## API

| Método y ruta | Rol | Descripción |
| --- | --- | --- |
| `GET /api/tickets/catalogo` | ADMIN, AGENTE | Tipos (las 7 tipologías), impactos, urgencias, estados y prioridades con SLA |
| `POST /api/tickets` | ADMIN, AGENTE | Reportar un incidente (201) |
| `GET /api/tickets` | ADMIN, AGENTE | El administrador ve todos; el agente, solo los que reportó |
| `GET /api/tickets/{id}` | ADMIN, AGENTE | Detalle (403 si es de otro usuario y no es administrador) |
| `PATCH /api/tickets/{id}` | ADMIN | Asignar responsable y/o equipo (`equipoAsignadoId`), cambiar de estado y registrar la solución |

Errores: 400 (datos o transición no válidos), 403, 404. Los textos rechazan `<` y `>` para frenar XSS.

## Pantalla

`/admin/incidentes` (administrador) y `/agente/incidentes` (agente): formulario de reporte (tipo, módulo afectado, título, descripción, impacto, urgencia y selector de prioridad con la sugerida),
panel de tickets con búsqueda, paginación, filtro «Solo activos» (Abierto y En Proceso) y refresco automático cada 30 s, y detalle con atención del ticket. Un ticket fuera de su límite SLA se marca como vencido.

## Pruebas

- Unitarias: `TicketServiceUnitTest` (matriz de prioridad, SLA, transiciones, visibilidad).
- Extremo a extremo: `scripts/prueba-incidentes.sh` contra el backend y MySQL reales; se ejecuta en el job
  **Base de datos** del CI.
