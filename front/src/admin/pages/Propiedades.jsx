import { useState, useEffect } from "react";
import AdminLayout from "../components/AdminLayout";
import EmptyState from "../components/EmptyState";
import SearchBar from "../components/SearchBar";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import AdminPagination from "../components/AdminPagination";
import { getUsuarios } from "../services/usuariosService";
import { useAuth } from "../../auth/useAuth";
import { getDistritos, createDistrito } from "../services/distritosService";
import {
  getCaracteristicas,
  createCaracteristica,
  deleteCaracteristica,
} from "../services/caracteristicasService";
import {
  getPropiedades,
  createPropiedad,
  updatePropiedad,
  deletePropiedad,
} from "../services/propiedadesService";

/**
 * Propiedades — Panel Administrativo
 *
 * Módulo principal para gestionar el catálogo de inmuebles,
 * distritos y características (Etapas 1 y 2).
 */

const TIPO_OPTIONS = ["Casa", "Departamento", "Terreno", "Oficina", "Local Comercial"];
const OPERACION_OPTIONS = ["Venta", "Alquiler"];
const ESTADO_OPTIONS = ["Disponible", "Alquilado", "Vendido", "Reservado", "Inactivo"];
const MONEDA_OPTIONS = ["USD", "PEN"];

const COLUMNS = [
  "ID", "Título", "Tipo", "Operación", "Precio", "Ubicación", "Estado", "Agente", "Acciones",
];
const DISTRITO_COLUMNS = ["ID", "Nombre", "Provincia", "Departamento"];
const CARAC_COLUMNS = ["ID", "Nombre", "Categoría", "Acciones"];
const CARAC_CATEGORIAS = ["General", "Interiores", "Exteriores", "Edificio", "Seguridad", "Servicios"];

const INITIAL_FORM = {
  titulo: "",
  tipo: "Casa",
  operacion: "Venta",
  moneda: "USD",
  precio: "",
  ubicacion: "",
  distrito_id: "",
  descripcion: "",
  area_construida: "",
  area_total: "",
  dormitorios: "",
  banos: "",
  estado: "Disponible",
  agente_id: "",
  imagen_principal: "",
  caracteristicas_ids: [],
};

const INITIAL_DISTRITO_FORM = {
  nombre: "",
  provincia: "Lima",
  departamento: "Lima",
};

const INITIAL_CARAC_FORM = {
  nombre: "",
  categoria: "General",
};

