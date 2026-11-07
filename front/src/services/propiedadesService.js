/**
 * Servicio API para gestión de propiedades e imágenes.
 */
import { apiRequest } from "./apiClient";

export const getPropiedades = async (params = {}) => {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.append(key, value);
    }
  });
  const queryString = query.toString() ? `?${query.toString()}` : "";
  return await apiRequest(`/api/propiedades${queryString}`);
};

export const getPropiedadById = async (id) => {
  return await apiRequest(`/api/propiedades/${id}`);
};

export const createPropiedad = async (data) => {
  return await apiRequest("/api/propiedades", {
    method: "POST",
    body: data,
  });
};

export const updatePropiedad = async (id, data) => {
  return await apiRequest(`/api/propiedades/${id}`, {
    method: "PUT",
    body: data,
  });
};

export const updateEstadoPropiedad = async (id, estado) => {
  return await apiRequest(`/api/propiedades/${id}/estado`, {
    method: "PATCH",
    body: { estado },
  });
};

export const deletePropiedad = async (id) => {
  return await apiRequest(`/api/propiedades/${id}`, {
    method: "DELETE",
  });
};

export const getImagenesPropiedad = async (propiedadId) => {
  return await apiRequest(`/api/propiedades/${propiedadId}/imagenes`);
};

export const addImagenPropiedad = async (propiedadId, imagenData) => {
  return await apiRequest(`/api/propiedades/${propiedadId}/imagenes`, {
    method: "POST",
    body: imagenData,
  });
};

export const deleteImagenPropiedad = async (propiedadId, imagenId) => {
  return await apiRequest(`/api/propiedades/${propiedadId}/imagenes/${imagenId}`, {
    method: "DELETE",
  });
};

export const setPrincipalImagenPropiedad = async (propiedadId, imagenId) => {
  return await apiRequest(`/api/propiedades/${propiedadId}/imagenes/${imagenId}/principal`, {
    method: "PATCH",
  });
};
