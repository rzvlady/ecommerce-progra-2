-- Datos de prueba para desarrollo local.
-- Requiere haber corrido antes db/schema-ecommerce.sql.
--
-- Uso:
--   psql -U <usuario> -h localhost -d <base_de_datos> -f db/seed-data.sql
--
-- Re-ejecutable: al inicio vacía las tablas sembradas por este script antes
-- de volver a insertar, para poder reiniciar los datos de prueba las veces
-- que haga falta sin dejar residuos de una corrida anterior.
--
-- Password de los usuarios sembrados: "password" (hash BCrypt de referencia de
-- Spring Security). Es solo para desarrollo local, no usar en ningún ambiente real.

BEGIN;

TRUNCATE TABLE
  core.movimientos_inventario, core.envios, core.pagos, core.detalles_pedido, core.pedidos,
  core.productos, core.categorias,
  seguridad.usuarios_direccion, seguridad.usuarios_contacto, seguridad.usuarios_perfil, seguridad.usuarios,
  catalogo.marcas, catalogo.municipios, catalogo.departamentos, catalogo.paises
RESTART IDENTITY CASCADE;

-- ==========================================================================
-- catalogo
-- ==========================================================================

INSERT INTO catalogo.paises (id_pais, nombre_pais) VALUES
  (1, 'El Salvador');

INSERT INTO catalogo.departamentos (id_departamento, id_pais, nombre_departamento) VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'La Libertad'),
  (3, 1, 'Santa Ana'),
  (4, 1, 'San Miguel');

INSERT INTO catalogo.municipios (id_municipio, id_departamento, nombre_municipio) VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'Soyapango'),
  (3, 1, 'Mejicanos'),
  (4, 1, 'Apopa'),
  (5, 2, 'Santa Tecla'),
  (6, 2, 'Antiguo Cuscatlán'),
  (7, 3, 'Santa Ana'),
  (8, 4, 'San Miguel');

INSERT INTO catalogo.marcas (id_marca, nombre_marca) VALUES
  (1, 'Samsung'),
  (2, 'LG'),
  (3, 'Sony'),
  (4, 'HP'),
  (5, 'Logitech'),
  (6, 'Xiaomi'),
  (7, 'Apple'),
  (8, 'Dell'),
  (9, 'Lenovo'),
  (10, 'JBL'),
  (11, 'Philips'),
  (12, 'Whirlpool'),
  (13, 'Nintendo');

-- ==========================================================================
-- seguridad
-- ==========================================================================

