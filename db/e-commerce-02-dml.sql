BEGIN;

INSERT INTO
    seguridad.roles (nombre, descripcion)
VALUES
    ('ROLE_ADMIN',      'Administrador general del sistema con acceso total a configuración e inventario'),
    ('ROLE_VENDEDOR',   'Personal de la tienda encargado de gestionar pedidos, envíos y catálogo'),
    ('ROLE_CLIENTE',    'Usuario comprador estándar de la plataforma e-commerce'),
    ('ROLE_SOPORTE',    'Personal de atención al cliente con acceso a lectura de pedidos y usuarios');

COMMIT;

BEGIN;

-- ============================================================
-- 1. VALIDAR QUE EXISTAN LOS ROLES REQUERIDOS
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM seguridad.roles
        WHERE id_rol = 1
    ) OR NOT EXISTS (
        SELECT 1
        FROM seguridad.roles
        WHERE id_rol = 3
    ) THEN
        RAISE EXCEPTION
            'Deben existir los roles con id_rol 1 y 3.';
END IF;
END;
$$;

-- ============================================================
-- 2. INSERTAR USUARIOS, ROLES, PERFILES Y CONTACTOS
-- ============================================================

WITH personas (
               persona_id,
               nombres,
               apellidos,
               primer_nombre,
               primer_apellido,
               genero
    ) AS (
    VALUES
        (1, 'Adrian Enrique',   'Pinto Elizondo',
         'adrian',    'pinto',    'M'),
        (2, 'Jairo Joaquin',    'Flamenco Joya',
         'jairo',     'flamenco', 'M'),
        (3, 'Vladimir Antonio', 'Ruiz Estrada',
         'vladimir',  'ruiz',     'M'),
        (4, 'Brandon Ismael',   'Mejia Rivera',
         'brandon',   'mejia',    'M'),
        (5, 'Ivan Fernando',    'Diaz Escobar',
         'ivan',      'diaz',     'M'),
        (6, 'Miguel Angel',     'Serrano Alfaro',
         'miguel',    'serrano',  'M'),
        (7, 'Katya Abigail',    'Ruiz Fernandez',
         'katya',     'ruiz',     'F'),
        (8, 'Alexander Alberto','Emestica Ramirez',
         'alexander', 'emestica', 'M')
),

-- Generar las dos cuentas por cada persona.
     usuarios_entrada AS (
         SELECT
             p.*,
             r.id_rol,
             r.sufijo,
             p.primer_nombre || '-' ||
             p.primer_apellido || '-' ||
             r.sufijo || '@testing.com' AS correo,
             ((p.persona_id - 1) * 2
                 + CASE WHEN r.id_rol = 1 THEN 1 ELSE 2 END
                 ) AS numero_prueba
         FROM personas p
                  CROSS JOIN (
             VALUES
                 (1, 'admin'),
                 (3, 'cliente')
         ) AS r(id_rol, sufijo)
     ),

-- Insertar usuarios y recuperar sus ID reales.
     usuarios_insertados AS (
INSERT INTO seguridad.usuarios (
    correo_inicio_sesion,
    contrasenia,
    eliminado,
    estado,
    ultimo_inicio_sesion,
    metadata
)
SELECT
    correo,
    '$2a$10$q9GnYIlFFSaPabkZ0RfW9OaAtf0VgsAsoTm3P.WW1kcAfuWzRV3lW',
    FALSE,
    'A',
    NULL,
    jsonb_build_object(
            'origen', 'datos_prueba',
            'persona_id', persona_id,
            'tipo_usuario', sufijo
    )
FROM usuarios_entrada
ORDER BY persona_id, id_rol
    RETURNING id_usuario, correo_inicio_sesion
),

-- Relacionar cada usuario con los datos de entrada.
usuarios_contexto AS (
SELECT
    u.id_usuario,
    e.*
FROM usuarios_insertados u
    JOIN usuarios_entrada e
ON e.correo = u.correo_inicio_sesion
    ),

-- Asignar los roles correspondientes.
    roles_insertados AS (
INSERT INTO seguridad.usuarios_roles (
    id_usuario,
    id_rol,
    auditoria
)
SELECT
    id_usuario,
    id_rol,
    jsonb_build_object(
    'operacion', 'asignacion_rol',
    'motivo', 'creacion_usuario_prueba',
    'rol', sufijo
    )
FROM usuarios_contexto
    RETURNING id_usuario, id_rol
    ),

-- Crear un perfil independiente para cada cuenta.
    perfiles_insertados AS (
INSERT INTO seguridad.usuarios_perfil (
    id_usuario,
    nombres,
    apellidos,
    dui,
    genero,
    fecha_nacimiento
)
SELECT
    id_usuario,
    nombres,
    apellidos,
    lpad(numero_prueba::text, 8, '0')
    || '-'
    || (numero_prueba % 10)::text,
    genero,
    DATE '1990-01-01'
    + (persona_id * 30
    + CASE WHEN id_rol = 1 THEN 1 ELSE 2 END)
FROM usuarios_contexto
    RETURNING id_perfil, id_usuario
    ),

-- Crear correo y móvil para cada usuario.
    contactos_insertados AS (
INSERT INTO seguridad.usuarios_contacto (
    id_usuario,
    tipo_contacto,
    valor,
    principal,
    verificado,
    metadata
)
SELECT
    id_usuario,
    'C',
    correo,
    TRUE,
    FALSE,
    jsonb_build_object('origen', 'datos_prueba')
FROM usuarios_contexto

UNION ALL

SELECT
    id_usuario,
    'M',
    '7000-' || lpad(numero_prueba::text, 4, '0'),
    FALSE,
    FALSE,
    jsonb_build_object(
    'origen', 'datos_prueba',
    'nota', 'numero ficticio'
    )
FROM usuarios_contexto

    RETURNING id_contacto, id_usuario
    )

-- Resumen de los registros insertados.
SELECT
    (SELECT COUNT(*) FROM usuarios_insertados)
        AS usuarios_creados,
    (SELECT COUNT(*) FROM roles_insertados)
        AS roles_asignados,
    (SELECT COUNT(*) FROM perfiles_insertados)
        AS perfiles_creados,
    (SELECT COUNT(*) FROM contactos_insertados)
        AS contactos_creados;

COMMIT;