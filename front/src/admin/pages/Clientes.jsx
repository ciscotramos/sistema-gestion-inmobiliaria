import { useState } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import AdminPagination from "../components/AdminPagination";
import { properties } from "../../data/properties";
import { getUsuariosMock } from "../../auth/mockUsers";
import { useAuth } from "../../auth/useAuth";

/**
 * Clientes — Panel Administrativo
 *
 * La lista de clientes se carga desde la API (getClientes).
 * Actualmente la tabla está vacía porque no existe backend.
 *
 * Cuando se conecte el backend:
 *   import { getClientes, createCliente, updateCliente, deleteCliente }
 *     from "../services/clientesService";
 *
 *   useEffect(() => {
 *     getClientes().then(setClientes);
 *   }, []);
 */

const TIPO_OPTIONS = ["Arrendatario", "Propietario", "Comprador", "Interesado"];
const ESTADO_OPTIONS = ["Activo", "Inactivo"];
const ORIGEN_OPTIONS = ["Registro manual", "Contacto desde propiedad", "Contacto general", "Solicitud de visita"];

const INITIAL_FORM = {
  nombre: "",
  apellido: "",
  telefono: "",
  email: "",
  tipo: "",
  origen: "Registro manual",
  propiedad_id: "",
  agente_id: "",
  estado: "Activo",
};

const COLUMNS = [
  "ID", "Nombre", "Apellido", "Teléfono", "Email", "Tipo",
  "Fecha Registro", "Estado", "Acciones",
];

const MOCK_CLIENTES = [
  { id: 1, nombre: "Luis", apellido: "Gómez", telefono: "+51 999111222", email: "luis@mail.com", tipo: "Comprador", origen: "Contacto desde propiedad", propiedad_id: 1, agente_id: 3, estado: "Activo", fechaRegistro: "20/08/2026" },
  { id: 2, nombre: "Ana", apellido: "Torres", telefono: "+51 999333444", email: "ana@mail.com", tipo: "Arrendatario", origen: "Contacto desde propiedad", propiedad_id: 2, agente_id: 2, estado: "Activo", fechaRegistro: "20/08/2026" },
];

