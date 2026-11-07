/**
 * EmptyState
 * Componente reutilizable para mostrar estados vacíos en tablas y módulos.
 *
 * Props:
 *   icon     - Emoji o ícono (string)
 *   title    - Título del estado vacío
 *   message  - Mensaje descriptivo
 *   action   - Botón de acción opcional (ReactNode)
 */
export default function EmptyState({
  icon = "📂",
  title = "No hay registros",
  message = "No existen registros todavía.",
  action,
}) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <h3>{title}</h3>
      <p>{message}</p>
      {action && action}
    </div>
  );
}
