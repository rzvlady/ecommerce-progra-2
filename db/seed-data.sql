-- Datos de prueba para desarrollo local.
-- Requiere haber corrido antes db/schema-ecommerce.sql.
--
-- Uso:
--   psql -U <usuario> -h localhost -d <base_de_datos> -f db/seed-data.sql
--
-- Password de los usuarios sembrados: "password" (hash BCrypt de referencia de
-- Spring Security). Es solo para desarrollo local, no usar en ningún ambiente real.

BEGIN;

-- ==========================================================================
-- catalogo
-- ==========================================================================

INSERT INTO catalogo.paises (id_pais, nombre_pais) VALUES
  (1, 'El Salvador');

INSERT INTO catalogo.departamentos (id_departamento, id_pais, nombre_departamento) VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'La Libertad');

INSERT INTO catalogo.municipios (id_municipio, id_departamento, nombre_municipio) VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'Soyapango'),
  (3, 2, 'Santa Tecla');

INSERT INTO catalogo.marcas (id_marca, nombre_marca) VALUES
  (1, 'Samsung'),
  (2, 'LG'),
  (3, 'Sony'),
  (4, 'HP'),
  (5, 'Logitech'),
  (6, 'Xiaomi');

-- ==========================================================================
-- seguridad
-- ==========================================================================

