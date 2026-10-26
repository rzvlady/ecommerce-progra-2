-- Datos de prueba para desarrollo local.
-- Requiere haber corrido antes db/schema-ecommerce.sql y
-- db/migracion-productos-sku-precio.sql (agrega codigo_sku/precio_venta/precio_costo,
-- columnas que este seed ya llena).
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
  (1, 1, 'Ana', 'Martínez', '00000001-1', 'F', '2005-02-02'),
  (2, 2, 'Carlos', 'López', '00000002-2', 'M', '1996-08-16'),
  (3, 3, 'María', 'Hernández', '00000003-3', 'F', '1994-05-26'),
  (4, 4, 'José', 'Ramírez', '00000004-4', 'M', '1988-04-02'),
  (5, 5, 'Lucía', 'Flores', '00000005-5', 'F', '2000-09-28'),
  (6, 6, 'Pedro', 'Gómez', '00000006-6', 'M', '1974-11-28'),
  (7, 7, 'Sofía', 'Rivas', '00000007-7', 'F', '1975-07-22'),
  (8, 8, 'Diego', 'Cruz', '00000008-8', 'M', '1985-05-13'),
  (9, 9, 'Valeria', 'Mejía', '00000009-9', 'F', '1976-02-18'),
  (10, 10, 'Andrés', 'Pineda', '00000010-0', 'M', '1993-05-06'),
  (11, 11, 'Camila', 'Escobar', '00000011-1', 'F', '1978-05-04'),
  (12, 12, 'Ricardo', 'Alas', '00000012-2', 'M', '1986-09-16');

INSERT INTO seguridad.usuarios_contacto (id_contacto, id_usuario, tipo_contacto, valor, principal, verificado) VALUES
  (1, 1, 1, '7093-9856', true, true),
  (2, 2, 1, '7037-7570', true, true),
  (3, 3, 1, '7068-5673', true, true),
  (4, 4, 1, '7084-9883', true, true),
  (5, 4, 2, 'jose.ramirez@example.com', false, true),
  (6, 5, 1, '7077-3579', true, true),
  (7, 5, 2, 'lucia.flores@example.com', false, true),
  (8, 6, 1, '7040-7615', true, true),
  (9, 6, 2, 'pedro.gomez@example.com', false, true),
  (10, 7, 1, '7084-9565', true, true),
  (11, 7, 2, 'sofia.rivas@example.com', false, true),
  (12, 8, 1, '7026-5915', true, true),
  (13, 9, 1, '7037-9288', true, true),
  (14, 9, 2, 'valeria.mejia@example.com', false, true),
  (15, 10, 1, '7066-9900', true, true),
  (16, 11, 1, '7023-3546', true, true),
  (17, 11, 2, 'camila.escobar@example.com', false, true),
  (18, 12, 1, '7042-1832', true, true),
  (19, 12, 2, 'ricardo.alas@example.com', false, true);

