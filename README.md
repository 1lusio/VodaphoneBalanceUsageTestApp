# Vodafone Account Service

Spring Boot microservice that returns balance and usage summary for a MyVodafone customer.

## Requirements

- Java 17+
- Maven 3.8+

## Running

```bash
mvn spring-boot:run
```

App starts on port 8080. To build a runnable jar instead:

```bash
mvn clean package
java -jar target/account-service-1.0.0.jar
```

## Endpoint

`GET /customers/{id}/account-summary`

Fetches the customer's balance from the local DB and usage from the external API, returns both in a single response.

```json
{
  "id": 1,
  "balance": 250.75,
  "usage": {
    "min": { "currentSpent": 120, "total": 500 },
    "sms": { "currentSpent": 50, "total": 100 },
    "internet": { "currentSpent": 2048, "total": 5120 }
  }
}
```

Returns 404 if the customer doesn't exist, 502 if the usage API is unreachable, 400 for a non-numeric id.

## Configuration

The external usage API base URL is in `application.yml`:

```yaml
usage:
  api:
    base-url: http://localhost:9090
```

The app uses H2 in-memory DB pre-seeded with customers 1, 2 and 3. H2 console is at
http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:vodafonedb`, no password).

## Tests

```bash
mvn test
```
