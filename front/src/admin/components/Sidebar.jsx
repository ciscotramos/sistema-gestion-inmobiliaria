import { NavLink, Link } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";

const ADMIN_NAV = [
  { to: "/admin/dashboard",   icon: "📊", label: "Dashboard" },
  { to: "/admin/clientes",    icon: "👥", label: "Clientes" },
  { to: "/admin/propiedades", icon: "🏠", label: "Propiedades" },
  { to: "/admin/alquileres",  icon: "📋", label: "Alquileres" },
  { to: "/admin/comisiones",  icon: "💰", label: "Comisiones" },
  { to: "/admin/usuarios",    icon: "👤", label: "Usuarios / Agentes" },
  { to: "/admin/operaciones", icon: "🤝", label: "Operaciones" },
  { to: "/admin/reportes",    icon: "📈", label: "Reportes" },
];

const AGENT_NAV = [
  { to: "/agente/propiedades", icon: "🏠", label: "Mis propiedades" },
  { to: "/agente/clientes",    icon: "👥", label: "Clientes / Prospectos" },
  { to: "/agente/alquileres",  icon: "📋", label: "Mis alquileres" },
  { to: "/agente/contactos",   icon: "✉️", label: "Solicitudes / Contactos" },
  { to: "/agente/perfil",      icon: "👤", label: "Mi perfil" },
];

export default function Sidebar({ collapsed, onToggle }) {
  const { usuario } = useAuth();

  const inicial = usuario?.nombre?.[0]?.toUpperCase() ?? "A";
  const nombreVisible = usuario?.nombre ?? "Administrador";
  const rolLabel = usuario?.rol ?? "Admin";

  const isAdmin = usuario?.rol === "ADMIN";
  const NAV_ITEMS = isAdmin ? ADMIN_NAV : AGENT_NAV;
  const homeLink = isAdmin ? "/admin/dashboard" : "/agente/propiedades";

  return (
    <aside className={`admin-sidebar${collapsed ? " collapsed" : ""}`}>
      {/* Brand */}
      <Link to={homeLink} className="sidebar-brand">
        <div className="sidebar-brand-icon">🏠</div>
        <div className="sidebar-brand-text">
          <strong>CENTURY</strong>
          <span> 21</span>
        </div>
        <span className="sidebar-brand-badge">{isAdmin ? "ADMIN" : "AGENTE"}</span>
      </Link>

      {/* Navigation */}
      <nav className="sidebar-nav">
        <p className="sidebar-section-label">Menú principal</p>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              "sidebar-nav-item" + (isActive ? " active" : "")
            }
            title={collapsed ? item.label : undefined}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
          </NavLink>
        ))}

        <p className="sidebar-section-label" style={{ marginTop: "16px" }}>
          Accesos
        </p>
        <Link to="/" className="sidebar-nav-item" title="Ver sitio público">
          <span className="nav-icon">🌐</span>
          <span className="nav-label">Ver sitio público</span>
        </Link>
      </nav>

      {/* Footer: usuario en sesión */}
      <div className="sidebar-footer">
        <div className="sidebar-footer-user">
          <div className="sidebar-footer-avatar">{inicial}</div>
          <div className="sidebar-footer-info">
            <strong>{nombreVisible}</strong>
            <span>{rolLabel}</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