INSERT INTO seguridad.usuarios (id_usuario, correo_inicio_sesion, contrasenia, eliminado, estado) VALUES
  (1, 'ana.martinez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (2, 'carlos.lopez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (3, 'maria.hernandez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (4, 'jose.ramirez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (5, 'lucia.flores@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (6, 'pedro.gomez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (7, 'sofia.rivas@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (8, 'diego.cruz@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (9, 'valeria.mejia@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (10, 'andres.pineda@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (11, 'camila.escobar@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (12, 'ricardo.alas@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', true, 'I');

INSERT INTO seguridad.usuarios_perfil (id_perfil, id_usuario, nombres, apellidos, dui, genero, fecha_nacimiento) VALUES
  (1, 1, 'Ana', 'Martínez', '00000001-1', 'F', '1993-08-17'),
  (2, 2, 'Carlos', 'López', '00000002-2', 'M', '2005-04-19'),
  (3, 3, 'María', 'Hernández', '00000003-3', 'F', '1972-06-03'),
  (4, 4, 'José', 'Ramírez', '00000004-4', 'M', '1978-12-19'),
  (5, 5, 'Lucía', 'Flores', '00000005-5', 'F', '1976-02-22'),
  (6, 6, 'Pedro', 'Gómez', '00000006-6', 'M', '1973-07-24'),
  (7, 7, 'Sofía', 'Rivas', '00000007-7', 'F', '1997-03-09'),
  (8, 8, 'Diego', 'Cruz', '00000008-8', 'M', '1976-01-21'),
  (9, 9, 'Valeria', 'Mejía', '00000009-9', 'F', '2001-08-07'),
  (10, 10, 'Andrés', 'Pineda', '00000010-0', 'M', '1999-05-14'),
  (11, 11, 'Camila', 'Escobar', '00000011-1', 'F', '2004-01-24'),
  (12, 12, 'Ricardo', 'Alas', '00000012-2', 'M', '2002-02-28');

INSERT INTO seguridad.usuarios_contacto (id_contacto, id_usuario, tipo_contacto, valor, principal, verificado) VALUES
  (1, 1, 1, '7067-2982', true, true),
  (2, 1, 2, 'ana.martinez@example.com', false, true),
  (3, 2, 1, '7038-1117', true, true),
  (4, 2, 2, 'carlos.lopez@example.com', false, true),
  (5, 3, 1, '7075-4899', true, true),
  (6, 3, 2, 'maria.hernandez@example.com', false, true),
  (7, 4, 1, '7083-8744', true, true),
  (8, 4, 2, 'jose.ramirez@example.com', false, true),
  (9, 5, 1, '7065-6804', true, true),
  (10, 6, 1, '7053-2790', true, true),
  (11, 6, 2, 'pedro.gomez@example.com', false, true),
  (12, 7, 1, '7069-5092', true, true),
  (13, 8, 1, '7079-1241', true, true),
  (14, 9, 1, '7061-1960', true, true),
  (15, 9, 2, 'valeria.mejia@example.com', false, true),
  (16, 10, 1, '7099-8973', true, true),
  (17, 10, 2, 'andres.pineda@example.com', false, true),
  (18, 11, 1, '7050-1936', true, true),
  (19, 11, 2, 'camila.escobar@example.com', false, true),
  (20, 12, 1, '7033-2122', true, true);

INSERT INTO seguridad.usuarios_direccion (id_direccion, id_usuario, id_municipio, direccion_linea_1, codigo_postal, tipo_direccion) VALUES
  (1, 1, 2, 'Dirección de prueba #1, Col. Escalón', 1002, 'T'),
  (2, 2, 1, 'Dirección de prueba #2, Col. Escalón', 1001, 'C'),
  (3, 3, 8, 'Dirección de prueba #3, Res. El Trébol', 1008, 'C'),
  (4, 4, 8, 'Dirección de prueba #4, Res. Las Palmeras', 1008, 'T'),
  (5, 5, 8, 'Dirección de prueba #5, Col. Escalón', 1008, 'C'),
  (6, 6, 4, 'Dirección de prueba #6, Res. Las Palmeras', 1004, 'T'),
  (7, 7, 2, 'Dirección de prueba #7, Res. El Trébol', 1002, 'T'),
  (8, 8, 4, 'Dirección de prueba #8, Col. Miramonte', 1004, 'C'),
  (9, 9, 1, 'Dirección de prueba #9, Barrio San José', 1001, 'T'),
  (10, 10, 5, 'Dirección de prueba #10, Col. Escalón', 1005, 'C'),
  (11, 11, 8, 'Dirección de prueba #11, Col. Escalón', 1008, 'C'),
  (12, 12, 4, 'Dirección de prueba #12, Col. Escalón', 1004, 'T');

-- ==========================================================================
-- core: catálogo de productos
-- ==========================================================================

INSERT INTO core.categorias (id_categoria, id_categoria_padre, nombre_categoria, descripccion) VALUES
  (1, NULL, 'Electrónica', 'Categoría raíz de dispositivos electrónicos'),
  (2, 1, 'Computación', 'Laptops, monitores y accesorios de cómputo'),
  (3, 1, 'Celulares', 'Teléfonos, tablets y accesorios móviles'),
  (4, 1, 'Audio', 'Audífonos, parlantes y equipos de sonido'),
  (5, 1, 'Televisores', 'Smart TVs y accesorios de video'),
  (6, 1, 'Gaming', 'Consolas, controles y accesorios de videojuegos'),
  (7, NULL, 'Hogar', 'Categoría raíz de artículos para el hogar'),
  (8, 7, 'Electrodomésticos', 'Línea blanca y pequeños electrodomésticos'),
  (9, 7, 'Cuidado Personal', 'Equipos de cuidado e higiene personal'),
  (10, NULL, 'Accesorios', 'Accesorios transversales: cables, cargadores, fundas');

-- stock_disponible y stock_minimo quedan variados a propósito (agotados,
-- críticos y saludables) para poder probar la alerta de stock bajo con casos reales.
INSERT INTO core.productos (id_producto, id_categoria, id_marca, nombre_producto, descripcion_producto, stock_disponible, stock_minimo, estado) VALUES
  (1, 2, 4, 'Laptop HP ThinkPad', 'Laptop 15", i5, 8GB RAM, 1024GB SSD', 5, 11, 'A'),
  (2, 2, 8, 'Laptop Dell Pavilion', 'Laptop 15", i7, 8GB RAM, 1024GB SSD', 106, 13, 'A'),
  (3, 2, 9, 'Laptop Lenovo Inspiron', 'Laptop 15", i5, 8GB RAM, 256GB SSD', 59, 13, 'A'),
  (4, 2, 7, 'Laptop Apple MacBook Air', 'Laptop 15", M2, 8GB RAM, 1024GB SSD', 27, 15, 'A'),
  (5, 2, 1, 'Monitor Samsung 27" QHD', 'Monitor curvo 27 pulgadas, 2560x1440, 144Hz', 3, 5, 'A'),
  (6, 2, 5, 'Mouse inalámbrico Logitech MX Master', 'Mouse ergonómico inalámbrico, múltiples dispositivos', 36, 4, 'A'),
  (7, 2, 5, 'Teclado mecánico Logitech G413', 'Teclado mecánico retroiluminado, switches táctiles', 108, 12, 'A'),
  (8, 2, 4, 'Impresora HP DeskJet 2720', 'Impresora multifuncional a color con WiFi', 8, 15, 'A'),
  (9, 3, 1, 'Smartphone Samsung Galaxy A54', 'Smartphone 128GB, cámara múltiple, 5G', 8, 15, 'A'),
  (10, 3, 7, 'Smartphone Apple iPhone 13', 'Smartphone 128GB, cámara múltiple, 5G', 0, 5, 'A'),
  (11, 3, 6, 'Smartphone Xiaomi Redmi Note 13', 'Smartphone 256GB, cámara múltiple, 5G', 9, 13, 'A'),
  (12, 3, 2, 'Smartphone LG Wing', 'Smartphone 128GB, cámara múltiple, 5G', 7, 14, 'A'),
  (13, 3, 1, 'Tablet Samsung Galaxy Tab A9', 'Tablet 64GB, pantalla 11", ideal para trabajo y estudio', 60, 9, 'A'),
  (14, 3, 1, 'Cargador rápido Samsung 25W', 'Cargador USB-C de carga rápida 25W', 5, 7, 'A'),
  (15, 3, 6, 'Funda protectora Xiaomi Redmi Note 13', 'Funda de silicona con protección de esquinas', 26, 15, 'A'),
  (16, 4, 3, 'Audífonos Sony WH-1000XM4', 'Audífonos inalámbricos con cancelación de ruido', 90, 5, 'A'),
  (17, 4, 10, 'Audífonos JBL Tune 510BT', 'Audífonos inalámbricos con cancelación de ruido', 7, 15, 'A'),
  (18, 4, 5, 'Audífonos Logitech G435', 'Audífonos inalámbricos con cancelación de ruido', 62, 9, 'A'),
  (19, 4, 11, 'Audífonos Philips TAH4205', 'Audífonos inalámbricos con cancelación de ruido', 82, 10, 'A'),
  (20, 4, 10, 'Parlante Bluetooth JBL Flip 6', 'Parlante portátil resistente al agua IP67', 1, 13, 'A'),
  (21, 4, 3, 'Barra de sonido Sony HT-S100F', 'Barra de sonido 2.0 canales, 120W', 96, 4, 'A'),
  (22, 5, 1, 'Smart TV Samsung 50" 4K UHD', 'Televisor LED 50" 4K con HDR10 y sistema operativo smart', 54, 15, 'A'),
  (23, 5, 2, 'Smart TV LG 55" 4K UHD', 'Televisor LED 55" 4K con HDR10 y sistema operativo smart', 27, 8, 'A'),
  (24, 5, 3, 'Smart TV Sony 43" 4K UHD', 'Televisor LED 43" 4K con HDR10 y sistema operativo smart', 4, 7, 'A'),
  (25, 5, 1, 'Smart TV Samsung 65" 4K UHD', 'Televisor LED 65" 4K con HDR10 y sistema operativo smart', 0, 7, 'A'),
  (26, 6, 13, 'Consola Nintendo Switch OLED', 'Consola híbrida con pantalla OLED de 7 pulgadas', 42, 15, 'A'),
  (27, 6, 3, 'Control inalámbrico Sony DualSense', 'Control inalámbrico para PlayStation 5', 89, 4, 'A'),
  (28, 6, 5, 'Audífonos gaming Logitech G335', 'Audífonos con micrófono para gaming, sonido envolvente', 9, 15, 'A'),
  (29, 6, 13, 'Control Nintendo Joy-Con (par)', 'Par de controles Joy-Con para Nintendo Switch', 57, 5, 'A'),
  (30, 8, 2, 'Refrigeradora LG 14 pies', 'Refrigeradora No Frost, dispensador de agua', 83, 11, 'A'),
  (31, 8, 1, 'Refrigeradora Samsung 14 pies', 'Refrigeradora No Frost, dispensador de agua', 58, 12, 'A'),
  (32, 8, 12, 'Refrigeradora Whirlpool 14 pies', 'Refrigeradora No Frost, dispensador de agua', 55, 4, 'A'),
  (33, 8, 11, 'Lavadora Philips 18kg', 'Lavadora carga superior, digital inverter', 59, 15, 'A'),
  (34, 8, 11, 'Licuadora Philips Serie 5000', 'Licuadora 600W, vaso de vidrio 1.5L', 2, 8, 'A'),
  (35, 8, 12, 'Aspiradora Whirlpool WVC-2200', 'Aspiradora sin bolsa, 1200W', 0, 10, 'A'),
  (36, 9, 11, 'Afeitadora eléctrica Philips Serie 3000', 'Afeitadora recargable con cabezales flexibles', 88, 15, 'A'),
  (37, 9, 11, 'Secadora de cabello Philips EssentialCare', 'Secadora 2100W, 6 configuraciones de calor', 94, 5, 'A'),
  (38, 9, 6, 'Báscula inteligente Xiaomi Body Composition', 'Báscula con medición de composición corporal, Bluetooth', 37, 11, 'A'),
  (39, 10, 6, 'Power bank Xiaomi 20000mAh', 'Batería portátil de carga rápida 20000mAh', 7, 14, 'A'),
  (40, 10, 5, 'Mochila para laptop Logitech 15.6"', 'Mochila acolchada con compartimento para laptop', 109, 11, 'A'),
  (41, 10, 7, 'Hub USB-C Apple multipuerto', 'Adaptador USB-C a HDMI, USB-A y carga', 58, 14, 'A'),
  (42, 10, 1, 'Cargador inalámbrico Samsung 15W', 'Base de carga inalámbrica rápida Qi', 11, 15, 'A');

-- ==========================================================================
-- core: pedidos y su detalle (montos en USD, moneda oficial de El Salvador)
-- ==========================================================================

INSERT INTO core.pedidos (id_pedido, id_usuario, id_direccion, estado_pedido, total, fecha_creacion) VALUES
  (1, 10, 10, 'ENTREGADO', 224.34, now() - interval '9 days'),
  (2, 11, 11, 'ENTREGADO', 209.45, now() - interval '7 days'),
  (3, 1, 1, 'ENTREGADO', 1154.68, now() - interval '60 days'),
  (4, 4, 4, 'CANCELADO', 4171.7, now() - interval '59 days'),
  (5, 5, 5, 'ENTREGADO', 4107.32, now() - interval '33 days'),
  (6, 6, 6, 'ENTREGADO', 1990.44, now() - interval '39 days'),
  (7, 12, 12, 'PENDIENTE', 982.06, now() - interval '34 days'),
  (8, 6, 6, 'ENTREGADO', 336.51, now() - interval '55 days'),
  (9, 7, 7, 'ENTREGADO', 2190.09, now() - interval '9 days'),
  (10, 9, 9, 'ENVIADO', 289.98, now() - interval '19 days'),
  (11, 8, 8, 'ENTREGADO', 3306.52, now() - interval '14 days'),
  (12, 4, 4, 'PAGADO', 550.02, now() - interval '2 days'),
  (13, 2, 2, 'ENTREGADO', 1979.01, now() - interval '57 days'),
  (14, 7, 7, 'PAGADO', 2231.65, now() - interval '52 days'),
  (15, 9, 9, 'CANCELADO', 873.38, now() - interval '58 days'),
  (16, 11, 11, 'CANCELADO', 1456.66, now() - interval '50 days'),
  (17, 9, 9, 'PAGADO', 2754.29, now() - interval '10 days'),
  (18, 7, 7, 'ENVIADO', 4294.54, now() - interval '35 days'),
  (19, 1, 1, 'ENVIADO', 283.39, now() - interval '49 days'),
  (20, 7, 7, 'ENTREGADO', 3852.37, now() - interval '44 days'),
  (21, 11, 11, 'PAGADO', 4362.34, now() - interval '56 days'),
  (22, 7, 7, 'CANCELADO', 3825.32, now() - interval '54 days'),
  (23, 11, 11, 'CANCELADO', 1670.77, now() - interval '2 days'),
  (24, 11, 11, 'ENTREGADO', 4330.39, now() - interval '14 days'),
  (25, 10, 10, 'ENVIADO', 1881.41, now() - interval '37 days'),
  (26, 11, 11, 'CANCELADO', 1866.81, now() - interval '37 days'),
  (27, 2, 2, 'PENDIENTE', 3080.25, now() - interval '55 days'),
  (28, 7, 7, 'ENTREGADO', 2757.77, now() - interval '34 days');

INSERT INTO core.detalles_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
  (1, 1, 20, 2, 112.17, 224.34),
  (2, 2, 36, 3, 35.24, 105.72),
  (3, 2, 27, 1, 66.55, 66.55),
  (4, 2, 40, 1, 37.18, 37.18),
  (5, 3, 27, 1, 66.55, 66.55),
  (6, 3, 16, 3, 275.01, 825.03),
  (7, 3, 18, 1, 263.1, 263.1),
  (8, 4, 23, 3, 622.27, 1866.81),
  (9, 4, 20, 1, 112.17, 112.17),
  (10, 4, 15, 2, 6.87, 13.74),
  (11, 4, 2, 2, 1089.49, 2178.98),
  (12, 5, 35, 2, 78.51, 157.02),
  (13, 5, 22, 1, 514.69, 514.69),
  (14, 5, 2, 3, 1089.49, 3268.47),
  (15, 5, 8, 2, 83.57, 167.14),
  (16, 6, 25, 3, 663.48, 1990.44),
  (17, 7, 24, 1, 794.23, 794.23),
  (18, 7, 28, 3, 62.61, 187.83),
  (19, 8, 20, 3, 112.17, 336.51),
  (20, 9, 27, 3, 66.55, 199.65),
  (21, 9, 25, 3, 663.48, 1990.44),
  (22, 10, 28, 3, 62.61, 187.83),
  (23, 10, 38, 3, 34.05, 102.15),
  (24, 11, 11, 3, 436.46, 1309.38),
  (25, 11, 6, 3, 64.97, 194.91),
  (26, 11, 19, 3, 240.27, 720.81),
  (27, 11, 33, 2, 540.71, 1081.42),
  (28, 12, 16, 2, 275.01, 550.02),
  (29, 13, 25, 2, 663.48, 1326.96),
  (30, 13, 32, 1, 652.05, 652.05),
  (31, 14, 4, 2, 697.84, 1395.68),
  (32, 14, 36, 1, 35.24, 35.24),
  (33, 14, 16, 2, 275.01, 550.02),
  (34, 14, 8, 3, 83.57, 250.71),
  (35, 15, 40, 2, 37.18, 74.36),
  (36, 15, 33, 1, 540.71, 540.71),
  (37, 15, 28, 3, 62.61, 187.83),
  (38, 15, 36, 2, 35.24, 70.48),
  (39, 16, 41, 1, 34.28, 34.28),
  (40, 16, 16, 3, 275.01, 825.03),
  (41, 16, 18, 2, 263.1, 526.2),
  (42, 16, 29, 1, 71.15, 71.15),
  (43, 17, 25, 3, 663.48, 1990.44),
  (44, 17, 10, 1, 763.85, 763.85),
  (45, 18, 27, 3, 66.55, 199.65),
  (46, 18, 4, 3, 697.84, 2093.52),
  (47, 18, 14, 1, 10.93, 10.93),
  (48, 18, 25, 3, 663.48, 1990.44),
  (49, 19, 27, 2, 66.55, 133.1),
  (50, 19, 35, 1, 78.51, 78.51),
  (51, 19, 39, 2, 29.02, 58.04),
  (52, 19, 15, 2, 6.87, 13.74),
  (53, 20, 11, 3, 436.46, 1309.38),
  (54, 20, 30, 1, 809.23, 809.23),
  (55, 20, 9, 2, 811.11, 1622.22),
  (56, 20, 40, 3, 37.18, 111.54),
  (57, 21, 12, 2, 965.67, 1931.34),
  (58, 21, 4, 1, 697.84, 697.84),
  (59, 21, 17, 2, 203.1, 406.2),
  (60, 21, 25, 2, 663.48, 1326.96),
  (61, 22, 17, 3, 203.1, 609.3),
  (62, 22, 6, 3, 64.97, 194.91),
  (63, 22, 31, 1, 842.13, 842.13),
  (64, 22, 2, 2, 1089.49, 2178.98),
  (65, 23, 13, 3, 193.76, 581.28),
  (66, 23, 2, 1, 1089.49, 1089.49),
  (67, 24, 17, 3, 203.1, 609.3),
  (68, 24, 24, 3, 794.23, 2382.69),
  (69, 24, 11, 3, 436.46, 1309.38),
  (70, 24, 39, 1, 29.02, 29.02),
  (71, 25, 26, 3, 331.7, 995.1),
  (72, 25, 13, 3, 193.76, 581.28),
  (73, 25, 5, 1, 270.98, 270.98),
  (74, 25, 38, 1, 34.05, 34.05),
  (75, 26, 23, 3, 622.27, 1866.81),
  (76, 27, 32, 3, 652.05, 1956.15),
  (77, 27, 7, 2, 71.02, 142.04),
  (78, 27, 28, 3, 62.61, 187.83),
  (79, 27, 24, 1, 794.23, 794.23),
  (80, 28, 40, 2, 37.18, 74.36),
  (81, 28, 35, 2, 78.51, 157.02),
  (82, 28, 31, 3, 842.13, 2526.39);

INSERT INTO core.pagos (id_pago, id_pedido, metodo_pago, monto, estado_transaccion, fecha_pago) VALUES
  (1, 1, 'TARJETA_CREDITO', 224.34, 'APROBADO', now() - interval '9 days'),
  (2, 2, 'TARJETA_CREDITO', 209.45, 'APROBADO', now() - interval '7 days'),
  (3, 3, 'PAYPAL', 1154.68, 'APROBADO', now() - interval '60 days'),
  (4, 4, 'TARJETA_CREDITO', 4171.7, 'RECHAZADO', now() - interval '59 days'),
  (5, 5, 'TARJETA_CREDITO', 4107.32, 'APROBADO', now() - interval '33 days'),
  (6, 6, 'TARJETA_CREDITO', 1990.44, 'APROBADO', now() - interval '39 days'),
  (7, 8, 'PAYPAL', 336.51, 'APROBADO', now() - interval '55 days'),
  (8, 9, 'TARJETA_CREDITO', 2190.09, 'APROBADO', now() - interval '9 days'),
  (9, 10, 'PAYPAL', 289.98, 'APROBADO', now() - interval '19 days'),
  (10, 11, 'TARJETA_CREDITO', 3306.52, 'APROBADO', now() - interval '14 days'),
  (11, 12, 'TRANSFERENCIA', 550.02, 'APROBADO', now() - interval '2 days'),
  (12, 13, 'TARJETA_CREDITO', 1979.01, 'APROBADO', now() - interval '57 days'),
  (13, 14, 'TRANSFERENCIA', 2231.65, 'APROBADO', now() - interval '52 days'),
  (14, 15, 'TARJETA_CREDITO', 873.38, 'RECHAZADO', now() - interval '58 days'),
  (15, 16, 'PAYPAL', 1456.66, 'RECHAZADO', now() - interval '50 days'),
  (16, 17, 'TARJETA_CREDITO', 2754.29, 'APROBADO', now() - interval '10 days'),
  (17, 18, 'PAYPAL', 4294.54, 'APROBADO', now() - interval '35 days'),
  (18, 19, 'PAYPAL', 283.39, 'APROBADO', now() - interval '49 days'),
  (19, 20, 'TRANSFERENCIA', 3852.37, 'APROBADO', now() - interval '44 days'),
  (20, 21, 'PAYPAL', 4362.34, 'APROBADO', now() - interval '56 days'),
  (21, 22, 'TARJETA_CREDITO', 3825.32, 'RECHAZADO', now() - interval '54 days'),
  (22, 23, 'PAYPAL', 1670.77, 'RECHAZADO', now() - interval '2 days'),
  (23, 24, 'TARJETA_CREDITO', 4330.39, 'APROBADO', now() - interval '14 days'),
  (24, 25, 'TRANSFERENCIA', 1881.41, 'APROBADO', now() - interval '37 days'),
  (25, 26, 'PAYPAL', 1866.81, 'RECHAZADO', now() - interval '37 days'),
  (26, 28, 'TRANSFERENCIA', 2757.77, 'APROBADO', now() - interval '34 days');

INSERT INTO core.envios (id_envio, id_pedido, empresa_transporte, numero_seguimiento, estado_envio, fecha_despacho, fecha_entrega) VALUES
  (1, 1, 'DHL', 'TRK-100001', 'ENTREGADO', now() - interval '8 days', now() - interval '5 days'),
  (2, 2, 'UPS', 'TRK-100002', 'ENTREGADO', now() - interval '6 days', now() - interval '4 days'),
  (3, 3, 'UPS', 'TRK-100003', 'ENTREGADO', now() - interval '59 days', now() - interval '58 days'),
  (4, 5, 'UPS', 'TRK-100005', 'ENTREGADO', now() - interval '32 days', now() - interval '31 days'),
  (5, 6, 'Correos de El Salvador', 'TRK-100006', 'ENTREGADO', now() - interval '38 days', now() - interval '35 days'),
  (6, 8, 'Fedex', 'TRK-100008', 'ENTREGADO', now() - interval '54 days', now() - interval '50 days'),
  (7, 9, 'UPS', 'TRK-100009', 'ENTREGADO', now() - interval '8 days', now() - interval '5 days'),
  (8, 10, 'UPS', 'TRK-100010', 'EN_TRANSITO', now() - interval '18 days', NULL),
  (9, 11, 'Fedex', 'TRK-100011', 'ENTREGADO', now() - interval '13 days', now() - interval '11 days'),
  (10, 13, 'Correos de El Salvador', 'TRK-100013', 'ENTREGADO', now() - interval '56 days', now() - interval '55 days'),
  (11, 18, 'UPS', 'TRK-100018', 'EN_TRANSITO', now() - interval '34 days', NULL),
  (12, 19, 'Correos de El Salvador', 'TRK-100019', 'EN_TRANSITO', now() - interval '48 days', NULL),
  (13, 20, 'Correos de El Salvador', 'TRK-100020', 'ENTREGADO', now() - interval '43 days', now() - interval '42 days'),
  (14, 24, 'Correos de El Salvador', 'TRK-100024', 'ENTREGADO', now() - interval '13 days', now() - interval '10 days'),
  (15, 25, 'Fedex', 'TRK-100025', 'EN_TRANSITO', now() - interval '36 days', NULL),
  (16, 28, 'Fedex', 'TRK-100028', 'ENTREGADO', now() - interval '33 days', now() - interval '30 days');

-- Movimientos de inventario: entrada inicial por producto, salidas por venta
-- ligadas a pedidos ya despachados/entregados, y algunos ajustes manuales.
INSERT INTO core.movimientos_inventario (id_movimiento, id_producto, tipo_movimiento, cantidad, motivo, id_pedido, fecha_movimiento) VALUES
  (1, 1, 'ENTRADA', 25, 'COMPRA_PROVEEDOR', NULL, now() - interval '82 days'),
  (2, 2, 'ENTRADA', 113, 'COMPRA_PROVEEDOR', NULL, now() - interval '84 days'),
  (3, 3, 'ENTRADA', 69, 'COMPRA_PROVEEDOR', NULL, now() - interval '71 days'),
  (4, 4, 'ENTRADA', 65, 'COMPRA_PROVEEDOR', NULL, now() - interval '65 days'),
  (5, 5, 'ENTRADA', 24, 'COMPRA_PROVEEDOR', NULL, now() - interval '58 days'),
  (6, 6, 'ENTRADA', 61, 'COMPRA_PROVEEDOR', NULL, now() - interval '60 days'),
  (7, 7, 'ENTRADA', 129, 'COMPRA_PROVEEDOR', NULL, now() - interval '70 days'),
  (8, 8, 'ENTRADA', 21, 'COMPRA_PROVEEDOR', NULL, now() - interval '87 days'),
  (9, 9, 'ENTRADA', 32, 'COMPRA_PROVEEDOR', NULL, now() - interval '74 days'),
  (10, 10, 'ENTRADA', 25, 'COMPRA_PROVEEDOR', NULL, now() - interval '49 days'),
  (11, 11, 'ENTRADA', 14, 'COMPRA_PROVEEDOR', NULL, now() - interval '74 days'),
  (12, 12, 'ENTRADA', 18, 'COMPRA_PROVEEDOR', NULL, now() - interval '49 days'),
  (13, 13, 'ENTRADA', 99, 'COMPRA_PROVEEDOR', NULL, now() - interval '58 days'),
  (14, 14, 'ENTRADA', 42, 'COMPRA_PROVEEDOR', NULL, now() - interval '61 days'),
  (15, 15, 'ENTRADA', 39, 'COMPRA_PROVEEDOR', NULL, now() - interval '67 days'),
  (16, 16, 'ENTRADA', 99, 'COMPRA_PROVEEDOR', NULL, now() - interval '60 days'),
  (17, 17, 'ENTRADA', 35, 'COMPRA_PROVEEDOR', NULL, now() - interval '63 days'),
  (18, 18, 'ENTRADA', 77, 'COMPRA_PROVEEDOR', NULL, now() - interval '73 days'),
  (19, 19, 'ENTRADA', 121, 'COMPRA_PROVEEDOR', NULL, now() - interval '90 days'),
  (20, 20, 'ENTRADA', 25, 'COMPRA_PROVEEDOR', NULL, now() - interval '84 days'),
  (21, 21, 'ENTRADA', 134, 'COMPRA_PROVEEDOR', NULL, now() - interval '45 days'),
  (22, 22, 'ENTRADA', 94, 'COMPRA_PROVEEDOR', NULL, now() - interval '64 days'),
  (23, 23, 'ENTRADA', 38, 'COMPRA_PROVEEDOR', NULL, now() - interval '53 days'),
  (24, 24, 'ENTRADA', 25, 'COMPRA_PROVEEDOR', NULL, now() - interval '52 days'),
  (25, 25, 'ENTRADA', 11, 'COMPRA_PROVEEDOR', NULL, now() - interval '80 days'),
  (26, 26, 'ENTRADA', 56, 'COMPRA_PROVEEDOR', NULL, now() - interval '62 days'),
  (27, 27, 'ENTRADA', 112, 'COMPRA_PROVEEDOR', NULL, now() - interval '83 days'),
  (28, 28, 'ENTRADA', 27, 'COMPRA_PROVEEDOR', NULL, now() - interval '90 days'),
  (29, 29, 'ENTRADA', 83, 'COMPRA_PROVEEDOR', NULL, now() - interval '58 days'),
  (30, 30, 'ENTRADA', 104, 'COMPRA_PROVEEDOR', NULL, now() - interval '77 days'),
  (31, 31, 'ENTRADA', 94, 'COMPRA_PROVEEDOR', NULL, now() - interval '61 days'),
  (32, 32, 'ENTRADA', 63, 'COMPRA_PROVEEDOR', NULL, now() - interval '50 days'),
  (33, 33, 'ENTRADA', 91, 'COMPRA_PROVEEDOR', NULL, now() - interval '62 days'),
  (34, 34, 'ENTRADA', 9, 'COMPRA_PROVEEDOR', NULL, now() - interval '45 days'),
  (35, 35, 'ENTRADA', 26, 'COMPRA_PROVEEDOR', NULL, now() - interval '53 days'),
  (36, 36, 'ENTRADA', 109, 'COMPRA_PROVEEDOR', NULL, now() - interval '55 days'),
  (37, 37, 'ENTRADA', 127, 'COMPRA_PROVEEDOR', NULL, now() - interval '80 days'),
  (38, 38, 'ENTRADA', 69, 'COMPRA_PROVEEDOR', NULL, now() - interval '80 days'),
  (39, 39, 'ENTRADA', 12, 'COMPRA_PROVEEDOR', NULL, now() - interval '52 days'),
  (40, 40, 'ENTRADA', 118, 'COMPRA_PROVEEDOR', NULL, now() - interval '89 days'),
  (41, 41, 'ENTRADA', 72, 'COMPRA_PROVEEDOR', NULL, now() - interval '79 days'),
  (42, 42, 'ENTRADA', 18, 'COMPRA_PROVEEDOR', NULL, now() - interval '68 days'),
  (43, 20, 'SALIDA', 2, 'VENTA', 1, now() - interval '9 days'),
  (44, 36, 'SALIDA', 3, 'VENTA', 2, now() - interval '7 days'),
  (45, 27, 'SALIDA', 1, 'VENTA', 2, now() - interval '7 days'),
  (46, 40, 'SALIDA', 1, 'VENTA', 2, now() - interval '7 days'),
  (47, 27, 'SALIDA', 1, 'VENTA', 3, now() - interval '60 days'),
  (48, 16, 'SALIDA', 3, 'VENTA', 3, now() - interval '60 days'),
  (49, 18, 'SALIDA', 1, 'VENTA', 3, now() - interval '60 days'),
  (50, 35, 'SALIDA', 2, 'VENTA', 5, now() - interval '33 days'),
  (51, 22, 'SALIDA', 1, 'VENTA', 5, now() - interval '33 days'),
  (52, 2, 'SALIDA', 3, 'VENTA', 5, now() - interval '33 days'),
  (53, 8, 'SALIDA', 2, 'VENTA', 5, now() - interval '33 days'),
  (54, 25, 'SALIDA', 3, 'VENTA', 6, now() - interval '39 days'),
  (55, 20, 'SALIDA', 3, 'VENTA', 8, now() - interval '55 days'),
  (56, 27, 'SALIDA', 3, 'VENTA', 9, now() - interval '9 days'),
  (57, 25, 'SALIDA', 3, 'VENTA', 9, now() - interval '9 days'),
  (58, 28, 'SALIDA', 3, 'VENTA', 10, now() - interval '19 days'),
  (59, 38, 'SALIDA', 3, 'VENTA', 10, now() - interval '19 days'),
  (60, 11, 'SALIDA', 3, 'VENTA', 11, now() - interval '14 days'),
  (61, 6, 'SALIDA', 3, 'VENTA', 11, now() - interval '14 days'),
  (62, 19, 'SALIDA', 3, 'VENTA', 11, now() - interval '14 days'),
  (63, 33, 'SALIDA', 2, 'VENTA', 11, now() - interval '14 days'),
  (64, 25, 'SALIDA', 2, 'VENTA', 13, now() - interval '57 days'),
  (65, 32, 'SALIDA', 1, 'VENTA', 13, now() - interval '57 days'),
  (66, 27, 'SALIDA', 3, 'VENTA', 18, now() - interval '35 days'),
  (67, 4, 'SALIDA', 3, 'VENTA', 18, now() - interval '35 days'),
  (68, 14, 'SALIDA', 1, 'VENTA', 18, now() - interval '35 days'),
  (69, 25, 'SALIDA', 3, 'VENTA', 18, now() - interval '35 days'),
  (70, 27, 'SALIDA', 2, 'VENTA', 19, now() - interval '49 days'),
  (71, 35, 'SALIDA', 1, 'VENTA', 19, now() - interval '49 days'),
  (72, 39, 'SALIDA', 2, 'VENTA', 19, now() - interval '49 days'),
  (73, 15, 'SALIDA', 2, 'VENTA', 19, now() - interval '49 days'),
  (74, 11, 'SALIDA', 3, 'VENTA', 20, now() - interval '44 days'),
  (75, 30, 'SALIDA', 1, 'VENTA', 20, now() - interval '44 days'),
  (76, 9, 'SALIDA', 2, 'VENTA', 20, now() - interval '44 days'),
  (77, 40, 'SALIDA', 3, 'VENTA', 20, now() - interval '44 days'),
  (78, 17, 'SALIDA', 3, 'VENTA', 24, now() - interval '14 days'),
  (79, 24, 'SALIDA', 3, 'VENTA', 24, now() - interval '14 days'),
  (80, 11, 'SALIDA', 3, 'VENTA', 24, now() - interval '14 days'),
  (81, 39, 'SALIDA', 1, 'VENTA', 24, now() - interval '14 days'),
  (82, 26, 'SALIDA', 3, 'VENTA', 25, now() - interval '37 days'),
  (83, 13, 'SALIDA', 3, 'VENTA', 25, now() - interval '37 days'),
  (84, 5, 'SALIDA', 1, 'VENTA', 25, now() - interval '37 days'),
  (85, 38, 'SALIDA', 1, 'VENTA', 25, now() - interval '37 days'),
  (86, 40, 'SALIDA', 2, 'VENTA', 28, now() - interval '34 days'),
  (87, 35, 'SALIDA', 2, 'VENTA', 28, now() - interval '34 days'),
  (88, 31, 'SALIDA', 3, 'VENTA', 28, now() - interval '34 days'),
  (89, 16, 'AJUSTE', 1, 'AJUSTE', NULL, now() - interval '9 days'),
  (90, 29, 'AJUSTE', 2, 'AJUSTE', NULL, now() - interval '15 days'),
  (91, 37, 'AJUSTE', 5, 'AJUSTE', NULL, now() - interval '13 days'),
  (92, 22, 'AJUSTE', 1, 'AJUSTE', NULL, now() - interval '16 days'),
  (93, 21, 'AJUSTE', 2, 'AJUSTE', NULL, now() - interval '16 days'),
  (94, 14, 'AJUSTE', 3, 'AJUSTE', NULL, now() - interval '9 days');

-- ==========================================================================
-- Reajusta las secuencias de las columnas identity para que el próximo
-- INSERT hecho por la aplicación no choque con los IDs sembrados a mano.
-- ==========================================================================
SELECT setval(pg_get_serial_sequence('core.pedidos', 'id_pedido'), (SELECT MAX(id_pedido) FROM core.pedidos));
SELECT setval(pg_get_serial_sequence('core.detalles_pedido', 'id_detalle'), (SELECT MAX(id_detalle) FROM core.detalles_pedido));
SELECT setval(pg_get_serial_sequence('core.pagos', 'id_pago'), (SELECT MAX(id_pago) FROM core.pagos));
SELECT setval(pg_get_serial_sequence('core.envios', 'id_envio'), (SELECT MAX(id_envio) FROM core.envios));
SELECT setval(pg_get_serial_sequence('core.movimientos_inventario', 'id_movimiento'), (SELECT MAX(id_movimiento) FROM core.movimientos_inventario));

COMMIT;
