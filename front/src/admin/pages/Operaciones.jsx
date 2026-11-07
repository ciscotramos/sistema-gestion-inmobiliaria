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
import { getOperacionesMock, setOperacionesMock } from "../../data/operaciones";

const TIPO_OPTIONS = ["Venta"];
const ESTADO_OPTIONS = ["En negociación", "Reservada", "Concretada", "Cancelada"];
const COLUMNS = ["ID", "Cliente", "Propiedad", "Agente", "Tipo", "Monto", "Fecha", "Estado", "Acciones"];

const INITIAL_FORM = {
  cliente_id: "",
  propiedad_id: "",
  agente_id: "",
  tipo: "Venta",
  monto: "",
  fecha: "",
  estado: "En negociación",
};

export default function Operaciones() {
  const [operaciones, setOperaciones] = useState([]);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(1);
  const PER_PAGE = 10;

  useEffect(() => {
    setOperaciones(getOperacionesMock());
  }, []);

  const agentes = getUsuariosMock().filter((u) => u.rol === "AGENTE" && u.estado === "Activo");

  const getClienteName = (id) => {
    if (!id) return "—";
    const c = clientesMock.find(c => String(c.id) === String(id));
    return c ? `${c.nombre} ${c.apellido}` : `Cliente #${id}`;
  };

  const getPropiedadTitle = (id) => {
    if (!id) return "—";
    const p = properties.find(p => String(p.id) === String(id));
    return p ? p.titulo : `Prop. #${id}`;
  };

  const getAgenteName = (id) => {
    if (!id) return "—";
    const a = getUsuariosMock().find((u) => String(u.id) === String(id));
    return a ? a.nombre : `Agente #${id}`;
  };

  const filtered = operaciones.filter((o) =>
    [String(o.cliente_id), String(o.propiedad_id), String(o.agente_id), o.estado]
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

  const openEdit = (o) => {
    setEditingId(o.id);
    setForm({ ...o });
    setModalOpen(true);
  };

  const handlePropiedadChange = (e) => {
    const propId = e.target.value;
    const prop = properties.find(p => String(p.id) === String(propId));
    setForm({
      ...form,
      propiedad_id: propId,
      agente_id: prop ? prop.agente_id : form.agente_id, // Autocompleta o retiene el actual
      monto: prop ? prop.precio : form.monto // Sugerir monto
    });
  };

  const handleSave = () => {
    if (!form.cliente_id || !form.propiedad_id || !form.agente_id) return;
    let newList;
    if (editingId !== null) {
      newList = operaciones.map(o => (o.id === editingId ? { ...o, ...form } : o));
    } else {
      newList = [{ ...form, id: Date.now() }, ...operaciones];
    }
    setOperaciones(newList);
    setOperacionesMock(newList);
    setModalOpen(false);
  };

  const askDelete = (id) => { setDeletingId(id); setConfirmOpen(true); };
  const handleDelete = () => {
    const newList = operaciones.filter(o => o.id !== deletingId);
    setOperaciones(newList);
    setOperacionesMock(newList);
    setConfirmOpen(false);
  };

  const getEstadoClass = (estado) => {
    switch(estado) {
      case "Concretada": return "status-activo";
      case "En negociación": return "status-pendiente";
      case "Reservada": return "status-agente"; // Azul/Púrpura
      case "Cancelada": return "status-finalizado"; // Rojo/Gris
      default: return "";
    }
  };

  return (
    <AdminLayout title="Operaciones">
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">Operaciones (Ventas)</p>
          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <SearchBar placeholder="Buscar operación..." value={search} onChange={(v) => { setSearch(v); setPage(1); }} />
            <button className="btn-admin btn-admin-primary" onClick={openCreate}>+ Nueva operación</button>
          </div>
        </div>

        {filtered.length === 0 ? (
          <EmptyState icon="🤝" title="No hay operaciones registradas" message="Haz clic en '+ Nueva operación' para registrar la primera." action={<button className="btn-admin btn-admin-primary" onClick={openCreate}>+ Nueva operación</button>} />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>{COLUMNS.map((c) => <th key={c}>{c}</th>)}</tr>
                </thead>
                <tbody>
                  {paginated.map((o) => (
                    <tr key={o.id}>
                      <td>#{o.id}</td>
                      <td>{getClienteName(o.cliente_id)}</td>
                      <td><span title={getPropiedadTitle(o.propiedad_id)}>{o.propiedad_id ? `Prop. #${o.propiedad_id}` : "—"}</span></td>
                      <td>{getAgenteName(o.agente_id)}</td>
                      <td>{o.tipo}</td>
                      <td>{o.monto ? `US$ ${Number(o.monto).toLocaleString("en-US")}` : "—"}</td>
                      <td>{o.fecha || "—"}</td>
                      <td><span className={`status-badge ${getEstadoClass(o.estado)}`}>{o.estado}</span></td>
                      <td>
                        <div className="td-actions">
                          <button className="btn-admin-icon btn-edit" onClick={() => openEdit(o)} title="Editar">✏️</button>
                          <button className="btn-admin-icon btn-delete" onClick={() => askDelete(o.id)} title="Eliminar">🗑️</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <AdminPagination currentPage={page} totalPages={totalPages} totalItems={filtered.length} perPage={PER_PAGE} onPageChange={setPage} />
          </>
        )}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editingId ? "Editar operación" : "Nueva operación"} footer={
        <>
          <button className="btn-cancel" onClick={() => setModalOpen(false)}>Cancelar</button>
          <button className="btn-admin btn-admin-primary" onClick={handleSave}>{editingId ? "Guardar cambios" : "Registrar"}</button>
        </>
      }>
        <div className="admin-form">
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Cliente *</label>
              <select className="admin-form-control" value={form.cliente_id} onChange={(e) => setForm({ ...form, cliente_id: e.target.value })}>
                <option value="">Seleccionar cliente ▼</option>
                {clientesMock.map(c => <option key={c.id} value={c.id}>{c.nombre} {c.apellido}</option>)}
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Propiedad *</label>
              <select className="admin-form-control" value={form.propiedad_id} onChange={handlePropiedadChange}>
                <option value="">Seleccionar propiedad ▼</option>
                {properties.map(p => <option key={p.id} value={p.id}>#{p.id} - {p.titulo}</option>)}
              </select>
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Agente responsable *</label>
              <select className="admin-form-control" value={form.agente_id} onChange={(e) => setForm({ ...form, agente_id: e.target.value })}>
                <option value="">Seleccionar agente ▼</option>
                {agentes.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
              </select>
              <small style={{color: "#888"}}>Se autocompleta al seleccionar la propiedad</small>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Tipo de operación</label>
              <select className="admin-form-control" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                {TIPO_OPTIONS.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Monto (US$)</label>
              <input type="number" className="admin-form-control" placeholder="0.00" value={form.monto} onChange={(e) => setForm({ ...form, monto: e.target.value })} />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Fecha</label>
              <input type="date" className="admin-form-control" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} />
            </div>
          </div>
          <div className="admin-form-group">
            <label className="admin-form-label">Estado de la operación</label>
            <select className="admin-form-control" value={form.estado} onChange={(e) => setForm({ ...form, estado: e.target.value })}>
              {ESTADO_OPTIONS.map(s => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>
      </Modal>

      <ConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} onConfirm={handleDelete} title="¿Eliminar operación?" message="Esta acción eliminará la operación permanentemente." />
    </AdminLayout>
  );
}
