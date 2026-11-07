-- =============================================================================
-- PROYECTO: Sistema de Gestión Inmobiliaria
-- ARCHIVO: backend/data.sql
-- COMPATIBILIDAD: schema_v1.sql (13 Tablas normalizadas en InnoDB / UTF8MB4)
--
-- ORDEN DE INSERCIÓN RIGUROSO POR DEPENDENCIAS DE LLAVES FORÁNEAS (FK):
-- 1. usuarios                  (Nivel 0 - Independiente)
-- 2. ubicaciones_distritos     (Nivel 0 - Independiente)
-- 3. caracteristicas           (Nivel 0 - Independiente)
-- 4. propiedades               (Nivel 1 - FK a ubicaciones_distritos, usuarios)
-- 5. propiedad_imagenes        (Nivel 2 - FK a propiedades)
-- 6. propiedad_caracteristicas (Nivel 2 - FK a propiedades, caracteristicas)
-- 7. clientes                  (Nivel 2 - FK a propiedades, usuarios)
-- 8. contactos                 (Nivel 3 - FK a clientes, propiedades, usuarios)
-- 9. solicitudes_visita        (Nivel 3 - FK a clientes, propiedades, usuarios)
-- 10. operaciones              (Nivel 3 - FK a clientes, propiedades, usuarios)
-- 11. alquileres               (Nivel 3 - FK a clientes, propiedades)
-- 12. seguimientos_contacto    (Nivel 4 - FK a contactos, usuarios)
-- 13. comisiones               (Nivel 4 - FK a operaciones, alquileres, usuarios)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. TABLA: usuarios
-- Roles válidos: 'ADMIN', 'AGENTE'
-- Contraseña de prueba almacenada como hash BCrypt (Rounds: 10)
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (id, nombre, email, password_hash, rol, telefono, cargo, foto_url, estado) VALUES
(1, 'Luis Administrador', 'admin@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'ADMIN', '+51 999 888 777', 'Administrador General', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80', 'Activo'),
(2, 'Juan Carlos Pérez', 'juan.perez@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'AGENTE', '+51 987 654 321', 'Asesor Comercial Senior', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80', 'Activo'),
(3, 'Carlos Mendoza', 'carlos.mendoza@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'AGENTE', '+51 912 345 678', 'Especialista Residencial', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80', 'Activo'),
(4, 'María Sánchez', 'maria.sanchez@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'AGENTE', '+51 923 456 789', 'Consultora Inmobiliaria', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80', 'Activo'),
(5, 'Ana Torres', 'ana.torres@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'AGENTE', '+51 934 567 890', 'Asesora Corporativa', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80', 'Activo'),
(6, 'Roberto Gómez', 'roberto.gomez@inmobiliaria.com', '$2a$10$fSoXx0TYvKNsaA9htPcusOkK4m.r/53QaywDaaABoTj66moP9CZxe', 'AGENTE', '+51 945 678 901', 'Asesor Junior', 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=300&q=80', 'Inactivo');

-- -----------------------------------------------------------------------------
-- 2. TABLA: ubicaciones_distritos
-- Distritos metropolitanos normalizados (Lima Metropolitana)
-- -----------------------------------------------------------------------------
INSERT INTO ubicaciones_distritos (id, nombre, provincia, departamento) VALUES
(1, 'San Miguel', 'Lima', 'Lima'),
(2, 'Miraflores', 'Lima', 'Lima'),
(3, 'San Isidro', 'Lima', 'Lima'),
(4, 'Santiago de Surco', 'Lima', 'Lima'),
(5, 'San Borja', 'Lima', 'Lima'),
(6, 'Barranco', 'Lima', 'Lima'),
(7, 'La Molina', 'Lima', 'Lima'),
(8, 'Jesús María', 'Lima', 'Lima'),
(9, 'Magdalena del Mar', 'Lima', 'Lima'),
(10, 'Lince', 'Lima', 'Lima'),
(11, 'Pueblo Libre', 'Lima', 'Lima'),
(12, 'Los Olivos', 'Lima', 'Lima');

-- -----------------------------------------------------------------------------
-- 3. TABLA: caracteristicas
-- Catálogo de amenidades y comodidades de inmuebles
-- -----------------------------------------------------------------------------
INSERT INTO caracteristicas (id, nombre, categoria) VALUES
(1, 'Piscina', 'Exteriores'),
(2, 'Estacionamiento techado', 'General'),
(3, 'Seguridad 24/7 y CCTV', 'Seguridad'),
(4, 'Ascensor directo', 'Comodidades'),
(5, 'Terraza con zona BBQ', 'Exteriores'),
(6, 'Balcón exterior', 'Exteriores'),
(7, 'Gimnasio equipado', 'Comodidades'),
(8, 'Jardín y áreas verdes', 'Exteriores'),
(9, 'Salón de Usos Múltiples (SUM)', 'Comodidades'),
(10, 'Pet Friendly', 'General'),
(11, 'Vista al mar / Malecón', 'Exteriores'),
(12, 'Depósito independiente', 'Comodidades'),
(13, 'Cocina integral equipada', 'Interiores'),
(14, 'Aire acondicionado central', 'Interiores'),
(15, 'Lobby y conserjería', 'General');

-- -----------------------------------------------------------------------------
-- 4. TABLA: propiedades
-- Inmuebles residenciales, corporativos y comerciales
-- Operaciones: 'Venta', 'Alquiler'
-- Tipos: 'Casa', 'Departamento', 'Terreno', 'Oficina', 'Local Comercial'
-- Estados: 'Disponible', 'Reservado', 'Vendido', 'Alquilado', 'Inactivo'
-- -----------------------------------------------------------------------------
INSERT INTO propiedades (id, titulo, descripcion, operacion, tipo, precio, moneda, dormitorios, banos, area_construida, area_total, direccion, distrito_id, agente_id, estado, destacada, activo) VALUES
(1, 'Casa Familiar con Amplio Jardín en San Miguel', 'Hermosa y espaciosa casa de 2 pisos, excelente iluminación natural, zona tranquila cerca a parques y centros comerciales.', 'Alquiler', 'Casa', 1500.00, 'USD', 4, 2, 150.00, 200.00, 'Av. Las Palmeras 450, San Miguel', 1, 2, 'Disponible', 1, 1),
(2, 'Penthouse Exclusivo con Vista Panorámica en Miraflores', 'Lujoso penthouse dúplex con acabados de primera, terraza privada con jacuzzi, ascensor directo y vista directa al mar.', 'Venta', 'Departamento', 485000.00, 'USD', 3, 4, 240.00, 310.00, 'Malecón de la Reserva 780, Miraflores', 2, 2, 'Disponible', 1, 1),
(3, 'Moderna Residencia con Piscina y Jardín en La Molina', 'Impresionante casa de estilo contemporáneo en condominio cerrado con máxima seguridad, piscina templada y acabados en mármol.', 'Venta', 'Casa', 620000.00, 'USD', 5, 5, 380.00, 550.00, 'Calle Las Acacias 230, Rinconada Alta', 7, 3, 'Disponible', 1, 1),
(4, 'Flat de Estreno en el Corazón Financiero de San Isidro', 'Departamento elegante de 2 dormitorios, cocina integrada con tablero de cuarzo, balcón y cochera subterránea en edificio eco-sostenible.', 'Alquiler', 'Departamento', 1250.00, 'USD', 2, 2, 88.00, 88.00, 'Av. Juan de Arona 540, San Isidro', 3, 4, 'Disponible', 0, 1),
(5, 'Oficina Corporativa Prime A+ en Centro Empresarial San Isidro', 'Piso completo de oficinas con certificación LEED, control de accesos biométrico, sistema contra incendios y 6 estacionamientos asignados.', 'Alquiler', 'Oficina', 4200.00, 'USD', NULL, 4, 280.00, 280.00, 'Av. Rivera Navarrete 600, Piso 12', 3, 5, 'Disponible', 1, 1),
(6, 'Terreno Residencial en Urbanización Privada en Surco', 'Excelente terreno plano ideal para proyecto unifamiliar o edificio de baja densidad, zonificación RDB, cerca a colegios de prestigio.', 'Venta', 'Terreno', 310000.00, 'USD', NULL, NULL, NULL, 350.00, 'Jr. Monte Grande 160, Chacarilla', 4, 3, 'Disponible', 0, 1),
(7, 'Departamento Familiar frente a Parque en San Borja', 'Acogedor departamento en segundo piso con ascensor directo, sala comedor luminosa, cocina amplia y depósito. Ubicación inmejorable.', 'Venta', 'Departamento', 225000.00, 'USD', 3, 2, 115.00, 115.00, 'Av. San Borja Norte 890, San Borja', 5, 4, 'Reservado', 0, 1),
(8, 'Loft Bohemio con Estilo Industrial en Barranco', 'Moderno loft de doble altura, techos altos, muros de ladrillo expuesto, terraza con vista urbana, a pasos del Puente de los Suspiros.', 'Alquiler', 'Departamento', 3400.00, 'PEN', 1, 1, 65.00, 75.00, 'Jr. Pedro de Osma 215, Barranco', 6, 2, 'Disponible', 1, 1),
(9, 'Local Comercial de Alto Tránsito en Av. Brasil, Jesús María', 'Local a pie de calle con 8 metros de frente comercial, zonificación comercial vecinal, ideal para entidad bancaria, farmacia o franquicia.', 'Alquiler', 'Local Comercial', 7500.00, 'PEN', NULL, 2, 180.00, 180.00, 'Av. Brasil 1420, Jesús María', 8, 5, 'Alquilado', 0, 1),
(10, 'Casa de 2 Niveles para Vivienda o Comercio en Magdalena', 'Propiedad con gran potencial, amplios ambientes, patio posterior, estacionamiento para 2 autos, a 3 cuadras del Malecón Castagnola.', 'Venta', 'Casa', 260000.00, 'USD', 4, 3, 210.00, 250.00, 'Jr. Bolognesi 630, Magdalena del Mar', 9, 3, 'Vendido', 0, 1),
(11, 'Departamento Iluminado con Balcón en Lince Límite San Isidro', 'Moderno flat ubicado estratégicamente a una cuadra de Av. Dos de Mayo, edificio nuevo con áreas comunes: piscina, parrilla y coworking.', 'Alquiler', 'Departamento', 2800.00, 'PEN', 2, 2, 72.00, 72.00, 'Jr. Los Mirtos 310, Lince', 10, 4, 'Disponible', 0, 1),
(12, 'Terreno Urbano con Excelente Frente Comercial en Los Olivos', 'Terreno comercial e industrial ligero, cercado perimétrico con portón de acceso para camiones, servicios completos de agua y luz trifásica.', 'Venta', 'Terreno', 340000.00, 'USD', NULL, NULL, NULL, 420.00, 'Av. Carlos Izaguirre 1180, Los Olivos', 12, 3, 'Disponible', 0, 1);

-- -----------------------------------------------------------------------------
-- 5. TABLA: propiedad_imagenes
-- Galería fotográfica con indicación de fotografía principal
-- -----------------------------------------------------------------------------
INSERT INTO propiedad_imagenes (id, propiedad_id, url, orden, es_principal) VALUES
-- Propiedad 1: Casa San Miguel
(1, 1, 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=800&q=80', 1, 1),
(2, 1, 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 2: Penthouse Miraflores
(3, 2, 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80', 1, 1),
(4, 2, 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80', 2, 0),
(5, 2, 'https://images.unsplash.com/photo-1600566753376-12c8ab7fb75b?auto=format&fit=crop&w=800&q=80', 3, 0),
-- Propiedad 3: Casa La Molina
(6, 3, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80', 1, 1),
(7, 3, 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80', 2, 0),
(8, 3, 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80', 3, 0),
-- Propiedad 4: Flat San Isidro
(9, 4, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80', 1, 1),
(10, 4, 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 5: Oficina Corporativa San Isidro
(11, 5, 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80', 1, 1),
(12, 5, 'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 6: Terreno Surco
(13, 6, 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=800&q=80', 1, 1),
-- Propiedad 7: Depa San Borja
(14, 7, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80', 1, 1),
(15, 7, 'https://images.unsplash.com/photo-1502005229762-ae1b464a51e5?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 8: Loft Barranco
(16, 8, 'https://images.unsplash.com/photo-1536376072261-38c75010e6c9?auto=format&fit=crop&w=800&q=80', 1, 1),
(17, 8, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 9: Local Comercial Jesús María
(18, 9, 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80', 1, 1),
-- Propiedad 10: Casa Magdalena
(19, 10, 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?auto=format&fit=crop&w=800&q=80', 1, 1),
(20, 10, 'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 11: Depa Lince
(21, 11, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=800&q=80', 1, 1),
(22, 11, 'https://images.unsplash.com/photo-1507089947368-19c1da9775ae?auto=format&fit=crop&w=800&q=80', 2, 0),
-- Propiedad 12: Terreno Los Olivos
(23, 12, 'https://images.unsplash.com/photo-1524813686514-a57563d77d61?auto=format&fit=crop&w=800&q=80', 1, 1);

-- -----------------------------------------------------------------------------
-- 6. TABLA: propiedad_caracteristicas (Relación N:M)
-- -----------------------------------------------------------------------------
INSERT INTO propiedad_caracteristicas (propiedad_id, caracteristica_id) VALUES
-- Propiedad 1: Casa San Miguel
(1, 2), (1, 8), (1, 10),
-- Propiedad 2: Penthouse Miraflores
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 7), (2, 11), (2, 13),
-- Propiedad 3: Casa La Molina
(3, 1), (3, 2), (3, 3), (3, 5), (3, 8), (3, 10), (3, 14),
-- Propiedad 4: Flat San Isidro
(4, 2), (4, 3), (4, 4), (4, 6), (4, 13), (4, 15),
-- Propiedad 5: Oficina San Isidro
(5, 2), (5, 3), (5, 4), (5, 12), (5, 14), (5, 15),
-- Propiedad 6: Terreno Surco
(6, 3),
-- Propiedad 7: Depa San Borja
(7, 2), (7, 4), (7, 8), (7, 9), (7, 12),
-- Propiedad 8: Loft Barranco
(8, 5), (8, 10), (8, 13),
-- Propiedad 9: Local Jesús María
(9, 3), (9, 14),
-- Propiedad 10: Casa Magdalena
(10, 2), (10, 6), (10, 8),
-- Propiedad 11: Depa Lince
(11, 1), (11, 2), (11, 3), (11, 4), (11, 5), (11, 6), (11, 7), (11, 9),
-- Propiedad 12: Terreno Los Olivos
(12, 3);

-- -----------------------------------------------------------------------------
-- 7. TABLA: clientes
-- Directorio de compradores, arrendatarios e interesados
-- Tipos: 'Arrendatario', 'Propietario', 'Comprador', 'Interesado'
-- Orígenes: 'Registro manual', 'Contacto desde propiedad', 'Contacto general', 'Solicitud de visita'
-- -----------------------------------------------------------------------------
INSERT INTO clientes (id, nombre, apellido, email, telefono, tipo, origen, propiedad_interes_id, agente_id, estado) VALUES
(1, 'Alejandro', 'Morales Benavides', 'amorales@empresa.pe', '+51 981 112 233', 'Comprador', 'Contacto desde propiedad', 2, 2, 'Activo'),
(2, 'Claudia', 'Vargas Llosa', 'claudia.vargas@gmail.com', '+51 982 223 344', 'Arrendatario', 'Contacto desde propiedad', 4, 4, 'Activo'),
(3, 'Fernando', 'Castillo Rivas', 'fcastillo@consulting.com', '+51 983 334 455', 'Comprador', 'Solicitud de visita', 3, 3, 'Activo'),
(4, 'Patricia', 'Quispe Navarro', 'patricia.quispe@outlook.com', '+51 984 445 566', 'Comprador', 'Contacto desde propiedad', 7, 4, 'Activo'),
(5, 'Corporación Inmobiliaria', 'Retail SAC', 'contacto@retailsac.pe', '+51 985 556 677', 'Arrendatario', 'Registro manual', 9, 5, 'Activo'),
(6, 'Gustavo', 'Alvarado Ruiz', 'gustavo.alvarado@hotmail.com', '+51 986 667 788', 'Comprador', 'Registro manual', 10, 3, 'Activo'),
(7, 'Lucía', 'Fernández Prado', 'lucia.fp@gmail.com', '+51 987 778 899', 'Arrendatario', 'Solicitud de visita', 8, 2, 'Activo'),
(8, 'Jorge', 'Herrera Salazar', 'jorge.herrera@inversiones.pe', '+51 988 889 900', 'Interesado', 'Contacto general', 6, 3, 'Activo');

-- -----------------------------------------------------------------------------
-- 8. TABLA: contactos
-- Mensajes y consultas recibidas a través de la plataforma web
-- Tipos: 'Contacto general', 'Contacto desde propiedad', 'Solicitud de visita'
-- Estados: 'Pendiente', 'En atención', 'Atendido', 'Convertido', 'No concretado'
-- -----------------------------------------------------------------------------
INSERT INTO contactos (id, cliente_id, propiedad_id, agente_id, tipo, mensaje, estado) VALUES
(1, 1, 2, 2, 'Contacto desde propiedad', 'Buenas tardes, deseo información detallada sobre el penthouse en Miraflores y disponibilidad para visita presencial.', 'Convertido'),
(2, 2, 4, 4, 'Contacto desde propiedad', 'Hola, me interesa alquilar el departamento en San Isidro por un periodo mínimo de 1 año. ¿Incluye mantenimiento?', 'Atendido'),
(3, 3, 3, 3, 'Solicitud de visita', 'Quisiera coordinar una visita presencial para conocer la casa en La Molina junto con mi familia este fin de semana.', 'En atención'),
(4, 4, 7, 4, 'Contacto desde propiedad', 'Solicito proforma formal y facilidades de financiamiento bancario con crédito hipotecario para el departamento en San Borja.', 'Convertido'),
(5, 5, 9, 5, 'Contacto desde propiedad', 'Requerimos evaluar el contrato comercial y planos sanitarios del local de Av. Brasil para instalación de sucursal bancaria.', 'Convertido'),
(6, 6, 10, 3, 'Contacto general', 'Deseo conocer el estado legal y cargas registrales de la propiedad en Magdalena antes de formalizar la compra.', 'Convertido'),
(7, 7, 8, 2, 'Solicitud de visita', 'Me encanta el diseño industrial del loft en Barranco. ¿Es posible visitarlo en horas de la tarde a partir de las 5pm?', 'Pendiente'),
(8, 8, 6, 3, 'Contacto general', 'Consulta sobre parámetros urbanísticos del terreno en Chacarilla para anteproyecto residencial unifamiliar.', 'En atención');

-- -----------------------------------------------------------------------------
-- 9. TABLA: solicitudes_visita
-- Agendamiento de visitas presenciales con horario preferido
-- Estados: 'Pendiente', 'Confirmada', 'Realizada', 'Cancelada', 'No asistió'
-- -----------------------------------------------------------------------------
INSERT INTO solicitudes_visita (id, cliente_id, propiedad_id, agente_id, fecha_preferida, franja_horaria, mensaje_adicional, estado) VALUES
(1, 1, 2, 2, '2026-10-10', '10:00 - 12:00', 'Visita con arquitecto para evaluar factibilidad de reformas interiores.', 'Realizada'),
(2, 2, 4, 4, '2026-10-12', '16:00 - 18:00', 'Verificación de cocheras subterráneas y áreas comunes del edificio.', 'Confirmada'),
(3, 3, 3, 3, '2026-10-15', '11:00 - 13:00', 'Visita familiar para inspeccionar piscina temperada y jardín privado.', 'Confirmada'),
(4, 4, 7, 4, '2026-10-05', '14:00 - 16:00', 'Inspección técnica de instalaciones antes de proceder a la firma de minuta.', 'Realizada'),
(5, 6, 10, 3, '2026-09-28', '10:30 - 12:00', 'Visita programada con perito tasador del banco para crédito hipotecario.', 'Realizada'),
(6, 7, 8, 2, '2026-10-18', '17:00 - 18:30', 'Coordinar con conserje de turno para ingreso por Jr. Pedro de Osma.', 'Pendiente');

-- -----------------------------------------------------------------------------
-- 10. TABLA: operaciones
-- Transacciones comerciales de compraventa de inmuebles
-- Tipo: 'Venta'
-- Estados: 'En negociación', 'Reservada', 'Concretada', 'Cancelada'
-- -----------------------------------------------------------------------------
INSERT INTO operaciones (id, cliente_id, propiedad_id, agente_id, tipo, monto, moneda, fecha, estado, observaciones) VALUES
(1, 6, 10, 3, 'Venta', 255000.00, 'USD', '2026-09-30', 'Concretada', 'Venta finalizada exitosamente mediante crédito hipotecario BCP. Escritura pública firmada en Notaría Tambini.'),
(2, 4, 7, 4, 'Venta', 220000.00, 'USD', '2026-10-02', 'Reservada', 'Separación cancelada con arras de USD 10,000. Evaluación de crédito aprobada por BBVA.'),
(3, 1, 2, 2, 'Venta', 470000.00, 'USD', '2026-10-04', 'En negociación', 'Oferta formal presentada por el cliente. En revisión de términos finales de desembolso con el propietario.');

-- -----------------------------------------------------------------------------
-- 11. TABLA: alquileres
-- Contratos de arrendamiento con vigencias temporales
-- Monedas: 'USD', 'PEN'
-- Estados: 'Activo', 'Finalizado', 'Pendiente'
-- Restricción: fecha_fin > fecha_inicio
-- -----------------------------------------------------------------------------
INSERT INTO alquileres (id, cliente_id, propiedad_id, fecha_inicio, fecha_fin, monto_mensual, moneda, estado, condiciones) VALUES
(1, 5, 9, '2026-09-01', '2029-08-31', 7500.00, 'PEN', 'Activo', 'Contrato comercial por 3 años forzosos. 2 meses de garantía y 1 de adelanto. Pago mensual por transferencia bancaria.'),
(2, 2, 4, '2026-10-15', '2027-10-14', 1250.00, 'USD', 'Pendiente', 'Contrato residencial de 1 año renovable. Se permite mascota pequeña. Mantenimiento no incluido en la renta.'),
(3, 7, 1, '2025-09-01', '2026-08-31', 1500.00, 'USD', 'Finalizado', 'Arrendamiento culminado a término regular sin contingencias de mantenimiento ni penalidades.');

-- -----------------------------------------------------------------------------
-- 12. TABLA: seguimientos_contacto
-- Bitácora de seguimiento comercial de agentes sobre consultas de clientes
-- -----------------------------------------------------------------------------
INSERT INTO seguimientos_contacto (id, contacto_id, agente_id, nota) VALUES
(1, 1, 2, 'Primera llamada telefónica realizada. Cliente calificado con alto poder adquisitivo. Se enviaron fotos y planos por WhatsApp.'),
(2, 1, 2, 'Visita presencial concretada satisfactoriamente. Cliente presentó propuesta formal de compra por USD 470,000.'),
(3, 2, 4, 'Cliente consultó detalles del mantenimiento del edificio (S/ 420 mensuales). Se coordinó firma de contrato para quincena de octubre.'),
(4, 3, 3, 'Llamada de confirmación de visita con el Sr. Fernando Castillo para inspección familiar de la casa en La Molina.'),
(5, 4, 4, 'Cliente firmó contrato de arras y abonó separación de USD 10,000. En espera de desembolso hipotecario BBVA.'),
(6, 5, 5, 'Representante legal de Retail SAC entregó vigencia de poder y RUC. Contrato comercial por 3 años formalizado en Notaría.');

-- -----------------------------------------------------------------------------
-- 13. TABLA: comisiones
-- Liquidaciones financieras generadas para los asesores inmobiliarios
-- Tipos: 'Venta', 'Alquiler'
-- Restricción de origen:
-- (tipo_operacion = 'Venta' AND operacion_id IS NOT NULL AND alquiler_id IS NULL) OR
-- (tipo_operacion = 'Alquiler' AND alquiler_id IS NOT NULL AND operacion_id IS NULL)
-- -----------------------------------------------------------------------------
INSERT INTO comisiones (id, tipo_operacion, operacion_id, alquiler_id, agente_id, monto_base, porcentaje, monto_comision, moneda, estado, fecha_calculo, fecha_pago) VALUES
(1, 'Venta', 1, NULL, 3, 255000.00, 3.00, 7650.00, 'USD', 'Pagada', '2026-09-30', '2026-10-02'),
(2, 'Venta', 2, NULL, 4, 220000.00, 3.00, 6600.00, 'USD', 'Pendiente', '2026-10-02', NULL),
(3, 'Alquiler', NULL, 1, 5, 7500.00, 100.00, 7500.00, 'PEN', 'Pagada', '2026-09-02', '2026-09-05'),
(4, 'Alquiler', NULL, 2, 4, 1250.00, 100.00, 1250.00, 'USD', 'Pendiente', '2026-10-04', NULL);
