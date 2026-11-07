# Seguridad

Controles implementados en el backend (Spring Boot 3.3.4) y en el repositorio, y lo que sigue pendiente.

## Controles implementados

| Área | Control | Dónde |
| --- | --- | --- |
| Autenticación | JWT HS256 con emisor (`iss`), identificador único (`jti`) y expiración; se rechaza token alterado, de otra clave, sin emisor o vencido | `JwtService` |
| Clave del JWT | Obligatoria y de al menos 32 bytes; sin valor por defecto: **la aplicación no arranca** si falta o es débil | `JwtService.validarConfiguracion` |
| Vigencia | 2 horas por defecto (`JWT_EXPIRATION` en milisegundos); un usuario inactivo pierde acceso aunque su token siga vigente | `JwtService`, `application.properties` |
| Contraseñas | BCrypt; alta de agentes con 8 a 72 caracteres, letras y números, sin `<` ni `>` (BCrypt solo procesa 72 bytes) | `AgenteCreateDTO` |
| Fuerza bruta | 5 fallos por cuenta o 20 por IP en 15 minutos bloquean con HTTP 429 y `Retry-After` | `LoginAttemptService`, `AuthController` |
| Usuarios de ejemplo | Desactivados por defecto; solo con `SEED_DEMO_USERS=true` (desarrollo y pruebas) | `DataSeeder` |
| Primer administrador | Se crea desde `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` (mínimo 10 caracteres) si aún no existe | `DataSeeder` |
| Autorización | RBAC `ADMIN` y `AGENTE`, verificación de propiedad del recurso, `anyRequest().authenticated()` | `SecurityConfig`, `PropiedadService` |
| CORS | Orígenes configurables con `CORS_ALLOWED_ORIGINS`; por defecto solo localhost | `SecurityConfig` |
| Cabeceras HTTP | `X-Content-Type-Options`, `X-Frame-Options: DENY`, `frame-ancestors 'none'`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, HSTS bajo HTTPS | `SecurityConfig` |
| Errores | Respuestas sin trazas ni detalles internos; cualquier fallo de autenticación responde 401 en JSON | `GlobalExceptionHandler`, `server.error.*` |
| Transporte a MySQL | `sslMode` configurable con `DB_SSL_MODE` (por defecto `PREFERRED`) | `application.properties` |
| Documentación | Swagger configurable con `SWAGGER_ENABLED` | `application.properties` |
| Inyección SQL | Consultas con parámetros: JPA y `Specification` | repositorios |
| Repositorio | Gitleaks en cada push, CodeQL (Java y JavaScript), Dependabot, plantilla de PR con revisión cruzada | `.github/` |

## Lista de verificación antes de desplegar en producción

1. Definir `JWT_SECRET` (aleatoria, 32 caracteres o más). Si antes se usó otra, **rotarla**: los tokens existentes dejan de valer.
2. Definir `DB_PASSWORD` y `DB_PASSWORD_REPLICA`, distintas de las de desarrollo.
3. Definir `CORS_ALLOWED_ORIGINS` con el dominio real del frontend.
4. **No** definir `SEED_DEMO_USERS`. Si la base ya tenía los usuarios de ejemplo, cambiarles la contraseña o desactivarlos.
5. Crear el primer administrador con `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`, o desde la base de datos.
6. Usar `DB_SSL_MODE=REQUIRED` (o `VERIFY_CA`) si MySQL lo soporta.
7. Valorar `SWAGGER_ENABLED=false` si no hace falta la documentación pública.

Generar una clave: `openssl rand -base64 48`

## Limitaciones conocidas

- El **token se guarda en `localStorage`** del navegador: un ataque XSS podría leerlo. La alternativa es una cookie `HttpOnly` con protección CSRF.
- No hay **refresh token** ni **revocación** de tokens (por ejemplo al cambiar la contraseña o cerrar sesión): un token robado sirve hasta que vence.
- El bloqueo de intentos vive **en memoria**: se reinicia con la aplicación y no se comparte entre varias instancias. Con más de una instancia haría falta Redis o la base de datos.
- No hay autenticación de dos factores.
- Las contraseñas del historial de Git del equipo (repositorio `gerson-13`) siguen visibles ahí: rotar cualquier valor que se haya usado en producción.