export default function Clientes() {
  const { usuario } = useAuth();
  const isAdmin = usuario?.rol === "ADMIN";
  const [clientes, setClientes] = useState(MOCK_CLIENTES);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(1);
  const PER_PAGE = 10;

  const agentes = getUsuariosMock().filter((u) => u.rol === "AGENTE" && u.estado === "Activo");

  // Filter
  const baseList = isAdmin ? clientes : clientes.filter(c => String(c.agente_id) === String(usuario?.id));

  const filtered = baseList.filter((c) =>
    [c.nombre, c.apellido, c.email, c.telefono]
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

  const openEdit = (cliente) => {
    setEditingId(cliente.id);
    setForm({
      nombre: cliente.nombre,
      apellido: cliente.apellido,
      telefono: cliente.telefono,
      email: cliente.email,
      tipo: cliente.tipo,
      origen: cliente.origen || "Registro manual",
      propiedad_id: cliente.propiedad_id || "",
      agente_id: cliente.agente_id || "",
      estado: cliente.estado,
    });
    setModalOpen(true);
  };

  const handleSave = () => {
    if (!form.nombre.trim() || !form.email.trim()) return;

    if (editingId !== null) {
      // TODO: Reemplazar con updateCliente(editingId, form)
      setClientes((prev) =>
        prev.map((c) => (c.id === editingId ? { ...c, ...form } : c))
      );
    } else {
      // TODO: Reemplazar con createCliente(form)
      const nuevo = {
        ...form,
        id: Date.now(),
        fechaRegistro: new Date().toLocaleDateString("es-PE"),
      };
      setClientes((prev) => [nuevo, ...prev]);
    }
    setModalOpen(false);
  };

  const askDelete = (id) => {
    setDeletingId(id);
    setConfirmOpen(true);
  };

  const handleDelete = () => {
    // TODO: Reemplazar con deleteCliente(deletingId)
    setClientes((prev) => prev.filter((c) => c.id !== deletingId));
    setConfirmOpen(false);
  };

  return (
    <AdminLayout title="Clientes">
      <div className="admin-card">
        {/* Header */}
        <div className="admin-card-header">
          <div>
            <p className="admin-card-title">{isAdmin ? "Clientes" : "Clientes / Prospectos"}</p>
          </div>
          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <SearchBar
              placeholder="Buscar cliente..."
              value={search}
              onChange={(v) => { setSearch(v); setPage(1); }}
            />
            {isAdmin && (
              <button className="btn-admin btn-admin-primary" onClick={openCreate}>
                + Nuevo cliente
              </button>
            )}
          </div>
        </div>

        {/* Table */}
        {filtered.length === 0 ? (
          <EmptyState
            icon="👥"
            title="No hay clientes registrados"
            message="Haz clic en '+ Nuevo cliente' para agregar el primer cliente."
            action={
              <button className="btn-admin btn-admin-primary" onClick={openCreate} style={{ display: isAdmin ? 'inline-block' : 'none' }}>
                + Nuevo cliente
              </button>
            }
          />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    {COLUMNS.map((c) => <th key={c}>{c}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((c) => (
                    <tr key={c.id}>
                      <td>#{c.id}</td>
                      <td>{c.nombre}</td>
                      <td>{c.apellido}</td>
                      <td>{c.telefono || "—"}</td>
                      <td>{c.email}</td>
                      <td>{c.tipo || "—"}</td>
                      <td>{c.fechaRegistro || "—"}</td>
                      <td>
                        <span className={`status-badge status-${c.estado?.toLowerCase()}`}>
                          {c.estado}
                        </span>
                      </td>
                      <td>
                        {isAdmin && (
                          <div className="td-actions">
                            <button
                              className="btn-admin-icon btn-edit"
                              onClick={() => openEdit(c)}
                              title="Editar"
                            >✏️</button>
                            <button
                              className="btn-admin-icon btn-delete"
                              onClick={() => askDelete(c.id)}
                              title="Eliminar"
                            >🗑️</button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <AdminPagination
              currentPage={page}
              totalPages={totalPages}
              totalItems={filtered.length}
              perPage={PER_PAGE}
              onPageChange={setPage}
            />
          </>
        )}
      </div>

      {/* Create / Edit Modal */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingId ? "Editar cliente" : "Nuevo cliente"}
        footer={
          <>
            <button className="btn-cancel" onClick={() => setModalOpen(false)}>
              Cancelar
            </button>
            <button className="btn-admin btn-admin-primary" onClick={handleSave}>
              {editingId ? "Guardar cambios" : "Crear cliente"}
            </button>
          </>
        }
      >
        <div className="admin-form">
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Nombre *</label>
              <input
                className="admin-form-control"
                placeholder="Nombre"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Apellido</label>
              <input
                className="admin-form-control"
                placeholder="Apellido"
                value={form.apellido}
                onChange={(e) => setForm({ ...form, apellido: e.target.value })}
              />
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Email *</label>
              <input
                type="email"
                className="admin-form-control"
                placeholder="correo@ejemplo.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Teléfono</label>
              <input
                className="admin-form-control"
                placeholder="+51 999 999 999"
                value={form.telefono}
                onChange={(e) => setForm({ ...form, telefono: e.target.value })}
              />
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Tipo</label>
              <select
                className="admin-form-control"
                value={form.tipo}
                onChange={(e) => setForm({ ...form, tipo: e.target.value })}
              >
                <option value="">Seleccionar...</option>
                {TIPO_OPTIONS.map((t) => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Estado</label>
              <select
                className="admin-form-control"
                value={form.estado}
                onChange={(e) => setForm({ ...form, estado: e.target.value })}
              >
                {ESTADO_OPTIONS.map((e) => <option key={e}>{e}</option>)}
              </select>
            </div>
          </div>
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Origen del contacto</label>
              <select
                className="admin-form-control"
                value={form.origen}
                onChange={(e) => setForm({ ...form, origen: e.target.value })}
              >
                {ORIGEN_OPTIONS.map((o) => <option key={o}>{o}</option>)}
              </select>
            </div>
            {form.origen === "Contacto desde propiedad" && (
              <div className="admin-form-group">
                <label className="admin-form-label">Propiedad interesada</label>
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
            )}
          </div>
          {form.origen === "Contacto desde propiedad" && (
            <div className="admin-form-row">
              <div className="admin-form-group">
                <label className="admin-form-label">Agente responsable</label>
                <select
                  className="admin-form-control"
                  value={form.agente_id}
                  onChange={(e) => setForm({ ...form, agente_id: e.target.value })}
                >
                  <option value="">Seleccionar agente ▼</option>
                  {agentes.map(a => (
                    <option key={a.id} value={a.id}>{a.nombre}</option>
                  ))}
                </select>
              </div>
            </div>
          )}
        </div>
      </Modal>

      {/* Confirm delete */}
      <ConfirmDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={handleDelete}
        title="¿Eliminar cliente?"
        message="Esta acción eliminará el cliente permanentemente."
      />
    </AdminLayout>
  );
}
