CREATE DATABASE ecommerce_progra2_db;

CREATE SCHEMA seguridad;
CREATE SCHEMA catalogo;

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