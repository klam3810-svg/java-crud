# java-crud

Sample Spring Boot CRUD API for products. Uses H2 in-memory database.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

API base: `http://localhost:8080`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products` | List products |
| GET | `/api/products/{id}` | Get product |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:productsdb`)

## Architecture

- **Layered architecture**: `ProductController` (HTTP concerns only) → `ProductService` (`@Transactional` business logic) → `ProductRepository` (a plain `JpaRepository<Product, Long>` with no custom queries).
- **Persistence**: H2 in-memory database (`jdbc:h2:mem:productsdb`); schema is managed by Hibernate `ddl-auto: update` (no Flyway/Liquibase migrations). Data does not persist across restarts and is reseeded on every startup by `DataSeeder`.
- **Validation & error handling**: Request bodies use `jakarta.validation` annotations on the `Product` entity; validation failures and other exceptions are caught centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`) and returned as structured JSON error bodies.
- **Package layout**: `com.example.javacru`, organized by layer — `controller`, `service`, `repository`, `model`, `exception`, and `config` (`DataSeeder`).

## Quote

> "Simplicity is the soul of efficiency." — Austin Freeman
