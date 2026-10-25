CREATE DATABASE ecommerce_progra2_db;

CREATE SCHEMA seguridad;
CREATE SCHEMA catalogo;
CREATE SCHEMA core;

CREATE TABLE IF NOT EXISTS catalogo.paises (
	id_pais 				INTEGER GENERATED ALWAYS AS IDENTITY,
	nombre_pais 			VARCHAR NOT NULL,
	CONSTRAINT pk_paises PRIMARY KEY(id_pais)
);

CREATE TABLE IF NOT EXISTS catalogo.departamentos (
	id_departamento 		INTEGER GENERATED ALWAYS AS IDENTITY,
	id_pais 				INTEGER NOT NULL,
	nombre_departamento 	VARCHAR NOT NULL,
	CONSTRAINT pk_departamentos PRIMARY KEY(id_departamento),
	CONSTRAINT fk_departamentos_to_paises FOREIGN KEY(id_pais) REFERENCES catalogo.paises(id_pais)
);

CREATE TABLE IF NOT EXISTS catalogo.municipios (
	id_municipio 			INTEGER GENERATED ALWAYS AS IDENTITY,
	id_departamento 		INTEGER NOT NULL,
	nombre_municipio 		VARCHAR NOT NULL,
	CONSTRAINT pk_municipios PRIMARY KEY(id_municipio),
	CONSTRAINT fk_municipios_to_departamentos FOREIGN KEY(id_departamento) REFERENCES catalogo.departamentos(id_departamento)
);

CREATE TABLE IF NOT EXISTS seguridad.roles (
    id_rol              INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre              VARCHAR(50) NOT NULL,
    descripcion         VARCHAR(255),
    eliminado           BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_roles PRIMARY KEY(id_rol),
    CONSTRAINT uq_roles_nombre UNIQUE(nombre)
);

CREATE TABLE IF NOT EXISTS seguridad.usuarios (
	id_usuario 				INTEGER GENERATED ALWAYS AS IDENTITY,
	correo_inicio_sesion 	VARCHAR(255) NOT NULL,
	contrasenia 			VARCHAR(255) NOT NULL,
	eliminado 				BOOLEAN NOT NULL DEFAULT FALSE,
	estado 					CHAR(1) NOT NULL,
	ultimo_inicio_sesion 	TIMESTAMPTZ,
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	metadata 				JSONB,
	CONSTRAINT pk_usuarios PRIMARY KEY(id_usuario),
	CONSTRAINT uq_usuarios_correo_inicio_sesion UNIQUE(correo_inicio_sesion)
);

CREATE TABLE IF NOT EXISTS seguridad.usuarios_roles (
    id_usuario          INTEGER NOT NULL,
    id_rol              INTEGER NOT NULL,
    fecha_asignacion    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    auditoria           JSONB, /* Registro de quién asignó o modificó este rol y por qué */
    CONSTRAINT pk_usuarios_roles PRIMARY KEY(id_usuario, id_rol),
    CONSTRAINT fk_usuarios_roles_to_usuarios FOREIGN KEY(id_usuario) REFERENCES seguridad.usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_to_roles FOREIGN KEY(id_rol) REFERENCES seguridad.roles(id_rol)
);