INSERT INTO seguridad.usuarios_direccion (id_direccion, id_usuario, id_municipio, direccion_linea_1, codigo_postal, tipo_direccion) VALUES
  (1, 1, 2, 'Dirección de prueba #1, Res. Las Palmeras', 1002, 'C'),
  (2, 2, 3, 'Dirección de prueba #2, Col. Escalón', 1003, 'T'),
  (3, 3, 8, 'Dirección de prueba #3, Res. Las Palmeras', 1008, 'C'),
  (4, 4, 6, 'Dirección de prueba #4, Col. Escalón', 1006, 'C'),
  (5, 5, 2, 'Dirección de prueba #5, Col. Escalón', 1002, 'C'),
  (6, 6, 4, 'Dirección de prueba #6, Res. El Trébol', 1004, 'C'),
  (7, 7, 5, 'Dirección de prueba #7, Barrio San José', 1005, 'C'),
  (8, 8, 2, 'Dirección de prueba #8, Col. Miramonte', 1002, 'C'),
  (9, 9, 6, 'Dirección de prueba #9, Res. Las Palmeras', 1006, 'C'),
  (10, 10, 1, 'Dirección de prueba #10, Col. Escalón', 1001, 'T'),
  (11, 11, 4, 'Dirección de prueba #11, Res. Las Palmeras', 1004, 'T'),
  (12, 12, 7, 'Dirección de prueba #12, Col. Escalón', 1007, 'T');

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
INSERT INTO core.productos (id_producto, id_categoria, id_marca, codigo_sku, nombre_producto, descripcion_producto, precio_venta, precio_costo, stock_disponible, stock_minimo, estado) VALUES
  (1, 2, 4, 'SKU-0001', 'Laptop HP ThinkPad', 'Laptop 15", i5, 8GB RAM, 1024GB SSD', 743.89, 556.36, 5, 11, 'A'),
  (2, 2, 8, 'SKU-0002', 'Laptop Dell Pavilion', 'Laptop 15", i7, 8GB RAM, 1024GB SSD', 1089.49, 748.4, 44, 11, 'A'),
  (3, 2, 9, 'SKU-0003', 'Laptop Lenovo Inspiron', 'Laptop 15", i5, 8GB RAM, 256GB SSD', 731.66, 435.93, 27, 15, 'A'),
  (4, 2, 7, 'SKU-0004', 'Laptop Apple MacBook Air', 'Laptop 15", M2, 8GB RAM, 1024GB SSD', 697.84, 439.8, 0, 8, 'A'),
  (5, 2, 1, 'SKU-0005', 'Monitor Samsung 27" QHD', 'Monitor curvo 27 pulgadas, 2560x1440, 144Hz', 270.98, 160.56, 0, 8, 'A'),
  (6, 2, 5, 'SKU-0006', 'Mouse inalámbrico Logitech MX Master', 'Mouse ergonómico inalámbrico, múltiples dispositivos', 64.97, 37.59, 76, 13, 'A'),
  (7, 2, 5, 'SKU-0007', 'Teclado mecánico Logitech G413', 'Teclado mecánico retroiluminado, switches táctiles', 71.02, 49.67, 0, 7, 'A'),
  (8, 2, 4, 'SKU-0008', 'Impresora HP DeskJet 2720', 'Impresora multifuncional a color con WiFi', 83.57, 52.01, 68, 12, 'A'),
  (9, 3, 1, 'SKU-0009', 'Smartphone Samsung Galaxy A54', 'Smartphone 128GB, cámara múltiple, 5G', 811.11, 526.17, 75, 5, 'A'),
  (10, 3, 7, 'SKU-0010', 'Smartphone Apple iPhone 13', 'Smartphone 128GB, cámara múltiple, 5G', 763.85, 515.97, 28, 4, 'A'),
  (11, 3, 6, 'SKU-0011', 'Smartphone Xiaomi Redmi Note 13', 'Smartphone 256GB, cámara múltiple, 5G', 436.46, 245.6, 90, 9, 'A'),
  (12, 3, 2, 'SKU-0012', 'Smartphone LG Wing', 'Smartphone 128GB, cámara múltiple, 5G', 965.67, 579.68, 9, 12, 'A'),
  (13, 3, 1, 'SKU-0013', 'Tablet Samsung Galaxy Tab A9', 'Tablet 64GB, pantalla 11", ideal para trabajo y estudio', 193.76, 134.5, 95, 3, 'A'),
  (14, 3, 1, 'SKU-0014', 'Cargador rápido Samsung 25W', 'Cargador USB-C de carga rápida 25W', 10.93, 7.69, 50, 11, 'A'),
  (15, 3, 6, 'SKU-0015', 'Funda protectora Xiaomi Redmi Note 13', 'Funda de silicona con protección de esquinas', 6.87, 4.0, 7, 9, 'A'),
  (16, 4, 3, 'SKU-0016', 'Audífonos Sony WH-1000XM4', 'Audífonos inalámbricos con cancelación de ruido', 275.01, 204.71, 0, 7, 'A'),
  (17, 4, 10, 'SKU-0017', 'Audífonos JBL Tune 510BT', 'Audífonos inalámbricos con cancelación de ruido', 203.1, 147.07, 29, 11, 'A'),
  (18, 4, 5, 'SKU-0018', 'Audífonos Logitech G435', 'Audífonos inalámbricos con cancelación de ruido', 263.1, 176.75, 9, 15, 'A'),
  (19, 4, 11, 'SKU-0019', 'Audífonos Philips TAH4205', 'Audífonos inalámbricos con cancelación de ruido', 240.27, 177.97, 5, 7, 'A'),
  (20, 4, 10, 'SKU-0020', 'Parlante Bluetooth JBL Flip 6', 'Parlante portátil resistente al agua IP67', 112.17, 68.97, 84, 3, 'A'),
  (21, 4, 3, 'SKU-0021', 'Barra de sonido Sony HT-S100F', 'Barra de sonido 2.0 canales, 120W', 158.12, 114.75, 0, 8, 'A'),
  (22, 5, 1, 'SKU-0022', 'Smart TV Samsung 50" 4K UHD', 'Televisor LED 50" 4K con HDR10 y sistema operativo smart', 514.69, 289.04, 42, 7, 'A'),
  (23, 5, 2, 'SKU-0023', 'Smart TV LG 55" 4K UHD', 'Televisor LED 55" 4K con HDR10 y sistema operativo smart', 622.27, 433.34, 19, 4, 'A'),
  (24, 5, 3, 'SKU-0024', 'Smart TV Sony 43" 4K UHD', 'Televisor LED 43" 4K con HDR10 y sistema operativo smart', 794.23, 558.48, 88, 15, 'A'),
  (25, 5, 1, 'SKU-0025', 'Smart TV Samsung 65" 4K UHD', 'Televisor LED 65" 4K con HDR10 y sistema operativo smart', 663.48, 490.56, 0, 10, 'A'),
  (26, 6, 13, 'SKU-0026', 'Consola Nintendo Switch OLED', 'Consola híbrida con pantalla OLED de 7 pulgadas', 331.7, 210.51, 10, 13, 'A'),
  (27, 6, 3, 'SKU-0027', 'Control inalámbrico Sony DualSense', 'Control inalámbrico para PlayStation 5', 66.55, 46.32, 13, 13, 'A'),
  (28, 6, 5, 'SKU-0028', 'Audífonos gaming Logitech G335', 'Audífonos con micrófono para gaming, sonido envolvente', 62.61, 46.9, 7, 9, 'A'),
  (29, 6, 13, 'SKU-0029', 'Control Nintendo Joy-Con (par)', 'Par de controles Joy-Con para Nintendo Switch', 71.15, 45.56, 81, 10, 'A'),
  (30, 8, 2, 'SKU-0030', 'Refrigeradora LG 14 pies', 'Refrigeradora No Frost, dispensador de agua', 809.23, 448.48, 3, 6, 'A'),
  (31, 8, 1, 'SKU-0031', 'Refrigeradora Samsung 14 pies', 'Refrigeradora No Frost, dispensador de agua', 842.13, 464.38, 45, 12, 'A'),
  (32, 8, 12, 'SKU-0032', 'Refrigeradora Whirlpool 14 pies', 'Refrigeradora No Frost, dispensador de agua', 652.05, 367.42, 37, 3, 'A'),
  (33, 8, 11, 'SKU-0033', 'Lavadora Philips 18kg', 'Lavadora carga superior, digital inverter', 540.71, 305.05, 0, 8, 'A'),
  (34, 8, 11, 'SKU-0034', 'Licuadora Philips Serie 5000', 'Licuadora 600W, vaso de vidrio 1.5L', 44.12, 26.16, 8, 15, 'A'),
  (35, 8, 12, 'SKU-0035', 'Aspiradora Whirlpool WVC-2200', 'Aspiradora sin bolsa, 1200W', 78.51, 47.0, 0, 10, 'A'),
  (36, 9, 11, 'SKU-0036', 'Afeitadora eléctrica Philips Serie 3000', 'Afeitadora recargable con cabezales flexibles', 35.24, 20.05, 38, 9, 'A'),
  (37, 9, 11, 'SKU-0037', 'Secadora de cabello Philips EssentialCare', 'Secadora 2100W, 6 configuraciones de calor', 25.34, 16.02, 67, 8, 'A'),
  (38, 9, 6, 'SKU-0038', 'Báscula inteligente Xiaomi Body Composition', 'Báscula con medición de composición corporal, Bluetooth', 34.05, 23.18, 94, 3, 'A'),
  (39, 10, 6, 'SKU-0039', 'Power bank Xiaomi 20000mAh', 'Batería portátil de carga rápida 20000mAh', 29.02, 20.19, 59, 3, 'A'),
  (40, 10, 5, 'SKU-0040', 'Mochila para laptop Logitech 15.6"', 'Mochila acolchada con compartimento para laptop', 37.18, 21.87, 40, 4, 'A'),
  (41, 10, 7, 'SKU-0041', 'Hub USB-C Apple multipuerto', 'Adaptador USB-C a HDMI, USB-A y carga', 34.28, 20.11, 64, 5, 'A'),
  (42, 10, 1, 'SKU-0042', 'Cargador inalámbrico Samsung 15W', 'Base de carga inalámbrica rápida Qi', 25.94, 18.46, 65, 4, 'A');

