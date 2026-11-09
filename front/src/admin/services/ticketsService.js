/**
 * ticketsService.js
 * Capa de servicio del módulo de incidentes (tickets).
 * Conectado a la API REST de Spring Boot con JWT y autorización RBAC.
 */

import { apiRequest } from "../../services/apiClient";

export const getCatalogoTickets = () => apiRequest("/api/tickets/catalogo");

export const getTickets = () => apiRequest("/api/tickets");

export const createTicket = (data) =>
  apiRequest("/api/tickets", {
    method: "POST",
    body: {
      tipo: data.tipo,
      titulo: data.titulo,
      descripcion: data.descripcion,
      impacto: data.impacto,
      urgencia: data.urgencia,
    },
  });

export const atenderTicket = (id, data) =>
  apiRequest(`/api/tickets/${id}`, {
    method: "PATCH",
    body: {
      estado: data.estado || null,
      asignadoAId: data.asignadoAId ? Number(data.asignadoAId) : null,
      solucion: data.solucion || null,
    },
  });
