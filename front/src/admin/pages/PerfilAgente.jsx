import { useAuth } from "../../auth/useAuth";
import AdminLayout from "../components/AdminLayout";

export default function PerfilAgente() {
  const { usuario } = useAuth();
  
  if (!usuario) return null;

  return (
    <AdminLayout title="Mi perfil">
      <div className="admin-card" style={{ maxWidth: "600px", margin: "0 auto" }}>
        <div className="admin-card-header">
          <p className="admin-card-title">Información del Perfil</p>
        </div>
        <div style={{ padding: "24px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "20px", marginBottom: "30px" }}>
            <div style={{ width: "80px", height: "80px", borderRadius: "50%", background: "var(--color-primary, #c9a84c)", color: "#1a1a1a", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "32px", fontWeight: "bold" }}>
              {usuario.nombre?.[0]?.toUpperCase()}
            </div>
            <div>
              <h2 style={{ margin: "0 0 5px 0" }}>{usuario.nombre}</h2>
              <span className="status-badge status-activo">{usuario.rol}</span>
            </div>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
            <div>
              <label style={{ display: "block", fontSize: "13px", color: "#666", marginBottom: "4px", fontWeight: 600 }}>Correo Electrónico</label>
              <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "6px", border: "1px solid #ddd", fontSize: "14px" }}>{usuario.email}</div>
            </div>
            <div>
              <label style={{ display: "block", fontSize: "13px", color: "#666", marginBottom: "4px", fontWeight: 600 }}>Teléfono</label>
              <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "6px", border: "1px solid #ddd", fontSize: "14px" }}>{usuario.telefono || "No especificado"}</div>
            </div>
            <div>
              <label style={{ display: "block", fontSize: "13px", color: "#666", marginBottom: "4px", fontWeight: 600 }}>Estado de la Cuenta</label>
              <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "6px", border: "1px solid #ddd", fontSize: "14px" }}>{usuario.estado}</div>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
}