-- ==========================================================================
-- core: pedidos y su detalle (montos en USD, moneda oficial de El Salvador)
-- ==========================================================================

INSERT INTO core.pedidos (id_pedido, id_usuario, id_direccion, estado_pedido, total, fecha_creacion) VALUES
  (1, 10, 10, 'ENTREGADO', 2072.62, now() - interval '58 days'),
  (2, 7, 7, 'ENTREGADO', 678.49, now() - interval '59 days'),
  (3, 7, 7, 'ENTREGADO', 2190.09, now() - interval '9 days'),
  (4, 9, 9, 'ENVIADO', 289.98, now() - interval '19 days'),
  (5, 8, 8, 'ENTREGADO', 3306.52, now() - interval '14 days'),
  (6, 4, 4, 'PAGADO', 550.02, now() - interval '2 days'),
  (7, 2, 2, 'ENTREGADO', 1979.01, now() - interval '57 days'),
  (8, 7, 7, 'PAGADO', 2231.65, now() - interval '52 days'),
  (9, 9, 9, 'CANCELADO', 873.38, now() - interval '58 days'),
  (10, 11, 11, 'CANCELADO', 1456.66, now() - interval '50 days'),
  (11, 9, 9, 'PAGADO', 2754.29, now() - interval '10 days'),
  (12, 7, 7, 'ENVIADO', 4294.54, now() - interval '35 days'),
  (13, 1, 1, 'ENVIADO', 283.39, now() - interval '49 days'),
  (14, 7, 7, 'ENTREGADO', 3852.37, now() - interval '44 days'),
  (15, 11, 11, 'PAGADO', 4362.34, now() - interval '56 days'),
  (16, 7, 7, 'CANCELADO', 3825.32, now() - interval '54 days'),
  (17, 11, 11, 'CANCELADO', 1670.77, now() - interval '2 days'),
  (18, 11, 11, 'ENTREGADO', 4330.39, now() - interval '14 days'),
  (19, 10, 10, 'ENVIADO', 1881.41, now() - interval '37 days'),
  (20, 11, 11, 'CANCELADO', 1866.81, now() - interval '37 days'),
  (21, 2, 2, 'PENDIENTE', 3080.25, now() - interval '55 days'),
  (22, 7, 7, 'ENTREGADO', 2757.77, now() - interval '34 days'),
  (23, 4, 4, 'ENVIADO', 2531.35, now() - interval '57 days'),
  (24, 3, 3, 'PAGADO', 2157.33, now() - interval '23 days'),
  (25, 9, 9, 'PENDIENTE', 4296.15, now() - interval '16 days'),
  (26, 1, 1, 'ENVIADO', 2577.14, now() - interval '15 days'),
  (27, 6, 6, 'ENVIADO', 678.59, now() - interval '20 days'),
  (28, 4, 4, 'ENTREGADO', 141.24, now() - interval '31 days');

