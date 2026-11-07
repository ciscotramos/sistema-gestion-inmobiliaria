/**
 * usuariosService.js
 * Capa de servicio para el módulo de Usuarios / Agentes.
 * Conectado a la API REST de Spring Boot con JWT y autorización RBAC (ROLE_ADMIN).
 */

import { apiRequest } from "../../services/apiClient";

export const getUsuarios = async () => {
  return await apiRequest("/api/admin/agentes");
};

export const createUsuario = async (data) => {
  return await apiRequest("/api/admin/agentes", {
    method: "POST",
    body: {
      nombre: data.nombre,
      email: data.email,
      password: data.password,
      telefono: data.telefono || "",
      cargo: data.cargo || "Asesor Inmobiliario",
    },
  });
};
