# Gestión de solicitudes de servicio (ITSM)

Es un proceso del equipo apoyado en GitHub Issues y Projects. No es una funcionalidad de la aplicación
ni existe una tabla de solicitudes en la base de datos.

## Catálogo de 7 tipos

| N° | Tipo | Descripción | Prioridad |
| --- | --- | --- | --- |
| 1 | Solicitud de información | Consultas sobre propiedades, características, precios y estados | Baja |
| 2 | Creación y gestión de usuarios | Nuevos usuarios, roles y permisos | Media |
| 3 | Problemas de autenticación | Inicio de sesión, recuperación de acceso, validación del token JWT | Alta |
| 4 | Incidencias en propiedades | Errores al registrar, editar, visualizar o actualizar el estado | Alta |
| 5 | Incidencias en operaciones comerciales | Registro y seguimiento de ventas o alquileres | Alta |
| 6 | Solicitudes de modificación | Actualización de información o ajustes de funcionalidades | Media |
| 7 | Errores de funcionamiento | Fallos de interfaz, respuestas inesperadas de la API, integración frontend-backend | Alta |

## Flujo de atención (6 pasos)

1. **Registro:** se abre un issue con la plantilla *Solicitud de servicio*.
2. **Clasificación y priorización:** se elige el tipo y la prioridad.
3. **Asignación:** se designa al responsable.
4. **Análisis y atención:** se reproduce el fallo con Postman o Swagger y se corrige en una rama `fix/`.
5. **Pruebas y validación:** Pull Request con revisión cruzada y verificación de la solución.
6. **Cierre:** se confirma con quien reportó y se cierra el issue.

Un defecto de código que requiere un parche conserva la prioridad de su tipo; no pasa a
"Solicitud de modificación", que es para mejoras.
