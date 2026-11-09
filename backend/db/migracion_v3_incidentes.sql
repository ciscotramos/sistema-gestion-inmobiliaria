-- Migración V3: módulo de gestión de incidentes (tablas prioridades, estados_ticket y tickets).
-- Aplicar en bases ya creadas con schema_v1.sql / migración V2 ANTES de desplegar esta versión,
-- porque spring.jpa.hibernate.ddl-auto=validate exige que las tablas existan.
-- Es aditiva: no modifica ni borra tablas existentes.

-- -----------------------------------------------------------------------------
-- 14. TABLA: prioridades (catálogo)
-- Propósito: Niveles de prioridad ITIL (impacto x urgencia) y su tiempo SLA de resolución
-- -----------------------------------------------------------------------------
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS prioridades (
    id INT NOT NULL AUTO_INCREMENT,
    nivel VARCHAR(2) NOT NULL,
    nombre VARCHAR(30) NOT NULL,
    tiempo_sla_horas INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_prioridades_nivel (nivel),
    CONSTRAINT chk_prioridades_sla CHECK (tiempo_sla_horas > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO prioridades (id, nivel, nombre, tiempo_sla_horas) VALUES
    (1, 'P1', 'Crítico', 2),
    (2, 'P2', 'Alto', 8),
    (3, 'P3', 'Normal', 24),
    (4, 'P4', 'Bajo', 72);

-- -----------------------------------------------------------------------------
-- 15. TABLA: estados_ticket (catálogo)
-- Propósito: Ciclo de vida del ticket: Abierto -> En Proceso -> Resuelto -> Cerrado
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estados_ticket (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_estados_ticket_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO estados_ticket (id, nombre) VALUES
    (1, 'Abierto'),
    (2, 'En Proceso'),
    (3, 'Resuelto'),
    (4, 'Cerrado');

-- -----------------------------------------------------------------------------
-- 16. TABLA: tickets
-- Propósito: Incidentes y solicitudes de servicio reportados por los usuarios
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id INT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    tipo VARCHAR(60) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    impacto VARCHAR(10) NOT NULL,
    urgencia VARCHAR(10) NOT NULL,
    prioridad_id INT NOT NULL,
    estado_id INT NOT NULL DEFAULT 1,
    reportado_por INT NOT NULL,
    asignado_a INT NULL,
    solucion TEXT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_sla DATETIME NOT NULL,
    fecha_resolucion DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tickets_codigo (codigo),
    KEY idx_tickets_estado (estado_id),
    KEY idx_tickets_prioridad (prioridad_id),
    KEY idx_tickets_reportado_por (reportado_por),
    KEY idx_tickets_asignado_a (asignado_a),
    CONSTRAINT fk_tickets_prioridad FOREIGN KEY (prioridad_id)
        REFERENCES prioridades(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_estado FOREIGN KEY (estado_id)
        REFERENCES estados_ticket(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_reportado_por FOREIGN KEY (reportado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_asignado_a FOREIGN KEY (asignado_a)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT chk_tickets_impacto CHECK (impacto IN ('Alto', 'Medio', 'Bajo')),
    CONSTRAINT chk_tickets_urgencia CHECK (urgencia IN ('Alta', 'Media', 'Baja'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
