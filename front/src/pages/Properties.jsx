import { useState, useEffect, useMemo } from "react";
import { useSearchParams, Link } from "react-router-dom";
import PropertyCard from "../components/PropertyCard";
import Filters from "../components/Filters";
import Pagination from "../components/Pagination";
import { getPropiedades } from "../services/propiedadesService";
import "./Properties.css";

const PER_PAGE = 6;

export default function Properties() {
  const [searchParams] = useSearchParams();

  const [filters, setFilters] = useState({
    operacion: searchParams.get("operacion") || "",
    tipos: searchParams.get("tipo") ? [searchParams.get("tipo")] : [],
    distrito: searchParams.get("distrito") || "",
    precioMin: "",
    precioMax: "",
    page: 1,
  });

  const [sort, setSort] = useState("recientes");
  const [dbProperties, setDbProperties] = useState([]);
  const [loading, setLoading] = useState(true);

  // Carga de propiedades
  useEffect(() => {
    let isMounted = true;
    setLoading(true);

    const params = {
      activo: true,
      estado: "Disponible",
      page: 0,
      size: 50,
    };
    if (filters.operacion) params.operacion = filters.operacion;
    if (filters.distrito && filters.distrito !== "Todos") params.distrito = filters.distrito;
    if (filters.tipos.length === 1) params.tipo = filters.tipos[0];
    if (filters.precioMin) params.precioMin = filters.precioMin;
    if (filters.precioMax) params.precioMax = filters.precioMax;

    getPropiedades(params)
      .then((data) => {
        if (!isMounted) return;
        const list = data.content || (Array.isArray(data) ? data : []);
        setDbProperties(list);
      })
      .catch((err) => {
        console.error("Error al cargar propiedades del backend:", err);
        if (isMounted) {
          setDbProperties([]);
        }
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [filters.operacion, filters.distrito, filters.tipos, filters.precioMin, filters.precioMax]);

  // Scroll to top on page change
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }, [filters.page]);

  // Filtrado y ordenamiento de propiedades
  const filtered = useMemo(() => {
    let result = [...dbProperties];

    if (filters.operacion) {
      result = result.filter((p) => p.operacion?.toLowerCase() === filters.operacion.toLowerCase());
    }
    if (filters.tipos.length > 0) {
      result = result.filter((p) => filters.tipos.includes(p.tipo));
    }
    if (filters.distrito && filters.distrito !== "Todos") {
      result = result.filter((p) => {
        const dNombre = p.distrito?.nombre || p.distrito || "";
        const ubic = p.ubicacion || "";
        return dNombre.toLowerCase().includes(filters.distrito.toLowerCase()) ||
               ubic.toLowerCase().includes(filters.distrito.toLowerCase());
      });
    }
    if (filters.precioMin !== "") {
      result = result.filter((p) => Number(p.precio) >= Number(filters.precioMin));
    }
    if (filters.precioMax !== "") {
      result = result.filter((p) => Number(p.precio) <= Number(filters.precioMax));
    }

    switch (sort) {
      case "precio-asc":
        result.sort((a, b) => Number(a.precio) - Number(b.precio));
        break;
      case "precio-desc":
        result.sort((a, b) => Number(b.precio) - Number(a.precio));
        break;
      default:
        result.sort((a, b) => Number(b.id) - Number(a.id));
    }

    return result;
  }, [dbProperties, filters, sort]);

  const totalPages = Math.ceil(filtered.length / PER_PAGE);
  const currentPage = Math.min(filters.page, totalPages || 1);
  const paginated = filtered.slice(
    (currentPage - 1) * PER_PAGE,
    currentPage * PER_PAGE
  );

  const handleFiltersChange = (newFilters) => {
    setFilters(newFilters);
  };

  const handlePageChange = (page) => {
    setFilters((f) => ({ ...f, page }));
  };

  return (
    <div className="properties-page">
      {/* Page header */}
      <div className="properties-page-header">
        <div className="container">
          <nav className="breadcrumb">
            <Link to="/">Inicio</Link>
            <span>›</span>
            <span>Propiedades</span>
          </nav>
          <h1>Propiedades</h1>
        </div>
      </div>

      {/* Content */}
      <div className="container">
        <div className="properties-layout">
          {/* Filters sidebar */}
          <Filters filters={filters} onChange={handleFiltersChange} />

          {/* Main content */}
          <div>
            {/* Toolbar */}
            <div className="properties-toolbar">
              <p className="properties-count">
                Mostrando <strong>{filtered.length}</strong> propiedad
                {filtered.length !== 1 ? "es" : ""}
              </p>
              <div className="properties-sort">
                <span>Ordenar por:</span>
                <select
                  value={sort}
                  onChange={(e) => {
                    setSort(e.target.value);
                    setFilters((f) => ({ ...f, page: 1 }));
                  }}
                >
                  <option value="recientes">Más recientes</option>
                  <option value="precio-asc">Menor precio</option>
                  <option value="precio-desc">Mayor precio</option>
                </select>
              </div>
            </div>

            {/* Grid */}
            <div className="properties-grid">
              {loading && filtered.length === 0 ? (
                <div style={{ gridColumn: "1 / -1", textAlign: "center", padding: "40px" }}>
                  <p>Cargando propiedades...</p>
                </div>
              ) : paginated.length === 0 ? (
                <div className="properties-empty">
                  <div className="empty-icon">🔍</div>
                  <h3>No se encontraron propiedades</h3>
                  <p>
                    Intenta ajustar los filtros para ver más resultados.
                  </p>
                </div>
              ) : (
                paginated.map((property) => (
                  <PropertyCard key={property.id} property={property} />
                ))
              )}
            </div>

            {/* Pagination */}
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
