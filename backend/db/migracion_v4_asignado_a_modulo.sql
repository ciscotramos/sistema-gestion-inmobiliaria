-- Migración V4 (Taller Sesión 28): equipos de soporte (entidad Asignado_A) y módulo afectado en tickets.
-- Aplicar UNA sola vez en bases ya creadas con schema_v1.sql / migraciones V2 y V3 ANTES de desplegar esta versión,
-- porque spring.jpa.hibernate.ddl-auto=validate exige que tablas y columnas existan.
-- Es aditiva: no borra ni modifica datos existentes (los tickets previos quedan con módulo 'Otro' y sin equipo).
SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 17. TABLA: asignado_a (catálogo de equipos de soporte)
-- Propósito: Equipo / nivel de soporte responsable de un ticket (Help Desk N1, Especialista DB N2, Desarrollo N3)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asignado_a (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    nivel_soporte TINYINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_asignado_a_nombre (nombre),
    CONSTRAINT chk_asignado_a_nivel CHECK (nivel_soporte BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO asignado_a (id, nombre, nivel_soporte) VALUES
    (1, 'Help Desk', 1),
    (2, 'Especialista DB', 2),
    (3, 'Equipo de Desarrollo', 3);

-- -----------------------------------------------------------------------------
-- tickets: módulo afectado y equipo de soporte asignado
-- -----------------------------------------------------------------------------
ALTER TABLE tickets
    ADD COLUMN modulo_afectado VARCHAR(60) NOT NULL DEFAULT 'Otro' AFTER tipo,
    ADD COLUMN equipo_asignado_id INT NULL AFTER asignado_a,
    ADD KEY idx_tickets_equipo_asignado (equipo_asignado_id),
    ADD CONSTRAINT fk_tickets_equipo_asignado FOREIGN KEY (equipo_asignado_id)
        REFERENCES asignado_a(id) ON DELETE SET NULL;
