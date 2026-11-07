-- Migración V2: columna de versión para el bloqueo optimista (@Version en Propiedad).
-- Aplicar en bases ya creadas con schema_v1.sql ANTES de desplegar esta versión,
-- porque spring.jpa.hibernate.ddl-auto=validate exige que la columna exista.
ALTER TABLE propiedades
    ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER activo;
