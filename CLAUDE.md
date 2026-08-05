# CLAUDE.md

Guidance for Claude Code (and other AI agents) working in this repository.

## Project Overview

`java-crud` is a sample Spring Boot 3 (Java 17) REST API implementing CRUD
operations for a `Product` resource, backed by an in-memory H2 database via
Spring Data JPA.

## Build Commands

This project uses Maven. No Maven wrapper (`mvnw`) is checked in, so use a
system-installed Maven (3.9+).

- **Install dependencies / compile**:
  ```bash
  mvn install -DskipTests
  ```
  or just to compile without installing:
  ```bash
  mvn compile
  ```
- **Package (build a runnable jar)**:
  ```bash
  mvn package
  ```
  Produces `target/java-crud-0.0.1-SNAPSHOT.jar`.
- **Env setup**: Requires JDK 17+ and Maven 3.9+ on the PATH. No environment
  variables or `.env` files are required — the H2 datasource is configured
  entirely in `src/main/resources/application.yml` and needs no external
  services.

## Test Commands

There is currently no `src/test` directory in this repository — no tests
exist yet. When adding tests, use the standard Maven Surefire conventions
(JUnit 5 is available transitively via `spring-boot-starter-test`):

- **Run the full test suite**:
  ```bash
  mvn test
  ```
- **Run a single test class**:
  ```bash
  mvn test -Dtest=ProductControllerTest
  ```
- **Run a single named test method**:
  ```bash
  mvn test -Dtest=ProductControllerTest#createProduct_returnsCreated
  ```

## Development Commands

- **Start the local dev server** (defaults to `http://localhost:8080`):
  ```bash
  mvn spring-boot:run
  ```
- **H2 console** (inspect the in-memory database while the app is running):
  `http://localhost:8080/h2-console`
  JDBC URL: `jdbc:h2:mem:productsdb`, username `sa`, empty password.
- The database is in-memory (`ddl-auto: update`) and reseeds automatically
  on every restart via `DataSeeder` (see below) — no manual DB setup needed.

## Project Structure

```
pom.xml                          Maven build file (Spring Boot 3.3.5 parent, Java 17)
README.md                        Quick-start instructions and endpoint table
src/main/java/com/example/javacru/
  JavaCrudApplication.java       Spring Boot entry point (@SpringBootApplication)
  config/
    DataSeeder.java              CommandLineRunner that seeds 3 sample products on startup
  controller/
    ProductController.java       REST controller: /api/products endpoints (GET/POST/PUT/DELETE)
  service/
    ProductService.java          Business logic layer; transactional boundaries live here
  repository/
    ProductRepository.java       Spring Data JPA repository interface for Product
  model/
    Product.java                 JPA entity (id, name, description, price, stock, timestamps)
  exception/
    ResourceNotFoundException.java  Thrown when a product id doesn't exist
    GlobalExceptionHandler.java     @RestControllerAdvice mapping exceptions to JSON error bodies
src/main/resources/
  application.yml                Server port, H2 datasource, JPA/Hibernate, logging config
```

There is no `src/test` directory yet.

## Architecture Notes

- **Layered architecture**: Controller → Service → Repository, following
  standard Spring Boot conventions. `ProductController` handles HTTP
  concerns only; `ProductService` (annotated `@Transactional`) owns business
  logic and transaction boundaries; `ProductRepository` is a plain
  `JpaRepository<Product, Long>` with no custom queries.
- **Persistence**: H2 in-memory database (`jdbc:h2:mem:productsdb`), schema
  managed by Hibernate `ddl-auto: update` — there are no SQL migration
  scripts (no Flyway/Liquibase). Data does not persist across restarts and
  is reseeded each time by `DataSeeder`.
- **Validation**: Request bodies use `jakarta.validation` annotations
  (`@NotBlank`, `@NotNull`, `@PositiveOrZero`) on the `Product` entity, and
  controller methods are annotated `@Valid`. Validation failures are caught
  by `GlobalExceptionHandler` and returned as a structured JSON error body
  (`timestamp`, `status`, `error`, `message`).
- **Error handling**: Centralized in `GlobalExceptionHandler`
  (`@RestControllerAdvice`) — `ResourceNotFoundException` → 404,
  `MethodArgumentNotValidException` → 400, all other exceptions → 500. When
  adding new failure modes, add a new `@ExceptionHandler` here rather than
  handling errors ad hoc in controllers.
- **Key dependencies**: `spring-boot-starter-web` (REST),
  `spring-boot-starter-data-jpa` (persistence), `spring-boot-starter-validation`
  (bean validation), `h2` (runtime, in-memory DB), `spring-boot-starter-test`
  (test scope, JUnit 5 + Mockito + Spring Test, not yet exercised by any
  tests).

## Code Style

- No linter/formatter config (no `.editorconfig`, Checkstyle, or Spotless
  plugin) is present in this repository — follow standard Java/Spring Boot
  conventions already used in the codebase:
  - Package-by-layer structure under `com.example.javacru`
    (`controller`, `service`, `repository`, `model`, `exception`, `config`).
  - Constructor injection for dependencies (see `ProductController`,
    `ProductService`) rather than field injection.
  - Standard Java import ordering (no wildcard imports); group
    `jakarta.*` and `org.springframework.*` imports as seen in existing files.
  - Entities expose plain getters/setters (no Lombok dependency is
    declared in `pom.xml`, so don't introduce Lombok annotations without
    adding the dependency first).
  - 4-space indentation, matching all existing source files.
