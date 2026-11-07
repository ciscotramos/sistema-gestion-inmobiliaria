/**
 * ConfirmDialog
 * Modal de confirmación para acciones destructivas (eliminar).
 *
 * Props:
 *   open     - boolean
 *   onClose  - función para cancelar
 *   onConfirm - función para confirmar
 *   title    - título de la confirmación
 *   message  - mensaje descriptivo
 */
import Modal from "./Modal";

export default function ConfirmDialog({ open, onClose, onConfirm, title, message }) {
  return (
    <Modal open={open} onClose={onClose} title="" size="sm">
      <div className="confirm-body">
        <div className="confirm-icon">🗑️</div>
        <h3>{title || "¿Estás seguro?"}</h3>
        <p>{message || "Esta acción no se puede deshacer."}</p>
      </div>
      <div className="modal-footer">
        <button className="btn-cancel" onClick={onClose}>
          Cancelar
        </button>
        <button className="btn-danger" onClick={onConfirm}>
          Eliminar
        </button>
      </div>
    </Modal>
  );
}
