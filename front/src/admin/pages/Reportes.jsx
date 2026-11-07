import { useState } from "react";
import AdminLayout from "../components/AdminLayout";

/**
 * src/admin/pages/Reportes.jsx
 *
 * Módulo de reportes — SOLO datos MOCK/placeholder.
 * No conecta backend ni modifica localStorage["usuarios_mock"].
 *
 * FUTURO: reemplazar MOCK_DATA por llamadas a:
 *   GET /api/reportes/propiedades
 *   GET /api/reportes/alquileres
 *   GET /api/reportes/clientes
 *   GET /api/reportes/comisiones
 * pasando los filtros como query params.
 */

/* ── Datos mock ───────────────────────────────────────── */
const MOCK_DATA = {
  propiedades: {
    total: 24,
    disponible: 14,
    alquilada: 5,
    vendida: 3,
    reservada: 2,
  },
  alquileres: {
    total: 8,
    activo: 5,
    finalizado: 2,
    pendiente: 1,
  },
  clientes: {
    total: 37,
    prospectos: 18,
    nuevos: 6,
    contactados: 9,
    interesados: 3,
  },
  comisiones: {
    total: 11,
    pendientes: 4,
    pagadas: 7,
    monto_total: "S/ 18,450.00",
  },
};

/* ── Tarjetas por sección ─────────────────────────────── */
function StatMini({ label, value, color }) {
  return (
    <div className="reporte-stat-mini">
      <span
        className="reporte-stat-dot"
        style={{ background: color }}
      />
      <div>
        <p className="reporte-stat-mini-label">{label}</p>
        <p className="reporte-stat-mini-value">{value}</p>
      </div>
    </div>
  );
}

function ReporteSection({ icon, title, children, onExport }) {
  return (
    <div className="admin-card reporte-section">
      <div className="admin-card-header">
        <p className="admin-card-title">
          {icon} {title}
        </p>
        <button
          className="btn-admin btn-admin-outline btn-admin-sm"
          onClick={onExport}
          title="Exportar este reporte"
        >
          ⬇ Exportar
        </button>
      </div>
      <div className="admin-card-body reporte-section-body">{children}</div>
    </div>
  );
}

/* ── Barra de progreso simple ─────────────────────────── */
function ProgressBar({ label, value, total, color }) {
  const pct = total > 0 ? Math.round((value / total) * 100) : 0;
  return (
    <div className="reporte-bar-row">
      <div className="reporte-bar-labels">
        <span>{label}</span>
        <span>
          <strong>{value}</strong>{" "}
          <span style={{ color: "#aaa", fontSize: "11px" }}>({pct}%)</span>
        </span>
      </div>
      <div className="reporte-bar-track">
        <div
          className="reporte-bar-fill"
          style={{ width: `${pct}%`, background: color }}
        />
      </div>
    </div>
  );
}

