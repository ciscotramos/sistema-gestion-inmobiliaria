import { useState, useEffect } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import AdminPagination from "../components/AdminPagination";
import { getUsuariosMock } from "../../auth/mockUsers";
import { properties } from "../../data/properties";
import { clientesMock } from "../../data/clientes";
import { getOperacionesMock } from "../../data/operaciones";
import { alquileresMock } from "../../data/alquileres";

const ESTADO_OPTIONS = ["Pagada", "Pendiente", "Cancelada"];
const TIPO_OPERACION_OPTIONS = ["Venta", "Alquiler"];

const COLUMNS = [
  "ID", "Operación", "Cliente", "Propiedad", "Agente", "Tipo Op.", "Monto Op.", "% Com.", "Comisión", "Estado", "Acciones",
];

const INITIAL_FORM = {
  operacion_id: "",
  tipo_operacion: "Venta",
  cliente_id: "",
  propiedad_id: "",
  agente_id: "",
  monto_operacion: "",
  porcentaje: "",
  fecha: "",
  estado: "Pendiente",
};

export default function Comisiones() {
  const [comisiones, setComisiones] = useState([]);
  const [ventasConcretadas, setVentasConcretadas] = useState([]);
  
  useEffect(() => {
    try {
      const raw = localStorage.getItem("comisiones_mock");
      if (raw) {
        setComisiones(JSON.parse(raw));
      } else {
        localStorage.setItem("comisiones_mock", JSON.stringify([]));
      }
    } catch (_) {}
  }, []);

  useEffect(() => {
    const ops = getOperacionesMock();
    setVentasConcretadas(ops.filter(o => o.estado === "Concretada"));
  }, []);

  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(1);
  const PER_PAGE = 10;

  const getAgenteName = (id) => {
    const a = getUsuariosMock().find((u) => String(u.id) === String(id));
    return a ? a.nombre : (id ? `Agente #${id}` : "—");
  };

  const getPropiedadTitle = (id) => {
    if (!id) return "—";
    const p = properties.find((p) => String(p.id) === String(id));
    return p ? p.titulo : `Prop. #${id}`;
  };

  const getClienteName = (id) => {
    if (!id) return "—";
    const c = clientesMock.find((c) => String(c.id) === String(id));
    return c ? `${c.nombre} ${c.apellido}` : `Cliente #${id}`;
  };

  const calcComision = (monto, pct) => {
    if (!monto || !pct) return 0;
    return ((Number(monto) * Number(pct)) / 100).toFixed(2);
  };

  const totalComisiones = comisiones
    .reduce((sum, c) => sum + Number(calcComision(c.monto_operacion, c.porcentaje)), 0)
    .toFixed(2);

  const filtered = comisiones.filter((c) =>
    [String(c.agente_id), c.tipo_operacion, c.estado]
      .join(" ")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  
  const totalPages = Math.max(1, Math.ceil(filtered.length / PER_PAGE));
  const paginated = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const openCreate = () => {
    setEditingId(null);
    setForm(INITIAL_FORM);
    setModalOpen(true);
  };

  const openEdit = (c) => {
    setEditingId(c.id);
    setForm({ ...c });
    setModalOpen(true);
  };

  const handleTipoOperacionChange = (e) => {
    setForm({
      ...INITIAL_FORM,
      tipo_operacion: e.target.value
    });
  };

  const handleOperacionChange = (e) => {
    const opId = e.target.value;
    let selectedOp = null;
    
    if (form.tipo_operacion === "Venta") {
      selectedOp = ventasConcretadas.find(o => String(o.id) === String(opId));
    } else {
      selectedOp = alquileresMock.find(a => String(a.id) === String(opId));
    }

    if (selectedOp) {
      setForm({
        ...form,
        operacion_id: opId,
        cliente_id: selectedOp.cliente_id,
        propiedad_id: selectedOp.propiedad_id,
        agente_id: form.tipo_operacion === "Venta" ? selectedOp.agente_id : properties.find(p => String(p.id) === String(selectedOp.propiedad_id))?.agente_id,
        monto_operacion: form.tipo_operacion === "Venta" ? selectedOp.monto : selectedOp.monto
      });
    } else {
      setForm({
        ...form,
        operacion_id: "",
        cliente_id: "",
        propiedad_id: "",
        agente_id: "",
        monto_operacion: ""
      });
    }
  };

  const handleSave = () => {
    if (!form.operacion_id || !form.porcentaje) return;
    let newList;
    if (editingId !== null) {
      newList = comisiones.map((c) => (c.id === editingId ? { ...c, ...form } : c));
    } else {
      newList = [{ ...form, id: Date.now(), fecha: new Date().toISOString().split('T')[0] }, ...comisiones];
    }
    setComisiones(newList);
    localStorage.setItem("comisiones_mock", JSON.stringify(newList));
    setModalOpen(false);
  };

  const askDelete = (id) => { setDeletingId(id); setConfirmOpen(true); };
  const handleDelete = () => {
    const newList = comisiones.filter((c) => c.id !== deletingId);
    setComisiones(newList);
    localStorage.setItem("comisiones_mock", JSON.stringify(newList));
    setConfirmOpen(false);
  };

  const getEstadoClass = (estado) => {
    switch (estado) {
      case "Pagada": return "status-activo";
      case "Pendiente": return "status-pendiente";
      case "Cancelada": return "status-finalizado";
      default: return "";
    }
  };

  return (
    <AdminLayout title="Comisiones">
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">Comisiones</p>
          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <SearchBar placeholder="Buscar comisión..." value={search} onChange={(v) => { setSearch(v); setPage(1); }} />
            <button className="btn-admin btn-admin-primary" onClick={openCreate}>+ Registrar comisión</button>
          </div>
        </div>

        {filtered.length === 0 ? (
          <EmptyState
            icon="💰"
            title="No hay comisiones registradas"
            message="Las comisiones aparecerán aquí cuando se registren operaciones concretadas."
            action={<button className="btn-admin btn-admin-primary" onClick={openCreate}>+ Registrar comisión</button>}
          />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>{COLUMNS.map((c) => <th key={c}>{c}</th>)}</tr>
                </thead>
                <tbody>
                  {paginated.map((c) => (
                    <tr key={c.id}>
                      <td>#{c.id}</td>
                      <td>Op. #{c.operacion_id}</td>
                      <td>{getClienteName(c.cliente_id)}</td>
                      <td><span title={getPropiedadTitle(c.propiedad_id)}>{c.propiedad_id ? `Prop. #${c.propiedad_id}` : "—"}</span></td>
                      <td>{getAgenteName(c.agente_id)}</td>
                      <td>{c.tipo_operacion}</td>
                      <td>{c.monto_operacion ? `US$ ${Number(c.monto_operacion).toLocaleString("en-US")}` : "—"}</td>
                      <td>{c.porcentaje ? `${c.porcentaje}%` : "—"}</td>
                      <td><strong style={{ color: "#c9a84c" }}>US$ {calcComision(c.monto_operacion, c.porcentaje)}</strong></td>
                      <td><span className={`status-badge ${getEstadoClass(c.estado)}`}>{c.estado}</span></td>
                      <td>
                        <div className="td-actions">
                          <button className="btn-admin-icon btn-edit" onClick={() => openEdit(c)} title="Editar">✏️</button>
                          <button className="btn-admin-icon btn-delete" onClick={() => askDelete(c.id)} title="Eliminar">🗑️</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div style={{ padding: "12px 24px", background: "#fafafa", borderTop: "2px solid #f0f0f0", fontSize: "13px", fontWeight: 700, color: "#1a1a1a" }}>
              Total comisiones: <span style={{ color: "#c9a84c" }}>US$ {totalComisiones}</span>
            </div>

            <AdminPagination currentPage={page} totalPages={totalPages} totalItems={filtered.length} perPage={PER_PAGE} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editingId ? "Editar comisión" : "Registrar comisión"} footer={
        <>
          <button className="btn-cancel" onClick={() => setModalOpen(false)}>Cancelar</button>
          <button className="btn-admin btn-admin-primary" onClick={handleSave}>{editingId ? "Guardar cambios" : "Registrar"}</button>
        </>
      }>
        <div className="admin-form">
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Tipo de operación</label>
              <select className="admin-form-control" value={form.tipo_operacion} onChange={handleTipoOperacionChange} disabled={editingId !== null}>
                {TIPO_OPERACION_OPTIONS.map((t) => <option key={t}>{t}</option>)}
              </select>
            </div>
            
            <div className="admin-form-group">
              <label className="admin-form-label">{form.tipo_operacion === "Venta" ? "Operación concretada *" : "Alquiler *"}</label>
              <select className="admin-form-control" value={form.operacion_id} onChange={handleOperacionChange} disabled={editingId !== null}>
                <option value="">Seleccionar {form.tipo_operacion.toLowerCase()} ▼</option>
                {form.tipo_operacion === "Venta" ? (
                  ventasConcretadas.map(o => (
                    <option key={o.id} value={o.id}>Op. #{o.id} - Prop #{o.propiedad_id} - US${o.monto}</option>
                  ))
                ) : (
                  alquileresMock.map(a => (
                    <option key={a.id} value={a.id}>Alq. #{a.id} - Prop #{a.propiedad_id} - {a.moneda} {a.monto}</option>
                  ))
                )}
              </select>
            </div>
          </div>

          <div className="admin-form-row" style={{ opacity: 0.7, pointerEvents: 'none' }}>
            <div className="admin-form-group">
              <label className="admin-form-label">Cliente</label>
              <input type="text" className="admin-form-control" value={getClienteName(form.cliente_id)} readOnly />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Propiedad</label>
              <input type="text" className="admin-form-control" value={getPropiedadTitle(form.propiedad_id)} readOnly />
            </div>
          </div>

          <div className="admin-form-row" style={{ opacity: 0.7, pointerEvents: 'none' }}>
            <div className="admin-form-group">
              <label className="admin-form-label">Agente responsable</label>
              <input type="text" className="admin-form-control" value={getAgenteName(form.agente_id)} readOnly />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Monto de operación (US$)</label>
              <input type="text" className="admin-form-control" value={form.monto_operacion ? Number(form.monto_operacion).toLocaleString("en-US") : ""} readOnly />
            </div>
          </div>

          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Porcentaje comisión (%) *</label>
              <input type="number" className="admin-form-control" placeholder="ej: 3" min="0" max="100" value={form.porcentaje} onChange={(e) => setForm({ ...form, porcentaje: e.target.value })} />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Estado de la comisión</label>
              <select className="admin-form-control" value={form.estado} onChange={(e) => setForm({ ...form, estado: e.target.value })}>
                {ESTADO_OPTIONS.map((s) => <option key={s}>{s}</option>)}
              </select>
            </div>
          </div>

          {form.monto_operacion && form.porcentaje && (
            <div style={{ background: "rgba(201,168,76,0.08)", border: "1px solid rgba(201,168,76,0.2)", borderRadius: "8px", padding: "12px 16px", fontSize: "14px" }}>
              💰 Comisión calculada: <strong style={{ color: "#c9a84c" }}>US$ {calcComision(form.monto_operacion, form.porcentaje)}</strong>
            </div>
          )}
        </div>
      </Modal>

      <ConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} onConfirm={handleDelete} title="¿Eliminar comisión?" message="Esta acción eliminará el registro permanentemente." />
    </AdminLayout>
  );
}
