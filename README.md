# billpay-platform

Bill Pay SaaS platform supporting Payment Automation and Bill Processing.

## Architecture

This project uses a **monorepo, single deployable** structure:

| Module | Role |
|---|---|
| `billpay-common` | Shared models, API response envelopes, validation utilities |
| `billpay-bill-processing` | Invoice capture, data extraction, processing workflow |
| `billpay-payment-automation` | Web autofill and IVR phone payment automation |
| `billpay-app` | Spring Boot entry point — assembles all modules into one JAR |

### Why a single deployable?
Microservices were considered but ruled out for this implementation.
Module boundaries reflect real service ownership and could be extracted
independently. For a portfolio project, one runnable app eliminates
operational complexity without sacrificing architectural clarity.

### Why Postgres from day one?
The platform roadmap includes an Oracle → Postgres migration.
Building greenfield on Postgres demonstrates the target state.
Flyway manages schema versioning — the same approach used in production migrations.

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker Desktop

## Running Locally

```bash
# Start Postgres
docker compose up -d

# Build and run
mvn clean package -DskipTests
java -jar billpay-app/target/billpay-app-0.0.1-SNAPSHOT.jar
```

## Running Tests

```bash
mvn clean verify
```

Tests use the `test` profile with an in-memory H2 database (`mvn clean verify` runs without Docker). A Testcontainers + Postgres profile (`application-test.yml`) is included for integration tests once Docker is available.

## API

Base URL: `http://localhost:8080/api/v1`

### Health
`GET /api/v1/health`

Response:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "billpay-platform"
  }
}
```

### Transactions

`POST /api/v1/transactions` — create (returns **201**)

Request:
```json
{
  "description": "Electric bill",
  "amount": 125.50,
  "currency": "USD",
  "transactionDate": "2026-05-28"
}
```

Validation rules:
- `description` — required, max 50 characters (not trimmed)
- `amount` — required, must be greater than zero
- `currency` — required, 3-letter uppercase ISO code (e.g. `USD`)
- `transactionDate` — required, cannot be in the future

`GET /api/v1/transactions/{id}` — get by id

`GET /api/v1/transactions` — list all (newest first)

`GET /api/v1/transactions/{id}/convert?targetCurrency=USD` — convert amount to another currency

Uses live [U.S. Treasury Fiscal Data](https://fiscaldata.treasury.gov/api-documentation/) exchange rates (`rates_of_exchange`). Rate selection rules:

- Use the most recent Treasury `record_date` that is **on or before** the transaction date (no exact-date match required)
- That rate must be within the **last 6 months** of the transaction date
- Returns **400** with a clear error when no qualifying rate exists
- Cross-currency conversions go through USD using Treasury rates (foreign units per one U.S. dollar)

Example response:
```json
{
  "success": true,
  "data": {
    "transactionId": "...",
    "sourceCurrency": "EUR",
    "targetCurrency": "USD",
    "originalAmount": 100.00,
    "convertedAmount": 117.5088,
    "exchangeRate": 1.1751,
    "exchangeRateDate": "2025-12-31"
  }
}
```

All responses follow this envelope:
- Success: `{ "success": true, "data": { ... } }`
- Error: `{ "success": false, "error": "descriptive message" }`