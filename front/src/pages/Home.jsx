import { useState, useEffect, useRef } from "react";
import { useNavigate, Link } from "react-router-dom";
import PropertyCard from "../components/PropertyCard";
import { getDistritos } from "../services/distritosService";
import { getPropiedades } from "../services/propiedadesService";
import "./Home.css";

const DEFAULT_DISTRITOS = [
  "Todos",
  "La Molina",
  "Miraflores",
  "San Isidro",
  "Surco",
  "Barranco",
  "Pachacamac",
  "Cieneguilla",
];

export default function Home() {
  const navigate = useNavigate();
  const [featured, setFeatured] = useState([]);
  const bgRef = useRef(null);

  const [distritos, setDistritos] = useState(DEFAULT_DISTRITOS);
  const [search, setSearch] = useState({
    operacion: "",
    tipo: "",
    ubicacion: "",
    precio: "",
  });

  useEffect(() => {
    let isMounted = true;
    getDistritos()
      .then((data) => {
        if (isMounted && Array.isArray(data) && data.length > 0) {
          setDistritos(["Todos", ...data.map((d) => d.nombre)]);
        }
      })
      .catch(() => {
        // Maintain fallback DEFAULT_DISTRITOS
      });
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;
    getPropiedades({ destacada: true, size: 6, activo: true, estado: "Disponible" })
      .then((data) => {
        if (!isMounted) return;
        const list = data?.content || (Array.isArray(data) ? data : []);
        setFeatured(list);
      })
      .catch((err) => {
        console.error("Error al cargar propiedades destacadas:", err);
        if (isMounted) setFeatured([]);
      });
    return () => {
      isMounted = false;
    };
  }, []);

  // Animar el Hero BG sin doble-fetch: clase 'loaded' tras primer frame de pintura
  useEffect(() => {
    const raf = requestAnimationFrame(() => {
      if (bgRef.current) bgRef.current.classList.add("loaded");
    });
    return () => cancelAnimationFrame(raf);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (search.operacion) params.set("operacion", search.operacion);
    if (search.tipo) params.set("tipo", search.tipo);
    if (search.ubicacion && search.ubicacion !== "Todos")
      params.set("distrito", search.ubicacion);
    navigate(`/propiedades?${params.toString()}`);
  };

  return (
    <main>
      {/* ==============================
          HERO
      ============================== */}
      <section className="home-hero">
        <div className="home-hero-bg" ref={bgRef} />
        <div className="home-hero-overlay" />

        <div className="container">
          <div className="home-hero-content">
            <div className="home-hero-tag">
              <span>✨</span> Tu hogar ideal te espera
            </div>

            <h1>
              Encuentra la propiedad
              <br />
              <em>perfecta para ti</em>
            </h1>

            <p>
              Casas, departamentos, terrenos y más. Compra o alquila con los
              mejores asesores inmobiliarios de Lima.
            </p>

            {/* Search form */}
            <form className="hero-search" onSubmit={handleSearch}>
              <div className="hero-search-field">
                <label>Operación</label>
                <select
                  value={search.operacion}
                  onChange={(e) =>
                    setSearch({ ...search, operacion: e.target.value })
                  }
                >
                  <option value="">Venta o alquiler</option>
                  <option value="Venta">Venta</option>
                  <option value="Alquiler">Alquiler</option>
                </select>
              </div>

              <div className="hero-search-field">
                <label>Tipo de propiedad</label>
                <select
                  value={search.tipo}
                  onChange={(e) =>
                    setSearch({ ...search, tipo: e.target.value })
                  }
                >
                  <option value="">Todos los tipos</option>
                  <option value="Casa">Casa</option>
                  <option value="Departamento">Departamento</option>
                  <option value="Terreno">Terreno</option>
                  <option value="Oficina">Oficina</option>
                  <option value="Local Comercial">Local Comercial</option>
                </select>
              </div>

              <div className="hero-search-field">
                <label>Ubicación</label>
                <select
                  value={search.ubicacion}
                  onChange={(e) =>
                    setSearch({ ...search, ubicacion: e.target.value })
                  }
                >
                  {distritos.map((d) => (
                    <option key={d} value={d === "Todos" ? "" : d}>
                      {d === "Todos" ? "Todas las ubicaciones" : d}
                    </option>
                  ))}
                </select>
              </div>

              <button type="submit" className="btn btn-primary">
                🔍 Buscar
              </button>
            </form>

            {/* Stats */}
            <div className="hero-stats">
              <div className="hero-stat">
                <strong>120+</strong>
                <span>Propiedades</span>
              </div>
              <div className="hero-stat">
                <strong>10+</strong>
                <span>Años de exp.</span>
              </div>
              <div className="hero-stat">
                <strong>500+</strong>
                <span>Clientes</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ==============================
          FEATURED PROPERTIES
      ============================== */}
      <section className="featured-section">
        <div className="container">
          <div className="featured-header">
            <div>
              <h2 className="section-title title-underline">
                Propiedades destacadas
              </h2>
              <p className="section-subtitle">
                Seleccionamos las mejores opciones para ti
              </p>
            </div>
            <Link to="/propiedades" className="btn btn-outline">
              Ver todas las propiedades →
            </Link>
          </div>

          {featured.length === 0 ? (
            <div style={{ textAlign: "center", padding: "48px 20px", color: "var(--color-text-muted, #718096)" }}>
              <p style={{ fontSize: "16px", marginBottom: "16px" }}>No hay propiedades destacadas registradas actualmente.</p>
              <Link to="/propiedades" className="btn btn-outline">
                Ver catálogo de propiedades
              </Link>
            </div>
          ) : (
            <div className="featured-grid">
              {featured.map((property) => (
                <PropertyCard key={property.id} property={property} />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* ==============================
          ABOUT STRIP
      ============================== */}
      <section className="about-strip">
        <div className="container">
          <div className="about-strip-grid">
            <div className="about-strip-text">
              <h2>
                Más de <em>10 años</em> construyendo sueños inmobiliarios
              </h2>
              <p>
                Somos una inmobiliaria comprometida con ayudarte a encontrar la
                mejor opción para tu hogar o inversión. Nuestro equipo de
                expertos te acompaña en cada paso del proceso.
              </p>

              <div className="about-features">
                <div className="about-feature-item">
                  <div className="about-feature-icon">🏆</div>
                  <div>
                    <h4>Experiencia probada</h4>
                    <p>Más de una década en el mercado</p>
                  </div>
                </div>
                <div className="about-feature-item">
                  <div className="about-feature-icon">🤝</div>
                  <div>
                    <h4>Atención personalizada</h4>
                    <p>Te acompañamos en todo el proceso</p>
                  </div>
                </div>
                <div className="about-feature-item">
                  <div className="about-feature-icon">✅</div>
                  <div>
                    <h4>Propiedades verificadas</h4>
                    <p>Seguridad y confianza garantizada</p>
                  </div>
                </div>
                <div className="about-feature-item">
                  <div className="about-feature-icon">📞</div>
                  <div>
                    <h4>Soporte inmediato</h4>
                    <p>Respondemos a la brevedad</p>
                  </div>
                </div>
              </div>

              <div style={{ marginTop: "32px", display: "flex", gap: "12px" }}>
                <Link to="/nosotros" className="btn btn-primary">
                  Conoce más
                </Link>
                <Link to="/contacto" className="btn btn-outline">
                  Contáctanos
                </Link>
              </div>
            </div>

            <div className="about-strip-image">
              <img
                src="https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=700&fm=webp&q=75"
                alt="Equipo inmobiliario"
                loading="lazy"
                width="700"
                height="560"
                fetchpriority="low"
              />
              <div className="about-strip-image-badge">
                <strong>500+</strong>
                <span>Familias satisfechas</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ==============================
          SERVICES
      ============================== */}
      <section className="services-section">
        <div className="container">
          <div className="services-header">
            <h2>¿Por qué elegirnos?</h2>
            <p>Ofrecemos soluciones integrales para comprar, vender o alquilar</p>
          </div>

          <div className="services-grid">
            {[
              {
                icon: "🏠",
                title: "Amplio catálogo",
                desc: "Más de 120 propiedades disponibles en toda Lima para elegir.",
              },
              {
                icon: "👨‍💼",
                title: "Asesoría personalizada",
                desc: "Nuestros asesores te guían desde la búsqueda hasta la firma.",
              },
              {
                icon: "🔒",
                title: "Seguridad jurídica",
                desc: "Todas las propiedades cuentan con documentación en regla.",
              },
              {
                icon: "💰",
                title: "Mejores precios",
                desc: "Negociamos para conseguirte las mejores condiciones del mercado.",
              },
            ].map((s) => (
              <div key={s.title} className="service-card">
                <div className="service-icon">{s.icon}</div>
                <h3>{s.title}</h3>
                <p>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ==============================
          CTA
      ============================== */}
      <section className="cta-section">
        <div className="container">
          <h2>¿Listo para encontrar tu propiedad ideal?</h2>
          <p>
            Explora nuestro catálogo completo o contáctanos y un asesor te
            ayudará de inmediato.
          </p>
          <div className="cta-actions">
            <Link to="/propiedades" className="btn btn-dark btn-lg">
              Ver propiedades
            </Link>
            <Link to="/contacto" className="btn btn-outline btn-lg">
              Contactar asesor
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
