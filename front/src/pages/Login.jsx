import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import "./Login.css";

/**
 * Pantalla de inicio de sesión para el personal administrativo y agentes.
 */
export default function Login() {
  const { iniciarSesion } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setError("");
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email.trim() || !form.password) {
      setError("Completa todos los campos.");
      return;
    }

    setLoading(true);
    try {
      const resultado = await iniciarSesion(form.email, form.password);
      setLoading(false);

      if (!resultado.ok) {
        setError(resultado.error);
        return;
      }

      if (resultado.rol === "ADMIN") {
        navigate("/admin/dashboard");
      } else if (resultado.rol === "AGENTE") {
        navigate("/agente/propiedades");
      } else {
        navigate("/");
      }
    } catch (err) {
      setLoading(false);
      setError("Error al conectar con el servidor.");
    }
  };

  return (
    <div className="login-page">
      {/* Panel izquierdo — branding */}
      <div className="login-brand">
        <div className="login-brand-inner">
          <div className="login-logo">🏠</div>
          <h1 className="login-brand-name">CENTURY 21</h1>
          <p className="login-brand-tagline">
            Sistema de gestión inmobiliaria
          </p>
          <div className="login-brand-divider" />
          <p className="login-brand-note">
            Acceso exclusivo para administradores y agentes del sistema.
          </p>
        </div>
        <Link to="/" className="login-public-link">
          ← Ver sitio público
        </Link>
      </div>

      {/* Panel derecho — formulario */}
      <div className="login-form-panel">
        <div className="login-form-box">
          <div className="login-form-header">
            <h2>Iniciar sesión</h2>
            <p>Ingresa tus credenciales para acceder al panel.</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form" noValidate>
            <div className="login-field">
              <label htmlFor="login-email">Correo electrónico</label>
              <input
                id="login-email"
                type="email"
                name="email"
                autoComplete="username"
                placeholder="correo@inmobiliaria.com"
                value={form.email}
                onChange={handleChange}
                className={error ? "input-error" : ""}
                disabled={loading}
              />
            </div>

            <div className="login-field">
              <label htmlFor="login-password">Contraseña</label>
              <div className="login-password-wrap">
                <input
                  id="login-password"
                  type={showPassword ? "text" : "password"}
                  name="password"
                  autoComplete="current-password"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={handleChange}
                  className={error ? "input-error" : ""}
                  disabled={loading}
                />
                <button
                  type="button"
                  className="login-eye-btn"
                  onClick={() => setShowPassword((v) => !v)}
                  tabIndex={-1}
                  aria-label={showPassword ? "Ocultar contraseña" : "Ver contraseña"}
                >
                  {showPassword ? "🙈" : "👁"}
                </button>
              </div>
            </div>

            {error && (
              <p className="login-error" role="alert">
                ⚠ {error}
              </p>
            )}

            <button
              type="submit"
              className="login-submit-btn"
              disabled={loading}
            >
              {loading ? (
                <span className="login-spinner" />
              ) : (
                "Ingresar"
              )}
            </button>
          </form>

          {/* Credenciales de demo visibles solo en desarrollo */}
          {import.meta.env.DEV && (
            <div className="login-demo-box">
              <p className="login-demo-title">Credenciales de prueba</p>
              <div className="login-demo-row">
                <span className="login-demo-badge admin">Admin</span>
                <code>admin@inmobiliaria.com</code>
                <code>admin123</code>
              </div>
              <div className="login-demo-row">
                <span className="login-demo-badge agente">Agente</span>
                <code>juan.perez@inmobiliaria.com</code>
                <code>agente123</code>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
