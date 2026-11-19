// Prueba de carga con k6 (Taller Sesión 30, Ficha 04, opción A).
// Uso:  k6 run --vus 50 --duration 60s scripts/script_carga.js
// Variables: BASE_URL (por defecto http://localhost:8080) y MONITORING_TOKEN (para el simulador).
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.MONITORING_TOKEN || '';

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<1000'],   // el criterio también es un percentil, no un promedio
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  // Tráfico normal: endpoint público de propiedades y estado
  check(http.get(`${BASE}/api/propiedades`), { 'propiedades 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/public/status`), { 'status 200': (r) => r.status === 200 });

  // Degradación controlada (requiere MONITORING_SIMULATION=true en el backend): 1 de cada 4 iteraciones falla
  if (TOKEN && __ITER % 4 === 0) {
    http.post(`${BASE}/api/monitoreo/simular?tipo=error500`, null, {
      headers: { Authorization: `Bearer ${TOKEN}` },
    });
  }
  sleep(0.2);
}
