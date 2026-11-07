/**
 * alquileresService.js
 * Capa de servicio para el módulo de Alquileres.
 *
 * PREPARADO PARA BACKEND.
 * Los alquileres son información administrativa interna.
 * NO se deben exponer al frontend público los detalles de contrato.
 * Solo se puede exponer el ESTADO de la propiedad (Disponible / Alquilada).
 */

// const API_BASE = `${import.meta.env.VITE_API_URL}/alquileres`;

export const getAlquileres = async () => {
  // TODO: return fetch(API_BASE).then(r => r.json());
  return [];
};

export const getAlquilerById = async (id) => {
  // TODO: return fetch(`${API_BASE}/${id}`).then(r => r.json());
  return null;
};

export const createAlquiler = async (data) => {
  // TODO:
  // return fetch(API_BASE, {
  //   method: "POST",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

export const updateAlquiler = async (id, data) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, {
  //   method: "PUT",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

export const deleteAlquiler = async (id) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  throw new Error("Backend no implementado aún.");
};
