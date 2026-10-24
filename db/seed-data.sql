-- ==========================================================================
-- E-COMMERCE SEED DATA
-- Datos de prueba para catálogo, productos y movimientos de inventario.
-- Alineado a las nuevas configuraciones:
--   - Categorías en catalogo.categorias (jerarquía y prefijos de taxonomía)
--   - Marcas en catalogo.marcas con descripciones y estado activo
--   - SKU estandarizado: [TAXONOMIA_CATEGORIA]-[CODIGO_MARCA]-[CORRELATIVO]
--   - Metadata técnica JSONB con atributos (sin URLs de imágenes)
--   - Movimientos iniciales en core.movimientos_inventario para auditoría
--   - Omite el esquema seguridad (roles, usuarios y perfiles gestionados por separado)
--   - Las tablas geográficas se insertan con idempotencia (ON CONFLICT DO NOTHING)
--     para evitar borrados en cascada hacia seguridad.usuarios_direccion.
-- ==========================================================================

BEGIN;

-- 1. Limpieza de tablas de catálogo de productos e inventario
TRUNCATE TABLE
  core.movimientos_inventario,
  core.detalles_pedido,
  core.pagos,
  core.envios,
  core.pedidos,
  core.productos,
  catalogo.categorias,
  catalogo.marcas
RESTART IDENTITY CASCADE;

-- ==========================================================================
-- 2. CATALOGO GEOGRAFICO (PAISES, DEPARTAMENTOS, MUNICIPIOS)
-- ==========================================================================

INSERT INTO catalogo.paises (id_pais, nombre_pais) OVERRIDING SYSTEM VALUE VALUES
  (1, 'El Salvador')
ON CONFLICT (id_pais) DO NOTHING;

INSERT INTO catalogo.departamentos (id_departamento, id_pais, nombre_departamento) OVERRIDING SYSTEM VALUE VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'La Libertad'),
  (3, 1, 'Santa Ana'),
  (4, 1, 'San Miguel')
ON CONFLICT (id_departamento) DO NOTHING;

INSERT INTO catalogo.municipios (id_municipio, id_departamento, nombre_municipio) OVERRIDING SYSTEM VALUE VALUES
  (1, 1, 'San Salvador'),
  (2, 1, 'Soyapango'),
  (3, 1, 'Mejicanos'),
  (4, 1, 'Apopa'),
  (5, 2, 'Santa Tecla'),
  (6, 2, 'Antiguo Cuscatlán'),
  (7, 3, 'Santa Ana'),
  (8, 4, 'San Miguel')
ON CONFLICT (id_municipio) DO NOTHING;

-- ==========================================================================
-- 3. CATALOGO DE MARCAS
-- ==========================================================================

INSERT INTO catalogo.marcas (id_marca, nombre_marca, descripcion_marca, estado) OVERRIDING SYSTEM VALUE VALUES
  (1,  'Samsung',   'Tecnología, electrónica de consumo y telefonía móvil de vanguardia.', 'A'),
  (2,  'LG',        'Líder en pantallas de alta definición y línea blanca para el hogar.',   'A'),
  (3,  'Sony',      'Excelencia en imagen, audio profesional y entretenimiento interactivo.', 'A'),
  (4,  'HP',        'Cómputo personal, soluciones empresariales y equipos de impresión.',    'A'),
  (5,  'Logitech',  'Periféricos y accesorios para productividad, streaming y videojuegos.', 'A'),
  (6,  'Xiaomi',    'Dispositivos inteligentes con alta relación costo-beneficio.',          'A'),
  (7,  'Apple',     'Ecosistema premium de computación, telefonía móvil y wearables.',       'A'),
  (8,  'Dell',      'Estaciones de trabajo, portátiles de alto rendimiento y monitores.',    'A'),
  (9,  'Lenovo',    'Laptops ThinkPad, portátiles ultraligeras e innovación en hardware.',    'A'),
  (10, 'JBL',       'Acústica portátil, parlantes para exteriores y audífonos deportivos.',  'A'),
  (11, 'Philips',   'Electrodomésticos para cocina, iluminación y cuidado personal.',         'A'),
  (12, 'Whirlpool', 'Línea blanca de máxima durabilidad y refrigeración inteligente.',       'A'),
  (13, 'Nintendo',  'Consolas híbridas familiares, videojuegos y periféricos gamer.',        'A');