INSERT INTO seguridad.usuarios (id_usuario, correo_inicio_sesion, contrasenia, eliminado, estado) VALUES
  (1, 'ana.martinez@example.com',    '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (2, 'carlos.lopez@example.com',    '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (3, 'maria.hernandez@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A'),
  (4, 'jose.ramirez@example.com',    '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', false, 'A');

INSERT INTO seguridad.usuarios_perfil (id_perfil, id_usuario, nombres, apellidos, dui, genero, fecha_nacimiento) VALUES
  (1, 1, 'Ana',   'Martínez',   '01234567-8', 'F', '1994-03-12'),
  (2, 2, 'Carlos','López',      '02345678-9', 'M', '1990-07-25'),
  (3, 3, 'María', 'Hernández',  '03456789-0', 'F', '1998-11-02'),
  (4, 4, 'José',  'Ramírez',    '04567890-1', 'M', '1985-01-30');

INSERT INTO seguridad.usuarios_contacto (id_contacto, id_usuario, tipo_contacto, valor, principal, verificado) VALUES
  (1, 1, 1, '7011-2233', true, true),
  (2, 2, 1, '7022-3344', true, true),
  (3, 3, 1, '7033-4455', true, false),
  (4, 4, 1, '7044-5566', true, true);

INSERT INTO seguridad.usuarios_direccion (id_direccion, id_usuario, id_municipio, direccion_linea_1, codigo_postal, tipo_direccion) VALUES
  (1, 1, 1, 'Colonia Escalón, Calle El Mirador #123',  1101, 'C'),
  (2, 2, 2, 'Reparto San Antonio, Av. Las Flores #45', 1502, 'C'),
  (3, 3, 3, 'Col. Jardines de la Sabana, Pje 2 #8',    1601, 'C'),
  (4, 4, 1, 'Col. Miramonte, 3ra Calle Pte #67',       1101, 'T');

-- ==========================================================================
-- core: catálogo de productos
-- ==========================================================================

INSERT INTO core.categorias (id_categoria, id_categoria_padre, nombre_categoria, descripccion) VALUES
  (1, NULL, 'Electrónica',       'Dispositivos y gadgets electrónicos'),
  (2, 1,    'Computación',       'Laptops, monitores y accesorios de cómputo'),
  (3, 1,    'Celulares',         'Teléfonos inteligentes y accesorios'),
  (4, NULL, 'Hogar',             'Electrodomésticos y artículos para el hogar'),
  (5, 4,    'Electrodomésticos', 'Línea blanca y pequeños electrodomésticos');

-- stock_disponible queda con valores variados (incluye 0 y stock muy bajo)
-- a propósito, para poder probar la futura alerta de stock bajo.
INSERT INTO core.productos (id_producto, id_categoria, id_marca, nombre_producto, descripcion_producto, stock_disponible, estado, metadata) VALUES
  (1,  2, 4, 'Laptop HP Pavilion 15',            'Laptop 15", Intel Core i5, 8GB RAM, 512GB SSD',            18, 'A', '{"color": "plata", "garantia_meses": 12}'),
  (2,  2, 1, 'Monitor Samsung 24" Full HD',      'Monitor LED 24 pulgadas, 1920x1080, 75Hz',                  3, 'A', NULL),
  (3,  2, 5, 'Mouse inalámbrico Logitech M170',  'Mouse óptico inalámbrico, USB 2.4GHz',                     42, 'A', NULL),
  (4,  2, 5, 'Teclado Logitech K380',            'Teclado inalámbrico multi-dispositivo Bluetooth',           0, 'A', NULL),
  (5,  3, 1, 'Samsung Galaxy A54',               'Smartphone 128GB, cámara triple 50MP, 5G',                  7, 'A', NULL),
  (6,  3, 6, 'Xiaomi Redmi Note 13',             'Smartphone 256GB, pantalla AMOLED 120Hz',                   2, 'A', NULL),
  (7,  3, 1, 'Cargador rápido Samsung 25W',      'Cargador USB-C de carga rápida 25W',                       65, 'A', NULL),
  (8,  1, 3, 'Audífonos Sony WH-1000XM4',        'Audífonos inalámbricos con cancelación de ruido',           1, 'A', NULL),
  (9,  1, 3, 'Parlante Sony SRS-XB13',           'Parlante Bluetooth portátil resistente al agua',           24, 'A', NULL),
  (10, 1, 2, 'Smart TV LG 50" 4K UHD',           'Televisor LED 4K con WebOS y HDR10',                        5, 'A', NULL),
  (11, 5, 2, 'Refrigeradora LG 14 pies',         'Refrigeradora No Frost, dispensador de agua',               4, 'A', NULL),
  (12, 5, 1, 'Lavadora Samsung 18kg',            'Lavadora carga superior, digital inverter',                 0, 'A', NULL),
  (13, 5, 2, 'Microondas LG 1.5 pies',           'Horno microondas con grill, 1000W',                        11, 'A', NULL),
  (14, 2, 4, 'Impresora HP DeskJet 2720',        'Impresora multifuncional a color con WiFi',                 9, 'A', NULL);

-- ==========================================================================
-- core: pedidos y su detalle (montos en USD, moneda oficial de El Salvador)
-- ==========================================================================

INSERT INTO core.pedidos (id_pedido, id_usuario, id_direccion, estado_pedido, total, fecha_creacion) VALUES
  (1, 1, 1, 'ENTREGADO', 615.98, now() - interval '20 days'),
  (2, 2, 2, 'ENVIADO',   404.00, now() - interval '6 days'),
  (3, 1, 1, 'PAGADO',    429.99, now() - interval '2 days'),
  (4, 3, 3, 'PENDIENTE', 319.98, now() - interval '1 day'),
  (5, 4, 4, 'CANCELADO', 129.99, now() - interval '15 days');

INSERT INTO core.detalles_pedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
  (1, 1, 1,  1, 599.99, 599.99),
  (2, 1, 3,  1,  15.99,  15.99),
  (3, 2, 5,  1, 379.00, 379.00),
  (4, 2, 7,  2,  12.50,  25.00),
  (5, 3, 10, 1, 429.99, 429.99),
  (6, 4, 8,  1, 279.99, 279.99),
  (7, 4, 9,  1,  39.99,  39.99),
  (8, 5, 13, 1, 129.99, 129.99);

INSERT INTO core.pagos (id_pago, id_pedido, metodo_pago, monto, estado_transaccion, fecha_pago) VALUES
  (1, 1, 'TARJETA_CREDITO', 615.98, 'APROBADO',  now() - interval '20 days'),
  (2, 2, 'PAYPAL',          404.00, 'APROBADO',  now() - interval '6 days'),
  (3, 3, 'TRANSFERENCIA',   429.99, 'APROBADO',  now() - interval '2 days'),
  (4, 5, 'TARJETA_CREDITO', 129.99, 'RECHAZADO', now() - interval '15 days');

INSERT INTO core.envios (id_envio, id_pedido, empresa_transporte, numero_seguimiento, estado_envio, fecha_despacho, fecha_entrega) VALUES
  (1, 1, 'Correos de El Salvador', 'CES-000123', 'ENTREGADO',   now() - interval '18 days', now() - interval '14 days'),
  (2, 2, 'DHL',                    'DHL-998877', 'EN_TRANSITO', now() - interval '4 days',  NULL);

-- Movimientos de inventario: salidas por venta ligadas a los pedidos ya
-- despachados, más entradas de stock inicial y un ajuste por conteo físico.
INSERT INTO core.movimientos_inventario (id_movimiento, id_producto, tipo_movimiento, cantidad, motivo, id_pedido, fecha_movimiento) VALUES
  (1, 1,  'SALIDA', 1, 'VENTA',            1, now() - interval '20 days'),
  (2, 3,  'SALIDA', 1, 'VENTA',            1, now() - interval '20 days'),
  (3, 5,  'SALIDA', 1, 'VENTA',            2, now() - interval '6 days'),
  (4, 7,  'SALIDA', 2, 'VENTA',            2, now() - interval '6 days'),
  (5, 8,  'ENTRADA', 5, 'COMPRA_PROVEEDOR', NULL, now() - interval '40 days'),
  (6, 8,  'AJUSTE',  4, 'AJUSTE',           NULL, now() - interval '3 days'),
  (7, 12, 'AJUSTE',  2, 'AJUSTE',           NULL, now() - interval '5 days');

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
