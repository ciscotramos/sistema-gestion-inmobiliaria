import { Link } from "react-router-dom";
import "./PropertyCard.css";

const formatPrice = (price, moneda = "USD", operacion = "Venta") => {
  const num = Number(price || 0);
  const formatted = num.toLocaleString("en-US");
  const suffix = operacion === "Alquiler" ? " / mes" : "";
  return `${moneda === "USD" ? "US$" : "S/"} ${formatted}${suffix}`;
};

export default function PropertyCard({ property }) {
  const {
    id,
    titulo,
    operacion = "Venta",
    precio,
    moneda = "USD",
    ubicacion,
    dormitorios,
    banos,
    area_construida,
    area_total,
    imagenes,
    imagenPrincipal,
  } = property;

  const areaC = area_construida ?? property.areaConstruida;
  const areaT = area_total ?? property.areaTotal;

  const imgSource = imagenPrincipal ||
    (Array.isArray(imagenes) && imagenes.length > 0
      ? (typeof imagenes[0] === "string" ? imagenes[0] : imagenes[0]?.url)
      : "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&q=60");

  return (
    <Link to={`/propiedades/${id}`} className="property-card">
      {/* Image */}
      <div className="property-card-img">
        <img
          src={imgSource}
          alt={titulo}
          loading="lazy"
          onError={(e) => {
            e.target.src =
              "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&q=60";
          }}
        />
        <div className="property-card-badge">
          <span className={`badge badge-${(operacion || 'venta').toLowerCase()}`}>
            {operacion}
          </span>
        </div>
        <div className="property-card-overlay" />
      </div>

      {/* Body */}
      <div className="property-card-body">
        <h3 className="property-card-title">{titulo}</h3>
        <p className="property-card-price">
          {formatPrice(precio, moneda, operacion)}
        </p>
        <p className="property-card-location">
          <span className="pin">📍</span>
          {ubicacion}
        </p>

        {/* Stats */}
        <div className="property-card-stats">
          {dormitorios !== null && (
            <div className="stat-item">
              <span className="stat-icon">🛏</span>
              <strong>{dormitorios}</strong>
              <span>dorm.</span>
            </div>
          )}
          {banos !== null && (
            <div className="stat-item">
              <span className="stat-icon">🚿</span>
              <strong>{banos}</strong>
              <span>baños</span>
            </div>
          )}
          {areaC !== null && areaC !== undefined && (
            <div className="stat-item">
              <span className="stat-icon">📐</span>
              <strong>{areaC}</strong>
              <span>m²</span>
            </div>
          )}
          {(areaC === null || areaC === undefined) && areaT !== null && areaT !== undefined && (
            <div className="stat-item">
              <span className="stat-icon">📐</span>
              <strong>{areaT}</strong>
              <span>m²</span>
            </div>
          )}
        </div>
      </div>
    </Link>
  );
}