-- ==========================================================================
-- 4. CATALOGO DE CATEGORIAS (TAXONOMIA JERARQUICA)
-- Nivel 1 (Departamentos): id_categoria_padre = NULL
-- Nivel 2 (Categorías): id_categoria_padre apunta al Departamento
-- ==========================================================================

INSERT INTO catalogo.categorias (id_categoria, id_categoria_padre, nombre_categoria, descripcion_categoria, estado) OVERRIDING SYSTEM VALUE VALUES
  (1,  NULL, 'Electrónica',        'Departamento raíz de tecnología y dispositivos electrónicos.',   'A'),
  (2,  1,    'Computación',        'Laptops, computadoras de escritorio, monitores y componentes.',    'A'),
  (3,  1,    'Celulares',          'Smartphones, tablets y accesorios de telefonía móvil.',           'A'),
  (4,  1,    'Audio',              'Audífonos, barras de sonido y parlantes portátiles.',             'A'),
  (5,  1,    'Televisores',        'Smart TVs 4K, pantallas UHD y accesorios de video.',              'A'),
  (6,  1,    'Gaming',             'Consolas de videojuegos, controles y periféricos gamer.',          'A'),
  (7,  NULL, 'Hogar',              'Departamento de confort, equipamiento del hogar y línea blanca.', 'A'),
  (8,  7,    'Electrodomésticos',  'Refrigeradoras, lavadoras y pequeños electrodomésticos de cocina.', 'A'),
  (9,  7,    'Cuidado Personal',   'Dispositivos de afeitado, secado y cuidado corporal.',             'A'),
  (10, NULL, 'Accesorios',         'Accesorios transversales de conectividad, carga y transporte.',   'A');

-- ==========================================================================
-- 5. CORE DE PRODUCTOS
-- SKU estandarizado por Backoffice:
--   - Prefijos de categoría: ELC-COM, ELC-CEL, ELC-AUD, ELC-TEL, ELC-GAM, HOG-ELD, HOG-CUI, ACC
--   - Prefijos de marca: SAM, LG, SON, HP, LOG, XIA, APP, DEL, LEN, JBL, PHI, WHI, NIN
--   - Correlativo a 4 dígitos: %04d
--   - Nombres en MAYÚSCULAS según estándar de registro de backoffice
--   - Metadata técnica JSONB sin campo de imágenes (gestionadas físicamente)
-- ==========================================================================

