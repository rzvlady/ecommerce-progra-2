# MultiStore · Programación II

> Proyecto final para la asignatura de **Programación II**, Facultad de Ingeniería, Ingeniería en Desarrollo de Software y Ciencia de Datos — Universidad Evangélica de El Salvador.

---

## 1. Integrantes del Equipo

| N° | Nombre Completo | Carnet / Identificador |
|:--:|:---|:---|
| 1 | Ivan Fernando Días Escobar | 2026010077 |
| 2 | Miguel Ángel Serrano Alfaro | 2026010554 |
| 3 | Katya Abigaíl Ruiz Fernández | 2026010182 |
| 4 | Adrian Enrique Pinto Elizondo | 2026010255 |
| 5 | Alexander Alberto Eméstica Ramírez | 2026010232 |
| 6 | Jairo Joaquín Flamenco Joya | 2026011324 |
| 7 | Vladimir Antonio Ruiz Estrada | 2026010465 |
| 8 | Brandon Ismael Mejía Rivera | 2026010101 |

---

## 2. Tecnologías Utilizadas

- **Lenguaje:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 4.1.0
  - Spring MVC (Web)
  - Spring Data JPA & Hibernate ORM
  - Spring Security 6 (Autenticación basada en roles, BCrypt, protección CSRF)
  - Spring Validation (Validación declarativa mediante Bean Validation)
- **Persistencia Híbrida:**
  - **JDBC Puro:** `Connection`, `PreparedStatement`, `ResultSet`, `try-with-resources` y manejo de excepciones desacopladas con `DatabasePersistenceException`.
  - **Spring Data JPA:** Mapeo relacional de entidades complejas y transaccionalidad.
- **Pool de Conexiones:** Apache Tomcat JDBC Connection Pool (`org.apache.tomcat.jdbc.pool.DataSource`) con 15 parámetros técnicos configurados.
- **Concurrencia:** `ExecutorService`, `ThreadPoolExecutor`, `Callable<T>`, `Future<T>` y estructuras atómicas (`AtomicInteger`).
- **Base de Datos:** PostgreSQL 15 (Alojada en Supabase Cloud con SSL requerido).
- **Scripts de Base de Datos:** `database/schema.sql` (DDL) y `database/data.sql` (DML / Semillas).
- **Motor de Plantillas & Frontend:** Thymeleaf 3, HTML5, CSS3, Tailwind CSS, JavaScript Vanilla (Fetch API y modals interactivos).
- **Pruebas Automatizadas:** JUnit 5 (Jupiter), Mockito 5, Assertions estructuradas y JaCoCo.
- **Gestor de Construcción:** Maven con Maven Wrapper (`./mvnw` y `.\mvnw.cmd`).
- **Integraciones:** Microservicio REST simulador de Pasarela de Pago (`mock-gateway-api`).

---

## 3. Requisitos Previos y Entorno

Para compilar y ejecutar el proyecto se requiere:

1. **Java Development Kit (JDK):** Versión 21 instalada y configurada en la variable de entorno `JAVA_HOME`.
   ```bash
   java -version
   ```
2. **Acceso a Internet:** Necesario para resolver dependencias de Maven y para conectar con la base de datos PostgreSQL alojada en Supabase.
3. **Maven Wrapper:** No es indispensable tener Maven instalado globalmente; el repositorio incluye los ejecutables del wrapper (`./mvnw` para Unix/macOS y `.\mvnw.cmd` para Windows).
4. **Navegador Web Moderno:** Chrome, Edge, Firefox o Safari con soporte para HTML5 y JavaScript.

---

## 4. Configuración de Base de Datos y Pool de Conexiones

### 4.1 Conexión Relacional y Scripts SQL
El sistema se encuentra configurado para conectarse a una instancia de PostgreSQL en la nube (Supabase) con modo SSL obligatorio. Las credenciales se encuentran parametrizadas en `ecommerce-webapp/src/main/resources/application.properties`:

- **URL:** `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
- **Driver:** `org.postgresql.Driver`
- **Esquemas relacionales:** `catalogo`, `core`, `seguridad`
- **Scripts en el repositorio:**
  - `database/schema.sql`: Definición DDL de todos los esquemas, tablas, restricciones de clave foránea e índices.
  - `database/data.sql`: Carga DML de datos iniciales (roles, usuarios administradores y clientes, taxonomía de categorías, marcas, productos y existencias iniciales).

### 4.2 Pool de Conexiones DataSource (`DataSourceConfig`)
La clase [`DataSourceConfig`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/config/DataSourceConfig.java) configura explícitamente el pool de conexiones de alto rendimiento **Apache Tomcat JDBC Pool**, documentando los 15 parámetros exigidos por la rúbrica:

| Parámetro | Valor Configurado | Justificación Técnica |
|---|---|---|
| `validationQuery` | `SELECT 1` | Consulta liviana ejecutada para verificar la salud de la conexión. |
| `initialSize` | `5` | Conexiones físicas creadas inmediatamente al arrancar el contenedor. |
| `minIdle` | `5` | Mínimo de conexiones inactivas retenidas listas para responder. |
| `maxActive` | `20` | Capacidad máxima concurrente para prevenir saturación de la base de datos. |
| `maxIdle` | `10` | Conexiones inactivas permitidas antes de liberar recursos sobrantes. |
| `maxWait` | `10000 ms` | Tiempo límite (10s) de espera al solicitar conexión antes de lanzar excepción. |
| `testOnBorrow` | `true` | Valida que la conexión esté viva antes de entregarla al hilo solicitante. |
| `testOnReturn` | `false` | Optimización de latencia evitando validación redundante al devolver la conexión. |
| `testWhileIdle` | `true` | El hilo de mantenimiento prueba conexiones inactivas en segundo plano. |
| `testOnConnect` | `true` | Valida la conectividad física al momento exacto de abrir un socket con la BD. |
| `timeBetweenEvictionRunsMillis` | `30000 ms` | Frecuencia de revisión (cada 30s) para detectar conexiones ociosas o muertas. |
| `minEvictableIdleTimeMillis` | `60000 ms` | Tiempo de inactividad (60s) requerido para que una conexión sea candidata a desalojo. |
| `removeAbandoned` | `true` | Mecanismo de seguridad contra fugas de conexiones que no fueron cerradas. |
| `removeAbandonedTimeout` | `60 s` | Umbral para considerar que una conexión fue abandonada y forzar su recuperación. |
| `validationInterval` | `30000 ms` | Evita validaciones sucesivas innecesarias en una misma conexión en menos de 30s. |

---

## 5. Cómo Ejecutar el Proyecto

El sistema se compone de dos módulos Maven coordinados:
1. **`mock-gateway-api`** (Puerto 8081): Simulador local de pasarela de pago bancaria, indispensable para procesar transacciones de tarjeta durante el checkout.
2. **`ecommerce-webapp`** (Puerto 8080): Aplicación web principal que alberga la Tienda online y el Panel Administrativo (Backoffice).

### 5.1 Iniciar el Simulador de Pasarela (`mock-gateway-api`)
En una **primera terminal**, levantar el servicio en el puerto `8081`:

* **En Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run -pl mock-gateway-api
  ```
* **En Linux / macOS:**
  ```bash
  chmod +x ./mvnw
  ./mvnw spring-boot:run -pl mock-gateway-api
  ```
  *(El servicio quedará a la escucha en `http://localhost:8081/mock-gateway/v1/payments`).*

### 5.2 Iniciar la Aplicación Web (`ecommerce-webapp`)
En una **segunda terminal**, levantar la plataforma de comercio electrónico:

* **En Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run -pl ecommerce-webapp
  ```
* **En Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run -pl ecommerce-webapp
  ```

