import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";

/**
 * AdminHeader — muestra la sesión activa y permite cerrar sesión.
 * Solo cambia: avatar inicial, nombre de usuario, dropdown con opciones.
 * El resto del header (toggle, notificaciones) permanece igual.
 */
export default function AdminHeader({ title, onToggleSidebar }) {
  const { usuario, cerrarSesion } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  // Cierra el dropdown al hacer clic fuera
  useEffect(() => {
    function handleOutside(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    }
    if (menuOpen) document.addEventListener("mousedown", handleOutside);
    return () => document.removeEventListener("mousedown", handleOutside);
  }, [menuOpen]);

  const handleLogout = () => {
    cerrarSesion();
    navigate("/login");
  };

  const inicial = usuario?.nombre?.[0]?.toUpperCase() ?? "A";
  const nombreVisible = usuario?.nombre ?? "Administrador";

  return (
    <header className="admin-header">
      <div className="admin-header-left">
        <button
          className="sidebar-toggle"
          onClick={onToggleSidebar}
          aria-label="Alternar sidebar"
          title="Alternar sidebar"
        >
          ☰
        </button>
        <h1 className="admin-header-title">{title}</h1>
      </div>

      <div className="admin-header-right">
        <button className="header-icon-btn" title="Notificaciones" aria-label="Notificaciones">
          🔔
          <span className="notif-dot" />
        </button>

        {/* Usuario con dropdown */}
        <div className="header-user-wrap" ref={menuRef}>
          <div
            className="header-user"
            role="button"
            aria-haspopup="true"
            aria-expanded={menuOpen}
            aria-label="Menú de usuario"
            onClick={() => setMenuOpen((v) => !v)}
          >
            <div className="header-user-avatar">{inicial}</div>
            <span>{nombreVisible}</span>
            <span style={{ fontSize: "11px", color: "#888" }}>▾</span>
          </div>

          {menuOpen && (
            <div className="header-user-dropdown">
              <div className="header-dropdown-info">
                <strong>{nombreVisible}</strong>
                <span>{usuario?.email ?? ""}</span>
              </div>
              <div className="header-dropdown-divider" />
              <button
                className="header-dropdown-item"
                onClick={() => { setMenuOpen(false); navigate("/admin/perfil"); }}
              >
                👤 Mi perfil
              </button>
              <button
                className="header-dropdown-item header-dropdown-logout"
                onClick={handleLogout}
              >
                🚪 Cerrar sesión
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