INSERT INTO core.detalles_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
  (1, 1, 17, 3, 203.1, 609.3),
  (2, 1, 3, 2, 731.66, 1463.32),
  (3, 2, 40, 3, 37.18, 111.54),
  (4, 2, 21, 2, 158.12, 316.24),
  (5, 2, 8, 3, 83.57, 250.71),
  (6, 3, 27, 3, 66.55, 199.65),
  (7, 3, 25, 3, 663.48, 1990.44),
  (8, 4, 28, 3, 62.61, 187.83),
  (9, 4, 38, 3, 34.05, 102.15),
  (10, 5, 11, 3, 436.46, 1309.38),
  (11, 5, 6, 3, 64.97, 194.91),
  (12, 5, 19, 3, 240.27, 720.81),
  (13, 5, 33, 2, 540.71, 1081.42),
  (14, 6, 16, 2, 275.01, 550.02),
  (15, 7, 25, 2, 663.48, 1326.96),
  (16, 7, 32, 1, 652.05, 652.05),
  (17, 8, 4, 2, 697.84, 1395.68),
  (18, 8, 36, 1, 35.24, 35.24),
  (19, 8, 16, 2, 275.01, 550.02),
  (20, 8, 8, 3, 83.57, 250.71),
  (21, 9, 40, 2, 37.18, 74.36),
  (22, 9, 33, 1, 540.71, 540.71),
  (23, 9, 28, 3, 62.61, 187.83),
  (24, 9, 36, 2, 35.24, 70.48),
  (25, 10, 41, 1, 34.28, 34.28),
  (26, 10, 16, 3, 275.01, 825.03),
  (27, 10, 18, 2, 263.1, 526.2),
  (28, 10, 29, 1, 71.15, 71.15),
  (29, 11, 25, 3, 663.48, 1990.44),
  (30, 11, 10, 1, 763.85, 763.85),
  (31, 12, 27, 3, 66.55, 199.65),
  (32, 12, 4, 3, 697.84, 2093.52),
  (33, 12, 14, 1, 10.93, 10.93),
  (34, 12, 25, 3, 663.48, 1990.44),
  (35, 13, 27, 2, 66.55, 133.1),
  (36, 13, 35, 1, 78.51, 78.51),
  (37, 13, 39, 2, 29.02, 58.04),
  (38, 13, 15, 2, 6.87, 13.74),
  (39, 14, 11, 3, 436.46, 1309.38),
  (40, 14, 30, 1, 809.23, 809.23),
  (41, 14, 9, 2, 811.11, 1622.22),
  (42, 14, 40, 3, 37.18, 111.54),
  (43, 15, 12, 2, 965.67, 1931.34),
  (44, 15, 4, 1, 697.84, 697.84),
  (45, 15, 17, 2, 203.1, 406.2),
  (46, 15, 25, 2, 663.48, 1326.96),
  (47, 16, 17, 3, 203.1, 609.3),
  (48, 16, 6, 3, 64.97, 194.91),
  (49, 16, 31, 1, 842.13, 842.13),
  (50, 16, 2, 2, 1089.49, 2178.98),
  (51, 17, 13, 3, 193.76, 581.28),
  (52, 17, 2, 1, 1089.49, 1089.49),
  (53, 18, 17, 3, 203.1, 609.3),
  (54, 18, 24, 3, 794.23, 2382.69),
  (55, 18, 11, 3, 436.46, 1309.38),
  (56, 18, 39, 1, 29.02, 29.02),
  (57, 19, 26, 3, 331.7, 995.1),
  (58, 19, 13, 3, 193.76, 581.28),
  (59, 19, 5, 1, 270.98, 270.98),
  (60, 19, 38, 1, 34.05, 34.05),
  (61, 20, 23, 3, 622.27, 1866.81),
  (62, 21, 32, 3, 652.05, 1956.15),
  (63, 21, 7, 2, 71.02, 142.04),
  (64, 21, 28, 3, 62.61, 187.83),
  (65, 21, 24, 1, 794.23, 794.23),
  (66, 22, 40, 2, 37.18, 74.36),
  (67, 22, 35, 2, 78.51, 157.02),
  (68, 22, 31, 3, 842.13, 2526.39),
  (69, 23, 16, 3, 275.01, 825.03),
  (70, 23, 30, 2, 809.23, 1618.46),
  (71, 23, 37, 2, 25.34, 50.68),
  (72, 23, 40, 1, 37.18, 37.18),
  (73, 24, 22, 3, 514.69, 1544.07),
  (74, 24, 18, 2, 263.1, 526.2),
  (75, 24, 39, 3, 29.02, 87.06),
  (76, 25, 32, 3, 652.05, 1956.15),
  (77, 25, 36, 3, 35.24, 105.72),
  (78, 25, 16, 2, 275.01, 550.02),
  (79, 25, 31, 2, 842.13, 1684.26),
  (80, 26, 16, 2, 275.01, 550.02),
  (81, 26, 20, 3, 112.17, 336.51),
  (82, 26, 38, 3, 34.05, 102.15),
  (83, 26, 24, 2, 794.23, 1588.46),
  (84, 27, 15, 2, 6.87, 13.74),
  (85, 27, 8, 1, 83.57, 83.57),
  (86, 27, 13, 3, 193.76, 581.28),
  (87, 28, 38, 2, 34.05, 68.1),
  (88, 28, 34, 1, 44.12, 44.12),
  (89, 28, 39, 1, 29.02, 29.02);

