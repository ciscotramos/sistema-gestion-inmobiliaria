/**
 * src/services/apiClient.js
 *
 * Cliente HTTP centralizado para peticiones a la API REST de Spring Boot.
 * Inyecta automáticamente la cabecera 'Authorization: Bearer <token>' obtenida del JWT.
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem("token");

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  if (config.body && typeof config.body === "object") {
    config.body = JSON.stringify(config.body);
  }

  const url = endpoint.startsWith("http") ? endpoint : `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, config);

  if (response.status === 401) {
    // Sesión expirada o token inválido
    localStorage.removeItem("token");
    localStorage.removeItem("sesion_actual");
    if (window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `Error ${response.status}: ${response.statusText}`;
    const err = new Error(errorMsg);
    err.status = response.status;
    err.data = data;
    throw err;
  }

  return data;
}

export default apiRequest;
