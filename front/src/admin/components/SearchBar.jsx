/**
 * SearchBar
 * Barra de búsqueda reutilizable para los módulos administrativos.
 *
 * Props:
 *   placeholder - Texto de placeholder
 *   value       - Valor actual
 *   onChange    - Handler de cambio
 */
export default function SearchBar({ placeholder = "Buscar...", value, onChange }) {
  return (
    <div className="admin-search-wrapper">
      <span className="search-icon">🔍</span>
      <input
        type="search"
        className="admin-search"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-label={placeholder}
      />
    </div>
  );
}
