-- =============================================================================
-- PROYECTO: SISTEMA DE GESTIÓN INMOBILIARIA
-- ARCHIVO: schema_v1.sql
-- MOTOR: MySQL 8.0+ (InnoDB, utf8mb4)
-- NORMALIZACIÓN: 3FN (Tercera Forma Normal)
-- TOTAL TABLAS: 13
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. TABLA: usuarios
-- Propósito: Autenticación, RBAC (ADMIN/AGENTE) y gestión de asesores
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'AGENTE',
    telefono VARCHAR(30) NULL,
    cargo VARCHAR(80) NOT NULL DEFAULT 'Asesor Inmobiliario',
    foto_url TEXT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    ultimo_login DATETIME NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_email (email),
    KEY idx_usuarios_rol (rol),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'AGENTE')),
    CONSTRAINT chk_usuarios_estado CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TABLA: ubicaciones_distritos
-- Propósito: Catálogo normalizado de distritos para evitar redundancia
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS ubicaciones_distritos;
CREATE TABLE ubicaciones_distritos (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    provincia VARCHAR(80) NOT NULL DEFAULT 'Lima',
    departamento VARCHAR(80) NOT NULL DEFAULT 'Lima',
    PRIMARY KEY (id),
    UNIQUE KEY uq_distrito_prov_dep (nombre, provincia, departamento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABLA: caracteristicas
-- Propósito: Catálogo maestro de comodidades y amenidades de inmuebles
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS caracteristicas;
CREATE TABLE caracteristicas (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL DEFAULT 'General',
    PRIMARY KEY (id),
    UNIQUE KEY uq_caracteristicas_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. TABLA: propiedades
-- Propósito: Inmuebles comercializados (Venta / Alquiler)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS propiedades;
CREATE TABLE propiedades (
    id INT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NULL,
    operacion VARCHAR(20) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    precio DECIMAL(14, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    dormitorios INT NULL,
    banos INT NULL,
    area_construida DECIMAL(10, 2) NULL,
    area_total DECIMAL(10, 2) NULL,
    direccion VARCHAR(255) NULL,
    distrito_id INT NOT NULL,
    agente_id INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Disponible',
    destacada TINYINT(1) NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_propiedades_distrito (distrito_id),
    KEY idx_propiedades_agente (agente_id),
    KEY idx_propiedades_operacion (operacion),
    KEY idx_propiedades_tipo (tipo),
    KEY idx_propiedades_precio (precio),
    KEY idx_propiedades_estado_act (estado, activo),
    CONSTRAINT fk_propiedades_distrito FOREIGN KEY (distrito_id)
        REFERENCES ubicaciones_distritos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_propiedades_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT chk_propiedades_operacion CHECK (operacion IN ('Venta', 'Alquiler')),
    CONSTRAINT chk_propiedades_tipo CHECK (tipo IN ('Casa', 'Departamento', 'Terreno', 'Oficina', 'Local Comercial')),
    CONSTRAINT chk_propiedades_precio CHECK (precio >= 0),
    CONSTRAINT chk_propiedades_moneda CHECK (moneda IN ('USD', 'PEN')),
    CONSTRAINT chk_propiedades_dormitorios CHECK (dormitorios IS NULL OR dormitorios >= 0),
    CONSTRAINT chk_propiedades_banos CHECK (banos IS NULL OR banos >= 0),
    CONSTRAINT chk_propiedades_area_c CHECK (area_construida IS NULL OR area_construida >= 0),
    CONSTRAINT chk_propiedades_area_t CHECK (area_total IS NULL OR area_total >= 0),
    CONSTRAINT chk_propiedades_estado CHECK (estado IN ('Disponible', 'Reservado', 'Vendido', 'Alquilado', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. TABLA: propiedad_imagenes
-- Propósito: Galería multimedia de fotografías asociadas al inmueble
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS propiedad_imagenes;
CREATE TABLE propiedad_imagenes (
    id INT NOT NULL AUTO_INCREMENT,
    propiedad_id INT NOT NULL,
    url TEXT NOT NULL,
    orden INT NOT NULL DEFAULT 0,
    es_principal TINYINT(1) NOT NULL DEFAULT 0,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_imagenes_propiedad (propiedad_id),
    CONSTRAINT fk_imagenes_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. TABLA: propiedad_caracteristicas (Asociativa N:M)
-- Propósito: Relación muchos a muchos entre propiedades y comodidades
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS propiedad_caracteristicas;
CREATE TABLE propiedad_caracteristicas (
    propiedad_id INT NOT NULL,
    caracteristica_id INT NOT NULL,
    PRIMARY KEY (propiedad_id, caracteristica_id),
    KEY idx_prop_caract_caract (caracteristica_id),
    CONSTRAINT fk_prop_caract_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE CASCADE,
    CONSTRAINT fk_prop_caract_caracteristica FOREIGN KEY (caracteristica_id)
        REFERENCES caracteristicas(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. TABLA: clientes
-- Propósito: Directorio unificado de prospectos, compradores y arrendatarios
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS clientes;
CREATE TABLE clientes (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NULL,
    email VARCHAR(150) NULL,
    telefono VARCHAR(30) NULL,
    tipo VARCHAR(30) NULL,
    origen VARCHAR(50) NOT NULL DEFAULT 'Registro manual',
    propiedad_interes_id INT NULL,
    agente_id INT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_clientes_propiedad (propiedad_interes_id),
    KEY idx_clientes_agente (agente_id),
    KEY idx_clientes_email (email),
    KEY idx_clientes_telefono (telefono),
    CONSTRAINT fk_clientes_propiedad FOREIGN KEY (propiedad_interes_id)
        REFERENCES propiedades(id) ON DELETE SET NULL,
    CONSTRAINT fk_clientes_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_clientes_tipo CHECK (tipo IS NULL OR tipo IN ('Arrendatario', 'Propietario', 'Comprador', 'Interesado')),
    CONSTRAINT chk_clientes_origen CHECK (origen IN ('Registro manual', 'Contacto desde propiedad', 'Contacto general', 'Solicitud de visita')),
    CONSTRAINT chk_clientes_estado CHECK (estado IN ('Activo', 'Inactivo')),
    CONSTRAINT chk_clientes_contacto CHECK (email IS NOT NULL OR telefono IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. TABLA: contactos
-- Propósito: Bandeja de mensajes y consultas enviadas desde la web
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS contactos;
CREATE TABLE contactos (
    id INT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    propiedad_id INT NULL,
    agente_id INT NULL,
    tipo VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_contactos_cliente (cliente_id),
    KEY idx_contactos_propiedad (propiedad_id),
    KEY idx_contactos_agente (agente_id),
    KEY idx_contactos_estado (estado),
    CONSTRAINT fk_contactos_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_contactos_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE SET NULL,
    CONSTRAINT fk_contactos_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_contactos_tipo CHECK (tipo IN ('Contacto general', 'Contacto desde propiedad', 'Solicitud de visita')),
    CONSTRAINT chk_contactos_estado CHECK (estado IN ('Pendiente', 'En atención', 'Atendido', 'Convertido', 'No concretado'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. TABLA: solicitudes_visita
-- Propósito: Coordinación de visitas presenciales con fecha y franja horaria
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS solicitudes_visita;
CREATE TABLE solicitudes_visita (
    id INT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    propiedad_id INT NOT NULL,
    agente_id INT NULL,
    fecha_preferida DATE NOT NULL,
    franja_horaria VARCHAR(50) NULL,
    mensaje_adicional TEXT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_visitas_cliente (cliente_id),
    KEY idx_visitas_propiedad (propiedad_id),
    KEY idx_visitas_agente (agente_id),
    KEY idx_visitas_fecha (fecha_preferida),
    CONSTRAINT fk_visitas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_visitas_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE CASCADE,
    CONSTRAINT fk_visitas_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_visitas_estado CHECK (estado IN ('Pendiente', 'Confirmada', 'Realizada', 'Cancelada', 'No asistió'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. TABLA: operaciones
-- Propósito: Transacciones comerciales de venta inmobiliaria
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS operaciones;
CREATE TABLE operaciones (
    id INT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    propiedad_id INT NOT NULL,
    agente_id INT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'Venta',
    monto DECIMAL(14, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    fecha DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'En negociación',
    observaciones TEXT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operaciones_cliente (cliente_id),
    KEY idx_operaciones_propiedad (propiedad_id),
    KEY idx_operaciones_agente (agente_id),
    KEY idx_operaciones_estado (estado),
    CONSTRAINT fk_operaciones_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_operaciones_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE RESTRICT,
    CONSTRAINT fk_operaciones_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT chk_operaciones_tipo CHECK (tipo IN ('Venta')),
    CONSTRAINT chk_operaciones_monto CHECK (monto >= 0),
    CONSTRAINT chk_operaciones_moneda CHECK (moneda IN ('USD', 'PEN')),
    CONSTRAINT chk_operaciones_estado CHECK (estado IN ('En negociación', 'Reservada', 'Concretada', 'Cancelada'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 11. TABLA: alquileres
-- Propósito: Contratos de arrendamiento y vigencias temporales
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS alquileres;
CREATE TABLE alquileres (
    id INT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    propiedad_id INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    monto_mensual DECIMAL(12, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    condiciones TEXT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alquileres_cliente (cliente_id),
    KEY idx_alquileres_propiedad (propiedad_id),
    KEY idx_alquileres_estado (estado),
    CONSTRAINT fk_alquileres_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alquileres_propiedad FOREIGN KEY (propiedad_id)
        REFERENCES propiedades(id) ON DELETE RESTRICT,
    CONSTRAINT chk_alquileres_monto CHECK (monto_mensual >= 0),
    CONSTRAINT chk_alquileres_moneda CHECK (moneda IN ('USD', 'PEN')),
    CONSTRAINT chk_alquileres_estado CHECK (estado IN ('Activo', 'Finalizado', 'Pendiente')),
    CONSTRAINT chk_alquileres_fechas CHECK (fecha_fin > fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 12. TABLA: seguimientos_contacto
-- Propósito: Bitácora de seguimiento comercial de agentes sobre contactos
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS seguimientos_contacto;
CREATE TABLE seguimientos_contacto (
    id INT NOT NULL AUTO_INCREMENT,
    contacto_id INT NOT NULL,
    agente_id INT NOT NULL,
    nota TEXT NOT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_seguimientos_contacto (contacto_id),
    KEY idx_seguimientos_agente (agente_id),
    CONSTRAINT fk_seguimientos_contacto FOREIGN KEY (contacto_id)
        REFERENCES contactos(id) ON DELETE CASCADE,
    CONSTRAINT fk_seguimientos_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 13. TABLA: comisiones
-- Propósito: Liquidación financiera a los agentes por venta o alquiler
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS comisiones;
CREATE TABLE comisiones (
    id INT NOT NULL AUTO_INCREMENT,
    tipo_operacion VARCHAR(20) NOT NULL,
    operacion_id INT NULL,
    alquiler_id INT NULL,
    agente_id INT NOT NULL,
    monto_base DECIMAL(14, 2) NOT NULL,
    porcentaje DECIMAL(5, 2) NOT NULL,
    monto_comision DECIMAL(14, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    fecha_calculo DATE NOT NULL,
    fecha_pago DATE NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comisiones_operacion (operacion_id),
    KEY idx_comisiones_alquiler (alquiler_id),
    KEY idx_comisiones_agente (agente_id),
    KEY idx_comisiones_estado (estado),
    CONSTRAINT fk_comisiones_operacion FOREIGN KEY (operacion_id)
        REFERENCES operaciones(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comisiones_alquiler FOREIGN KEY (alquiler_id)
        REFERENCES alquileres(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comisiones_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT chk_comisiones_tipo CHECK (tipo_operacion IN ('Venta', 'Alquiler')),
    CONSTRAINT chk_comisiones_base CHECK (monto_base >= 0),
    CONSTRAINT chk_comisiones_pct CHECK (porcentaje >= 0 AND porcentaje <= 100),
    CONSTRAINT chk_comisiones_monto CHECK (monto_comision >= 0),
    CONSTRAINT chk_comisiones_moneda CHECK (moneda IN ('USD', 'PEN')),
    CONSTRAINT chk_comisiones_estado CHECK (estado IN ('Pendiente', 'Pagada', 'Cancelada')),
    CONSTRAINT chk_comisiones_origen CHECK (
        (tipo_operacion = 'Venta' AND operacion_id IS NOT NULL AND alquiler_id IS NULL) OR
        (tipo_operacion = 'Alquiler' AND alquiler_id IS NOT NULL AND operacion_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
