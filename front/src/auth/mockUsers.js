/**
 * src/auth/mockUsers.js
 *
 * FUENTE ÚNICA de usuarios del sistema (mock).
 * Se persiste en localStorage["usuarios_mock"].
 *
 * Roles:
 *   ADMIN  → panel completo /admin/*
 *   AGENTE → panel limitado /agente/*
 *
 * El Administrador NO se crea desde la interfaz.
 * Los Agentes son creados por el Administrador desde /admin/usuarios.
 *
 * FUTURO: Reemplazar la lectura de localStorage por POST /api/auth/login
 * y GET /api/usuarios?rol=AGENTE cuando exista el backend.
 */

export const SEED_USERS = [
  {
    id: 1,
    nombre: "Luis Administrador",
    email: "admin@inmobiliaria.com",
    password: "admin123", // solo mock — NO usar en producción
    rol: "ADMIN",
    telefono: "",
    estado: "Activo",
  },
  {
    id: 2,
    nombre: "Juan Carlos Pérez",
    email: "juan.perez@inmobiliaria.com",
    password: "agente123",
    rol: "AGENTE",
    telefono: "+51 987 654 321",
    estado: "Activo",
  },
  {
    id: 3,
    nombre: "María Fernanda López",
    email: "maria.lopez@inmobiliaria.com",
    password: "agente123",
    rol: "AGENTE",
    telefono: "+51 987 123 456",
    estado: "Activo",
  },
];

const STORAGE_KEY = "usuarios_mock";

/**
 * Lee la lista de usuarios desde localStorage.
 * Si no existe, inicializa con SEED_USERS.
 */
export function getUsuariosMock() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (_) {
    // storage corrupto → reinicializar
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(SEED_USERS));
  return SEED_USERS;
}

/**
 * Persiste la lista completa de usuarios en localStorage.
 */
export function setUsuariosMock(lista) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lista));
}

/**
 * Valida credenciales y retorna el usuario si existe y está Activo.
 * @returns {object|null}
 */
export function validarCredenciales(email, password) {
  const usuarios = getUsuariosMock();
  return (
    usuarios.find(
      (u) =>
        u.email === email.trim() &&
        u.password === password &&
        u.estado === "Activo"
    ) || null
  );
}
