import { useState, useEffect } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import AdminPagination from "../components/AdminPagination";
import Modal from "../components/Modal";
import { getContactosMock, setContactosMock } from "../../data/contactos";
import { useAuth } from "../../auth/useAuth";

const ESTADOS_CONTACTO = ["Pendiente", "En atención", "Atendido", "Convertido", "No concretado"];

export default function ContactosAgente() {
  const { usuario } = useAuth();
  const isAdmin = usuario?.rol === "ADMIN";
  const [contactos, setContactos] = useState([]);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const PER_PAGE = 10;

  // Modal states
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedContact, setSelectedContact] = useState(null);
  const [formEstado, setFormEstado] = useState("");
  const [formNota, setFormNota] = useState("");
  
  // Feedback
  const [feedback, setFeedback] = useState("");

  useEffect(() => {
    loadContacts();
  }, []);

  const loadContacts = () => {
    const all = getContactosMock();
    if (isAdmin) {
      setContactos(all);
    } else {
      setContactos(all.filter(c => String(c.agente_id) === String(usuario.id)));
    }
  };

  const filtered = contactos.filter((c) =>
    [c.nombre, c.email, c.propiedad, c.tipo, c.estado]
      .join(" ")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  
  const totalPages = Math.max(1, Math.ceil(filtered.length / PER_PAGE));
  const paginated = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const openManage = (contacto) => {
    setSelectedContact(contacto);
    setFormEstado(contacto.estado || "Pendiente");
    setFormNota("");
    setFeedback("");
    setModalOpen(true);
  };

  const handleSave = () => {
    if (!selectedContact) return;
    const allContacts = getContactosMock();
    const index = allContacts.findIndex(c => String(c.id) === String(selectedContact.id));
    
    if (index !== -1) {
      const updatedContact = { ...allContacts[index], estado: formEstado };
      if (!updatedContact.seguimientos) updatedContact.seguimientos = [];
      
      if (formNota.trim()) {
        updatedContact.seguimientos.push({
          fecha: new Date().toISOString(),
          nota: formNota.trim(),
          agente_id: usuario.id,
          agente_nombre: usuario.nombre
        });
      }
      
      allContacts[index] = updatedContact;
      setContactosMock(allContacts);
      
      setFeedback("Seguimiento actualizado correctamente.");
      setTimeout(() => {
        setModalOpen(false);
        loadContacts();
      }, 1500);
    }
  };

  return (
    <AdminLayout title={isAdmin ? "Gestión Global de Contactos" : "Solicitudes / Contactos"}>
      <div className="admin-card">
        <div className="admin-card-header">
          <p className="admin-card-title">{isAdmin ? "Todos los Contactos" : "Mis Contactos Asignados"}</p>
          <SearchBar placeholder="Buscar contacto..." value={search} onChange={(v) => { setSearch(v); setPage(1); }} />
        </div>

        {filtered.length === 0 ? (
          <EmptyState icon="✉️" title="No hay contactos" message="Aún no hay solicitudes o mensajes de prospectos que coincidan con tu búsqueda." />
        ) : (
          <>
            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Nombre</th><th>Email</th><th>Teléfono</th><th>Propiedad</th><th>Tipo</th><th>Fecha</th><th>Estado</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((c) => (
                    <tr key={c.id}>
                      <td>{c.nombre}</td>
                      <td>{c.email}</td>
                      <td>{c.telefono}</td>
                      <td>{c.propiedad || "—"}</td>
                      <td>{c.tipo}</td>
                      <td>{new Date(c.fecha).toLocaleDateString()}</td>
                      <td><span className={`status-badge status-${c.estado === 'Atendido' || c.estado === 'Convertido' ? 'activo' : c.estado === 'No concretado' || c.estado === 'Cancelada' ? 'finalizado' : 'pendiente'}`}>{c.estado}</span></td>
                      <td>
                        <button className="btn-admin btn-admin-primary" style={{ padding: "4px 8px", fontSize: "12px" }} onClick={() => openManage(c)}>Ver / Gestionar</button>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Gestionar Contacto" footer={
        <>
          <button className="btn-cancel" onClick={() => setModalOpen(false)}>Cancelar</button>
          <button className="btn-admin btn-admin-primary" onClick={handleSave}>Guardar seguimiento</button>
        </>
      }>
        {selectedContact && (
          <div className="admin-form">
            {feedback && <div style={{ background: "#d4edda", color: "#155724", padding: "10px", borderRadius: "4px", marginBottom: "15px", fontWeight: "bold" }}>✅ {feedback}</div>}
            
            <div style={{ background: "#f9f9f9", padding: "12px", borderRadius: "8px", marginBottom: "20px", fontSize: "14px", border: "1px solid #eee" }}>
              <p style={{ margin: "0 0 5px 0" }}><strong>Nombre:</strong> {selectedContact.nombre}</p>
              <p style={{ margin: "0 0 5px 0" }}><strong>Email:</strong> {selectedContact.email}</p>
              <p style={{ margin: "0 0 5px 0" }}><strong>Teléfono:</strong> {selectedContact.telefono}</p>
              <p style={{ margin: "0 0 5px 0" }}><strong>Propiedad:</strong> {selectedContact.propiedad || "—"}</p>
              <p style={{ margin: "0 0 5px 0" }}><strong>Tipo:</strong> {selectedContact.tipo}</p>
              <p style={{ margin: "0 0 5px 0" }}><strong>Fecha original:</strong> {new Date(selectedContact.fecha).toLocaleString()}</p>
              <div style={{ marginTop: "10px", padding: "10px", background: "#fff", border: "1px solid #ddd", borderRadius: "4px" }}>
                <strong>Mensaje original:</strong><br/>
                {selectedContact.mensaje}
              </div>
            </div>

            <div className="admin-form-row">
              <div className="admin-form-group" style={{ width: "100%" }}>
                <label className="admin-form-label">Estado</label>
                <select className="admin-form-control" value={formEstado} onChange={(e) => setFormEstado(e.target.value)}>
                  {ESTADOS_CONTACTO.map(e => <option key={e} value={e}>{e}</option>)}
                </select>
              </div>
            </div>

            <div className="admin-form-group">
              <label className="admin-form-label">Nota de seguimiento (Opcional)</label>
              <textarea 
                className="admin-form-control" 
                rows="3" 
                placeholder="Escribe detalles del avance con el prospecto..."
                value={formNota}
                onChange={(e) => setFormNota(e.target.value)}
              />
            </div>

            {selectedContact.seguimientos && selectedContact.seguimientos.length > 0 && (
              <div style={{ marginTop: "20px" }}>
                <label className="admin-form-label">Historial de seguimiento</label>
                <div style={{ maxHeight: "200px", overflowY: "auto", border: "1px solid #eee", borderRadius: "4px" }}>
                  {selectedContact.seguimientos.slice().reverse().map((seg, idx) => (
                    <div key={idx} style={{ padding: "10px", borderBottom: "1px solid #eee", fontSize: "13px" }}>
                      <div style={{ color: "#888", marginBottom: "4px" }}>
                        {new Date(seg.fecha).toLocaleString()} - <strong>{seg.agente_nombre || "Agente"}</strong>
                      </div>
                      <div style={{ color: "#333" }}>{seg.nota}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </AdminLayout>
  );
}
