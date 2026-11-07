/**
 * Modal
 * Componente de modal reutilizable para formularios y confirmaciones.
 *
 * Props:
 *   open      - boolean: si el modal está abierto
 *   onClose   - función para cerrar el modal
 *   title     - título del modal
 *   size      - "sm" | "md" | "lg" (default: "md")
 *   children  - contenido del modal
 *   footer    - pie del modal (opcional, ReactNode)
 */
import { useEffect } from "react";

export default function Modal({ open, onClose, title, size = "md", children, footer }) {
  // Cerrar con Escape
  useEffect(() => {
    if (!open) return;
    const handler = (e) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [open, onClose]);

  // Bloquear scroll del body cuando el modal está abierto
  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  if (!open) return null;

  const sizeClass = size === "lg" ? "modal-lg" : size === "sm" ? "modal-sm" : "";

  return (
    <div
      className="modal-overlay"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className={`modal ${sizeClass}`}>
        <div className="modal-header">
          <h2 className="modal-title" id="modal-title">{title}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Cerrar modal">✕</button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}
