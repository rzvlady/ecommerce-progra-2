BEGIN;

INSERT INTO
    seguridad.roles (nombre, descripcion)
VALUES
    ('ROLE_ADMIN',      'Administrador general del sistema con acceso total a configuración e inventario'),
    ('ROLE_VENDEDOR',   'Personal de la tienda encargado de gestionar pedidos, envíos y catálogo'),
    ('ROLE_CLIENTE',    'Usuario comprador estándar de la plataforma e-commerce'),
    ('ROLE_SOPORTE',    'Personal de atención al cliente con acceso a lectura de pedidos y usuarios');

COMMIT;