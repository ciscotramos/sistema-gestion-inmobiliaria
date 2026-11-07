import { useState } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import AdminPagination from "../components/AdminPagination";
import { properties } from "../../data/properties";
import { useAuth } from "../../auth/useAuth";

/**
 * Alquileres — Panel Administrativo
 *
 * Módulo interno para gestionar contratos de alquiler.
 * La información de alquileres NO se expone al frontend público.
 * Solo se expone el ESTADO de la propiedad (Disponible / Alquilada).
 *
 * Cuando exista el backend:
 *   import { getAlquileres, createAlquiler, updateAlquiler, deleteAlquiler }
 *     from "../services/alquileresService";
 */
const ESTADO_OPTIONS = ["Activo", "Finalizado", "Pendiente"];

const CLIENTES_MOCK = [
  { id: 1, nombre: "Juan Pérez (Mock)" },
  { id: 2, nombre: "María García (Mock)" },
  { id: 3, nombre: "Empresa XYZ (Mock)" },
];

const COLUMNS = [
  "ID", "Cliente", "Propiedad", "Fecha Inicio", "Fecha Fin", "Monto", "Estado", "Acciones",
];

const INITIAL_FORM = {
  cliente_id: "",
  propiedad_id: "",
  fecha_inicio: "",
  fecha_fin: "",
  monto: "",
  moneda: "PEN",
  estado: "Activo",
};

const ALQUILERES_MOCK = [
  { id: 1, cliente_id: 1, propiedad_id: 1, fecha_inicio: "2026-08-01", fecha_fin: "2027-08-01", monto: "1500", moneda: "USD", estado: "Activo" },
  { id: 2, cliente_id: 2, propiedad_id: 2, fecha_inicio: "2026-07-15", fecha_fin: "2027-07-15", monto: "2500", moneda: "PEN", estado: "Activo" },
];

