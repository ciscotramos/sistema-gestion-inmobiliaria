export const SEED_OPERACIONES = [
  {
    id: 1,
    cliente_id: 1,
    propiedad_id: 3,
    agente_id: 3,
    tipo: "Venta",
    monto: 220000,
    fecha: "2026-08-10",
    estado: "Concretada"
  },
  {
    id: 2,
    cliente_id: 3,
    propiedad_id: 11,
    agente_id: 2,
    tipo: "Venta",
    monto: 390000,
    fecha: "2026-08-15",
    estado: "En negociación"
  }
];

const STORAGE_KEY = "operaciones_mock";

export function getOperacionesMock() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (_) {}
  localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_OPERACIONES));
  return SEED_OPERACIONES;
}

export function setOperacionesMock(lista) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lista));
}
