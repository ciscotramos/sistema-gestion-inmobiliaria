#!/bin/bash
# Prueba de extremo a extremo del módulo de incidentes contra el backend real y MySQL:
# login -> reportar -> prioridad y SLA automáticos -> atender -> reglas del ciclo de vida.
set -e
BASE=${BASE_URL:-http://localhost:8080}
EMAIL=${APP_ADMIN_EMAIL:?Define APP_ADMIN_EMAIL}
PASS=${APP_ADMIN_PASSWORD:?Define APP_ADMIN_PASSWORD}

echo "Esperando al backend..."
TOKEN=""
for i in $(seq 1 60); do
  TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASS\"}" | jq -r '.token // empty' 2>/dev/null || true)
  [ -n "$TOKEN" ] && break
  sleep 3
done
[ -n "$TOKEN" ] || { echo "FALLO: no se pudo iniciar sesión."; exit 1; }
echo "OK: login correcto."

AUTH="Authorization: Bearer $TOKEN"
JSON='Content-Type: application/json'
fallo() { echo "FALLO: $1"; exit 1; }
codigo() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

# 1. Reportar un incidente de impacto Alto y urgencia Alta -> P1, SLA de 2 h
RESP=$(curl -s -X POST "$BASE/api/tickets" -H "$AUTH" -H "$JSON" -d '{
  "tipo":"Errores de funcionamiento","titulo":"Error 500 al registrar cliente",
  "descripcion":"Al guardar un cliente nuevo la API responde 500.","impacto":"Alto","urgencia":"Alta"}')
ID=$(echo "$RESP" | jq -r .id); PRIO=$(echo "$RESP" | jq -r .prioridad); EST=$(echo "$RESP" | jq -r .estado)
[ "$PRIO" = "P1" ] || fallo "se esperaba P1 y llegó '$PRIO' ($RESP)"
[ "$EST" = "Abierto" ] || fallo "se esperaba estado Abierto"
[ "$(echo "$RESP" | jq -r .prioridadNombre)" = "Crítico" ] || fallo "tildes mal guardadas: llegó '$(echo "$RESP" | jq -r .prioridadNombre)' (juego de caracteres de la BD)"
echo "OK: ticket $(echo "$RESP" | jq -r .codigo) creado con prioridad $PRIO y SLA $(echo "$RESP" | jq -r .tiempoSlaHoras) h."

# 2. Validaciones: tipo inventado y HTML en la descripción
[ "$(codigo -X POST "$BASE/api/tickets" -H "$AUTH" -H "$JSON" -d '{"tipo":"Inventado","titulo":"Titulo valido","descripcion":"Descripcion valida larga","impacto":"Alto","urgencia":"Alta"}')" = "400" ] || fallo "tipo inválido debía dar 400"
[ "$(codigo -X POST "$BASE/api/tickets" -H "$AUTH" -H "$JSON" -d '{"tipo":"Errores de funcionamiento","titulo":"Titulo valido","descripcion":"<script>alert(1)</script>","impacto":"Alto","urgencia":"Alta"}')" = "400" ] || fallo "HTML en la descripción debía dar 400"
echo "OK: las validaciones rechazan datos inválidos (400)."

# 3. Reglas del ciclo de vida
[ "$(codigo -X PATCH "$BASE/api/tickets/$ID" -H "$AUTH" -H "$JSON" -d '{"estado":"Cerrado"}')" = "400" ] || fallo "Abierto -> Cerrado debía dar 400"
[ "$(codigo -X PATCH "$BASE/api/tickets/$ID" -H "$AUTH" -H "$JSON" -d '{"estado":"En Proceso"}')" = "400" ] || fallo "En Proceso sin responsable debía dar 400"
ADMIN_ID=$(curl -s "$BASE/api/admin/agentes" -H "$AUTH" | jq -r --arg e "$(echo "$EMAIL" | tr 'A-Z' 'a-z')" '.[] | select(.email==$e) | .id')
[ -n "$ADMIN_ID" ] || fallo "no se encontró el id del administrador"
EN=$(curl -s -X PATCH "$BASE/api/tickets/$ID" -H "$AUTH" -H "$JSON" -d "{\"asignadoAId\":$ADMIN_ID,\"estado\":\"En Proceso\"}" | jq -r .estado)
[ "$EN" = "En Proceso" ] || fallo "no pasó a En Proceso"
[ "$(codigo -X PATCH "$BASE/api/tickets/$ID" -H "$AUTH" -H "$JSON" -d '{"estado":"Resuelto"}')" = "400" ] || fallo "Resuelto sin solución debía dar 400"
RES=$(curl -s -X PATCH "$BASE/api/tickets/$ID" -H "$AUTH" -H "$JSON" -d '{"estado":"Resuelto","solucion":"Se corrigió la validación y se desplegó el parche."}')
[ "$(echo "$RES" | jq -r .estado)" = "Resuelto" ] || fallo "no pasó a Resuelto ($RES)"
[ "$(echo "$RES" | jq -r .slaVencido)" = "false" ] || fallo "el SLA no debía estar vencido"
echo "OK: flujo Abierto -> En Proceso -> Resuelto con reglas de negocio."

# 4. Listado y seguridad
N=$(curl -s "$BASE/api/tickets" -H "$AUTH" | jq 'length')
[ "$N" -ge 1 ] || fallo "el listado debía incluir el ticket"
[ "$(codigo "$BASE/api/tickets")" = "403" ] || [ "$(codigo "$BASE/api/tickets")" = "401" ] || fallo "sin token debía rechazarse"
echo "OK: listado ($N tickets) y acceso protegido sin token."
echo "TODAS LAS PRUEBAS DE INCIDENTES PASARON."
