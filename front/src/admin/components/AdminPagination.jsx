/**
 * AdminPagination
 * Componente de paginación para tablas administrativas.
 *
 * Props:
 *   currentPage  - página actual
 *   totalPages   - total de páginas
 *   totalItems   - total de registros
 *   perPage      - registros por página
 *   onPageChange - función al cambiar de página
 */
export default function AdminPagination({
  currentPage,
  totalPages,
  totalItems,
  perPage,
  onPageChange,
}) {
  if (totalPages <= 1 && totalItems === 0) return null;

  const from = totalItems === 0 ? 0 : (currentPage - 1) * perPage + 1;
  const to = Math.min(currentPage * perPage, totalItems);

  const pages = Array.from({ length: totalPages }, (_, i) => i + 1);

  return (
    <div className="admin-pagination">
      <span>
        Mostrando{" "}
        <strong>
          {from}–{to}
        </strong>{" "}
        de <strong>{totalItems}</strong> registros
      </span>

      <div className="admin-pagination-pages">
        <button
          className="admin-pg-btn"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          aria-label="Página anterior"
        >
          ‹
        </button>

        {pages.map((p) => (
          <button
            key={p}
            className={`admin-pg-btn${p === currentPage ? " active" : ""}`}
            onClick={() => onPageChange(p)}
            aria-label={`Página ${p}`}
          >
            {p}
          </button>
        ))}

        <button
          className="admin-pg-btn"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          aria-label="Página siguiente"
        >
          ›
        </button>
      </div>
    </div>
  );
}