INSERT INTO core.productos (
  id_producto, codigo_sku, id_categoria, id_marca,
  nombre_producto, descripcion_producto,
  precio_venta, precio_costo,
  stock_disponible, stock_minimo, estado, metadata
) OVERRIDING SYSTEM VALUE VALUES
  (1,  'ELC-COM-HP-0001',  2,  4,  'LAPTOP HP THINKPAD',                         'Laptop 15", i5, 8GB RAM, 1024GB SSD',                       743.89,  556.36, 5,  11, 'A', '{"procesador": "Intel Core i5", "ram": "8GB", "almacenamiento": "1TB SSD", "pantalla": "15.6 pulgadas"}'::jsonb),
  (2,  'ELC-COM-DEL-0002', 2,  8,  'LAPTOP DELL PAVILION',                       'Laptop 15", i7, 8GB RAM, 1024GB SSD',                       1089.49, 748.40, 44, 11, 'A', '{"procesador": "Intel Core i7", "ram": "8GB", "almacenamiento": "1TB SSD", "pantalla": "15.6 pulgadas"}'::jsonb),
  (3,  'ELC-COM-LEN-0003', 2,  9,  'LAPTOP LENOVO INSPIRON',                     'Laptop 15", i5, 8GB RAM, 256GB SSD',                        731.66,  435.93, 27, 15, 'A', '{"procesador": "Intel Core i5", "ram": "8GB", "almacenamiento": "256GB SSD", "pantalla": "15.6 pulgadas"}'::jsonb),
  (4,  'ELC-COM-APP-0004', 2,  7,  'LAPTOP APPLE MACBOOK AIR',                   'Laptop 15", M2, 8GB RAM, 1024GB SSD',                       697.84,  439.80, 0,  8,  'A', '{"procesador": "Apple M2", "ram": "8GB", "almacenamiento": "1TB SSD", "pantalla": "15.3 Liquid Retina"}'::jsonb),
  (5,  'ELC-COM-SAM-0005', 2,  1,  'MONITOR SAMSUNG 27" QHD',                    'Monitor curvo 27 pulgadas, 2560x1440, 144Hz',               270.98,  160.56, 0,  8,  'A', '{"resolucion": "2560x1440 QHD", "tasa_refresco": "144Hz", "panel": "Curvo VA", "tamano": "27 pulgadas"}'::jsonb),
  (6,  'ELC-COM-LOG-0006', 2,  5,  'MOUSE INALÁMBRICO LOGITECH MX MASTER',       'Mouse ergonómico inalámbrico, múltiples dispositivos',       64.97,   37.59,  76, 13, 'A', '{"conectividad": "Bluetooth / Wireless 2.4GHz", "sensor": "Darkfield 4000 DPI", "bateria": "Recargable USB-C"}'::jsonb),
  (7,  'ELC-COM-LOG-0007', 2,  5,  'TECLADO MECÁNICO LOGITECH G413',             'Teclado mecánico retroiluminado, switches táctiles',         71.02,   49.67,  0,  7,  'A', '{"tipo_switch": "Mecánico Romer-G Táctil", "retroiluminacion": "LED Blanco", "estructura": "Aluminio cepillado"}'::jsonb),
  (8,  'ELC-COM-HP-0008',  2,  4,  'IMPRESORA HP DESKJET 2720',                  'Impresora multifuncional a color con WiFi',                 83.57,   52.01,  68, 12, 'A', '{"tipo": "Inyección térmica de tinta", "funciones": "Impresión, copia, escaneo", "conectividad": "WiFi, USB 2.0"}'::jsonb),
  (9,  'ELC-CEL-SAM-0009', 3,  1,  'SMARTPHONE SAMSUNG GALAXY A54',              'Smartphone 128GB, cámara múltiple, 5G',                     811.11,  526.17, 75, 5,  'A', '{"pantalla": "6.4 Super AMOLED 120Hz", "almacenamiento": "128GB", "ram": "8GB", "red": "5G"}'::jsonb),
  (10, 'ELC-CEL-APP-0010', 3,  7,  'SMARTPHONE APPLE IPHONE 13',                 'Smartphone 128GB, cámara múltiple, 5G',                     763.85,  515.97, 28, 4,  'A', '{"pantalla": "6.1 Super Retina XDR", "almacenamiento": "128GB", "chip": "A15 Bionic", "red": "5G"}'::jsonb),
  (11, 'ELC-CEL-XIA-0011', 3,  6,  'SMARTPHONE XIAOMI REDMI NOTE 13',            'Smartphone 256GB, cámara múltiple, 5G',                     436.46,  245.60, 90, 9,  'A', '{"pantalla": "6.67 AMOLED 120Hz", "almacenamiento": "256GB", "ram": "8GB", "camara": "108MP"}'::jsonb),
  (12, 'ELC-CEL-LG-0012',  3,  2,  'SMARTPHONE LG WING',                         'Smartphone 128GB, cámara múltiple, 5G',                     965.67,  579.68, 9,  12, 'A', '{"pantalla": "6.8 P-OLED Giratoria", "almacenamiento": "128GB", "ram": "8GB", "conectividad": "5G"}'::jsonb),
  (13, 'ELC-CEL-SAM-0013', 3,  1,  'TABLET SAMSUNG GALAXY TAB A9',               'Tablet 64GB, pantalla 11", ideal para trabajo y estudio',   193.76,  134.50, 95, 3,  'A', '{"pantalla": "11 WQXGA", "almacenamiento": "64GB", "bateria": "7040mAh", "wifi": "Doble Banda"}'::jsonb),
  (14, 'ELC-CEL-SAM-0014', 3,  1,  'CARGADOR RÁPIDO SAMSUNG 25W',                'Cargador USB-C de carga rápida 25W',                        10.93,   7.69,   50, 11, 'A', '{"potencia": "25W", "puerto": "USB Tipo C", "tecnologia": "Power Delivery (PD 3.0)"}'::jsonb),
  (15, 'ELC-CEL-XIA-0015', 3,  6,  'FUNDA PROTECTORA XIAOMI REDMI NOTE 13',       'Funda de silicona con protección de esquinas',              6.87,    4.00,   7,  9,  'A', '{"material": "Silicona TPU Flexible", "compatibilidad": "Xiaomi Redmi Note 13", "color": "Negro Mate"}'::jsonb),
  (16, 'ELC-AUD-SON-0016', 4,  3,  'AUDÍFONOS SONY WH-1000XM4',                  'Audífonos inalámbricos con cancelación de ruido',           275.01,  204.71, 0,  7,  'A', '{"tipo": "Over-Ear", "cancelacion_ruido": "Activa (ANC)", "autonomia": "30 horas", "bluetooth": "5.0"}'::jsonb),
  (17, 'ELC-AUD-JBL-0017', 4,  10, 'AUDÍFONOS JBL TUNE 510BT',                   'Audífonos inalámbricos con cancelación de ruido',           203.10,  147.07, 29, 11, 'A', '{"tipo": "On-Ear", "sonido": "JBL Pure Bass", "autonomia": "40 horas", "carga_rapida": "Sí"}'::jsonb),
  (18, 'ELC-AUD-LOG-0018', 4,  5,  'AUDÍFONOS LOGITECH G435',                    'Audífonos inalámbricos con cancelación de ruido',           263.10,  176.75, 9,  15, 'A', '{"conectividad": "Lightspeed Wireless / Bluetooth", "peso": "165g", "microfonos": "Doble haz"}'::jsonb),
  (19, 'ELC-AUD-PHI-0019', 4,  11, 'AUDÍFONOS PHILIPS TAH4205',                  'Audífonos inalámbricos con cancelación de ruido',           240.27,  177.97, 5,  7,  'A', '{"tipo": "On-Ear", "controladores": "32mm", "autonomia": "29 horas", "boton_bass": "Sí"}'::jsonb),
  (20, 'ELC-AUD-JBL-0020', 4,  10, 'PARLANTE BLUETOOTH JBL FLIP 6',              'Parlante portátil resistente al agua IP67',                 112.17,  68.97,  84, 3,  'A', '{"potencia": "20W RMS", "resistencia_agua": "IP67", "autonomia": "12 horas", "bluetooth": "5.1"}'::jsonb),
  (21, 'ELC-AUD-SON-0021', 4,  3,  'BARRA DE SONIDO SONY HT-S100F',              'Barra de sonido 2.0 canales, 120W',                         158.12,  114.75, 0,  8,  'A', '{"potencia": "120W", "canales": "2.0 con Bass Reflex", "conectividad": "HDMI ARC, Bluetooth, Óptico"}'::jsonb),
  (22, 'ELC-TEL-SAM-0022', 5,  1,  'SMART TV SAMSUNG 50" 4K UHD',                'Televisor LED 50" 4K con HDR10 y sistema operativo smart',  514.69,  289.04, 42, 7,  'A', '{"tamano": "50 pulgadas", "resolucion": "4K UHD (3840x2160)", "hdr": "HDR10+", "so": "Tizen"}'::jsonb),
  (23, 'ELC-TEL-LG-0023',  5,  2,  'SMART TV LG 55" 4K UHD',                     'Televisor LED 55" 4K con HDR10 y sistema operativo smart',  622.27,  433.34, 19, 4,  'A', '{"tamano": "55 pulgadas", "resolucion": "4K UHD (3840x2160)", "procesador": "a5 Gen5 AI", "so": "webOS"}'::jsonb),
  (24, 'ELC-TEL-SON-0024', 5,  3,  'SMART TV SONY 43" 4K UHD',                   'Televisor LED 43" 4K con HDR10 y sistema operativo smart',  794.23,  558.48, 88, 15, 'A', '{"tamano": "43 pulgadas", "resolucion": "4K UHD (3840x2160)", "motor": "4K X-Reality PRO", "so": "Google TV"}'::jsonb),
  (25, 'ELC-TEL-SAM-0025', 5,  1,  'SMART TV SAMSUNG 65" 4K UHD',                'Televisor LED 65" 4K con HDR10 y sistema operativo smart',  663.48,  490.56, 0,  10, 'A', '{"tamano": "65 pulgadas", "resolucion": "4K UHD (3840x2160)", "purcolor": "Sí", "so": "Tizen"}'::jsonb),
  (26, 'ELC-GAM-NIN-0026', 6,  13, 'CONSOLA NINTENDO SWITCH OLED',               'Consola híbrida con pantalla OLED de 7 pulgadas',           331.70,  210.51, 10, 13, 'A', '{"pantalla": "7.0 pulgadas OLED", "almacenamiento": "64GB", "audio": "Altavoces integrados optimizados"}'::jsonb),
  (27, 'ELC-GAM-SON-0027', 6,  3,  'CONTROL INALÁMBRICO SONY DUALSENSE',         'Control inalámbrico para PlayStation 5',                    66.55,   46.32,  13, 13, 'A', '{"compatibilidad": "PS5, PC, Móvil", "retroalimentacion": "Háptica inmersiva", "gatillos": "Adaptativos"}'::jsonb),
  (28, 'ELC-GAM-LOG-0028', 6,  5,  'AUDÍFONOS GAMING LOGITECH G335',             'Audífonos con micrófono para gaming, sonido envolvente',    62.61,   46.90,  7,  9,  'A', '{"conexion": "Jack 3.5mm", "peso": "240g", "transductores": "Neodimio 40mm", "certificacion": "Discord"}'::jsonb),
  (29, 'ELC-GAM-NIN-0029', 6,  13, 'CONTROL NINTENDO JOY-CON (PAR)',             'Par de controles Joy-Con para Nintendo Switch',             71.15,   45.56,  81, 10, 'A', '{"color": "Azul Neón / Rojo Neón", "sensores": "Giroscopio, Acelerómetro", "vibracion": "HD Rumble"}'::jsonb),
  (30, 'HOG-ELD-LG-0030',  8,  2,  'REFRIGERADORA LG 14 PIES',                   'Refrigeradora No Frost, dispensador de agua',               809.23,  448.48, 3,  6,  'A', '{"capacidad": "14 pies cúbicos", "tecnologia": "Smart Inverter", "enfriamiento": "DoorCooling+", "color": "Acero Inox"}'::jsonb),
  (31, 'HOG-ELD-SAM-0031', 8,  1,  'REFRIGERADORA SAMSUNG 14 PIES',              'Refrigeradora No Frost, dispensador de agua',               842.13,  464.38, 45, 12, 'A', '{"capacidad": "14 pies cúbicos", "compresor": "Digital Inverter", "sistema": "All Around Cooling", "color": "Plata"}'::jsonb),
  (32, 'HOG-ELD-WHI-0032', 8,  12, 'REFRIGERADORA WHIRLPOOL 14 PIES',            'Refrigeradora No Frost, dispensador de agua',               652.05,  367.42, 37, 3,  'A', '{"capacidad": "14 pies cúbicos", "tecnologia": "Evox anti-huellas", "dispensador": "Agua exterior", "color": "Gris Titanio"}'::jsonb),
  (33, 'HOG-ELD-PHI-0033', 8,  11, 'LAVADORA PHILIPS 18KG',                      'Lavadora carga superior, digital inverter',                 540.71,  305.05, 0,  8,  'A', '{"capacidad": "18 kg", "tipo_carga": "Superior", "tina": "Acero inoxidable", "programas_lavado": "8"}'::jsonb),
  (34, 'HOG-ELD-PHI-0034', 8,  11, 'LICUADORA PHILIPS SERIE 5000',               'Licuadora 600W, vaso de vidrio 1.5L',                       44.12,   26.16,  8,  15, 'A', '{"potencia": "600W", "vaso": "Vidrio 1.5L resistente", "cuchillas": "ProBlend 4 de acero inoxidable"}'::jsonb),
  (35, 'HOG-ELD-WHI-0035', 8,  12, 'ASPIRADORA WHIRLPOOL WVC-2200',              'Aspiradora sin bolsa, 1200W',                               78.51,   47.00,  0,  10, 'A', '{"potencia": "1200W", "filtro": "HEPA lavable", "capacidad_deposito": "2.2L", "cable": "5 metros retráctil"}'::jsonb),
  (36, 'HOG-CUI-PHI-0036', 9,  11, 'AFEITADORA ELÉCTRICA PHILIPS SERIE 3000',    'Afeitadora recargable con cabezales flexibles',             35.24,   20.05,  38, 9,  'A', '{"cabezales": "Flex 5D pivotantes", "uso": "Seco y húmedo", "autonomia": "60 minutos inalámbrica"}'::jsonb),
  (37, 'HOG-CUI-PHI-0037', 9,  11, 'SECADORA DE CABELLO PHILIPS ESSENTIALCARE',  'Secadora 2100W, 6 configuraciones de calor',                 25.34,   16.02,  67, 8,  'A', '{"potencia": "2100W", "ajustes": "6 velocidades y temperaturas", "boquilla": "Concentradora 14mm"}'::jsonb),
  (38, 'HOG-CUI-XIA-0038', 9,  6,  'BÁSCULA INTELIGENTE XIAOMI BODY COMPOSITION','Báscula con medición de composición corporal, Bluetooth',      34.05,   23.18,  94, 3,  'A', '{"metricas": "13 datos corporales", "sensores": "Bioimpedancia de alta precisión", "conectividad": "Bluetooth 5.0"}'::jsonb),
  (39, 'ACC-XIA-0039',     10, 6,  'POWER BANK XIAOMI 20000MAH',                 'Batería portátil de carga rápida 20000mAh',                 29.02,   20.19,  59, 3,  'A', '{"capacidad": "20000mAh 74Wh", "salidas": "2x USB-A + 1x USB-C", "potencia_maxima": "18W Fast Charge"}'::jsonb),
  (40, 'ACC-LOG-0040',     10, 5,  'MOCHILA PARA LAPTOP LOGITECH 15.6"',         'Mochila acolchada con compartimento para laptop',           37.18,   21.87,  40, 4,  'A', '{"compatibilidad": "Laptops hasta 15.6 pulgadas", "material": "Poliéster impermeable", "compartimentos": "Multiuso con cierre"}'::jsonb),
  (41, 'ACC-APP-0041',     10, 7,  'HUB USB-C APPLE MULTIPUERTO',                'Adaptador USB-C a HDMI, USB-A y carga',                      34.28,   20.11,  64, 5,  'A', '{"puertos": "1x HDMI 4K, 1x USB 3.0, 1x USB-C Pass-through", "compatibilidad": "MacBook / iPad Pro"}'::jsonb),
  (42, 'ACC-SAM-0042',     10, 1,  'CARGADOR INALÁMBRICO SAMSUNG 15W',           'Base de carga inalámbrica rápida Qi',                       25.94,   18.46,  65, 4,  'A', '{"potencia": "15W Fast Wireless Charging", "compatibilidad": "Qi Certified", "enfriamiento": "Ventilador integrado"}'::jsonb);

