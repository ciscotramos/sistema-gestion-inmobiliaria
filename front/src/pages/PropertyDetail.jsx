import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { getPropiedadById } from "../services/propiedadesService";
import { getContactosMock, setContactosMock } from "../data/contactos";
import "./PropertyDetail.css";

const formatPrice = (price, moneda = "USD", operacion = "Venta") => {
  const f = Number(price || 0).toLocaleString("en-US");
  const suffix = operacion === "Alquiler" ? " / mes" : "";
  return `${moneda === "USD" ? "US$" : "S/"} ${f}${suffix}`;
};

export default function PropertyDetail() {
  const { id } = useParams();
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeImg, setActiveImg] = useState(0);
  const [modalType, setModalType] = useState(null);
  const [form, setForm] = useState({ nombre: "", email: "", telefono: "", mensaje: "", fecha: "" });
  const [errors, setErrors] = useState({});
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "smooth" });
    setActiveImg(0);
    setLoading(true);

    getPropiedadById(id)
      .then((data) => {
        setProperty(data || null);
      })
      .catch((err) => {
        console.error("Error al obtener detalle de propiedad:", err);
        setProperty(null);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [id]);

  const handleOpenModal = (type) => {
    setModalType(type);
    setForm({ nombre: "", email: "", telefono: "", mensaje: "", fecha: "" });
    setErrors({});
    setSubmitted(false);
  };

  const validateModal = () => {
    const e = {};
    if (!form.nombre.trim()) e.nombre = "Requerido";
    if (!form.email.trim()) e.email = "Requerido";
    if (!form.telefono.trim()) e.telefono = "Requerido";
    if (modalType === 'contact' && !form.mensaje.trim()) e.mensaje = "Requerido";
    if (modalType === 'visit' && !form.fecha.trim()) e.fecha = "Requerido";
    return e;
  };

  const handleModalSubmit = (e, agentName, propertyTitle) => {
    e.preventDefault();
    const errs = validateModal();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }
    // Registro de contacto / visita
    const currentContacts = getContactosMock();
    const registro = {
      id: Date.now(),
      nombre: form.nombre,
      email: form.email,
      telefono: form.telefono,
      mensaje: form.mensaje,
      fechaPreferida: modalType === 'visit' ? form.fecha : undefined,
      propiedad: propertyTitle,
      propiedad_id: property.id,
      agente: agentName,
      agente_id: property.agente_id || property.agente?.id,
      origen: modalType === 'contact' ? "Contacto desde propiedad" : "Solicitud de visita",
      tipo: modalType === 'contact' ? "Contacto desde propiedad" : "Solicitud de visita",
      fecha: new Date().toISOString(),
      estado: "Pendiente",
      seguimientos: []
    };
    
    const newList = [registro, ...currentContacts];
    setContactosMock(newList);
    setSubmitted(true);
  };

  if (loading && !property) {
    return (
      <div className="detail-page" style={{ padding: "80px 0", textAlign: "center" }}>
        <div className="container">
          <p>Cargando detalles de la propiedad...</p>
        </div>
      </div>
    );
  }

  if (!property) {
    return (
      <div className="detail-not-found">
        <div className="not-found">
          <h2>404</h2>
          <h3>Propiedad no encontrada</h3>
          <p>La propiedad que buscas no existe o fue desactivada.</p>
          <Link to="/propiedades" className="btn btn-primary">
            Ver propiedades
          </Link>
        </div>
      </div>
    );
  }

  const agent = property.agente || null;
  const {
    titulo, operacion, tipo,
    precio, moneda, dormitorios, banos,
    area_construida, area_total,
    descripcion, caracteristicas, imagenes,
    imagenesUrls, imagenPrincipal,
  } = property;

  const cleanUbicacion = (() => {
    let raw = property.ubicacion || property.direccion || "";
    const distName = property.distrito?.nombre || "";
    const provName = property.distrito?.provincia || "Lima";
    if (distName) {
      const suffix = `${distName}, ${provName}, Perú`;
      while (raw.endsWith(suffix) && raw.indexOf(suffix) !== raw.lastIndexOf(suffix)) {
        raw = raw.substring(0, raw.lastIndexOf(suffix)).replace(/[,\s]+$/, "");
      }
    }
    return raw;
  })();

  const areaC = area_construida ?? property.areaConstruida;
  const areaT = area_total ?? property.areaTotal;

  const galleryImages = imagenesUrls?.length > 0
    ? imagenesUrls
    : (Array.isArray(imagenes) && imagenes.length > 0
      ? imagenes.map(img => typeof img === 'string' ? img : img.url)
      : [imagenPrincipal || "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&q=80"]);

  /* ── Ficha técnica: pares etiqueta/valor ── */
  const ficha = [
    { label: "Tipo", value: tipo },
    { label: "Operación", value: operacion },
    { label: "Precio", value: formatPrice(precio, moneda, operacion) },
    areaT && { label: "Área total", value: `${areaT} m²` },
    areaC && { label: "Área construida", value: `${areaC} m²` },
    dormitorios !== null && dormitorios !== undefined && { label: "Dormitorios", value: dormitorios },
    banos !== null && banos !== undefined && { label: "Baños", value: banos },
    { label: "Ubicación", value: cleanUbicacion },
  ].filter(Boolean);

  /* ── Estadísticas rápidas (header) ── */
  const quickStats = [
    areaT && { lbl: "Terreno", val: `${areaT} m²`, icon: "⬛" },
    areaC && { lbl: "Construcción", val: `${areaC} m²`, icon: "🏗" },
    dormitorios !== null && dormitorios !== undefined && { lbl: "Dormitorios", val: dormitorios, icon: "🛏" },
    banos !== null && banos !== undefined && { lbl: "Baños", val: banos, icon: "🚿" },
  ].filter(Boolean);

  const handleFichaTecnica = () => {
    alert("La descarga de la ficha técnica estará disponible próximamente.");
  };

  return (
    <div className="detail-page">

      {/* ── Breadcrumb ── */}
      <div className="detail-page-header">
        <div className="container">
          <nav className="breadcrumb">
            <Link to="/">Inicio</Link>
            <span>›</span>
            <Link to="/propiedades">Propiedades</Link>
            <span>›</span>
            <span>{titulo}</span>
          </nav>
        </div>
      </div>

      {/* ── Galería ── */}
      <div className="detail-gallery">
        <div className="gallery-main">
          <img
            src={galleryImages[activeImg] || galleryImages[0]}
            alt={`${titulo} - imagen ${activeImg + 1}`}
            onError={(e) => {
              e.target.src =
                "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&q=60";
            }}
          />
        </div>
        {galleryImages.length > 1 && (
          <div className="gallery-thumbs">
            {galleryImages.map((img, i) => (
              <div
                key={i}
                className={`gallery-thumb ${i === activeImg ? "active" : ""}`}
                onClick={() => setActiveImg(i)}
              >
                <img
                  src={img}
                  alt={`miniatura ${i + 1}`}
                  onError={(e) => {
                    e.target.src =
                      "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=200&q=60";
                  }}
                />
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="container">
        <div className="detail-layout">

          {/* ════════════ COLUMNA IZQUIERDA ════════════ */}
          <div className="detail-main">

            {/* ── 1. Encabezado principal ── */}
            <div className="detail-header-block">
              <div className="detail-badges">
                <span className={`badge badge-${(operacion || 'venta').toLowerCase()}`}>
                  {operacion}
                </span>
                <span className="badge badge-tipo">{tipo}</span>
              </div>

              <h1 className="detail-title">{titulo}</h1>
              <p className="detail-price">{formatPrice(precio, moneda, operacion)}</p>

              <p className="detail-location">
                <span className="pin">📍</span>
                {cleanUbicacion}
              </p>

              {/* ── 2. Stats rápidas ── */}
              {quickStats.length > 0 && (
                <div className="detail-stats">
                  {quickStats.map((s, i) => (
                    <div key={i} className="detail-stat">
                      <span className="stat-lbl">{s.lbl}</span>
                      <span className="stat-val">{s.val}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* ── 3. Descripción ── */}
            <div className="detail-card">
              <h2 className="detail-section-title">Descripción</h2>
              <p className="detail-description">{descripcion}</p>
            </div>

            {/* ── 4. Características generales (ficha técnica en tabla) ── */}
            <div className="detail-card">
              <h2 className="detail-section-title">Características generales</h2>
              <div className="detail-ficha-grid">
                {ficha.map((item, i) => (
                  <div key={i} className="detail-ficha-item">
                    <span className="detail-ficha-label">{item.label}</span>
                    <span className="detail-ficha-value">{item.value}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* ── 5. Características adicionales ── */}
            {caracteristicas && caracteristicas.length > 0 && (
              <div className="detail-card">
                <h2 className="detail-section-title">Características adicionales</h2>
                <div className="detail-characteristics">
                  {caracteristicas.map((c, i) => (
                    <div key={i} className="detail-char-item">
                      {typeof c === "object" ? c.nombre : c}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* ── 6. Ubicación / Mapa ── */}
            <div className="detail-card">
              <h2 className="detail-section-title">Ubicación</h2>
              <p className="detail-location" style={{ marginBottom: "16px", fontSize: "13px" }}>
                <span className="pin">📍</span>
                {ubicacion}
              </p>
              <div className="detail-map">
              </div>
            </div>

            {/* ── 6b. Botón Ficha Técnica ── */}
            <div className="detail-ficha-btn-wrap">
              <button
                className="btn btn-dark btn-lg detail-ficha-btn"
                onClick={handleFichaTecnica}
                type="button"
              >
                📄 Descargar Ficha Técnica
              </button>
            </div>

          </div>{/* /detail-main */}

          {/* ════════════ COLUMNA DERECHA: Agente ════════════ */}
          <div className="detail-sidebar">
            {agent && (
              <div className="agent-card">
                <p className="agent-card-title">Agente encargado</p>

                <div className="agent-info">
                  <div className="agent-avatar">
                  </div>
                  <h3 className="agent-name">{agent.nombre}</h3>
                  <p className="agent-role">{agent.cargo || "Agente Inmobiliario"}</p>
                </div>

                <div className="agent-contacts">
                  <div className="agent-contact-item">
                    <span className="icon">📞</span>
                    <span>{agent.telefono}</span>
                  </div>
                  <div className="agent-contact-item">
                    <span className="icon">✉️</span>
                    <span style={{ wordBreak: "break-all", fontSize: "12px" }}>
                      {agent.email}
                    </span>
                  </div>
                </div>

                <div className="agent-actions">
                  <button onClick={() => handleOpenModal('contact')} className="btn btn-primary" style={{ width: "100%", marginBottom: "10px", justifyContent: "center" }}>
                    Contactar
                  </button>
                  <button onClick={() => handleOpenModal('visit')} className="btn btn-outline" style={{ width: "100%", marginBottom: "10px", justifyContent: "center" }}>
                    🗓️ Solicitar visita
                  </button>
                  <a
                    href={`https://wa.me/${agent.telefono?.replace(/\D/g, "")}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn-outline"
                    style={{ width: "100%", justifyContent: "center" }}
                  >
                    💬 WhatsApp
                  </a>
                </div>

                <p className="agent-note">
                  ¿Te interesa esta propiedad?<br />
                  Escríbenos y te contactamos.
                </p>
              </div>
            )}
          </div>

        </div>{/* /detail-layout */}
      </div>

      {/* ── Modals MOCK ── */}
      {modalType && (
        <div className="modal-overlay" style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, background: "rgba(0,0,0,0.6)", zIndex: 9999, display: "flex", alignItems: "center", justifyContent: "center", padding: "20px" }}>
          <div className="modal-content" style={{ background: "#fff", width: "100%", maxWidth: "500px", borderRadius: "12px", padding: "24px", position: "relative" }}>
            <button onClick={() => setModalType(null)} style={{ position: "absolute", top: "16px", right: "16px", background: "none", border: "none", fontSize: "20px", cursor: "pointer", color: "#666" }}>✖</button>
            
            <h2 style={{ marginTop: 0, marginBottom: "16px", fontSize: "20px", color: "var(--color-primary-dark)" }}>
              {modalType === 'contact' ? "Contactar al agente" : "Solicitar visita"}
            </h2>
            
            <div style={{ background: "#f9f9f9", padding: "12px", borderRadius: "8px", marginBottom: "20px", fontSize: "14px", border: "1px solid #eee" }}>
              <strong>Propiedad:</strong> {titulo} <br/>
              <strong>Agente responsable:</strong> {agent?.nombre}
            </div>

            {submitted ? (
              <div style={{ textAlign: "center", padding: "20px 0" }}>
                <span style={{ fontSize: "40px", display: "block", marginBottom: "10px" }}>✅</span>
                <h3 style={{ margin: "0 0 10px 0", color: "var(--color-primary-dark)" }}>¡Mensaje enviado exitosamente!</h3>
                <p style={{ color: "#666", marginBottom: "20px" }}>El agente se pondrá en contacto contigo pronto.</p>
                <button className="btn btn-primary" onClick={() => setModalType(null)} style={{ width: "100%" }}>Cerrar</button>
              </div>
            ) : (
              <form onSubmit={(e) => handleModalSubmit(e, agent?.nombre, titulo)} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                <div>
                  <label style={{ display: "block", marginBottom: "6px", fontSize: "13px", fontWeight: "600", color: "#333" }}>Nombre completo *</label>
                  <input type="text" style={{ width: "100%", padding: "10px 12px", border: errors.nombre ? "1px solid var(--color-error)" : "1px solid #ccc", borderRadius: "6px", fontSize: "14px", boxSizing: "border-box" }} value={form.nombre} onChange={e => setForm({...form, nombre: e.target.value})} placeholder="Tu nombre y apellido" />
                </div>
                <div>
                  <label style={{ display: "block", marginBottom: "6px", fontSize: "13px", fontWeight: "600", color: "#333" }}>Correo electrónico *</label>
                  <input type="email" style={{ width: "100%", padding: "10px 12px", border: errors.email ? "1px solid var(--color-error)" : "1px solid #ccc", borderRadius: "6px", fontSize: "14px", boxSizing: "border-box" }} value={form.email} onChange={e => setForm({...form, email: e.target.value})} placeholder="tu@correo.com" />
                </div>
                <div>
                  <label style={{ display: "block", marginBottom: "6px", fontSize: "13px", fontWeight: "600", color: "#333" }}>Teléfono *</label>
                  <input type="tel" style={{ width: "100%", padding: "10px 12px", border: errors.telefono ? "1px solid var(--color-error)" : "1px solid #ccc", borderRadius: "6px", fontSize: "14px", boxSizing: "border-box" }} value={form.telefono} onChange={e => setForm({...form, telefono: e.target.value})} placeholder="+51 999 999 999" />
                </div>

                {modalType === 'visit' && (
                  <div>
                    <label style={{ display: "block", marginBottom: "6px", fontSize: "13px", fontWeight: "600", color: "#333" }}>Fecha preferida *</label>
                    <input type="date" style={{ width: "100%", padding: "10px 12px", border: errors.fecha ? "1px solid var(--color-error)" : "1px solid #ccc", borderRadius: "6px", fontSize: "14px", boxSizing: "border-box" }} value={form.fecha} onChange={e => setForm({...form, fecha: e.target.value})} />
                  </div>
                )}

                <div>
                  <label style={{ display: "block", marginBottom: "6px", fontSize: "13px", fontWeight: "600", color: "#333" }}>{modalType === 'contact' ? "Mensaje *" : "Mensaje opcional"}</label>
                  <textarea style={{ width: "100%", padding: "10px 12px", border: errors.mensaje ? "1px solid var(--color-error)" : "1px solid #ccc", borderRadius: "6px", fontSize: "14px", minHeight: "80px", fontFamily: "inherit", boxSizing: "border-box" }} value={form.mensaje} onChange={e => setForm({...form, mensaje: e.target.value})} placeholder={modalType === 'contact' ? "Me interesa esta propiedad..." : "Algún comentario adicional..."} />
                </div>

                <button type="submit" className="btn btn-primary" style={{ width: "100%", padding: "12px", marginTop: "8px", justifyContent: "center" }}>
                  Enviar {modalType === 'contact' ? "mensaje" : "solicitud"}
                </button>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
