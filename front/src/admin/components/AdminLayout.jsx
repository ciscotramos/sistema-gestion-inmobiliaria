import { useState } from "react";
import Sidebar from "./Sidebar";
import AdminHeader from "./AdminHeader";
import "../../admin/admin.css";

/**
 * AdminLayout
 * Wrapper principal del panel administrativo.
 * Contiene el Sidebar, AdminHeader y el área de contenido.
 *
 * Uso:
 *   <AdminLayout title="Dashboard">
 *     <MiContenido />
 *   </AdminLayout>
 */
export default function AdminLayout({ title, children }) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleToggle = () => {
    // En mobile: abre/cierra el sidebar con overlay
    if (window.innerWidth <= 768) {
      setMobileOpen((v) => !v);
    } else {
      setCollapsed((v) => !v);
    }
  };

  return (
    <div className="admin-wrapper">
      {/* Mobile overlay */}
      {mobileOpen && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0,0,0,0.5)",
            zIndex: 199,
          }}
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Sidebar */}
      <Sidebar
        collapsed={collapsed}
        mobileOpen={mobileOpen}
      />

      {/* Main */}
      <div className={`admin-main${collapsed ? " collapsed" : ""}`}>
        <AdminHeader title={title} onToggleSidebar={handleToggle} />
        <main className="admin-content">
          {children}
        </main>
      </div>
    </div>
  );
}
