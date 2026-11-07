/**
 * src/services/caracteristicasService.js
 *
 * Capa de servicio para el módulo de Características / Amenidades.
 * Conectado a la API REST de Spring Boot (/api/caracteristicas).
 */

import { apiRequest } from "./apiClient";

export const getCaracteristicas = async () => {
  return await apiRequest("/api/caracteristicas");
};

export const getCaracteristicaById = async (id) => {
  return await apiRequest(`/api/caracteristicas/${id}`);
};

export const createCaracteristica = async (data) => {
  return await apiRequest("/api/caracteristicas", {
    method: "POST",
    body: {
      nombre: data.nombre ? data.nombre.trim() : "",
      categoria: data.categoria ? data.categoria.trim() : "General",
    },
  });
};

export const deleteCaracteristica = async (id) => {
  return await apiRequest(`/api/caracteristicas/${id}`, {
    method: "DELETE",
  });
};
