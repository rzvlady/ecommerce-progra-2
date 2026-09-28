-- Migración: agrega a "core"."productos" las columnas que la entidad Producto.java
-- ya esperaba (codigo_sku, precio_venta, precio_costo) pero que nunca se agregaron
-- a schema-ecommerce.sql. Sin esto, cualquier consulta JPA sobre Producto falla con
-- "column ... does not exist" (incluye el alta de producto y las alertas de stock).
--
-- Correr una sola vez, después de schema-ecommerce.sql y antes de seed-data.sql:
--   psql -U <usuario> -h localhost -d <base_de_datos> -f db/migracion-productos-sku-precio.sql

ALTER TABLE core.productos ADD COLUMN codigo_sku varchar;
ALTER TABLE core.productos ADD COLUMN precio_venta numeric(12,2);
ALTER TABLE core.productos ADD COLUMN precio_costo numeric(12,2);

-- Backfill por si la tabla ya tiene filas (por ejemplo, ejecutada sobre una base
-- que ya tenía productos sembrados antes de esta migración).
UPDATE core.productos SET codigo_sku = 'SKU-' || lpad(id_producto::text, 4, '0') WHERE codigo_sku IS NULL;
UPDATE core.productos SET precio_venta = 49.99, precio_costo = 30.00 WHERE precio_venta IS NULL;

ALTER TABLE core.productos ALTER COLUMN codigo_sku SET NOT NULL;
ALTER TABLE core.productos ADD CONSTRAINT productos_codigo_sku_key UNIQUE (codigo_sku);
ALTER TABLE core.productos ALTER COLUMN precio_venta SET NOT NULL;
