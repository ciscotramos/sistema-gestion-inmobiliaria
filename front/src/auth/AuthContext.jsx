import { createContext, useState, useCallback } from "react";
import { apiRequest } from "../services/apiClient";

/**
 * src/auth/AuthContext.jsx
 * Contexto de autenticación y gestión de sesión con JWT.
 */

const SESSION_KEY = "sesion_actual";
const TOKEN_KEY = "token";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (_) {
      return null;
    }
  });

  const iniciarSesion = useCallback(async (email, password) => {
    try {
      const data = await apiRequest("/api/auth/login", {
        method: "POST",
        body: { email, password },
      });

      if (!data || !data.token) {
        return { ok: false, error: "Respuesta de autenticación inválida" };
      }

      localStorage.setItem(TOKEN_KEY, data.token);

      const sesion = {
        nombre: data.nombre,
        email: data.email,
        rol: data.rol,
      };
      localStorage.setItem(SESSION_KEY, JSON.stringify(sesion));
      setUsuario(sesion);

      return { ok: true, rol: data.rol };
    } catch (err) {
      return {
        ok: false,
        error: err.message || "Credenciales incorrectas o usuario inactivo.",
      };
    }
  }, []);

  const cerrarSesion = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
    setUsuario(null);
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, iniciarSesion, cerrarSesion }}>
      {children}
    </AuthContext.Provider>
  );
}