INSERT INTO core.pagos (id_pago, id_pedido, metodo_pago, monto, estado_transaccion, fecha_pago) VALUES
  (1, 1, 'TARJETA_CREDITO', 2072.62, 'APROBADO', now() - interval '58 days'),
  (2, 2, 'PAYPAL', 678.49, 'APROBADO', now() - interval '59 days'),
  (3, 3, 'TARJETA_CREDITO', 2190.09, 'APROBADO', now() - interval '9 days'),
  (4, 4, 'PAYPAL', 289.98, 'APROBADO', now() - interval '19 days'),
  (5, 5, 'TARJETA_CREDITO', 3306.52, 'APROBADO', now() - interval '14 days'),
  (6, 6, 'TRANSFERENCIA', 550.02, 'APROBADO', now() - interval '2 days'),
  (7, 7, 'TARJETA_CREDITO', 1979.01, 'APROBADO', now() - interval '57 days'),
  (8, 8, 'TRANSFERENCIA', 2231.65, 'APROBADO', now() - interval '52 days'),
  (9, 9, 'TARJETA_CREDITO', 873.38, 'RECHAZADO', now() - interval '58 days'),
  (10, 10, 'PAYPAL', 1456.66, 'RECHAZADO', now() - interval '50 days'),
  (11, 11, 'TARJETA_CREDITO', 2754.29, 'APROBADO', now() - interval '10 days'),
  (12, 12, 'PAYPAL', 4294.54, 'APROBADO', now() - interval '35 days'),
  (13, 13, 'PAYPAL', 283.39, 'APROBADO', now() - interval '49 days'),
  (14, 14, 'TRANSFERENCIA', 3852.37, 'APROBADO', now() - interval '44 days'),
  (15, 15, 'PAYPAL', 4362.34, 'APROBADO', now() - interval '56 days'),
  (16, 16, 'TARJETA_CREDITO', 3825.32, 'RECHAZADO', now() - interval '54 days'),
  (17, 17, 'PAYPAL', 1670.77, 'RECHAZADO', now() - interval '2 days'),
  (18, 18, 'TARJETA_CREDITO', 4330.39, 'APROBADO', now() - interval '14 days'),
  (19, 19, 'TRANSFERENCIA', 1881.41, 'APROBADO', now() - interval '37 days'),
  (20, 20, 'PAYPAL', 1866.81, 'RECHAZADO', now() - interval '37 days'),
  (21, 22, 'TRANSFERENCIA', 2757.77, 'APROBADO', now() - interval '34 days'),
  (22, 23, 'PAYPAL', 2531.35, 'APROBADO', now() - interval '57 days'),
  (23, 24, 'TARJETA_CREDITO', 2157.33, 'APROBADO', now() - interval '23 days'),
  (24, 26, 'PAYPAL', 2577.14, 'APROBADO', now() - interval '15 days'),
  (25, 27, 'TRANSFERENCIA', 678.59, 'APROBADO', now() - interval '20 days'),
  (26, 28, 'PAYPAL', 141.24, 'APROBADO', now() - interval '31 days');

