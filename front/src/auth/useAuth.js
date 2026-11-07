import { useContext } from "react";
import { AuthContext } from "./AuthContext";

/**
 * src/auth/useAuth.js
 *
 * Hook de conveniencia para consumir el AuthContext.
 *
 * Uso:
 *   const { usuario, iniciarSesion, cerrarSesion } = useAuth();
 *
 * Propiedades del objeto usuario (cuando hay sesión activa):
 *   usuario.nombre   → "Luis Administrador"
 *   usuario.email    → "admin@inmobiliaria.com"
 *   usuario.rol      → "ADMIN" | "AGENTE"
 *   usuario.telefono → "+51 ..."
 */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth debe usarse dentro de <AuthProvider>");
  }
  return ctx;
}