export default function Alquileres() {
  const { usuario } = useAuth();
  const isAdmin = usuario?.rol === "ADMIN";
  const [alquileres, setAlquileres] = useState(ALQUILERES_MOCK);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(1);
  const PER_PAGE = 10;

  const getClienteName = (id) => {
    if (!id) return "—";
    const c = CLIENTES_MOCK.find(c => String(c.id) === String(id));
    return c ? c.nombre : `Cliente #${id}`;
  };

  const getPropiedadTitle = (id) => {
    if (!id) return "—";
    const p = properties.find(p => String(p.id) === String(id));
    return p ? p.titulo : `Prop. #${id}`;
  };

  const myPropertiesIds = properties.filter(p => String(p.agente_id) === String(usuario?.id)).map(p => String(p.id));
  const baseList = isAdmin ? alquileres : alquileres.filter(a => myPropertiesIds.includes(String(a.propiedad_id)));

  const filtered = baseList.filter((a) =>
    [String(a.cliente_id), String(a.propiedad_id), a.estado]
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

  const openEdit = (a) => {
    setEditingId(a.id);
    setForm({ ...a });
    setModalOpen(true);
  };

  const handleSave = () => {
    if (!form.cliente_id || !form.propiedad_id) return;
    if (editingId !== null) {
      // TODO: updateAlquiler(editingId, form)
      setAlquileres((prev) =>
        prev.map((a) => (a.id === editingId ? { ...a, ...form } : a))
      );
    } else {
      // TODO: createAlquiler(form)
      setAlquileres((prev) => [{ ...form, id: Date.now() }, ...prev]);
    }
    setModalOpen(false);
  };

  const askDelete = (id) => { setDeletingId(id); setConfirmOpen(true); };
  const handleDelete = () => {
    // TODO: deleteAlquiler(deletingId)
    setAlquileres((prev) => prev.filter((a) => a.id !== deletingId));
    setConfirmOpen(false);
  };

  const estadoClass = {
    Activo: "status-activo",
    Finalizado: "status-finalizado",
    Pendiente: "status-pendiente",
  };

  return (
    <AdminLayout title="Alquileres">
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">{isAdmin ? "Alquileres" : "Mis alquileres"}</p>
          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <SearchBar
              placeholder="Buscar alquiler..."
              value={search}
              onChange={(v) => { setSearch(v); setPage(1); }}
            />
            {isAdmin && (
              <button className="btn-admin btn-admin-primary" onClick={openCreate}>
                + Nuevo alquiler
              </button>
            )}
          </div>
        </div>

        {filtered.length === 0 ? (
          <EmptyState
            icon="📋"
            title="No hay alquileres registrados"
            message="Haz clic en '+ Nuevo alquiler' para registrar el primer contrato."
            action={
              <button className="btn-admin btn-admin-primary" onClick={openCreate} style={{ display: isAdmin ? 'inline-block' : 'none' }}>
                + Nuevo alquiler
              </button>
            }
          />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>{COLUMNS.map((c) => <th key={c}>{c}</th>)}</tr>
                </thead>
                <tbody>
                  {paginated.map((a) => (
                    <tr key={a.id}>
                      <td>#{a.id}</td>
                      <td>{getClienteName(a.cliente_id)}</td>
                      <td>
                        <span title={getPropiedadTitle(a.propiedad_id)}>
                          {a.propiedad_id ? `Prop. #${a.propiedad_id}` : "—"}
                        </span>
                      </td>
                      <td>{a.fecha_inicio || "—"}</td>
                      <td>{a.fecha_fin || "—"}</td>
                      <td>
                        {a.monto
                          ? `${a.moneda === "USD" ? "US$" : "S/"} ${Number(a.monto).toLocaleString("en-US")}`
                          : "—"}
                      </td>
                      <td>
                        <span className={`status-badge ${estadoClass[a.estado] || ""}`}>
                          {a.estado}
                        </span>
                      </td>
                      <td>
                        {isAdmin && (
                          <div className="td-actions">
                            <button className="btn-admin-icon btn-edit" onClick={() => openEdit(a)} title="Editar">✏️</button>
                            <button className="btn-admin-icon btn-delete" onClick={() => askDelete(a.id)} title="Eliminar">🗑️</button>
                          </div>
                        )}
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

      {/* Modal */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingId ? "Editar alquiler" : "Nuevo alquiler"}
        footer={
          <>
            <button className="btn-cancel" onClick={() => setModalOpen(false)}>Cancelar</button>
            <button className="btn-admin btn-admin-primary" onClick={handleSave}>
              {editingId ? "Guardar cambios" : "Registrar alquiler"}
            </button>
          </>
        }
      >
        <div className="admin-form">
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Cliente *</label>
              <select
                className="admin-form-control"
                value={form.cliente_id}
                onChange={(e) => setForm({ ...form, cliente_id: e.target.value })}
              >
                <option value="">Seleccionar cliente ▼</option>
                {CLIENTES_MOCK.map(c => (
                  <option key={c.id} value={c.id}>{c.nombre}</option>
                ))}
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Propiedad *</label>
              <select
                className="admin-form-control"
                value={form.propiedad_id}
                onChange={(e) => setForm({ ...form, propiedad_id: e.target.value })}
              >
                <option value="">Seleccionar propiedad ▼</option>
                {properties.map(p => (
                  <option key={p.id} value={p.id}>#{p.id} - {p.titulo}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Fecha de inicio</label>
              <input type="date" className="admin-form-control" value={form.fecha_inicio}
                onChange={(e) => setForm({ ...form, fecha_inicio: e.target.value })} />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Fecha de fin</label>
              <input type="date" className="admin-form-control" value={form.fecha_fin}
                onChange={(e) => setForm({ ...form, fecha_fin: e.target.value })} />
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Moneda</label>
              <select className="admin-form-control" value={form.moneda}
                onChange={(e) => setForm({ ...form, moneda: e.target.value })}>
                <option>PEN</option>
                <option>USD</option>
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Monto mensual</label>
              <input type="number" className="admin-form-control" placeholder="0.00"
                value={form.monto}
                onChange={(e) => setForm({ ...form, monto: e.target.value })} />
            </div>
          </div>
          <div className="admin-form-group">
            <label className="admin-form-label">Estado</label>
            <select className="admin-form-control" value={form.estado}
              onChange={(e) => setForm({ ...form, estado: e.target.value })}>
              {ESTADO_OPTIONS.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        open={confirmOpen} onClose={() => setConfirmOpen(false)}
        onConfirm={handleDelete} title="¿Eliminar alquiler?"
        message="Esta acción eliminará el registro del alquiler permanentemente."
      />
    </AdminLayout>
  );
}