export default function Propiedades() {
  const { usuario } = useAuth();
  const isAdmin = usuario?.rol === "ADMIN";

  // Tab state (solo visible para ADMIN)
  const [activeTab, setActiveTab] = useState("propiedades"); // "propiedades" | "distritos" | "caracteristicas"

  const [distritosList, setDistritosList] = useState([]);
  const [caracteristicasList, setCaracteristicasList] = useState([]);
  const [agentesList, setAgentesList] = useState([]);

  const [propiedades, setPropiedades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [savePropiedadError, setSavePropiedadError] = useState("");
  const [savingPropiedad, setSavingPropiedad] = useState(false);

  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [page, setPage] = useState(1);
  const PER_PAGE = 8;

  const [distritoModalOpen, setDistritoModalOpen] = useState(false);
  const [distritoForm, setDistritoForm] = useState(INITIAL_DISTRITO_FORM);
  const [distritoSaveError, setDistritoSaveError] = useState("");
  const [distritoSaving, setDistritoSaving] = useState(false);

  const [caracModalOpen, setCaracModalOpen] = useState(false);
  const [caracForm, setCaracForm] = useState(INITIAL_CARAC_FORM);
  const [caracSaveError, setCaracSaveError] = useState("");
  const [caracSaving, setCaracSaving] = useState(false);
  const [caracDeleting, setCaracDeleting] = useState(null);
  const [caracConfirmOpen, setCaracConfirmOpen] = useState(false);

  // Carga inicial de datos
  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [distData, caracData, propData, userData] = await Promise.all([
        getDistritos().catch(() => []),
        getCaracteristicas().catch(() => []),
        getPropiedades({ page: 0, size: 100, activo: isAdmin ? undefined : true }).catch(() => null),
        isAdmin ? getUsuarios().catch(() => []) : Promise.resolve([]),
      ]);
      setDistritosList(Array.isArray(distData) ? distData : []);
      setCaracteristicasList(Array.isArray(caracData) ? caracData : []);
      const activeAgentes = (Array.isArray(userData) ? userData : []).filter(
        (u) => u.rol === "AGENTE" && (u.estado === "Activo" || !u.estado)
      );
      setAgentesList(activeAgentes);

      if (propData) {
        const list = propData.content || (Array.isArray(propData) ? propData : []);
        setPropiedades(list);
      } else {
        setPropiedades([]);
      }
    } catch (err) {
      console.error("Error al cargar propiedades del backend:", err);
      setPropiedades([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, [isAdmin]);

  const getAgenteName = (p) => {
    if (p.agente?.nombre) return p.agente.nombre;
    const agId = p.agenteId || p.agente_id;
    const a = agentesList.find((u) => String(u.id) === String(agId));
    return a ? a.nombre : (agId ? `Agente #${agId}` : "—");
  };

  // Filtrado de propiedades según rol y búsqueda
  const baseList = isAdmin
    ? propiedades
    : propiedades.filter(p => {
        const propAgId = p.agenteId || p.agente?.id || p.agente_id;
        return String(propAgId) === String(usuario?.id);
      });

  const filteredPropiedades = baseList.filter((p) =>
    [p.titulo, p.ubicacion, p.direccion, p.tipo]
      .join(" ")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  const totalPagesPropiedades = Math.max(1, Math.ceil(filteredPropiedades.length / PER_PAGE));
  const paginatedPropiedades = filteredPropiedades.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const filteredDistritos = distritosList.filter((d) =>
    [d.nombre, d.provincia, d.departamento]
      .join(" ")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  const totalPagesDistritos = Math.max(1, Math.ceil(filteredDistritos.length / PER_PAGE));
  const paginatedDistritos = filteredDistritos.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const filteredCarac = caracteristicasList.filter((c) =>
    [c.nombre, c.categoria]
      .join(" ")
      .toLowerCase()
      .includes(search.toLowerCase())
  );
  const totalPagesCarac = Math.max(1, Math.ceil(filteredCarac.length / PER_PAGE));
  const paginatedCarac = filteredCarac.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    setSearch("");
    setPage(1);
  };

  // Handlers para propiedades
  const openCreatePropiedad = () => {
    setEditingId(null);
    setSavePropiedadError("");
    setForm({
      ...INITIAL_FORM,
      distrito_id: distritosList[0]?.id || "",
      agente_id: isAdmin ? (agentes[0]?.id || "") : (usuario?.id || ""),
    });
    setModalOpen(true);
  };

  const openEditPropiedad = (prop) => {
    setEditingId(prop.id);
    setSavePropiedadError("");

    const caracIds = Array.isArray(prop.caracteristicas)
      ? prop.caracteristicas.map(c => typeof c === 'object' ? c.id : c)
      : (prop.caracteristicas_ids || []);

    const imgUrl = prop.imagenPrincipal ||
      (Array.isArray(prop.imagenes) && prop.imagenes.length > 0
        ? (typeof prop.imagenes[0] === 'string' ? prop.imagenes[0] : prop.imagenes[0]?.url)
        : (prop.imagen_principal || ""));

    let cleanDir = prop.direccion || "";
    if (prop.distrito?.nombre) {
      const dNom = prop.distrito.nombre;
      cleanDir = cleanDir
        .replace(new RegExp(`(,\\s*Perú)+$`, "gi"), "")
        .replace(new RegExp(`(,\\s*Lima)+$`, "gi"), "")
        .replace(new RegExp(`(,\\s*${dNom})+$`, "gi"), "")
        .replace(/,\s*$/, "")
        .trim();
    }
    if (!cleanDir && prop.ubicacion) {
      let u = prop.ubicacion;
      if (prop.distrito?.nombre) {
        const dNom = prop.distrito.nombre;
        u = u
          .replace(new RegExp(`(,\\s*Perú)+$`, "gi"), "")
          .replace(new RegExp(`(,\\s*Lima)+$`, "gi"), "")
          .replace(new RegExp(`(,\\s*${dNom})+$`, "gi"), "")
          .replace(/,\s*$/, "")
          .trim();
      }
      cleanDir = u;
    }

    setForm({
      titulo: prop.titulo || "",
      tipo: prop.tipo || "Casa",
      operacion: prop.operacion || "Venta",
      moneda: prop.moneda || "USD",
      precio: prop.precio || "",
      ubicacion: cleanDir,
      distrito_id: prop.distritoId || prop.distrito?.id || "",
      descripcion: prop.descripcion || "",
      area_construida: prop.area_construida ?? prop.areaConstruida ?? "",
      area_total: prop.area_total ?? prop.areaTotal ?? "",
      dormitorios: prop.dormitorios !== null && prop.dormitorios !== undefined ? prop.dormitorios : "",
      banos: prop.banos !== null && prop.banos !== undefined ? prop.banos : "",
      estado: prop.estado || "Disponible",
      agente_id: prop.agenteId || prop.agente?.id || prop.agente_id || "",
      imagen_principal: imgUrl,
      caracteristicas_ids: caracIds,
    });
    setModalOpen(true);
  };

  const handleSavePropiedad = async () => {
    if (!form.titulo.trim()) {
      setSavePropiedadError("El título de la propiedad es obligatorio.");
      return;
    }
    if (!form.precio || Number(form.precio) < 0) {
      setSavePropiedadError("El precio debe ser un número válido mayor o igual a 0.");
      return;
    }
    setSavingPropiedad(true);
    setSavePropiedadError("");

    try {
      let resolvedDistritoId = form.distrito_id ? parseInt(form.distrito_id) : null;
      if (!resolvedDistritoId && form.ubicacion && distritosList.length > 0) {
        const match = distritosList.find(d => form.ubicacion.toLowerCase().includes(d.nombre.toLowerCase()));
        if (match) resolvedDistritoId = match.id;
      }
      if (!resolvedDistritoId && distritosList.length > 0) {
        resolvedDistritoId = distritosList[0].id;
      }

      let dirToSend = form.ubicacion?.trim() || null;
      if (dirToSend && resolvedDistritoId) {
        const distObj = distritosList.find(d => d.id === resolvedDistritoId);
        if (distObj?.nombre) {
          dirToSend = dirToSend
            .replace(new RegExp(`(,\\s*Perú)+$`, "gi"), "")
            .replace(new RegExp(`(,\\s*Lima)+$`, "gi"), "")
            .replace(new RegExp(`(,\\s*${distObj.nombre})+$`, "gi"), "")
            .replace(/,\s*$/, "")
            .trim();
        }
      }

      const payload = {
        titulo: form.titulo.trim(),
        descripcion: form.descripcion?.trim() || null,
        operacion: form.operacion || "Venta",
        tipo: form.tipo || "Casa",
        precio: parseFloat(form.precio),
        moneda: form.moneda || "USD",
        dormitorios: form.dormitorios !== "" && form.dormitorios !== null ? parseInt(form.dormitorios) : null,
        banos: form.banos !== "" && form.banos !== null ? parseInt(form.banos) : null,
        areaConstruida: form.area_construida !== "" && form.area_construida !== null ? parseFloat(form.area_construida) : null,
        areaTotal: form.area_total !== "" && form.area_total !== null ? parseFloat(form.area_total) : null,
        direccion: dirToSend || null,
        distritoId: resolvedDistritoId || 7,
        agenteId: isAdmin && form.agente_id ? parseInt(form.agente_id) : undefined,
        estado: form.estado || "Disponible",
        destacada: Boolean(form.destacada),
        activo: true,
        caracteristicasIds: form.caracteristicas_ids || [],
        imagenes: form.imagen_principal ? [form.imagen_principal] : [],
      };

      if (editingId !== null) {
        const actualizada = await updatePropiedad(editingId, payload);
        setPropiedades((prev) =>
          prev.map((p) => (p.id === editingId ? { ...p, ...actualizada } : p))
        );
      } else {
        const nueva = await createPropiedad(payload);
        setPropiedades((prev) => [nueva, ...prev]);
      }
      setModalOpen(false);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || "Error al guardar propiedad";
      setSavePropiedadError(msg);
    } finally {
      setSavingPropiedad(false);
    }
  };

  const askDeletePropiedad = (id) => {
    setDeletingId(id);
    setConfirmOpen(true);
  };

  const handleDeletePropiedad = async () => {
    if (!deletingId) return;
    try {
      await deletePropiedad(deletingId);
      setPropiedades((prev) => prev.filter((p) => p.id !== deletingId));
      setConfirmOpen(false);
      setDeletingId(null);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || "Error al desactivar propiedad";
      alert(msg);
      setConfirmOpen(false);
    }
  };

  // Handlers para distritos (ADMIN)
  const openCreateDistrito = () => {
    setDistritoSaveError("");
    setDistritoForm(INITIAL_DISTRITO_FORM);
    setDistritoModalOpen(true);
  };

  const handleSaveDistrito = async () => {
    if (!distritoForm.nombre.trim()) {
      setDistritoSaveError("El nombre del distrito es obligatorio.");
      return;
    }
    setDistritoSaving(true);
    setDistritoSaveError("");
    try {
      const nuevo = await createDistrito({
        nombre: distritoForm.nombre.trim(),
        provincia: distritoForm.provincia?.trim() || "Lima",
        departamento: distritoForm.departamento?.trim() || "Lima",
      });
      setDistritosList((prev) => [...prev, nuevo]);
      setDistritoModalOpen(false);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || "Error al crear distrito";
      setDistritoSaveError(msg);
    } finally {
      setDistritoSaving(false);
    }
  };

  // Handlers para características (ADMIN)
  const openCreateCarac = () => {
    setCaracSaveError("");
    setCaracForm(INITIAL_CARAC_FORM);
    setCaracModalOpen(true);
  };

  const handleSaveCarac = async () => {
    if (!caracForm.nombre.trim()) {
      setCaracSaveError("El nombre de la característica es obligatorio.");
      return;
    }
    setCaracSaving(true);
    setCaracSaveError("");
    try {
      const nuevo = await createCaracteristica({
        nombre: caracForm.nombre.trim(),
        categoria: caracForm.categoria?.trim() || "General",
      });
      setCaracteristicasList((prev) => [...prev, nuevo]);
      setCaracModalOpen(false);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || "Error al crear característica";
      setCaracSaveError(msg);
    } finally {
      setCaracSaving(false);
    }
  };

  const askDeleteCarac = (carac) => {
    setCaracDeleting(carac);
    setCaracConfirmOpen(true);
  };

  const handleConfirmDeleteCarac = async () => {
    if (!caracDeleting) return;
    try {
      await deleteCaracteristica(caracDeleting.id);
      setCaracteristicasList((prev) => prev.filter((c) => c.id !== caracDeleting.id));
      setCaracConfirmOpen(false);
      setCaracDeleting(null);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || "Error al eliminar característica";
      alert(msg);
      setCaracConfirmOpen(false);
    }
  };

  return (
    <AdminLayout title="Propiedades">
      {/* Selector de pestañas para Administradores */}
      {isAdmin && (
        <div style={{ display: "flex", gap: "10px", marginBottom: "16px", flexWrap: "wrap" }}>
          <button
            type="button"
            className={`btn-admin ${activeTab === "propiedades" ? "btn-admin-primary" : "btn-admin-outline"}`}
            onClick={() => handleTabSwitch("propiedades")}
          >
            🏠 Inmuebles ({propiedades.length})
          </button>
          <button
            type="button"
            className={`btn-admin ${activeTab === "distritos" ? "btn-admin-primary" : "btn-admin-outline"}`}
            onClick={() => handleTabSwitch("distritos")}
          >
            📍 Distritos ({distritosList.length})
          </button>
          <button
            type="button"
            className={`btn-admin ${activeTab === "caracteristicas" ? "btn-admin-primary" : "btn-admin-outline"}`}
            onClick={() => handleTabSwitch("caracteristicas")}
          >
            ✨ Características ({caracteristicasList.length})
          </button>
        </div>
      )}

      {/* =======================================================
          TAB 1: INMUEBLES / PROPIEDADES
      ======================================================= */}
      {activeTab === "propiedades" && (
        <div className="admin-card">
          <div className="admin-card-header">
            <p className="admin-card-title">{isAdmin ? "Propiedades" : "Mis propiedades"}</p>
            <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
              <SearchBar
                placeholder="Buscar propiedad..."
                value={search}
                onChange={(v) => { setSearch(v); setPage(1); }}
              />
              <button className="btn-admin btn-admin-primary" onClick={openCreatePropiedad}>
                + Nueva propiedad
              </button>
            </div>
          </div>

          {filteredPropiedades.length === 0 ? (
            <EmptyState
              icon="🏠"
              title="No hay propiedades registradas"
              message="Haz clic en '+ Nueva propiedad' para agregar una propiedad al sistema."
              action={
                <button
                  className="btn-admin btn-admin-primary"
                  onClick={openCreatePropiedad}
                >
                  + Nueva propiedad
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
                    {paginatedPropiedades.map((p) => {
                      const canEdit = isAdmin || String(p.agenteId || p.agente?.id || p.agente_id) === String(usuario?.id);
                      return (
                        <tr key={p.id}>
                          <td>#{p.id}</td>
                          <td>
                            <strong style={{ fontSize: "13px" }}>{p.titulo}</strong>
                          </td>
                          <td>{p.tipo || "—"}</td>
                          <td>{p.operacion || "—"}</td>
                          <td>
                            {p.precio
                              ? `${p.moneda === "USD" ? "US$" : "S/"} ${Number(p.precio).toLocaleString("en-US")}`
                              : "—"}
                          </td>
                          <td>{p.ubicacion || p.direccion || (p.distrito?.nombre ? `${p.distrito.nombre}, Lima` : "—")}</td>
                          <td>
                            <span className={`status-badge status-${p.estado?.toLowerCase()}`}>
                              {p.estado}
                            </span>
                          </td>
                          <td>{getAgenteName(p)}</td>
                          <td>
                            <div className="td-actions">
                              {canEdit && (
                                <button
                                  className="btn-admin-icon btn-edit"
                                  onClick={() => openEditPropiedad(p)}
                                  title="Editar"
                                >✏️</button>
                              )}
                              {isAdmin && (
                                <button
                                  className="btn-admin-icon btn-delete"
                                  onClick={() => askDeletePropiedad(p.id)}
                                  title="Desactivar propiedad"
                                >🗑️</button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              <AdminPagination
                currentPage={page}
                totalPages={totalPagesPropiedades}
                totalItems={filteredPropiedades.length}
                perPage={PER_PAGE}
                onPageChange={setPage}
              />
            </>
          )}
        </div>
      )}

      {/* =======================================================
          TAB 2: DISTRITOS (ADMIN ONLY)
      ======================================================= */}
      {isAdmin && activeTab === "distritos" && (
        <div className="admin-card">
          <div className="admin-card-header">
            <p className="admin-card-title">Catálogo de Distritos</p>
            <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
              <SearchBar
                placeholder="Buscar distrito..."
                value={search}
                onChange={(v) => { setSearch(v); setPage(1); }}
              />
              <button className="btn-admin btn-admin-primary" onClick={openCreateDistrito}>
                + Nuevo distrito
              </button>
            </div>
          </div>

          {filteredDistritos.length === 0 ? (
            <EmptyState
              icon="📍"
              title="No hay distritos registrados"
              message="Haz clic en '+ Nuevo distrito' para registrar un distrito en la base de datos."
              action={
                <button className="btn-admin btn-admin-primary" onClick={openCreateDistrito}>
                  + Nuevo distrito
                </button>
              }
            />
          ) : (
            <>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      {DISTRITO_COLUMNS.map((c) => <th key={c}>{c}</th>)}
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedDistritos.map((d) => (
                      <tr key={d.id}>
                        <td>#{d.id}</td>
                        <td><strong style={{ fontSize: "13px" }}>{d.nombre}</strong></td>
                        <td>{d.provincia || "—"}</td>
                        <td>{d.departamento || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <AdminPagination
                currentPage={page}
                totalPages={totalPagesDistritos}
                totalItems={filteredDistritos.length}
                perPage={PER_PAGE}
                onPageChange={setPage}
              />
            </>
          )}
        </div>
      )}

      {/* =======================================================
          TAB 3: CARACTERÍSTICAS (ADMIN ONLY)
      ======================================================= */}
      {isAdmin && activeTab === "caracteristicas" && (
        <div className="admin-card">
          <div className="admin-card-header">
            <p className="admin-card-title">Catálogo de Características</p>
            <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
              <SearchBar
                placeholder="Buscar característica..."
                value={search}
                onChange={(v) => { setSearch(v); setPage(1); }}
              />
              <button className="btn-admin btn-admin-primary" onClick={openCreateCarac}>
                + Nueva característica
              </button>
            </div>
          </div>

          {filteredCarac.length === 0 ? (
            <EmptyState
              icon="✨"
              title="No hay características registradas"
              message="Haz clic en '+ Nueva característica' para registrar una en la base de datos."
              action={
                <button className="btn-admin btn-admin-primary" onClick={openCreateCarac}>
                  + Nueva característica
                </button>
              }
            />
          ) : (
            <>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      {CARAC_COLUMNS.map((c) => <th key={c}>{c}</th>)}
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedCarac.map((c) => (
                      <tr key={c.id}>
                        <td>#{c.id}</td>
                        <td><strong style={{ fontSize: "13px" }}>{c.nombre}</strong></td>
                        <td>
                          <span className="status-badge status-agente">
                            {c.categoria || "General"}
                          </span>
                        </td>
                        <td>
                          <div className="td-actions">
                            <button
                              className="btn-admin-icon btn-delete"
                              onClick={() => askDeleteCarac(c)}
                              title="Eliminar característica"
                            >
                              🗑️
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <AdminPagination
                currentPage={page}
                totalPages={totalPagesCarac}
                totalItems={filteredCarac.length}
                perPage={PER_PAGE}
                onPageChange={setPage}
              />
            </>
          )}
        </div>
      )}

      {/* =======================================================
          MODAL: CREAR / EDITAR PROPIEDAD
      ======================================================= */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingId ? "Editar propiedad" : "Nueva propiedad"}
        size="lg"
        footer={
          <>
            <button className="btn-cancel" onClick={() => setModalOpen(false)} disabled={savingPropiedad}>
              Cancelar
            </button>
            <button className="btn-admin btn-admin-primary" onClick={handleSavePropiedad} disabled={savingPropiedad}>
              {savingPropiedad ? "Guardando..." : (editingId ? "Guardar cambios" : "Crear propiedad")}
            </button>
          </>
        }
      >
        <div className="admin-form">
          {savePropiedadError && (
            <div style={{
              padding: "10px 14px",
              marginBottom: "14px",
              borderRadius: "6px",
              background: "#fee2e2",
              border: "1px solid #f87171",
              color: "#991b1b",
              fontSize: "13px"
            }}>
              ⚠️ {savePropiedadError}
            </div>
          )}

          {/* Imagen principal (URL) */}
          <div className="admin-form-group">
            <label className="admin-form-label">URL de Imagen Principal</label>
            <input
              className="admin-form-control"
              placeholder="https://images.unsplash.com/photo-..."
              value={form.imagen_principal}
              onChange={(e) => setForm({ ...form, imagen_principal: e.target.value })}
            />
            {form.imagen_principal && (
              <div style={{ marginTop: "10px", textAlign: "center" }}>
                <img
                  src={form.imagen_principal}
                  alt="Preview"
                  style={{ maxHeight: "140px", borderRadius: "8px", border: "1px solid #ddd" }}
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
              </div>
            )}
          </div>

          {/* Título y tipo */}
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Título *</label>
              <input
                className="admin-form-control"
                placeholder="Ej: Casa en La Molina"
                value={form.titulo}
                onChange={(e) => setForm({ ...form, titulo: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Tipo de propiedad</label>
              <select
                className="admin-form-control"
                value={form.tipo}
                onChange={(e) => setForm({ ...form, tipo: e.target.value })}
              >
                {TIPO_OPTIONS.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
          </div>

          {/* Operación, moneda, precio */}
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Operación</label>
              <select
                className="admin-form-control"
                value={form.operacion}
                onChange={(e) => setForm({ ...form, operacion: e.target.value })}
              >
                {OPERACION_OPTIONS.map((o) => <option key={o} value={o}>{o}</option>)}
              </select>
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Moneda</label>
              <select
                className="admin-form-control"
                value={form.moneda}
                onChange={(e) => setForm({ ...form, moneda: e.target.value })}
              >
                {MONEDA_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
              </select>
            </div>
          </div>

          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Precio *</label>
              <input
                type="number"
                className="admin-form-control"
                placeholder="0.00"
                value={form.precio}
                onChange={(e) => setForm({ ...form, precio: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Estado</label>
              <select
                className="admin-form-control"
                value={form.estado}
                onChange={(e) => setForm({ ...form, estado: e.target.value })}
              >
                {ESTADO_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>

          {/* Distrito */}
          <div className="admin-form-group">
            <label className="admin-form-label">Distrito *</label>
            <select
              className="admin-form-control"
              value={form.distrito_id}
              onChange={(e) => setForm({ ...form, distrito_id: e.target.value })}
            >
              <option value="">Seleccionar distrito...</option>
              {distritosList.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.nombre} ({d.provincia})
                </option>
              ))}
            </select>
          </div>

          {/* Dirección */}
          <div className="admin-form-group">
            <label className="admin-form-label">Dirección física</label>
            <input
              className="admin-form-control"
              placeholder="Ej: Av. Las Palmeras 450"
              value={form.ubicacion}
              onChange={(e) => setForm({ ...form, ubicacion: e.target.value })}
            />
          </div>

          {/* Características */}
          {caracteristicasList.length > 0 && (
            <div className="admin-form-group">
              <label className="admin-form-label">Características del inmueble</label>
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(auto-fill, minmax(180px, 1fr))",
                  gap: "8px",
                  maxHeight: "150px",
                  overflowY: "auto",
                  padding: "10px",
                  background: "#fafafa",
                  border: "1px solid #e2e8f0",
                  borderRadius: "6px",
                }}
              >
                {caracteristicasList.map((c) => {
                  const isChecked = (form.caracteristicas_ids || []).includes(c.id);
                  return (
                    <label
                      key={c.id}
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                        fontSize: "13px",
                        cursor: "pointer",
                        margin: 0,
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={isChecked}
                        onChange={(e) => {
                          const current = form.caracteristicas_ids || [];
                          const next = e.target.checked
                            ? [...current, c.id]
                            : current.filter((id) => id !== c.id);
                          setForm({ ...form, caracteristicas_ids: next });
                        }}
                      />
                      <span>
                        {c.nombre} <small style={{ color: "#888" }}>({c.categoria})</small>
                      </span>
                    </label>
                  );
                })}
              </div>
            </div>
          )}

          {/* Áreas y habitaciones */}
          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Área construida (m²)</label>
              <input
                type="number"
                className="admin-form-control"
                placeholder="0"
                value={form.area_construida}
                onChange={(e) => setForm({ ...form, area_construida: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Área total (m²)</label>
              <input
                type="number"
                className="admin-form-control"
                placeholder="0"
                value={form.area_total}
                onChange={(e) => setForm({ ...form, area_total: e.target.value })}
              />
            </div>
          </div>

          <div className="admin-form-row">
            <div className="admin-form-group">
              <label className="admin-form-label">Dormitorios</label>
              <input
                type="number"
                className="admin-form-control"
                placeholder="0"
                min="0"
                value={form.dormitorios}
                onChange={(e) => setForm({ ...form, dormitorios: e.target.value })}
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Baños</label>
              <input
                type="number"
                className="admin-form-control"
                placeholder="0"
                min="0"
                value={form.banos}
                onChange={(e) => setForm({ ...form, banos: e.target.value })}
              />
            </div>
          </div>

          {/* Descripción */}
          <div className="admin-form-group">
            <label className="admin-form-label">Descripción</label>
            <textarea
              className="admin-form-control"
              placeholder="Descripción detallada de la propiedad..."
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
            />
          </div>

          {/* Agente responsable (solo ADMIN puede asignar; AGENTE se auto-asigna) */}
          {isAdmin && (
            <div className="admin-form-group">
              <label className="admin-form-label">Agente responsable</label>
              <select
                className="admin-form-control"
                value={form.agente_id}
                onChange={(e) => setForm({ ...form, agente_id: e.target.value })}
              >
                <option value="">Seleccionar agente ▼</option>
                {agentesList.map((a) => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>
            </div>
          )}
        </div>
      </Modal>

      {/* =======================================================
          MODAL: CREAR DISTRITO (ADMIN ONLY)
      ======================================================= */}
      {isAdmin && (
        <Modal
          open={distritoModalOpen}
          onClose={() => { setDistritoModalOpen(false); setDistritoSaveError(""); }}
          title="Nuevo Distrito"
          size="md"
          footer={
            <>
              <button
                className="btn-cancel"
                onClick={() => { setDistritoModalOpen(false); setDistritoSaveError(""); }}
                disabled={distritoSaving}
              >
                Cancelar
              </button>
              <button
                className="btn-admin btn-admin-primary"
                onClick={handleSaveDistrito}
                disabled={distritoSaving}
              >
                {distritoSaving ? "Guardando..." : "Guardar distrito"}
              </button>
            </>
          }
        >
          <div className="admin-form">
            {distritoSaveError && (
              <div style={{
                padding: "10px 14px",
                marginBottom: "14px",
                borderRadius: "6px",
                background: "#fee2e2",
                border: "1px solid #f87171",
                color: "#991b1b",
                fontSize: "13px"
              }}>
                ⚠️ {distritoSaveError}
              </div>
            )}
            <div className="admin-form-group">
              <label className="admin-form-label">Nombre del distrito *</label>
              <input
                className="admin-form-control"
                placeholder="Ej: San Miguel"
                value={distritoForm.nombre}
                onChange={(e) => setDistritoForm({ ...distritoForm, nombre: e.target.value })}
                autoFocus
              />
            </div>
            <div className="admin-form-row">
              <div className="admin-form-group">
                <label className="admin-form-label">Provincia</label>
                <input
                  className="admin-form-control"
                  placeholder="Lima"
                  value={distritoForm.provincia}
                  onChange={(e) => setDistritoForm({ ...distritoForm, provincia: e.target.value })}
                />
              </div>
              <div className="admin-form-group">
                <label className="admin-form-label">Departamento</label>
                <input
                  className="admin-form-control"
                  placeholder="Lima"
                  value={distritoForm.departamento}
                  onChange={(e) => setDistritoForm({ ...distritoForm, departamento: e.target.value })}
                />
              </div>
            </div>
          </div>
        </Modal>
      )}

      {/* =======================================================
          MODAL: CREAR CARACTERÍSTICA (ADMIN ONLY)
      ======================================================= */}
      {isAdmin && (
        <Modal
          open={caracModalOpen}
          onClose={() => { setCaracModalOpen(false); setCaracSaveError(""); }}
          title="Nueva Característica"
          size="md"
          footer={
            <>
              <button
                className="btn-cancel"
                onClick={() => { setCaracModalOpen(false); setCaracSaveError(""); }}
                disabled={caracSaving}
              >
                Cancelar
              </button>
              <button
                className="btn-admin btn-admin-primary"
                onClick={handleSaveCarac}
                disabled={caracSaving}
              >
                {caracSaving ? "Guardando..." : "Guardar característica"}
              </button>
            </>
          }
        >
          <div className="admin-form">
            {caracSaveError && (
              <div style={{
                padding: "10px 14px",
                marginBottom: "14px",
                borderRadius: "6px",
                background: "#fee2e2",
                border: "1px solid #f87171",
                color: "#991b1b",
                fontSize: "13px"
              }}>
                ⚠️ {caracSaveError}
              </div>
            )}
            <div className="admin-form-group">
              <label className="admin-form-label">Nombre de la característica *</label>
              <input
                className="admin-form-control"
                placeholder="Ej: Piscina, Ascensor, Terraza..."
                value={caracForm.nombre}
                onChange={(e) => setCaracForm({ ...caracForm, nombre: e.target.value })}
                autoFocus
              />
            </div>
            <div className="admin-form-group">
              <label className="admin-form-label">Categoría</label>
              <select
                className="admin-form-control"
                value={caracForm.categoria}
                onChange={(e) => setCaracForm({ ...caracForm, categoria: e.target.value })}
              >
                {CARAC_CATEGORIAS.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
          </div>
        </Modal>
      )}

      {/* Confirm delete propiedad */}
      <ConfirmDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={handleDeletePropiedad}
        title="¿Desactivar propiedad?"
        message="Esta acción desactivará la propiedad lógicamente para conservar el historial en el sistema."
      />

      {/* Confirm delete característica (ADMIN ONLY) */}
      {isAdmin && (
        <ConfirmDialog
          open={caracConfirmOpen}
          onClose={() => { setCaracConfirmOpen(false); setCaracDeleting(null); }}
          onConfirm={handleConfirmDeleteCarac}
          title={`¿Eliminar "${caracDeleting?.nombre}"?`}
          message="Esta acción eliminará permanentemente la característica del catálogo."
        />
      )}
    </AdminLayout>
  );
}