/* ── Componente principal ─────────────────────────────── */
export default function Reportes() {
  const [filtros, setFiltros] = useState({
    fecha_desde: "",
    fecha_hasta: "",
    estado: "",
    tipo_operacion: "",
  });

  const handleFiltroChange = (e) => {
    setFiltros((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleLimpiar = () => {
    setFiltros({ fecha_desde: "", fecha_hasta: "", estado: "", tipo_operacion: "" });
  };

  const handleExportarGeneral = () => {
    // TODO: Conectar con endpoint GET /api/reportes/exportar?formato=pdf o excel
    alert("La exportación de reportes estará disponible cuando se conecte el backend.");
  };

  const handleExportarSeccion = (seccion) => {
    // TODO: Conectar con GET /api/reportes/:seccion/exportar
    alert(`Exportar reporte de "${seccion}" estará disponible al conectar el backend.`);
  };

  const d = MOCK_DATA;

  return (
    <AdminLayout title="Reportes">
      {/* ── Cabecera de página ── */}
      <div className="admin-page-header">
        <div>
          <p className="admin-page-title">Reportes</p>
          <p className="admin-page-subtitle">
            Resumen general del sistema — datos de ejemplo
          </p>
        </div>
        <button
          className="btn-admin btn-admin-primary"
          onClick={handleExportarGeneral}
        >
          ⬇ Exportar reporte completo
        </button>
      </div>

      {/* ── Aviso de datos mock ── */}
      <div className="reporte-mock-notice">
        📊 Los datos mostrados son de ejemplo. Se actualizarán automáticamente
        al conectar el backend.
      </div>

      {/* ── Filtros ── */}
      <div className="admin-card reporte-filtros-card">
        <div className="admin-card-header">
          <p className="admin-card-title">🔍 Filtros</p>
          <button
            className="btn-admin btn-admin-sm"
            style={{ background: "#f4f5f7", color: "#555" }}
            onClick={handleLimpiar}
          >
            Limpiar
          </button>
        </div>
        <div className="admin-card-body">
          <div className="reporte-filtros-grid">
            <div className="admin-form-group">
              <label className="admin-form-label">Fecha desde</label>
              <input
                type="date"
                name="fecha_desde"
                className="admin-form-control"
                value={filtros.fecha_desde}
                onChange={handleFiltroChange}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Fecha hasta</label>
              <input
                type="date"
                name="fecha_hasta"
                className="admin-form-control"
                value={filtros.fecha_hasta}
                onChange={handleFiltroChange}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Estado</label>
              <select
                name="estado"
                className="admin-form-control"
                value={filtros.estado}
                onChange={handleFiltroChange}
              >
                <option value="">Todos</option>
                <option value="Activo">Activo</option>
                <option value="Finalizado">Finalizado</option>
                <option value="Pendiente">Pendiente</option>
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Tipo de operación</label>
              <select
                name="tipo_operacion"
                className="admin-form-control"
                value={filtros.tipo_operacion}
                onChange={handleFiltroChange}
              >
                <option value="">Todos</option>
                <option value="Venta">Venta</option>
                <option value="Alquiler">Alquiler</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* ── Tarjetas de resumen global ── */}
      <div className="admin-stat-grid">
        <div className="admin-stat-card">
          <div className="admin-stat-info">
            <p className="admin-stat-label">Propiedades</p>
            <p className="admin-stat-value">{d.propiedades.total}</p>
            <p className="admin-stat-sub">{d.propiedades.disponible} disponibles</p>
          </div>
          <div className="admin-stat-icon stat-icon-green">🏠</div>
        </div>
        <div className="admin-stat-card">
          <div className="admin-stat-info">
            <p className="admin-stat-label">Alquileres</p>
            <p className="admin-stat-value">{d.alquileres.total}</p>
            <p className="admin-stat-sub">{d.alquileres.activo} activos</p>
          </div>
          <div className="admin-stat-icon stat-icon-blue">📋</div>
        </div>
        <div className="admin-stat-card">
          <div className="admin-stat-info">
            <p className="admin-stat-label">Clientes</p>
            <p className="admin-stat-value">{d.clientes.total}</p>
            <p className="admin-stat-sub">{d.clientes.prospectos} prospectos</p>
          </div>
          <div className="admin-stat-icon stat-icon-gold">👥</div>
        </div>
        <div className="admin-stat-card">
          <div className="admin-stat-info">
            <p className="admin-stat-label">Comisiones</p>
            <p className="admin-stat-value">{d.comisiones.total}</p>
            <p className="admin-stat-sub">{d.comisiones.monto_total}</p>
          </div>
          <div className="admin-stat-icon stat-icon-purple">💰</div>
        </div>
      </div>

      {/* ── Grid de secciones ── */}
      <div className="reporte-grid-2">

        {/* Propiedades */}
        <ReporteSection
          icon="🏠"
          title="Propiedades"
          onExport={() => handleExportarSeccion("propiedades")}
        >
          <ProgressBar
            label="Disponibles"
            value={d.propiedades.disponible}
            total={d.propiedades.total}
            color="#22c55e"
          />
          <ProgressBar
            label="Alquiladas"
            value={d.propiedades.alquilada}
            total={d.propiedades.total}
            color="#3b82f6"
          />
          <ProgressBar
            label="Vendidas"
            value={d.propiedades.vendida}
            total={d.propiedades.total}
            color="#c9a84c"
          />
          <ProgressBar
            label="Reservadas"
            value={d.propiedades.reservada}
            total={d.propiedades.total}
            color="#a855f7"
          />
          <div className="reporte-stat-row">
            <StatMini label="Total" value={d.propiedades.total} color="#1a1a1a" />
            <StatMini label="Disponibles" value={d.propiedades.disponible} color="#22c55e" />
            <StatMini label="Alquiladas" value={d.propiedades.alquilada} color="#3b82f6" />
            <StatMini label="Vendidas" value={d.propiedades.vendida} color="#c9a84c" />
          </div>
        </ReporteSection>

        {/* Alquileres */}
        <ReporteSection
          icon="📋"
          title="Alquileres"
          onExport={() => handleExportarSeccion("alquileres")}
        >
          <ProgressBar
            label="Activos"
            value={d.alquileres.activo}
            total={d.alquileres.total}
            color="#22c55e"
          />
          <ProgressBar
            label="Finalizados"
            value={d.alquileres.finalizado}
            total={d.alquileres.total}
            color="#888"
          />
          <ProgressBar
            label="Pendientes"
            value={d.alquileres.pendiente}
            total={d.alquileres.total}
            color="#f59e0b"
          />
          <div className="reporte-stat-row">
            <StatMini label="Total" value={d.alquileres.total} color="#1a1a1a" />
            <StatMini label="Activos" value={d.alquileres.activo} color="#22c55e" />
            <StatMini label="Finalizados" value={d.alquileres.finalizado} color="#888" />
            <StatMini label="Pendientes" value={d.alquileres.pendiente} color="#f59e0b" />
          </div>
        </ReporteSection>

        {/* Clientes */}
        <ReporteSection
          icon="👥"
          title="Clientes / Prospectos"
          onExport={() => handleExportarSeccion("clientes")}
        >
          <ProgressBar
            label="Prospectos"
            value={d.clientes.prospectos}
            total={d.clientes.total}
            color="#3b82f6"
          />
          <ProgressBar
            label="Nuevos"
            value={d.clientes.nuevos}
            total={d.clientes.total}
            color="#22c55e"
          />
          <ProgressBar
            label="Contactados"
            value={d.clientes.contactados}
            total={d.clientes.total}
            color="#c9a84c"
          />
          <ProgressBar
            label="Interesados"
            value={d.clientes.interesados}
            total={d.clientes.total}
            color="#a855f7"
          />
          <div className="reporte-stat-row">
            <StatMini label="Total" value={d.clientes.total} color="#1a1a1a" />
            <StatMini label="Prospectos" value={d.clientes.prospectos} color="#3b82f6" />
            <StatMini label="Nuevos" value={d.clientes.nuevos} color="#22c55e" />
            <StatMini label="Interesados" value={d.clientes.interesados} color="#a855f7" />
          </div>
        </ReporteSection>

        {/* Comisiones */}
        <ReporteSection
          icon="💰"
          title="Comisiones"
          onExport={() => handleExportarSeccion("comisiones")}
        >
          <ProgressBar
            label="Pagadas"
            value={d.comisiones.pagadas}
            total={d.comisiones.total}
            color="#22c55e"
          />
          <ProgressBar
            label="Pendientes"
            value={d.comisiones.pendientes}
            total={d.comisiones.total}
            color="#f59e0b"
          />
          <div className="reporte-comision-total">
            <span>Monto total</span>
            <strong>{d.comisiones.monto_total}</strong>
          </div>
          <div className="reporte-stat-row">
            <StatMini label="Total" value={d.comisiones.total} color="#1a1a1a" />
            <StatMini label="Pagadas" value={d.comisiones.pagadas} color="#22c55e" />
            <StatMini label="Pendientes" value={d.comisiones.pendientes} color="#f59e0b" />
          </div>
        </ReporteSection>
      </div>
    </AdminLayout>
  );
}
