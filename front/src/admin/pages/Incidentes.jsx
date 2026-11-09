import { useCallback, useEffect, useMemo, useState } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import Modal from "../components/Modal";
import AdminPagination from "../components/AdminPagination";
import { useAuth } from "../../auth/useAuth";
import { getCatalogoTickets, getTickets, createTicket, atenderTicket } from "../services/ticketsService";
import { getUsuarios } from "../services/usuariosService";

/**
 * Incidentes — Gestión de incidentes y solicitudes de servicio (ITIL).
 *
 * Cualquier usuario autenticado reporta un incidente; el servidor calcula la prioridad
 * (matriz impacto x urgencia) y el límite SLA. El administrador atiende los tickets:
 * asigna responsable, cambia el estado (Abierto -> En Proceso -> Resuelto -> Cerrado)
 * y registra la solución. Un agente solo ve los tickets que él reportó.
 */
const PER_PAGE = 10;

const INITIAL_FORM = { tipo: "", titulo: "", descripcion: "", impacto: "Medio", urgencia: "Media" };

const NEXT_ESTADOS = {
  Abierto: ["En Proceso"],
  "En Proceso": ["Resuelto"],
  Resuelto: ["Cerrado", "En Proceso"],
  Cerrado: [],
};

const estadoClass = {
  Abierto: "status-pendiente",
  "En Proceso": "status-alquilado",
  Resuelto: "status-activo",
  Cerrado: "status-finalizado",
};

const prioridadClass = { P1: "status-p1", P2: "status-p2", P3: "status-p3", P4: "status-p4" };

const formatFecha = (iso) =>
  iso ? new Date(iso).toLocaleString("es-PE", { dateStyle: "short", timeStyle: "short" }) : "—";

const errorBox = (mensaje) => (
  <div style={{
    background: "#fef2f2", border: "1px solid #f87171", color: "#b91c1c",
    padding: "10px 14px", borderRadius: "6px", marginBottom: "16px", fontSize: "14px",
  }}>
    ⚠ {mensaje}
  </div>
);

