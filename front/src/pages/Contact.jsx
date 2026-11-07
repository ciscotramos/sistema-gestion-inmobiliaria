import { useState } from "react";
import { getContactosMock, setContactosMock } from "../data/contactos";
import "./Contact.css";

const CONTACT_INFO = [
  {
    icon: "📍",
    label: "Dirección",
    text: "Av. Javier Prado Este 1234\nSan Isidro, Lima, Perú",
  },
  {
    icon: "📞",
    label: "Teléfono",
    text: "+51 987 654 321",
  },
  {
    icon: "✉️",
    label: "Correo electrónico",
    text: "info@century21.com",
  },
  {
    icon: "🕐",
    label: "Horario de atención",
    text: "Lunes a Viernes: 9:00 am – 6:00 pm\nSábados: 10:00 am – 1:00 pm",
  },
];

const INITIAL_FORM = {
  nombre: "",
  apellido: "",
  email: "",
  telefono: "",
  mensaje: "",
};

export default function Contact() {
  const [form, setForm] = useState(INITIAL_FORM);
  const [errors, setErrors] = useState({});
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.nombre.trim()) e.nombre = "El nombre es requerido";
    if (!form.email.trim()) {
      e.email = "El correo es requerido";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      e.email = "El correo no es válido";
    }
    if (!form.telefono.trim()) e.telefono = "El teléfono es requerido";
    if (!form.mensaje.trim()) e.mensaje = "El mensaje es requerido";
    return e;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
    if (errors[name]) setErrors((er) => ({ ...er, [name]: undefined }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setLoading(true);
    // Simulating API call and MOCK persistence
    setTimeout(() => {
      const currentContacts = getContactosMock();
      const registro = {
        id: Date.now(),
        nombre: form.nombre + (form.apellido ? " " + form.apellido : ""),
        email: form.email,
        telefono: form.telefono,
        mensaje: form.mensaje,
        propiedad: "General",
        propiedad_id: null,
        agente_id: null,
        tipo: "Contacto general",
        origen: "Contacto general",
        fecha: new Date().toISOString(),
        estado: "Pendiente",
        seguimientos: []
      };
      
      const newList = [registro, ...currentContacts];
      setContactosMock(newList);
      console.log("✅ Registro guardado en MOCK (Contacto General):", registro);

      setLoading(false);
      setSubmitted(true);
      setForm(INITIAL_FORM);
    }, 1200);
  };

  return (
    <div className="contact-page">
      {/* Hero */}
      <section className="contact-hero">
        <div className="container">
          <h1>Contáctanos</h1>
          <p>
            Estamos aquí para asesorarte. Escríbenos y uno de nuestros
            asesores te contactará a la brevedad.
          </p>
        </div>
      </section>

      {/* Content */}
      <div className="container">
        <div className="contact-layout">
          {/* ===== INFO COLUMN ===== */}
          <div className="contact-info">
            <div className="contact-info-header">
              <h2>Información de contacto</h2>
              <p>
                Puedes visitarnos en nuestra oficina o comunicarte por
                cualquiera de los siguientes medios.
              </p>
            </div>

            <div className="contact-items">
              {CONTACT_INFO.map((item) => (
                <div key={item.label} className="contact-item">
                  <div className="contact-item-icon">{item.icon}</div>
                  <div className="contact-item-content">
                    <h4>{item.label}</h4>
                    <p style={{ whiteSpace: "pre-line" }}>{item.text}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="contact-map-mini">
              <span className="map-icon">🗺️</span>
              <strong>San Isidro, Lima, Perú</strong>
              <span style={{ fontSize: "12px" }}>
                Mapa disponible próximamente
              </span>
            </div>
          </div>

          {/* ===== FORM COLUMN ===== */}
          <div className="contact-form-card">
            <h2>Envíanos un mensaje</h2>
            <p>Completa el formulario y te responderemos en menos de 24 horas.</p>

            <form className="contact-form" onSubmit={handleSubmit} noValidate>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="nombre">Nombre *</label>
                  <input
                    id="nombre"
                    name="nombre"
                    type="text"
                    className={`form-control${errors.nombre ? " error" : ""}`}
                    placeholder="Tu nombre"
                    value={form.nombre}
                    onChange={handleChange}
                    style={errors.nombre ? { borderColor: "var(--color-error)" } : {}}
                  />
                  {errors.nombre && (
                    <span style={{ fontSize: "12px", color: "var(--color-error)" }}>
                      {errors.nombre}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="apellido">Apellido</label>
                  <input
                    id="apellido"
                    name="apellido"
                    type="text"
                    className="form-control"
                    placeholder="Tu apellido"
                    value={form.apellido}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="email">Correo electrónico *</label>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    className="form-control"
                    placeholder="tu@correo.com"
                    value={form.email}
                    onChange={handleChange}
                    style={errors.email ? { borderColor: "var(--color-error)" } : {}}
                  />
                  {errors.email && (
                    <span style={{ fontSize: "12px", color: "var(--color-error)" }}>
                      {errors.email}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="telefono">Teléfono *</label>
                  <input
                    id="telefono"
                    name="telefono"
                    type="tel"
                    className="form-control"
                    placeholder="+51 999 999 999"
                    value={form.telefono}
                    onChange={handleChange}
                    style={errors.telefono ? { borderColor: "var(--color-error)" } : {}}
                  />
                  {errors.telefono && (
                    <span style={{ fontSize: "12px", color: "var(--color-error)" }}>
                      {errors.telefono}
                    </span>
                  )}
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="mensaje">Mensaje *</label>
                <textarea
                  id="mensaje"
                  name="mensaje"
                  className="form-control"
                  placeholder="Escribe tu mensaje aquí..."
                  value={form.mensaje}
                  onChange={handleChange}
                  style={errors.mensaje ? { borderColor: "var(--color-error)" } : {}}
                />
                {errors.mensaje && (
                  <span style={{ fontSize: "12px", color: "var(--color-error)" }}>
                    {errors.mensaje}
                  </span>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-lg"
                disabled={loading}
              >
                {loading ? "Enviando..." : "Enviar mensaje →"}
              </button>

              {submitted && (
                <div className="contact-success">
                  <span className="check">✅</span>
                  <div>
                    <strong>¡Mensaje enviado correctamente!</strong>
                    <br />
                    Te contactaremos a la brevedad. Gracias.
                  </div>
                </div>
              )}
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
