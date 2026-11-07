import { Link } from "react-router-dom";
import "./About.css";

const WHY_ITEMS = [
  {
    icon: "🏠",
    title: "Atención personalizada",
    desc: "Cada cliente es único. Nuestros asesores se dedican exclusivamente a entender tus necesidades y encontrar la mejor opción para ti.",
  },
  {
    icon: "📋",
    title: "Gestión organizada",
    desc: "Administramos todo el proceso de compra, venta o alquiler de principio a fin, para que tú solo te preocupes por elegir.",
  },
  {
    icon: "💼",
    title: "Asesoría inmobiliaria",
    desc: "Te orientamos sobre precios de mercado, zonas de inversión y aspectos legales para que tomes la mejor decisión.",
  },
  {
    icon: "🔒",
    title: "Seguridad y confianza",
    desc: "Todas nuestras operaciones están respaldadas jurídicamente. Trabajamos solo con propiedades y propietarios verificados.",
  },
  {
    icon: "📍",
    title: "Amplia cobertura",
    desc: "Contamos con propiedades en los principales distritos de Lima: Miraflores, San Isidro, La Molina, Surco y más.",
  },
  {
    icon: "⚡",
    title: "Respuesta inmediata",
    desc: "Entendemos que el tiempo es valioso. Respondemos en menos de 24 horas y coordinamos visitas con total flexibilidad.",
  },
];

export default function About() {
  return (
    <div className="about-page">
      {/* ==============================
          HERO
      ============================== */}
      <section className="about-hero">
        <div className="container">
          <div className="about-hero-content">
            <span className="section-tag">Sobre nosotros</span>
            <h1>
              Tu confianza es nuestra
              <br />
              <em>mayor responsabilidad</em>
            </h1>
            <p>
              Somos CENTURY 21, una empresa comprometida con
              ayudarte a encontrar el hogar o la inversión perfecta. Más de
              10 años de experiencia nos respaldan.
            </p>
          </div>
        </div>
      </section>

      {/* ==============================
          QUIÉNES SOMOS
      ============================== */}
      <section className="about-who">
        <div className="container">
          <div className="about-who-grid">
            <div className="about-who-image">
              <img
                src="https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=700&q=80"
                alt="Equipo CENTURY 21"
              />
            </div>
            <div className="about-who-text">
              <h2 className="title-underline">¿Quiénes somos?</h2>
              <p style={{ marginTop: "24px" }}>
                CENTURY 21 es una empresa peruana fundada con el
                propósito de hacer más accesible, seguro y transparente el
                proceso de compra, venta y alquiler de propiedades en Lima.
              </p>
              <p>
                Contamos con un equipo de asesores altamente capacitados,
                comprometidos con brindar la mejor atención y orientarte a
                tomar decisiones inteligentes en el mercado inmobiliario.
              </p>
              <p>
                Nuestra cartera incluye casas, departamentos, terrenos,
                oficinas y locales comerciales en los principales distritos
                de Lima Metropolitana.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ==============================
          MISSION / VISION / VALUES
      ============================== */}
      <section className="mvv-section">
        <div className="container">
          <div style={{ textAlign: "center" }}>
            <h2 className="section-title title-underline" style={{ display: "inline-block" }}>
              Nuestros pilares
            </h2>
            <p className="section-subtitle" style={{ marginTop: "16px" }}>
              Los principios que guían cada una de nuestras acciones
            </p>
          </div>

          <div className="mvv-grid">
            {/* Mission */}
            <div className="mvv-card">
              <div className="mvv-icon">🎯</div>
              <h3>Misión</h3>
              <p>
                Conectar a las personas con la propiedad que se ajusta a sus necesidades, brindando un servicio de intermediación cercano, ágil y respaldado por procesos comerciales ordenados.
              </p>
            </div>

            {/* Vision */}
            <div className="mvv-card">
              <div className="mvv-icon">🔭</div>
              <h3>Visión</h3>
              <p>
                Ser reconocida en el mercado limeño como una inmobiliaria de referencia por la transparencia, agilidad y confiabilidad de su proceso comercial.
              </p>
            </div>

            {/* Values */}
            <div className="mvv-card">
              <div className="mvv-icon">⭐</div>
              <h3>Valores</h3>
              <ul>
                <li>Honestidad y transparencia</li>
                <li>Compromiso con el cliente</li>
                <li>Profesionalismo</li>
                <li>Innovación continua</li>
                <li>Responsabilidad</li>
                <li>Trabajo en equipo</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* ==============================
          STATS
      ============================== */}
      <section className="stats-section">
        <div className="container">
          <div className="stats-grid">
            {[
              { val: "10+", lbl: "Años de experiencia" },
              { val: "120+", lbl: "Propiedades activas" },
              { val: "500+", lbl: "Clientes satisfechos" },
              { val: "15+", lbl: "Asesores expertos" },
            ].map((s) => (
              <div key={s.lbl} className="stats-item">
                <strong>{s.val}</strong>
                <span>{s.lbl}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ==============================
          WHY US
      ============================== */}
      <section className="why-section">
        <div className="container">
          <div className="why-header">
            <h2>¿Por qué elegirnos?</h2>
            <p>Razones que nos hacen diferentes</p>
          </div>
          <div className="why-grid">
            {WHY_ITEMS.map((item) => (
              <div key={item.title} className="why-card">
                <div className="why-card-icon">{item.icon}</div>
                <h4>{item.title}</h4>
                <p>{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ==============================
          CTA
      ============================== */}
      <section className="about-cta">
        <div className="container">
          <h2>¿Tienes alguna consulta?</h2>
          <p>
            Nuestro equipo está listo para orientarte. Contáctanos y te
            responderemos a la brevedad.
          </p>
          <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
            <Link to="/contacto" className="btn btn-primary btn-lg">
              Contáctanos
            </Link>
            <Link to="/propiedades" className="btn btn-outline btn-lg">
              Ver propiedades
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
