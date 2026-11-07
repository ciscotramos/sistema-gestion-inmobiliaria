import { Suspense, lazy } from "react";
import { BrowserRouter, Routes, Route, Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./auth/useAuth";

// ── Auth ──────────────────────────────────────────────────
import { AuthProvider } from "./auth/AuthContext";

// ── Componentes de layout (siempre necesarios) ────────────
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";

// ── Home: carga eager (es la ruta crítica del LCP) ────────
import Home from "./pages/Home";

// ── Páginas públicas: lazy (no se necesitan en el primer render) ──
const Properties    = lazy(() => import("./pages/Properties"));
const PropertyDetail = lazy(() => import("./pages/PropertyDetail"));
const About         = lazy(() => import("./pages/About"));
const Contact       = lazy(() => import("./pages/Contact"));
const Login         = lazy(() => import("./pages/Login"));

// ── Panel Admin: lazy (nunca se necesita en carga pública) ─
const Dashboard      = lazy(() => import("./admin/pages/Dashboard"));
const Clientes       = lazy(() => import("./admin/pages/Clientes"));
const Propiedades    = lazy(() => import("./admin/pages/Propiedades"));
const Operaciones    = lazy(() => import("./admin/pages/Operaciones"));
const Alquileres     = lazy(() => import("./admin/pages/Alquileres"));
const Comisiones     = lazy(() => import("./admin/pages/Comisiones"));
const Usuarios       = lazy(() => import("./admin/pages/Usuarios"));
const Reportes       = lazy(() => import("./admin/pages/Reportes"));
const ContactosAgente = lazy(() => import("./admin/pages/ContactosAgente"));
const PerfilAgente   = lazy(() => import("./admin/pages/PerfilAgente"));

/** Spinner mínimo para Suspense fallback — no bloquea pintura */
function PageLoader() {
  return <div className="spinner" aria-label="Cargando..." />;
}

function RequireRole({ rol, children }) {
  const { usuario } = useAuth();
  if (!usuario) return <Navigate to="/login" replace />;
  if (usuario.rol !== rol && rol === "ADMIN") return <Navigate to="/agente/propiedades" replace />;
  if (usuario.rol !== rol && rol === "AGENTE") return <Navigate to="/admin/dashboard" replace />;
  return children;
}

function NotFound() {
  return (
    <div style={{ paddingTop: "68px" }}>
      <div className="not-found">
        <h2>404</h2>
        <h3>Página no encontrada</h3>
        <p>La página que buscas no existe.</p>
        <a href="/" className="btn btn-primary">
          Volver al inicio
        </a>
      </div>
    </div>
  );
}

/** Wrapper del frontend público: incluye Navbar y Footer */
function PublicLayout({ children }) {
  return (
    <>
      <Navbar />
      {children}
      <Footer />
    </>
  );
}



export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<PageLoader />}>
          <Routes>
            {/* ─── LOGIN ────────────────────────────────────────── */}
            <Route path="/login" element={<Login />} />

            {/* ─── FRONTEND PÚBLICO ─────────────────────────────── */}
            <Route path="/" element={<PublicLayout><Home /></PublicLayout>} />
            <Route path="/propiedades" element={<PublicLayout><Properties /></PublicLayout>} />
            <Route path="/propiedades/:id" element={<PublicLayout><PropertyDetail /></PublicLayout>} />
            <Route path="/nosotros" element={<PublicLayout><About /></PublicLayout>} />
            <Route path="/contacto" element={<PublicLayout><Contact /></PublicLayout>} />

            {/* ─── PANEL ADMINISTRATIVO ─────────────────────────── */}
            <Route element={<RequireRole rol="ADMIN"><Outlet /></RequireRole>}>
              <Route path="/admin" element={<Dashboard />} />
              <Route path="/admin/dashboard" element={<Dashboard />} />
              <Route path="/admin/clientes" element={<Clientes />} />
              <Route path="/admin/propiedades" element={<Propiedades />} />
              <Route path="/admin/operaciones" element={<Operaciones />} />
              <Route path="/admin/alquileres" element={<Alquileres />} />
              <Route path="/admin/comisiones" element={<Comisiones />} />
              <Route path="/admin/usuarios" element={<Usuarios />} />
              <Route path="/admin/contactos" element={<ContactosAgente />} />
              <Route path="/admin/reportes" element={<Reportes />} />
            </Route>

            {/* ─── PANEL DEL AGENTE ─────────────────────────────── */}
            <Route element={<RequireRole rol="AGENTE"><Outlet /></RequireRole>}>
              <Route path="/agente" element={<Navigate to="/agente/propiedades" replace />} />
              <Route path="/agente/propiedades" element={<Propiedades />} />
              <Route path="/agente/clientes" element={<Clientes />} />
              <Route path="/agente/alquileres" element={<Alquileres />} />
              <Route path="/agente/contactos" element={<ContactosAgente />} />
              <Route path="/agente/perfil" element={<PerfilAgente />} />
            </Route>

            {/* ─── 404 ──────────────────────────────────────────── */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </AuthProvider>
  );
}
