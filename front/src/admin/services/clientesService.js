/**
 * clientesService.js
 * Capa de servicio para el módulo de Clientes.
 *
 * PREPARADO PARA BACKEND:
 * Cuando exista la API REST, reemplazar el cuerpo de cada función
 * con la llamada fetch/axios correspondiente.
 *
 * Ejemplo futuro:
 *   const API_BASE = import.meta.env.VITE_API_URL + "/clientes";
 *   export const getClientes = () => fetch(API_BASE).then(r => r.json());
 */

// TODO: Descomentar y configurar cuando exista el backend
// const API_BASE = `${import.meta.env.VITE_API_URL}/clientes`;

/**
 * Obtiene el listado de clientes.
 * @returns {Promise<Array>}
 */
export const getClientes = async () => {
  // TODO: return fetch(API_BASE).then(r => r.json());
  return [];
};

/**
 * Obtiene un cliente por ID.
 * @param {number|string} id
 * @returns {Promise<Object>}
 */
export const getClienteById = async (id) => {
  // TODO: return fetch(`${API_BASE}/${id}`).then(r => r.json());
  return null;
};

/**
 * Crea un nuevo cliente.
 * @param {Object} data
 * @returns {Promise<Object>}
 */
export const createCliente = async (data) => {
  // TODO:
  // return fetch(API_BASE, {
  //   method: "POST",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

/**
 * Actualiza un cliente existente.
 * @param {number|string} id
 * @param {Object} data
 * @returns {Promise<Object>}
 */
export const updateCliente = async (id, data) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, {
  //   method: "PUT",
  //   headers: { "Content-Type": "application/json" },
  //   body: JSON.stringify(data),
  // }).then(r => r.json());
  throw new Error("Backend no implementado aún.");
};

/**
 * Elimina un cliente por ID.
 * @param {number|string} id
 * @returns {Promise<void>}
 */
export const deleteCliente = async (id) => {
  // TODO:
  // return fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  throw new Error("Backend no implementado aún.");
};