-- ==========================================================================
-- 6. MOVIMIENTOS INICIALES DE INVENTARIO
-- Se registran entradas de auditoría para productos con stock disponible > 0,
-- consistente con BackofficeProductoServiceImpl.registrarProducto().
-- ==========================================================================

INSERT INTO core.movimientos_inventario (
  id_producto, tipo_movimiento, cantidad, motivo, fecha_movimiento
)
SELECT
  id_producto,
  'ENTRADA',
  stock_disponible,
  'COMPRA_PROVEEDOR - Stock inicial de registro',
  CURRENT_TIMESTAMP
FROM
  core.productos
WHERE
  stock_disponible > 0;

-- ==========================================================================
-- 7. REAJUSTE DE SECUENCIAS IDENTITY
-- Garantiza que las próximas inserciones generadas por la aplicación no
-- colisionen con las llaves primarias sembradas.
-- ==========================================================================

SELECT setval(pg_get_serial_sequence('catalogo.paises', 'id_pais'), coalesce(max(id_pais), 1)) FROM catalogo.paises;
SELECT setval(pg_get_serial_sequence('catalogo.departamentos', 'id_departamento'), coalesce(max(id_departamento), 1)) FROM catalogo.departamentos;
SELECT setval(pg_get_serial_sequence('catalogo.municipios', 'id_municipio'), coalesce(max(id_municipio), 1)) FROM catalogo.municipios;
SELECT setval(pg_get_serial_sequence('catalogo.marcas', 'id_marca'), coalesce(max(id_marca), 1)) FROM catalogo.marcas;
SELECT setval(pg_get_serial_sequence('catalogo.categorias', 'id_categoria'), coalesce(max(id_categoria), 1)) FROM catalogo.categorias;
SELECT setval(pg_get_serial_sequence('core.productos', 'id_producto'), coalesce(max(id_producto), 1)) FROM core.productos;
SELECT setval(pg_get_serial_sequence('core.movimientos_inventario', 'id_movimiento'), coalesce(max(id_movimiento), 1)) FROM core.movimientos_inventario;

COMMIT;