export default function Incidentes() {
  const { usuario } = useAuth();
  const isAdmin = usuario?.rol === "ADMIN";

  const [tickets, setTickets] = useState([]);
  const [catalogo, setCatalogo] = useState(null);
  const [responsables, setResponsables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);

  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState(INITIAL_FORM);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  const [selected, setSelected] = useState(null);
  const [atencion, setAtencion] = useState({ estado: "", asignadoAId: "", solucion: "" });
  const [atencionError, setAtencionError] = useState("");

  const cargar = useCallback(async () => {
    setLoading(true);
    setLoadError("");
    try {
      const [lista, cat] = await Promise.all([getTickets(), getCatalogoTickets()]);
      setTickets(lista || []);
      setCatalogo(cat);
    } catch (e) {
      setLoadError(e.message || "No se pudieron cargar los tickets");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  useEffect(() => {
    if (!isAdmin) return;
    getUsuarios()
      .then((lista) => setResponsables(lista || []))
      .catch(() => setResponsables([]));
  }, [isAdmin]);

  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    return tickets.filter((t) =>
      [t.codigo, t.titulo, t.tipo, t.estado, t.prioridad, t.reportadoPor, t.asignadoA || ""]
        .join(" ").toLowerCase().includes(q)
    );
  }, [tickets, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PER_PAGE));
  const paginated = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const prioridadPrevista = useMemo(() => {
    const score = { Alto: 3, Medio: 2, Bajo: 1, Alta: 3, Media: 2, Baja: 1 };
    const total = score[form.impacto] + score[form.urgencia];
    const nivel = total >= 6 ? "P1" : total === 5 ? "P2" : total === 4 ? "P3" : "P4";
    return catalogo?.prioridades?.find((p) => p.nivel === nivel) || { nivel, nombre: "", tiempoSlaHoras: "" };
  }, [form.impacto, form.urgencia, catalogo]);

  const openCreate = () => {
    setForm({ ...INITIAL_FORM, tipo: catalogo?.tipos?.[0] || "" });
    setFormError("");
    setCreateOpen(true);
  };

  const handleCreate = async () => {
    setFormError("");
    if (form.titulo.trim().length < 5) return setFormError("El título debe tener al menos 5 caracteres");
    if (form.descripcion.trim().length < 10) return setFormError("Describa el problema con al menos 10 caracteres");
    setSaving(true);
    try {
      await createTicket(form);
      setCreateOpen(false);
      setPage(1);
      await cargar();
    } catch (e) {
      setFormError(e.message || "No se pudo registrar el incidente");
    } finally {
      setSaving(false);
    }
  };

  const openAtender = (t) => {
    setSelected(t);
    setAtencion({ estado: "", asignadoAId: t.asignadoAId || "", solucion: t.solucion || "" });
    setAtencionError("");
  };

  const handleAtender = async () => {
    setAtencionError("");
    setSaving(true);
    try {
      await atenderTicket(selected.id, atencion);
      setSelected(null);
      await cargar();
    } catch (e) {
      setAtencionError(e.message || "No se pudo actualizar el ticket");
    } finally {
      setSaving(false);
    }
  };

  // /api/admin/agentes lista a todos los usuarios (administradores y agentes)
  const opcionesResponsable = responsables;

  return (
    <AdminLayout title="Incidentes">
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">{isAdmin ? "Tickets de incidentes" : "Mis incidentes reportados"}</p>
          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <SearchBar
              placeholder="Buscar ticket..."
              value={search}
              onChange={(v) => { setSearch(v); setPage(1); }}
            />
            <button className="btn-admin btn-admin-primary" onClick={openCreate} disabled={!catalogo}>
              + Reportar incidente
            </button>
          </div>
        </div>

        {loadError && errorBox(loadError)}

        {loading ? (
          <p style={{ padding: "24px", color: "#6b7280" }}>Cargando tickets…</p>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon="🎫"
            title="No hay tickets"
            message="Use '+ Reportar incidente' para registrar el primero."
          />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    {["Código", "Título", "Tipo", "Prioridad", "Estado", "Reportó", "Asignado a", "Límite SLA", "Acciones"]
                      .map((c) => <th key={c}>{c}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((t) => (
                    <tr key={t.id}>
                      <td>{t.codigo}</td>
                      <td title={t.descripcion}>{t.titulo}</td>
                      <td>{t.tipo}</td>
                      <td>
                        <span className={`status-badge ${prioridadClass[t.prioridad] || ""}`}>
                          {t.prioridad} {t.prioridadNombre}
                        </span>
                      </td>
                      <td>
                        <span className={`status-badge ${estadoClass[t.estado] || ""}`}>{t.estado}</span>
                      </td>
                      <td>{t.reportadoPor}</td>
                      <td>{t.asignadoA || "Sin asignar"}</td>
                      <td style={t.slaVencido ? { color: "#b91c1c", fontWeight: 600 } : undefined}>
                        {formatFecha(t.fechaLimiteSla)}{t.slaVencido ? " ⚠ vencido" : ""}
                      </td>
                      <td>
                        <div className="td-actions">
                          <button className="btn-admin-icon btn-edit" onClick={() => openAtender(t)}
                            title={isAdmin ? "Atender" : "Ver detalle"}>
                            {isAdmin ? "🛠️" : "👁️"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <AdminPagination
              currentPage={page} totalPages={totalPages}
              totalItems={filtered.length} perPage={PER_PAGE}
              onPageChange={setPage}
            />
          </>
        )}
      </div>

      {/* Formulario de reporte */}
      <Modal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        title="Reportar incidente"
        size="lg"
        footer={
          <>
            <button className="btn-admin btn-admin-secondary" onClick={() => setCreateOpen(false)}>Cancelar</button>
            <button className="btn-admin btn-admin-primary" onClick={handleCreate} disabled={saving}>
              {saving ? "Enviando…" : "Enviar reporte"}
            </button>
          </>
        }
      >
        <div className="admin-form">
          {formError && errorBox(formError)}
          <div className="admin-form-group">
            <label className="admin-form-label">Tipo de incidencia *</label>
            <select className="admin-form-control" value={form.tipo}
              onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
              {(catalogo?.tipos || []).map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div className="admin-form-group">
            <label className="admin-form-label">Título *</label>
            <input className="admin-form-control" maxLength={150} placeholder="Ej.: Error 500 al registrar un cliente"
              value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} />
          </div>
          <div className="admin-form-group">
            <label className="admin-form-label">Descripción *</label>
            <textarea className="admin-form-control" rows={4} maxLength={2000}
              placeholder="¿Qué estaba haciendo, qué esperaba y qué ocurrió?"
              value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Impacto *</label>
              <select className="admin-form-control" value={form.impacto}
                onChange={(e) => setForm({ ...form, impacto: e.target.value })}>
                {(catalogo?.impactos || []).map((i) => <option key={i} value={i}>{i}</option>)}
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Urgencia *</label>
              <select className="admin-form-control" value={form.urgencia}
                onChange={(e) => setForm({ ...form, urgencia: e.target.value })}>
                {(catalogo?.urgencias || []).map((u) => <option key={u} value={u}>{u}</option>)}
              </select>
            </div>
          </div>
          <p style={{ fontSize: "13px", color: "#4b5563" }}>
            Prioridad prevista:{" "}
            <span className={`status-badge ${prioridadClass[prioridadPrevista.nivel]}`}>
              {prioridadPrevista.nivel} {prioridadPrevista.nombre}
            </span>
            {prioridadPrevista.tiempoSlaHoras ? ` · resolución en ${prioridadPrevista.tiempoSlaHoras} h` : ""}
          </p>
        </div>
      </Modal>

      {/* Detalle / atención */}
      <Modal
        open={!!selected}
        onClose={() => setSelected(null)}
        title={selected ? `${selected.codigo} · ${selected.titulo}` : ""}
        size="lg"
        footer={
          <>
            <button className="btn-admin btn-admin-secondary" onClick={() => setSelected(null)}>Cerrar</button>
            {isAdmin && (
              <button className="btn-admin btn-admin-primary" onClick={handleAtender} disabled={saving}>
                {saving ? "Guardando…" : "Guardar"}
              </button>
            )}
          </>
        }
      >
        {selected && (
          <div className="admin-form">
            {atencionError && errorBox(atencionError)}
            <p style={{ fontSize: "14px", marginBottom: "8px" }}>
              <strong>{selected.tipo}</strong> · Impacto {selected.impacto} · Urgencia {selected.urgencia} ·{" "}
              {selected.prioridad} {selected.prioridadNombre} ({selected.tiempoSlaHoras} h)
            </p>
            <p style={{ fontSize: "14px", whiteSpace: "pre-wrap", marginBottom: "12px" }}>{selected.descripcion}</p>
            <p style={{ fontSize: "13px", color: "#6b7280", marginBottom: "16px" }}>
              Registrado {formatFecha(selected.fechaRegistro)} · Estado: {selected.estado}
              {selected.fechaResolucion ? ` · Resuelto ${formatFecha(selected.fechaResolucion)}` : ""}
            </p>
            {isAdmin ? (
              <>
                <div className="admin-form-row">
                  <div className="admin-form-group">
                    <label className="admin-form-label">Responsable</label>
                    <select className="admin-form-control" value={atencion.asignadoAId}
                      onChange={(e) => setAtencion({ ...atencion, asignadoAId: e.target.value })}>
                      <option value="">Sin asignar</option>
                      {opcionesResponsable.map((r) => <option key={r.id} value={r.id}>{r.nombre}</option>)}
                    </select>
                  </div>
                  <div className="admin-form-group">
                    <label className="admin-form-label">Cambiar estado a</label>
                    <select className="admin-form-control" value={atencion.estado}
                      onChange={(e) => setAtencion({ ...atencion, estado: e.target.value })}>
                      <option value="">Mantener ({selected.estado})</option>
                      {(NEXT_ESTADOS[selected.estado] || []).map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </div>
                </div>
                <div className="admin-form-group">
                  <label className="admin-form-label">Solución aplicada (obligatoria para resolver)</label>
                  <textarea className="admin-form-control" rows={3} maxLength={2000}
                    value={atencion.solucion} onChange={(e) => setAtencion({ ...atencion, solucion: e.target.value })} />
                </div>
              </>
            ) : (
              <>
                <p style={{ fontSize: "14px" }}><strong>Responsable:</strong> {selected.asignadoA || "Sin asignar"}</p>
                {selected.solucion && (
                  <p style={{ fontSize: "14px", marginTop: "8px" }}><strong>Solución:</strong> {selected.solucion}</p>
                )}
              </>
            )}
          </div>
        )}
      </Modal>
    </AdminLayout>
  );
}