Una vez desplegada la aplicación, estará disponible en:
- **Tienda Pública:** [http://localhost:8080](http://localhost:8080)
- **Panel Administrativo (Backoffice):** [http://localhost:8080/backoffice](http://localhost:8080/backoffice)

---

## 6. Cómo Ejecutar las Pruebas Unitarias

El proyecto cuenta con suites de pruebas unitarias implementadas con JUnit 5 y Mockito, cubriendo la lógica de negocio, persistencia JDBC pura, concurrencia y controladores.

Para ejecutar todas las pruebas unitarias y generar el informe de cobertura:

### En Windows:
```powershell
.\mvnw.cmd test -pl ecommerce-webapp
```

### En Linux / macOS:
```bash
./mvnw test -pl ecommerce-webapp
```

Para ejecutar específicamente las pruebas de persistencia JDBC y concurrencia:
```bash
./mvnw test -Dtest="MovimientoInventarioJdbcDAOTest,AuditoriaInventarioConcurrenteServiceTest" -pl ecommerce-webapp
```

---

## 7. Credenciales de Prueba

Para la evaluación y defensa del proyecto, se encuentran disponibles cuentas de prueba dedicadas para el docente evaluador:

### 7.1 Acceso al Backoffice (Administrador)
- **URL de Ingreso:** [http://localhost:8080/backoffice/login](http://localhost:8080/backoffice/login)
- **Usuario / Correo:** `docente.admin@ecommerce.com`
- **Contraseña:** `Docente2026!`
- **Rol asignado:** `ROLE_ADMIN`
- **Permisos:** Gestión completa del catálogo (categorías y marcas), inventario con JDBC puro, auditoría concurrente de stock y administración de usuarios.

### 7.2 Acceso a la Tienda (Cliente)
- **URL de Ingreso:** [http://localhost:8080/login](http://localhost:8080/login)
- **Usuario / Correo:** `docente.cliente@ecommerce.com`
- **Contraseña:** `Docente2026!`
- **Rol asignado:** `ROLE_CLIENTE`
- **Permisos:** Navegación por catálogo y filtros, agregar productos al carrito, proceso de checkout con pasarela simulada (`mock-gateway-api`) y consulta de historial de pedidos.

---

## 8. Persistencia con JDBC Puro

De acuerdo a la rúbrica del Avance 3, se implementó una capa de persistencia mediante **JDBC puro** desacoplada de ORM:

- **Interfaz:** [`MovimientoInventarioJdbcDAO`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/dao/jdbc/MovimientoInventarioJdbcDAO.java)
- **Implementación:** [`MovimientoInventarioJdbcDAOImpl`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/dao/jdbc/MovimientoInventarioJdbcDAOImpl.java)
- **Operaciones CRUD:**
  - `insertar(movimiento)`: Ejecuta `PreparedStatement` con `executeUpdate()` y recupera llaves autogeneradas (`RETURN_GENERATED_KEYS`).
  - `buscarPorId(id)`: Ejecuta consulta SQL contra `core.movimientos_inventario` y `core.productos` con `executeQuery()` y extrae datos mediante `ResultSet`.
  - `listarTodos()` y `listarPorProducto(id)`: Mapea cursores relacionales a colecciones tipadas.
  - `actualizarMotivoYCantidad(id, motivo, cantidad)`: Actualiza registros con `executeUpdate()`.
  - `eliminar(id)`: Elimina registros físicos con `executeUpdate()`.
  - `contarPorTipo(tipo)`: Realiza funciones de agregación SQL directa.
- **Gestión de Recursos y Errores:**
  - Uso estricto de sentencias `try-with-resources` para garantizar el cierre determinista de `Connection`, `PreparedStatement` y `ResultSet`.
  - Captura de `SQLException` y encapsulamiento en la excepción personalizada [`DatabasePersistenceException`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/exceptions/DatabasePersistenceException.java).
  - Manejo global de excepciones mediante [`GlobalExceptionHandler`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/exceptions/GlobalExceptionHandler.java), registrando el stack trace en logs y mostrando `templates/error.html` sin exponer datos sensibles.

---

## 9. Descripción de la Programación Concurrente + Base de Datos

### 9.1 Proceso Ejecutado Concurrentemente
El módulo concurrente implementa el servicio de **Auditoría de Integridad de Inventario y Conciliación Multihilo** ([`AuditoriaInventarioConcurrenteService`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/services/AuditoriaInventarioConcurrenteService.java) y [`AuditoriaInventarioConcurrenteServiceImpl`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/services/impl/AuditoriaInventarioConcurrenteServiceImpl.java)).

### 9.2 Justificación de la Concurrencia
En plataformas de comercio electrónico con amplios catálogos y transacciones simultáneas de compra, la validación de inventarios por categoría puede provocar tiempos de respuesta elevados si se ejecuta de forma secuencial. Mediante la partición por categorías y ejecución concurrente:
- Se divide la carga de trabajo entre múltiples hilos de ejecución paralelos.
- Se optimiza el tiempo de CPU y de latencia I/O hacia la base de datos relacional.
- Se detectan productos en umbral crítico de stock (`core.productos.stock_disponible <= stock_minimo`) o con discrepancias de movimiento en tiempo real.

### 9.3 Mecanismo Utilizado
- **ExecutorService & ThreadPoolExecutor:** Pool de hilos de tamaño configurable administrado mediante tareas `Callable<AuditoriaCategoriaItem>`.
- **ThreadFactory Personalizado:** Asigna nombres rastreables a cada hilo de trabajo (`audit-worker-thread-X`).
- **Seguridad Concurrente (Thread-Safety):** 
  - Uso de colecciones seguras e inmutabilidad de resultados intermedios.
  - Sincronización libre de condiciones de carrera utilizando contadores atómicos (`AtomicInteger`).
  - Cierre ordenado del pool con `awaitTermination()` y control de interrupciones con `Thread.currentThread().interrupt()`.

### 9.4 Interacción con la Base de Datos a través del DataSource
Cada tarea concurrente solicita su propia conexión física independiente desde el pool de conexiones (`dataSource.getConnection()`), garantizando aislamiento transaccional y demostrando la capacidad del pool Tomcat JDBC de despachar y reutilizar conexiones bajo demanda simultánea sin fugas ni bloqueos.

---

## 10. Diagramas y Evidencias de Arquitectura

La documentación visual del proyecto se encuentra disponible en la carpeta [`docs/`](docs/):

- **Diagrama de Arquitectura por Capas:** [`docs/arquitectura.png`](docs/arquitectura.png)  
  Representa la separación de responsabilidades entre Capa de Presentación (Thymeleaf/Tailwind), Capa de Controladores y Seguridad (Spring Security 6), Capa de Servicios y Concurrencia (ExecutorService), Capa de Persistencia Híbrida (JPA + JDBC Puro + Tomcat JDBC Pool) y Capa de Datos (PostgreSQL en Supabase).

- **Diagrama de Clases UML:** [`docs/uml.png`](docs/uml.png)  
  Detalla el modelado Orientado a Objetos (POO), herencia (`Usuario` -> `Cliente`, `Administrador`), composición (`Pedido`, `DetallePedido`, `Envio`, `Pago`), contratos de interfaz (`MovimientoInventarioJdbcDAO`, `AuditoriaInventarioConcurrenteService`) y manejo de excepciones propias.

---

## 11. Enlace al Repositorio Público

- **URL de GitHub:** https://github.com/rzvlady/ecommerce-progra-2
- **Ramas Principales:** `develop` / `master`
