/**
 * comisionesService.js
 * Capa de servicio para el módulo de Comisiones.
 *
 * PREPARADO PARA BACKEND.
 * Las comisiones son información INTERNA del sistema.
 * NUNCA deben exponerse al frontend público.
 *
 * Acceso futuro por rol:
 *   ADMIN → ver todas las comisiones
 *   AGENTE → ver solo sus propias comisiones (/agente/comisiones)
 */

// const API_BASE = `${import.meta.env.VITE_API_URL}/comisiones`;

export const getComisiones = async () => {
  // TODO: return fetch(API_BASE).then(r => r.json());
  return [];
};

export const getComisionById = async (id) => {
  // TODO: return fetch(`${API_BASE}/${id}`).then(r => r.json());
  return null;
};

export const createComision = async (data) => {
  // TODO:
  // return fetch(API_BASE, {
  //   method: "POST",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

export const updateComision = async (id, data) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, {
  //   method: "PUT",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

export const deleteComision = async (id) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  throw new Error("Backend no implementado aún.");
};
