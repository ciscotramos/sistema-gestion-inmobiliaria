import AdminLayout from "../components/AdminLayout";

const STAT_CARDS = [
  {
    label: "Clientes",
    value: "0",
    sub: "Total",
    icon: "👥",
    iconClass: "stat-icon-blue",
  },
  {
    label: "Propiedades",
    value: "0",
    sub: "Total",
    icon: "🏠",
    iconClass: "stat-icon-green",
  },
  {
    label: "Alquileres",
    value: "0",
    sub: "Activos",
    icon: "📋",
    iconClass: "stat-icon-gold",
  },
  {
    label: "Comisiones (mes)",
    value: "S/ 0.00",
    sub: "Total",
    icon: "💰",
    iconClass: "stat-icon-purple",
  },
];

export default function Dashboard() {
  return (
    <AdminLayout title="Dashboard">
      {/* ---- Stat Cards ---- */}
      <div className="admin-stat-grid">
        {STAT_CARDS.map((card) => (
          <div className="admin-stat-card" key={card.label}>
            <div className="admin-stat-info">
              <p className="admin-stat-label">{card.label}</p>
              <p className="admin-stat-value">{card.value}</p>
              <p className="admin-stat-sub">{card.sub}</p>
            </div>
            <div className={`admin-stat-icon ${card.iconClass}`}>
              {card.icon}
            </div>
          </div>
        ))}
      </div>

      {/* ---- Charts row ---- */}
      <div className="dashboard-grid-2" style={{ marginBottom: "20px" }}>
        {/* Alquileres chart */}
        <div className="admin-card">
          <div className="admin-card-header">
            <p className="admin-card-title">Alquileres por estado</p>
          </div>
          <div className="admin-card-body">
            <div className="chart-placeholder">
              <span className="chart-icon">🍩</span>
              <span>No hay información disponible</span>
              <small style={{ color: "#bbb", fontSize: "11px" }}>
                Se mostrará cuando existan alquileres registrados
              </small>
            </div>
          </div>
        </div>

        {/* Comisiones chart */}
        <div className="admin-card">
          <div className="admin-card-header">
            <p className="admin-card-title">Comisiones por mes</p>
          </div>
          <div className="admin-card-body">
            <div className="chart-placeholder">
              <span className="chart-icon">📈</span>
              <span>No hay información disponible</span>
              <small style={{ color: "#bbb", fontSize: "11px" }}>
                Se mostrará cuando existan comisiones registradas
              </small>
            </div>
          </div>
        </div>
      </div>

      {/* ---- Recent activity ---- */}
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">Actividad reciente</p>
          <span style={{ fontSize: "12px", color: "#aaa" }}>Ver todo</span>
        </div>
        <div className="admin-card-body">
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              padding: "32px 0",
              color: "#aaa",
              gap: "8px",
            }}
          >
            <span style={{ fontSize: "32px", opacity: 0.4 }}>📭</span>
            <p style={{ fontSize: "14px" }}>No existen registros todavía.</p>
            <p style={{ fontSize: "12px", color: "#ccc" }}>
              La actividad reciente aparecerá aquí cuando se registren operaciones.
            </p>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
}
