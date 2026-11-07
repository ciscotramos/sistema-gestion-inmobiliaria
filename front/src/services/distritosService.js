/**
 * src/services/distritosService.js
 *
 * Capa de servicio para el módulo de Distritos / Ubicaciones.
 * Conectado a la API REST de Spring Boot (/api/distritos).
 */

import { apiRequest } from "./apiClient";

export const getDistritos = async () => {
  return await apiRequest("/api/distritos");
};

export const getDistritoById = async (id) => {
  return await apiRequest(`/api/distritos/${id}`);
};

export const createDistrito = async (data) => {
  return await apiRequest("/api/distritos", {
    method: "POST",
    body: {
      nombre: data.nombre ? data.nombre.trim() : "",
      provincia: data.provincia ? data.provincia.trim() : "Lima",
      departamento: data.departamento ? data.departamento.trim() : "Lima",
    },
  });
};
