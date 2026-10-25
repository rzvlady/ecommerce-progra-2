# E-Commerce Platform · Programación II

> Proyecto final para la asignatura de **Programación II**, Facultad de Ingeniería, Ingeniería en Desarrollo de Software y Ciencia de Datos — Universidad Evangélica de El Salvador.

---

## 1. Integrantes del Equipo

| N° | Nombre Completo | Carnet / Identificador | Rol Principal |
|:--:|:---|:---|:---|
| 1 | [Nombre del Integrante 1] | [Carnet 1] | [Rol / Responsabilidad] |
| 2 | [Nombre del Integrante 2] | [Carnet 2] | [Rol / Responsabilidad] |
| 3 | [Nombre del Integrante 3] | [Carnet 3] | [Rol / Responsabilidad] |
| 4 | [Nombre del Integrante 4] | [Carnet 4] | [Rol / Responsabilidad] |
| 5 | [Nombre del Integrante 5] | [Carnet 5] | [Rol / Responsabilidad] |
| 6 | [Nombre del Integrante 6] | [Carnet 6] | [Rol / Responsabilidad] |
| 7 | [Nombre del Integrante 7] | [Carnet 7] | [Rol / Responsabilidad] |
| 8 | [Nombre del Integrante 8] | [Carnet 8] | [Rol / Responsabilidad] |

---

## 2. Tecnologías Utilizadas

- **Lenguaje:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 4.1.0
  - Spring MVC (Web)
  - Spring Data JPA & Hibernate ORM
  - Spring Security 6 (Autenticación basada en roles, BCrypt, protección CSRF)
  - Spring Validation (Validación declarativa mediante Bean Validation)
- **Persistencia Híbrida:**
  - **JDBC Puro:** `Connection`, `PreparedStatement`, `ResultSet`, `try-with-resources` y manejo de excepciones con `DatabasePersistenceException`.
  - **Spring Data JPA:** Mapeo relacional de entidades complejas y transaccionalidad.
- **Pool de Conexiones:** Apache Tomcat JDBC Connection Pool (`org.apache.tomcat.jdbc.pool.DataSource`) con 15 parámetros técnicos configurados.
- **Concurrencia:** `ExecutorService`, `ThreadPoolExecutor`, `Callable<T>`, `Future<T>` y estructuras atómicas (`AtomicInteger`).
- **Base de Datos:** PostgreSQL 15 (Alojada en Supabase Cloud con SSL requerido).
- **Motor de Plantillas & Frontend:** Thymeleaf 3, HTML5, CSS3, Tailwind CSS, JavaScript Vanilla (Fetch API y modals interactivos).
- **Pruebas Automatizadas:** JUnit 5 (Jupiter), Mockito 5, Assertions estructuradas y JaCoCo.
- **Gestor de Construcción:** Maven con Maven Wrapper (`./mvnw` y `.\mvnw.cmd`).
- **Integraciones:** API REST Mock para Pasarela de Pago (`mock-gateway-api`).

---

## 3. Requisitos Previos y Entorno

Para compilar y ejecutar el proyecto se requiere:

1. **Java Development Kit (JDK):** Versión 21 o superior instalada y configurada en la variable de entorno `JAVA_HOME`.
   ```bash
   java -version
   ```
2. **Acceso a Internet:** Necesario para resolver dependencias de Maven y para conectar con la base de datos PostgreSQL alojada en Supabase.
3. **Maven:** No es indispensable tenerlo instalado globalmente; el repositorio incluye los ejecutables del Maven Wrapper (`./mvnw` para Unix/macOS y `.\mvnw.cmd` para Windows).
4. **Navegador Web Moderno:** Chrome, Edge, Firefox o Safari con soporte para HTML5 y JavaScript.

---

## 4. Configuración de Base de Datos y Pool de Conexiones

### 4.1 Conexión Relacional (PostgreSQL en Supabase)
El sistema se encuentra configurado para conectarse a una instancia de PostgreSQL en la nube (Supabase) con modo SSL obligatorio. Las credenciales predeterminadas se encuentran parametrizadas en `ecommerce-webapp/src/main/resources/application.properties`:

- **URL:** `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
- **Driver:** `org.postgresql.Driver`
- **Esquemas relacionales:** `catalogo`, `core`, `seguridad`, `geo`
- **Variables de Entorno (Opcionales para sobreescritura):**
  - `DB_USER`: Usuario de la base de datos.
  - `DB_PASSWORD`: Contraseña del usuario.

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

### Opción A: Ejecución en Windows (PowerShell / CMD)
```powershell
.\mvnw.cmd spring-boot:run -pl ecommerce-webapp
```

### Opción B: Ejecución en Linux / macOS
```bash
chmod +x ./mvnw
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

### 7.1 Acceso al Backoffice (Administrador)
- **URL de Ingreso:** [http://localhost:8080/backoffice/login](http://localhost:8080/backoffice/login)
- **Usuario:** `[admin@ecommerce.com / usuario administrador de prueba]`
- **Contraseña:** `[password_admin_prueba]`
- **Rol asignado:** `ROLE_ADMIN`

### 7.2 Acceso a la Tienda (Cliente)
- **URL de Ingreso:** [http://localhost:8080/login](http://localhost:8080/login)
- **Usuario:** `[cliente@ecommerce.com / usuario cliente de prueba]`
- **Contraseña:** `[password_cliente_prueba]`
- **Rol asignado:** `ROLE_CLIENTE`

---

## 8. Persistencia con JDBC Puro

De acuerdo a la rúbrica del Avance 3, se implementó una capa de persistencia mediante **JDBC puro** desacoplada de ORM:

- **Interfaz:** [`MovimientoInventarioJdbcDAO`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/dao/jdbc/MovimientoInventarioJdbcDAO.java)
- **Implementación:** [`MovimientoInventarioJdbcDAOImpl`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/dao/jdbc/MovimientoInventarioJdbcDAOImpl.java)
- **Operaciones CRUD:**
  - `insertar(movimiento)`: Ejecuta `PreparedStatement` con `executeUpdate()` y recupera llaves autogeneradas (`RETURN_GENERATED_KEYS`).
  - `buscarPorId(id)`: Ejecuta consulta SQL con `executeQuery()` y extrae datos mediante `ResultSet`.
  - `listarTodos()` y `listarPorProducto(id)`: Mapea cursores relacionales a colecciones tipadas.
  - `actualizarMotivoYCantidad(id, motivo, cantidad)`: Actualiza registros con `executeUpdate()`.
  - `eliminar(id)`: Elimina registros físicos con `executeUpdate()`.
  - `contarPorTipo(tipo)`: Realiza funciones de agregación SQL directa.
- **Gestión de Recursos y Errores:**
  - Uso estricto de sentencias `try-with-resources` para garantizar el cierre determinista de `Connection`, `PreparedStatement` y `ResultSet`.
  - Captura de `SQLException` y encapsulamiento en la excepción personalizada [`DatabasePersistenceException`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/exceptions/DatabasePersistenceException.java).

---

## 9. Descripción de la Programación Concurrente + Base de Datos

### 9.1 Proceso Ejecutado Concurrentemente
El módulo concurrente implementa el servicio de **Auditoría de Integridad de Inventario y Conciliación Multihilo** ([`AuditoriaInventarioConcurrenteService`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/services/AuditoriaInventarioConcurrenteService.java) y [`AuditoriaInventarioConcurrenteServiceImpl`](ecommerce-webapp/src/main/java/com/sv/grupo1/ecommerce/services/impl/AuditoriaInventarioConcurrenteServiceImpl.java)).

### 9.2 Justificación de la Concurrencia
En plataformas de comercio electrónico con amplios catálogos y transacciones simultáneas de compra, la validación de inventarios por categoría puede provocar tiempos de respuesta elevados si se ejecuta de forma secuencial. Mediante la partición por categorías y ejecución concurrente:
- Se divide la carga de trabajo entre múltiples hilos de ejecución paralelos.
- Se optimiza el tiempo de CPU y de latencia I/O hacia la base de datos relacional.
- Se detectan productos en umbral crítico de stock o con discrepancias de movimiento en tiempo real.

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

- **URL de GitHub:** `https://github.com/rzvlady/ecommerce-progra-2`
- **Rama Principal:** `develop` / `main`