INSERT INTO core.envios (id_envio, id_pedido, empresa_transporte, numero_seguimiento, estado_envio, fecha_despacho, fecha_entrega) VALUES
  (1, 1, 'Fedex', 'TRK-100001', 'ENTREGADO', now() - interval '57 days', now() - interval '55 days'),
  (2, 2, 'Fedex', 'TRK-100002', 'ENTREGADO', now() - interval '58 days', now() - interval '54 days'),
  (3, 3, 'UPS', 'TRK-100003', 'ENTREGADO', now() - interval '8 days', now() - interval '5 days'),
  (4, 4, 'UPS', 'TRK-100004', 'EN_TRANSITO', now() - interval '18 days', NULL),
  (5, 5, 'Fedex', 'TRK-100005', 'ENTREGADO', now() - interval '13 days', now() - interval '11 days'),
  (6, 7, 'Correos de El Salvador', 'TRK-100007', 'ENTREGADO', now() - interval '56 days', now() - interval '55 days'),
  (7, 12, 'UPS', 'TRK-100012', 'EN_TRANSITO', now() - interval '34 days', NULL),
  (8, 13, 'Correos de El Salvador', 'TRK-100013', 'EN_TRANSITO', now() - interval '48 days', NULL),
  (9, 14, 'Correos de El Salvador', 'TRK-100014', 'ENTREGADO', now() - interval '43 days', now() - interval '42 days'),
  (10, 18, 'Correos de El Salvador', 'TRK-100018', 'ENTREGADO', now() - interval '13 days', now() - interval '10 days'),
  (11, 19, 'Fedex', 'TRK-100019', 'EN_TRANSITO', now() - interval '36 days', NULL),
  (12, 22, 'Fedex', 'TRK-100022', 'ENTREGADO', now() - interval '33 days', now() - interval '30 days'),
  (13, 23, 'Fedex', 'TRK-100023', 'EN_TRANSITO', now() - interval '56 days', NULL),
  (14, 26, 'Fedex', 'TRK-100026', 'EN_TRANSITO', now() - interval '14 days', NULL),
  (15, 27, 'DHL', 'TRK-100027', 'EN_TRANSITO', now() - interval '19 days', NULL),
  (16, 28, 'Fedex', 'TRK-100028', 'ENTREGADO', now() - interval '30 days', now() - interval '28 days');

