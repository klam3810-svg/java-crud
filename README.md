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
