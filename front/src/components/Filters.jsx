import { useState, useEffect } from "react";
import { getDistritos } from "../services/distritosService";
import "./Filters.css";

const TIPOS = ["Casa", "Departamento", "Terreno", "Oficina", "Local Comercial"];
const DEFAULT_DISTRITOS = [
  "Todas las ubicaciones",
  "La Molina",
  "Miraflores",
  "San Isidro",
  "Surco",
  "Barranco",
  "Pachacamac",
  "Cieneguilla",
  "San Borja",
  "Surquillo",
];

export default function Filters({ filters, onChange }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [distritos, setDistritos] = useState(DEFAULT_DISTRITOS);

  useEffect(() => {
    getDistritos()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          const nombres = data.map((d) => d.nombre);
          const unicos = ["Todas las ubicaciones", ...Array.from(new Set([...nombres, ...DEFAULT_DISTRITOS.slice(1)]))];
          setDistritos(unicos);
        }
      })
      .catch(() => {});
  }, []);

  const handleOperacion = (val) => {
    onChange({ ...filters, operacion: val, page: 1 });
  };

  const handleTipo = (tipo) => {
    const current = filters.tipos || [];
    const next = current.includes(tipo)
      ? current.filter((t) => t !== tipo)
      : [...current, tipo];
    onChange({ ...filters, tipos: next, page: 1 });
  };

  const handleDistrito = (e) => {
    onChange({
      ...filters,
      distrito: e.target.value === "Todas las ubicaciones" ? "" : e.target.value,
      page: 1,
    });
  };

  const handlePrice = (field, val) => {
    onChange({ ...filters, [field]: val, page: 1 });
  };

  const handleClear = () => {
    onChange({ operacion: "", tipos: [], distrito: "", precioMin: "", precioMax: "", page: 1 });
  };

  const handleApply = () => setMobileOpen(false);

  return (
    <aside className="filters">
      {/* Mobile toggle */}
      <button
        className="btn btn-outline btn-sm filters-mobile-toggle"
        onClick={() => setMobileOpen((v) => !v)}
      >
        🔍 {mobileOpen ? "Ocultar filtros" : "Mostrar filtros"}
      </button>

      <div className={`filters-collapsible${mobileOpen ? " open" : ""}`}>
        <p className="filters-title">Filtros</p>

        {/* Operación */}
        <div className="filter-section">
          <p className="filter-section-label">Operación</p>
          <div className="filter-options">
            {["", "Venta", "Alquiler"].map((op) => (
              <label key={op} className="filter-option">
                <input
                  type="radio"
                  name="operacion"
                  checked={filters.operacion === op}
                  onChange={() => handleOperacion(op)}
                />
                {op === "" ? "Todos" : op}
              </label>
            ))}
          </div>
        </div>

        {/* Tipo de propiedad */}
        <div className="filter-section">
          <p className="filter-section-label">Tipo de propiedad</p>
          <div className="filter-options">
            {TIPOS.map((tipo) => (
              <label key={tipo} className="filter-option">
                <input
                  type="checkbox"
                  checked={(filters.tipos || []).includes(tipo)}
                  onChange={() => handleTipo(tipo)}
                />
                {tipo}
              </label>
            ))}
          </div>
        </div>

        {/* Ubicación */}
        <div className="filter-section">
          <p className="filter-section-label">Ubicación</p>
          <select
            className="form-control"
            value={filters.distrito || "Todas las ubicaciones"}
            onChange={handleDistrito}
          >
            {distritos.map((d) => (
              <option key={d} value={d}>{d}</option>
            ))}
          </select>
        </div>

        {/* Precio */}
        <div className="filter-section">
          <p className="filter-section-label">Precio (US$)</p>
          <div className="filter-price-row">
            <input
              type="number"
              className="form-control"
              placeholder="Mínimo"
              value={filters.precioMin || ""}
              onChange={(e) => handlePrice("precioMin", e.target.value)}
              min="0"
            />
            <input
              type="number"
              className="form-control"
              placeholder="Máximo"
              value={filters.precioMax || ""}
              onChange={(e) => handlePrice("precioMax", e.target.value)}
              min="0"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="filters-actions">
          <button className="btn btn-primary" onClick={handleApply}>
            Filtrar
          </button>
          <button className="btn-clear" onClick={handleClear}>
            Limpiar filtros
          </button>
        </div>
      </div>
    </aside>
  );
}
