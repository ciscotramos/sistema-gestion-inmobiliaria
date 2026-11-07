# Política de seguridad

## Reportar una vulnerabilidad

Si encuentras una vulnerabilidad, **no abras un issue público**. Avísanos por un canal privado
(correo del responsable del repositorio o *Security advisories* de GitHub) con:

- descripción del problema y su impacto,
- pasos para reproducirlo,
- versión o rama afectada.

Responderemos lo antes posible y publicaremos la corrección antes de divulgar el detalle.

## Buenas prácticas del proyecto

- Los secretos (`JWT_SECRET`, contraseñas de base de datos) solo se entregan por variables de entorno.
- Gitleaks revisa cada push y Pull Request para detectar secretos filtrados.
- CodeQL y Dependabot vigilan el código y las dependencias.
- Todo cambio entra por Pull Request con revisión cruzada.

Detalle de los controles técnicos: [docs/seguridad.md](docs/seguridad.md).