CREATE TABLE IF NOT EXISTS seguridad.usuarios_contacto (
	id_contacto 			INTEGER GENERATED ALWAYS AS IDENTITY,
	id_usuario 				INTEGER NOT NULL,
	tipo_contacto 			CHAR(1),
	valor 					VARCHAR NOT NULL,
	principal 				BOOLEAN NOT NULL DEFAULT TRUE,
	verificado 				BOOLEAN,
	metadata 				JSONB,
	CONSTRAINT pk_usuarios_contacto PRIMARY KEY(id_contacto),
	CONSTRAINT fk_usuarios_contacto_to_usuarios FOREIGN KEY(id_usuario) REFERENCES seguridad.usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS seguridad.usuarios_perfil (
	id_perfil 				INTEGER GENERATED ALWAYS AS IDENTITY,
	id_usuario 				INTEGER NOT NULL,
	nombres 				VARCHAR NOT NULL,
	apellidos 				VARCHAR NOT NULL,
	dui 					VARCHAR NOT NULL,
	genero 					CHAR(1),
	fecha_nacimiento 		DATE,
	CONSTRAINT pk_usuarios_perfil PRIMARY KEY(id_perfil),
	CONSTRAINT uq_usuarios_perfil_dui UNIQUE(dui),
	CONSTRAINT uq_usuarios_perfil_usuario UNIQUE(id_usuario),
	CONSTRAINT fk_usuarios_perfil_to_usuarios FOREIGN KEY(id_usuario) REFERENCES seguridad.usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS seguridad.usuarios_direccion (
	id_direccion 			INTEGER GENERATED ALWAYS AS IDENTITY,
	id_usuario 				INTEGER NOT NULL,
	id_municipio 			INTEGER NOT NULL,
	direccion_linea_1 		VARCHAR NOT NULL,
	codigo_postal 			VARCHAR(10),
	tipo_direccion 			CHAR(1),
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	metadata 				JSONB,
	CONSTRAINT pk_usuarios_direccion PRIMARY KEY(id_direccion),
	CONSTRAINT fk_usuarios_direccion_to_usuarios FOREIGN KEY(id_usuario) REFERENCES seguridad.usuarios(id_usuario),
	CONSTRAINT fk_usuarios_direccion_to_municipios FOREIGN KEY(id_municipio) REFERENCES catalogo.municipios(id_municipio)
);

CREATE TABLE IF NOT EXISTS catalogo.marcas (
	id_marca 				INTEGER GENERATED ALWAYS AS IDENTITY,
	nombre_marca 			VARCHAR NOT NULL,
	descripcion_marca		VARCHAR(255),
	estado 					CHAR(1) NOT NULL DEFAULT 'A',
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_marcas PRIMARY KEY(id_marca)
);

CREATE TABLE IF NOT EXISTS catalogo.categorias (
	id_categoria 			INTEGER GENERATED ALWAYS AS IDENTITY,
	id_categoria_padre 		INTEGER,
	nombre_categoria 		VARCHAR NOT NULL,
	descripcion_categoria 	VARCHAR(255),
	estado 					CHAR(1) NOT NULL DEFAULT 'A',
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_categorias PRIMARY KEY(id_categoria),
	CONSTRAINT fk_categorias_to_categorias FOREIGN KEY(id_categoria_padre) REFERENCES catalogo.categorias(id_categoria)
);

CREATE TABLE IF NOT EXISTS core.productos (
	id_producto 			INTEGER GENERATED ALWAYS AS IDENTITY,
	codigo_sku 				VARCHAR(255) NOT NULL,
	id_categoria 			INTEGER NOT NULL,
	id_marca 				INTEGER NOT NULL,
	nombre_producto 		VARCHAR(100) NOT NULL,
	descripcion_producto 	VARCHAR(255) NOT NULL,
	precio_venta 			NUMERIC(12,2) NOT NULL,
	precio_costo 			NUMERIC(12,2),
	stock_disponible 		INTEGER DEFAULT 0,
	stock_minimo 			INTEGER DEFAULT 0,
	estado 					CHAR(1) NOT NULL DEFAULT 'A',
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	metadata 				JSONB,
	CONSTRAINT pk_productos PRIMARY KEY(id_producto),
	CONSTRAINT uq_productos_codigo_sku UNIQUE(codigo_sku),
	CONSTRAINT fk_productos_to_categorias FOREIGN KEY(id_categoria) REFERENCES catalogo.categorias(id_categoria),
	CONSTRAINT fk_productos_to_marcas FOREIGN KEY(id_marca) REFERENCES catalogo.marcas(id_marca),
	CONSTRAINT chk_productos_stock_disponible CHECK (stock_disponible >= 0)
);

CREATE TABLE IF NOT EXISTS core.movimientos_inventario (
	id_movimiento 			INTEGER GENERATED BY DEFAULT AS IDENTITY,
	id_producto 			INTEGER NOT NULL,
	tipo_movimiento 		VARCHAR NOT NULL,
	cantidad 				INTEGER NOT NULL,
	motivo 					VARCHAR NOT NULL,
	fecha_movimiento 		TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	id_pedido 				INTEGER,
	CONSTRAINT pk_movimientos_inventario PRIMARY KEY(id_movimiento),
	CONSTRAINT fk_movimientos_to_productos FOREIGN KEY(id_producto) REFERENCES core.productos(id_producto)
);

/* Estados de pedido (estado_pedido): P=Pendiente, C=Confirmado, E=Enviado, F=Finalizado/Entregado, A=Anulado */
CREATE TABLE IF NOT EXISTS core.pedidos (
	id_pedido 				INTEGER GENERATED ALWAYS AS IDENTITY,
	id_usuario 				INTEGER NOT NULL,
	id_direccion 			INTEGER NOT NULL,
	estado_pedido 			CHAR(1) NOT NULL DEFAULT 'P',
	descuento				NUMERIC(10,2) DEFAULT 0.0,
	total 					NUMERIC(10,2) NOT NULL,
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	fecha_actualizacion 	TIMESTAMPTZ,
	metadata_pedido 		JSONB,
	auditoria_pedido 		JSONB,
	CONSTRAINT pk_pedidos PRIMARY KEY(id_pedido),
	CONSTRAINT fk_pedidos_to_usuarios FOREIGN KEY(id_usuario) REFERENCES seguridad.usuarios(id_usuario),
	CONSTRAINT fk_pedidos_to_usuarios_direccion FOREIGN KEY(id_direccion) REFERENCES seguridad.usuarios_direccion(id_direccion)
);

CREATE TABLE IF NOT EXISTS core.detalles_pedido (
	id_detalle 				INTEGER GENERATED ALWAYS AS IDENTITY,
	id_pedido 				INTEGER NOT NULL,
	id_producto 			INTEGER NOT NULL,
	cantidad 				INTEGER NOT NULL,
	precio_unitario 		NUMERIC(10,2) NOT NULL,
	descuento				NUMERIC(10,2) DEFAULT 0.0,
	subtotal 				NUMERIC(10,2) NOT NULL,
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	metadata_det_pedido 	JSONB,
	auditoria_det_pedido 	JSONB,
	CONSTRAINT pk_detalles_pedido PRIMARY KEY(id_detalle),
	CONSTRAINT fk_detalles_pedido_to_pedidos FOREIGN KEY(id_pedido) REFERENCES core.pedidos(id_pedido),
	CONSTRAINT fk_detalles_pedido_to_productos FOREIGN KEY(id_producto) REFERENCES core.productos(id_producto)
);

/* Estados de transacción (estado_transaccion): P=Pendiente, E=Exitoso/Aprobado, R=Rechazado, V=Reversado */
CREATE TABLE IF NOT EXISTS core.pagos (
	id_pago 				INTEGER GENERATED ALWAYS AS IDENTITY,
	id_pedido 				INTEGER NOT NULL, /* UNIQUE removido para soportar múltiples intentos fallidos */
	metodo_pago 			VARCHAR(255) NOT NULL,
	monto 					NUMERIC(10,2) NOT NULL,
	estado_transaccion 		CHAR(1) NOT NULL,
	referencia_pasarela 	VARCHAR(255), /* Agregado para trazabilidad con el banco o proveedor */
	fecha_pago 				TIMESTAMPTZ,
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	auditoria_pago			JSONB,
	CONSTRAINT pk_pagos PRIMARY KEY(id_pago),
	CONSTRAINT fk_pagos_to_pedidos FOREIGN KEY(id_pedido) REFERENCES core.pedidos(id_pedido)
);

/* Estados de envío (estado_envio): P=Preparando, L=Listo para recolección, T=En Tránsito, E=Entregado, D=Devuelto */
CREATE TABLE IF NOT EXISTS core.envios (
	id_envio 				INTEGER GENERATED ALWAYS AS IDENTITY,
	id_pedido 				INTEGER UNIQUE NOT NULL,
	empresa_transporte 		VARCHAR(255),
	numero_seguimiento 		VARCHAR(100),
	costo_envio 			NUMERIC(10,2), /* Agregado para separar el costo del flete */
	estado_envio 			CHAR(1) NOT NULL,
	fecha_despacho 			TIMESTAMPTZ,
	fecha_entrega 			TIMESTAMPTZ,
	fecha_creacion 			TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
	auditoria_envio			JSONB,
	CONSTRAINT pk_envios PRIMARY KEY(id_envio),
	CONSTRAINT fk_envios_to_pedidos FOREIGN KEY(id_pedido) REFERENCES core.pedidos(id_pedido)
);