-- Movimientos de inventario: entrada inicial por producto, salidas por venta
-- ligadas a pedidos ya despachados/entregados, y algunos ajustes manuales.
INSERT INTO core.movimientos_inventario (id_movimiento, id_producto, tipo_movimiento, cantidad, motivo, id_pedido, fecha_movimiento) VALUES
  (1, 1, 'ENTRADA', 10, 'COMPRA_PROVEEDOR', NULL, now() - interval '66 days'),
  (2, 2, 'ENTRADA', 57, 'COMPRA_PROVEEDOR', NULL, now() - interval '85 days'),
  (3, 3, 'ENTRADA', 48, 'COMPRA_PROVEEDOR', NULL, now() - interval '55 days'),
  (4, 4, 'ENTRADA', 33, 'COMPRA_PROVEEDOR', NULL, now() - interval '80 days'),
  (5, 5, 'ENTRADA', 32, 'COMPRA_PROVEEDOR', NULL, now() - interval '80 days'),
  (6, 6, 'ENTRADA', 81, 'COMPRA_PROVEEDOR', NULL, now() - interval '52 days'),
  (7, 7, 'ENTRADA', 9, 'COMPRA_PROVEEDOR', NULL, now() - interval '89 days'),
  (8, 8, 'ENTRADA', 82, 'COMPRA_PROVEEDOR', NULL, now() - interval '79 days'),
  (9, 9, 'ENTRADA', 82, 'COMPRA_PROVEEDOR', NULL, now() - interval '68 days'),
  (10, 10, 'ENTRADA', 68, 'COMPRA_PROVEEDOR', NULL, now() - interval '54 days'),
  (11, 11, 'ENTRADA', 122, 'COMPRA_PROVEEDOR', NULL, now() - interval '53 days'),
  (12, 12, 'ENTRADA', 16, 'COMPRA_PROVEEDOR', NULL, now() - interval '64 days'),
  (13, 13, 'ENTRADA', 123, 'COMPRA_PROVEEDOR', NULL, now() - interval '47 days'),
  (14, 14, 'ENTRADA', 77, 'COMPRA_PROVEEDOR', NULL, now() - interval '58 days'),
  (15, 15, 'ENTRADA', 27, 'COMPRA_PROVEEDOR', NULL, now() - interval '87 days'),
  (16, 16, 'ENTRADA', 11, 'COMPRA_PROVEEDOR', NULL, now() - interval '67 days'),
  (17, 17, 'ENTRADA', 69, 'COMPRA_PROVEEDOR', NULL, now() - interval '71 days'),
  (18, 18, 'ENTRADA', 23, 'COMPRA_PROVEEDOR', NULL, now() - interval '60 days'),
  (19, 19, 'ENTRADA', 20, 'COMPRA_PROVEEDOR', NULL, now() - interval '56 days'),
  (20, 20, 'ENTRADA', 115, 'COMPRA_PROVEEDOR', NULL, now() - interval '46 days'),
  (21, 21, 'ENTRADA', 16, 'COMPRA_PROVEEDOR', NULL, now() - interval '66 days'),
  (22, 22, 'ENTRADA', 73, 'COMPRA_PROVEEDOR', NULL, now() - interval '87 days'),
  (23, 23, 'ENTRADA', 39, 'COMPRA_PROVEEDOR', NULL, now() - interval '62 days'),
  (24, 24, 'ENTRADA', 103, 'COMPRA_PROVEEDOR', NULL, now() - interval '89 days'),
  (25, 25, 'ENTRADA', 11, 'COMPRA_PROVEEDOR', NULL, now() - interval '69 days'),
  (26, 26, 'ENTRADA', 17, 'COMPRA_PROVEEDOR', NULL, now() - interval '75 days'),
  (27, 27, 'ENTRADA', 32, 'COMPRA_PROVEEDOR', NULL, now() - interval '57 days'),
  (28, 28, 'ENTRADA', 41, 'COMPRA_PROVEEDOR', NULL, now() - interval '67 days'),
  (29, 29, 'ENTRADA', 105, 'COMPRA_PROVEEDOR', NULL, now() - interval '59 days'),
  (30, 30, 'ENTRADA', 22, 'COMPRA_PROVEEDOR', NULL, now() - interval '46 days'),
  (31, 31, 'ENTRADA', 62, 'COMPRA_PROVEEDOR', NULL, now() - interval '70 days'),
  (32, 32, 'ENTRADA', 63, 'COMPRA_PROVEEDOR', NULL, now() - interval '62 days'),
  (33, 33, 'ENTRADA', 9, 'COMPRA_PROVEEDOR', NULL, now() - interval '62 days'),
  (34, 34, 'ENTRADA', 35, 'COMPRA_PROVEEDOR', NULL, now() - interval '86 days'),
  (35, 35, 'ENTRADA', 37, 'COMPRA_PROVEEDOR', NULL, now() - interval '70 days'),
  (36, 36, 'ENTRADA', 77, 'COMPRA_PROVEEDOR', NULL, now() - interval '66 days'),
  (37, 37, 'ENTRADA', 73, 'COMPRA_PROVEEDOR', NULL, now() - interval '52 days'),
  (38, 38, 'ENTRADA', 115, 'COMPRA_PROVEEDOR', NULL, now() - interval '56 days'),
  (39, 39, 'ENTRADA', 80, 'COMPRA_PROVEEDOR', NULL, now() - interval '47 days'),
  (40, 40, 'ENTRADA', 51, 'COMPRA_PROVEEDOR', NULL, now() - interval '83 days'),
  (41, 41, 'ENTRADA', 96, 'COMPRA_PROVEEDOR', NULL, now() - interval '67 days'),
  (42, 42, 'ENTRADA', 90, 'COMPRA_PROVEEDOR', NULL, now() - interval '72 days'),
  (43, 17, 'SALIDA', 3, 'VENTA', 1, now() - interval '58 days'),
  (44, 3, 'SALIDA', 2, 'VENTA', 1, now() - interval '58 days'),
  (45, 40, 'SALIDA', 3, 'VENTA', 2, now() - interval '59 days'),
  (46, 21, 'SALIDA', 2, 'VENTA', 2, now() - interval '59 days'),
  (47, 8, 'SALIDA', 3, 'VENTA', 2, now() - interval '59 days'),
  (48, 27, 'SALIDA', 3, 'VENTA', 3, now() - interval '9 days'),
  (49, 25, 'SALIDA', 3, 'VENTA', 3, now() - interval '9 days'),
  (50, 28, 'SALIDA', 3, 'VENTA', 4, now() - interval '19 days'),
  (51, 38, 'SALIDA', 3, 'VENTA', 4, now() - interval '19 days'),
  (52, 11, 'SALIDA', 3, 'VENTA', 5, now() - interval '14 days'),
  (53, 6, 'SALIDA', 3, 'VENTA', 5, now() - interval '14 days'),
  (54, 19, 'SALIDA', 3, 'VENTA', 5, now() - interval '14 days'),
  (55, 33, 'SALIDA', 2, 'VENTA', 5, now() - interval '14 days'),
  (56, 25, 'SALIDA', 2, 'VENTA', 7, now() - interval '57 days'),
  (57, 32, 'SALIDA', 1, 'VENTA', 7, now() - interval '57 days'),
  (58, 27, 'SALIDA', 3, 'VENTA', 12, now() - interval '35 days'),
  (59, 4, 'SALIDA', 3, 'VENTA', 12, now() - interval '35 days'),
  (60, 14, 'SALIDA', 1, 'VENTA', 12, now() - interval '35 days'),
  (61, 25, 'SALIDA', 3, 'VENTA', 12, now() - interval '35 days'),
  (62, 27, 'SALIDA', 2, 'VENTA', 13, now() - interval '49 days'),
  (63, 35, 'SALIDA', 1, 'VENTA', 13, now() - interval '49 days'),
  (64, 39, 'SALIDA', 2, 'VENTA', 13, now() - interval '49 days'),
  (65, 15, 'SALIDA', 2, 'VENTA', 13, now() - interval '49 days'),
  (66, 11, 'SALIDA', 3, 'VENTA', 14, now() - interval '44 days'),
  (67, 30, 'SALIDA', 1, 'VENTA', 14, now() - interval '44 days'),
  (68, 9, 'SALIDA', 2, 'VENTA', 14, now() - interval '44 days'),
  (69, 40, 'SALIDA', 3, 'VENTA', 14, now() - interval '44 days'),
  (70, 17, 'SALIDA', 3, 'VENTA', 18, now() - interval '14 days'),
  (71, 24, 'SALIDA', 3, 'VENTA', 18, now() - interval '14 days'),
  (72, 11, 'SALIDA', 3, 'VENTA', 18, now() - interval '14 days'),
  (73, 39, 'SALIDA', 1, 'VENTA', 18, now() - interval '14 days'),
  (74, 26, 'SALIDA', 3, 'VENTA', 19, now() - interval '37 days'),
  (75, 13, 'SALIDA', 3, 'VENTA', 19, now() - interval '37 days'),
  (76, 5, 'SALIDA', 1, 'VENTA', 19, now() - interval '37 days'),
  (77, 38, 'SALIDA', 1, 'VENTA', 19, now() - interval '37 days'),
  (78, 40, 'SALIDA', 2, 'VENTA', 22, now() - interval '34 days'),
  (79, 35, 'SALIDA', 2, 'VENTA', 22, now() - interval '34 days'),
  (80, 31, 'SALIDA', 3, 'VENTA', 22, now() - interval '34 days'),
  (81, 16, 'SALIDA', 3, 'VENTA', 23, now() - interval '57 days'),
  (82, 30, 'SALIDA', 2, 'VENTA', 23, now() - interval '57 days'),
  (83, 37, 'SALIDA', 2, 'VENTA', 23, now() - interval '57 days'),
  (84, 40, 'SALIDA', 1, 'VENTA', 23, now() - interval '57 days'),
  (85, 16, 'SALIDA', 2, 'VENTA', 26, now() - interval '15 days'),
  (86, 20, 'SALIDA', 3, 'VENTA', 26, now() - interval '15 days'),
  (87, 38, 'SALIDA', 3, 'VENTA', 26, now() - interval '15 days'),
  (88, 24, 'SALIDA', 2, 'VENTA', 26, now() - interval '15 days'),
  (89, 15, 'SALIDA', 2, 'VENTA', 27, now() - interval '20 days'),
  (90, 8, 'SALIDA', 1, 'VENTA', 27, now() - interval '20 days'),
  (91, 13, 'SALIDA', 3, 'VENTA', 27, now() - interval '20 days'),
  (92, 38, 'SALIDA', 2, 'VENTA', 28, now() - interval '31 days'),
  (93, 34, 'SALIDA', 1, 'VENTA', 28, now() - interval '31 days'),
  (94, 39, 'SALIDA', 1, 'VENTA', 28, now() - interval '31 days'),
  (95, 12, 'AJUSTE', 3, 'AJUSTE', NULL, now() - interval '1 days'),
  (96, 35, 'AJUSTE', 2, 'AJUSTE', NULL, now() - interval '9 days'),
  (97, 3, 'AJUSTE', 1, 'AJUSTE', NULL, now() - interval '18 days'),
  (98, 19, 'AJUSTE', 2, 'AJUSTE', NULL, now() - interval '16 days'),
  (99, 7, 'AJUSTE', 1, 'AJUSTE', NULL, now() - interval '19 days'),
  (100, 19, 'AJUSTE', 4, 'AJUSTE', NULL, now() - interval '16 days');

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
