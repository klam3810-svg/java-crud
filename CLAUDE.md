# CLAUDE.md

This file provides guidance for working with the `java-crud` codebase.

## Project Overview

`java-crud` is a sample Spring Boot 3 (Java 17) REST API implementing CRUD
operations for a `Product` resource, backed by an in-memory H2 database via
Spring Data JPA.

## Build Commands

Requirements: JDK 17+, Maven 3.9+ (no Maven Wrapper is checked into this repo,
so use a system-installed `mvn`).

```bash
# Install dependencies and compile
mvn compile

# Full build (compiles, runs tests, packages a jar into target/)
mvn package

# Build without running tests
mvn package -DskipTests

# Install to local repo
mvn install
```

No additional environment setup is required — the app uses an in-memory H2
database, so there is no external datastore or `.env` file to configure.

## Test Commands

There is currently no `src/test` directory in this repo. `spring-boot-starter-test`
is already declared as a test-scope dependency in `pom.xml`, so JUnit 5,
Mockito, and Spring Test are available as soon as tests are added under
`src/test/java`.

```bash
# Run the full test suite
mvn test

# Run a single test class
mvn test -Dtest=ProductServiceTest

# Run a single test method
mvn test -Dtest=ProductServiceTest#shouldCreateProduct
```

## Development Commands

```bash
# Start the app locally (default port 8080)
mvn spring-boot:run
```

- API base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:productsdb`, user `sa`, empty password)
- On startup, `DataSeeder` (see below) seeds three sample products if the
  table is empty.

### API Endpoints

| Method | Path                  | Description     |
|--------|-----------------------|------------------|
| GET    | `/api/products`       | List products    |
| GET    | `/api/products/{id}`  | Get product      |
| POST   | `/api/products`       | Create product   |
| PUT    | `/api/products/{id}`  | Update product   |
| DELETE | `/api/products/{id}`  | Delete product   |

## Project Structure

```
src/main/java/com/example/javacru/
├── JavaCrudApplication.java   # Spring Boot entry point (@SpringBootApplication)
├── config/
│   └── DataSeeder.java        # CommandLineRunner bean that seeds sample products on startup
├── controller/
│   └── ProductController.java # REST controller, maps /api/products endpoints
├── exception/
│   ├── GlobalExceptionHandler.java   # @RestControllerAdvice, translates exceptions to JSON error bodies
│   └── ResourceNotFoundException.java
├── model/
│   └── Product.java            # JPA @Entity for the products table
├── repository/
│   └── ProductRepository.java  # Spring Data JPA repository (extends JpaRepository)
└── service/
    └── ProductService.java     # Business logic layer between controller and repository

src/main/resources/
└── application.yml             # Server, datasource, JPA, and logging configuration
```

There is no `src/test` directory yet; add tests under
`src/test/java/com/example/javacru/...` mirroring the main package layout.

## Architecture Notes

- **Layered architecture**: Controller → Service → Repository, standard Spring
  Boot conventions. `ProductController` handles HTTP concerns only; business
  logic (timestamps, existence checks) lives in `ProductService`;
  `ProductRepository` is a plain `JpaRepository<Product, Long>`.
- **Validation**: Request bodies are validated with Jakarta Bean Validation
  annotations on `Product` (`@NotBlank`, `@NotNull`, `@PositiveOrZero`) and
  `@Valid` on controller method parameters (`spring-boot-starter-validation`).
- **Error handling**: Centralized in `GlobalExceptionHandler`
  (`@RestControllerAdvice`), which converts `ResourceNotFoundException`,
  `MethodArgumentNotValidException`, and generic exceptions into a consistent
  JSON error shape (`timestamp`, `status`, `error`, `message`).
- **Persistence**: H2 in-memory database (`jdbc:h2:mem:productsdb`), with
  `hibernate.ddl-auto: update` so the schema is generated automatically from
  the `Product` entity. Data does not persist across restarts. SQL statements
  are logged (`show-sql: true`, formatted) for debugging.
- **Data seeding**: `DataSeeder` runs on application startup and inserts three
  sample products if the table is currently empty — useful for manual
  exploration via the H2 console or REST client.
- **Key dependencies**: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
  `spring-boot-starter-validation`, `h2` (runtime), `spring-boot-starter-test`
  (test scope). Managed via `spring-boot-starter-parent` (version 3.3.5).

## Code Style

- No linter/formatter configuration files (Checkstyle, Spotless, etc.) are
  present in this repo — follow standard Java/Spring conventions already used
  in the codebase:
  - Package-by-layer structure: `config`, `controller`, `exception`, `model`,
    `repository`, `service` under `com.example.javacru`.
  - Standard import ordering: `java.*`/`javax.*`/`jakarta.*` and third-party
    imports listed individually (no wildcard imports), as seen in existing
    files.
  - Constructor injection for dependencies (see `ProductController`,
    `ProductService`) rather than field injection.
  - `@Transactional` at the class level on services, with
    `@Transactional(readOnly = true)` on read-only methods.
  - Standard Java naming conventions: `PascalCase` for classes,
    `camelCase` for methods/fields, entity classes annotated with
    `@Entity`/`@Table`.
