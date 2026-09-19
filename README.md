# E-commerce · Programación 2 (Grupo 1)

Aplicación de e-commerce con Spring Boot y Thymeleaf.

## Stack

- Java 21
- Spring Boot 4.1 (Web, Data JPA, Security, Validation, Thymeleaf)
- PostgreSQL
- Maven (con wrapper `mvnw` / `mvnw.cmd`)

## Estructura del proyecto

```
src/main/java/com/sv/grupo1/ecommerce/
├── ECommerceApplication.java
├── controllers/        # controladores Spring MVC
├── dao/                 # repositorios (Spring Data JPA)
├── dto/                  # objetos de transferencia para formularios
├── entities/
│   ├── catalogo/        # Departamento, Marca, Municipio, Pais
│   ├── core/             # Producto, Categoria, Pedido, DetallePedido, Envio, Pago, MovimientoInventario
│   └── seguridad/       # Usuario y datos asociados (contacto, perfil, dirección)
└── services/            # lógica de negocio

src/main/resources/
├── application.properties
└── templates/            # vistas Thymeleaf (admin/...)

db/
├── schema-ecommerce.sql                    # DDL: crea los esquemas seguridad, catalogo y core
├── migracion-productos-sku-precio.sql      # agrega codigo_sku/precio_venta/precio_costo a core.productos
├── seed-data.sql                           # datos de prueba (re-ejecutable)
└── e-commerce-design-db.dbml               # modelo fuente para dbdiagram.io
```

La base de datos usa tres esquemas: `seguridad` (usuarios y datos asociados), `catalogo` (país, departamento, municipio, marca) y `core` (productos, pedidos, pagos, envíos, movimientos de inventario).

## Base de datos local

1. Tener PostgreSQL corriendo en `localhost:5432`.
2. Crear la base de datos:
   ```
   createdb ecommerce_progra2_db
   ```
3. Cargar el esquema y la migración:
   ```
   psql -U postgres -d ecommerce_progra2_db -f db/schema-ecommerce.sql
   psql -U postgres -d ecommerce_progra2_db -f db/migracion-productos-sku-precio.sql
   ```
4. (Opcional) Poblar con datos de prueba — re-ejecutable, vacía sus propias tablas antes de insertar:
   ```
   psql -U postgres -d ecommerce_progra2_db -f db/seed-data.sql
   ```
5. Variables de entorno (opcionales — ver [application.properties](src/main/resources/application.properties)):

   | Variable | Default |
   |---|---|
   | `DB_NAME` | `ecommerce_progra2_db` |
   | `DB_USER` | `postgres` |
   | `DB_PASSWORD` | `postgres` |

## Correr la aplicación

```
./mvnw spring-boot:run      # Linux/Mac
mvnw.cmd spring-boot:run    # Windows
```

La app queda disponible en `http://localhost:8080`.

## Tests

```
./mvnw test
```
