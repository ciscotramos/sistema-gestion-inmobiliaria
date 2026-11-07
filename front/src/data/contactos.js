export const SEED_CONTACTOS = [
  {
    id: 1,
    nombre: "Pedro",
    email: "pedro@mail.com",
    telefono: "999888111",
    propiedad: "Departamento en Surquillo",
    propiedad_id: 12,
    agente_id: 3,
    tipo: "Solicitud de visita",
    mensaje: "Quiero visitarlo mañana",
    fecha: new Date().toISOString(),
    estado: "Pendiente",
    seguimientos: []
  },
  {
    id: 2,
    nombre: "María",
    email: "maria@mail.com",
    telefono: "999888222",
    propiedad: "Local Comercial Ica",
    propiedad_id: null,
    agente_id: null,
    tipo: "Contacto general",
    mensaje: "Me interesa comprar un local comercial",
    fecha: new Date().toISOString(),
    estado: "Atendido",
    seguimientos: []
  },
  {
    id: 3,
    nombre: "Carlos",
    email: "carlos@mail.com",
    telefono: "999888333",
    propiedad: "Casa en La Molina",
    propiedad_id: 1,
    agente_id: 2,
    tipo: "Contacto desde propiedad",
    mensaje: "Deseo más información de esta propiedad.",
    fecha: new Date().toISOString(),
    estado: "Pendiente",
    seguimientos: []
  }
];

const STORAGE_KEY = "contactos_mock";

export function getContactosMock() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (_) {}
  localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_CONTACTOS));
  return SEED_CONTACTOS;
}

export function setContactosMock(lista) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lista));
}
