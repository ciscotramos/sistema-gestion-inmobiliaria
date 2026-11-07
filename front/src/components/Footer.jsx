import { Link } from "react-router-dom";
import "./Footer.css";

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          {/* Brand */}
          <div className="footer-brand">
            <div className="footer-logo">
              <div className="footer-logo-icon">🏠</div>
              <div className="footer-logo-text">
                <strong>CENTURY</strong>
                <span> 21</span>
              </div>
            </div>
            <p>
              Somos una inmobiliaria dedicada a ayudarte a encontrar la mejor
              opción para tu hogar o inversión. Más de 10 años de experiencia
              nos respaldan.
            </p>
            <div className="footer-social">
              <div className="social-icon" title="Facebook">f</div>
              <div className="social-icon" title="Instagram">in</div>
              <div className="social-icon" title="WhatsApp">w</div>
            </div>
          </div>

          {/* Navigation */}
          <div className="footer-col">
            <h4>Navegación</h4>
            <ul>
              <li><Link to="/">Inicio</Link></li>
              <li><Link to="/propiedades">Propiedades</Link></li>
              <li><Link to="/nosotros">Nosotros</Link></li>
              <li><Link to="/contacto">Contacto</Link></li>
            </ul>
          </div>

          {/* Properties */}
          <div className="footer-col">
            <h4>Propiedades</h4>
            <ul>
              <li><Link to="/propiedades?operacion=Venta">En Venta</Link></li>
              <li><Link to="/propiedades?operacion=Alquiler">En Alquiler</Link></li>
              <li><Link to="/propiedades?tipo=Casa">Casas</Link></li>
              <li><Link to="/propiedades?tipo=Departamento">Departamentos</Link></li>
              <li><Link to="/propiedades?tipo=Terreno">Terrenos</Link></li>
            </ul>
          </div>

          {/* Contact */}
          <div className="footer-col">
            <h4>Contacto</h4>
            <div className="footer-contact-item">
              <span className="icon">📍</span>
              <p>Av. Javier Prado Este 1234<br />San Isidro, Lima, Perú</p>
            </div>
            <div className="footer-contact-item">
              <span className="icon">📞</span>
              <p>+51 987 654 321</p>
            </div>
            <div className="footer-contact-item">
              <span className="icon">✉️</span>
              <p>info@century21.com</p>
            </div>
            <div className="footer-contact-item">
              <span className="icon">🕐</span>
              <p>Lun–Vie: 9:00 am – 6:00 pm<br />Sáb: 10:00 am – 1:00 pm</p>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <p>© {year} CENTURY 21. Todos los derechos reservados.</p>
          <div className="footer-bottom-links">
            <a href="#">Política de privacidad</a>
            <a href="#">Términos de uso</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
