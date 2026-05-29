# billpay-platform

[![CI](https://github.com/dan-markovitch/billpay-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/dan-markovitch/billpay-platform/actions/workflows/ci.yml)

Bill Pay SaaS platform supporting **Payment Automation** and **Bill Processing** — a Java Spring Boot monorepo with persisted APIs, Treasury FX conversion, and automated tests.

## Quick start

**Run tests only** (no Docker required):

```bash
git clone https://github.com/dan-markovitch/billpay-platform.git
cd billpay-platform
mvn clean verify
```

**Run the app** (requires Docker for Postgres):

```bash
docker compose up -d
mvn clean package -DskipTests
java -jar billpay-app/target/billpay-app-0.0.1-SNAPSHOT.jar
```

Health check: `curl http://localhost:8080/api/v1/health`

## Prerequisites

| Need | When |
|------|------|
| Java 17+ | Always |
| Maven 3.9+ | Always |
| Docker Desktop | Running the app locally (Postgres via `docker compose`) |
| Network access | FX conversion calls the live [U.S. Treasury Fiscal Data API](https://fiscaldata.treasury.gov/api-documentation/) |

## Architecture

Monorepo, **single deployable** — one Spring Boot JAR, module boundaries aligned to team ownership:

| Module | Role |
|--------|------|
| `billpay-common` | Shared `ApiResponse` envelope, `ApiException`, global error handling |
| `billpay-bill-processing` | Invoice ingest, transactions, Treasury FX conversion |
| `billpay-payment-automation` | Web autofill and IVR payment session stubs |
| `billpay-app` | Entry point, Flyway migrations, Postgres config, assembles all modules |

```
HTTP request
    → Controller (validation)
    → Service (business rules)
    → Repository (JPA / Postgres)
```

### Design decisions

- **Single deployable over microservices** — modules could be extracted later; one JAR keeps local and CI setup simple while preserving clear boundaries.
- **Postgres + Flyway from day one** — matches the platform's Oracle → Postgres migration target; schema lives in `billpay-app/src/main/resources/db/migration/`.
- **H2 for default tests, Postgres for runtime** — `mvn verify` uses the `test` profile (in-memory H2, no Docker). Running the JAR uses Postgres from `docker compose`.
- **Real Treasury API in application code** — FX conversion calls `rates_of_exchange` at runtime; tests mock the HTTP client.
- **Consistent API envelope** — all endpoints return `{ "success", "data" }` or `{ "success", "error" }` with appropriate HTTP status codes.

## Running tests

```bash
mvn clean verify
```

CI runs the same command on every push/PR to `main` (see [GitHub Actions](https://github.com/dan-markovitch/billpay-platform/actions)).

Test layout:
- **Unit / slice tests** — e.g. `TreasuryExchangeRateClientImplTest`, `GlobalExceptionHandlerTest`
- **Integration tests** — `@SpringBootTest` + MockMvc against H2 (`@ActiveProfiles("test")`)

## API reference

Base URL: `http://localhost:8080/api/v1`

All responses use:

```json
{ "success": true, "data": { ... } }
```

```json
{ "success": false, "error": "descriptive message" }
```

### End-to-end example (curl)

```bash
# 1. Health
curl -s http://localhost:8080/api/v1/health | jq

# 2. Create a transaction
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Electric bill",
    "amount": 100.00,
    "currency": "EUR",
    "transactionDate": "2026-05-28"
  }' | jq
# Save the "id" from the response as TX_ID

# 3. Convert to USD (calls live Treasury API)
curl -s "http://localhost:8080/api/v1/transactions/$TX_ID/convert?targetCurrency=USD" | jq

# 4. Ingest an invoice
curl -s -X POST http://localhost:8080/api/v1/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "externalReference": "INV-1001",
    "amount": 250.00,
    "currency": "USD"
  }' | jq

# 5. Start a payment session (WEB or IVR)
curl -s -X POST http://localhost:8080/api/v1/payment-sessions \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "WEB",
    "merchantId": "merchant-42"
  }' | jq
```

> **Windows PowerShell:** use `curl.exe` instead of `curl` if the alias points to `Invoke-WebRequest`.

---

### Health

`GET /api/v1/health`

Returns **200** when app and database are up; **503** if the database is unreachable.

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "billpay-platform",
    "database": "UP"
  }
}
```

---

### Transactions

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/transactions` | Create (returns **201**) |
| `GET` | `/transactions/{id}` | Get by id |
| `GET` | `/transactions` | List all (newest first) |
| `GET` | `/transactions/{id}/convert?targetCurrency=USD` | FX conversion |

**Create request:**

```json
{
  "description": "Electric bill",
  "amount": 125.50,
  "currency": "USD",
  "transactionDate": "2026-05-28"
}
```

**Validation:**
- `description` — required, max **50** characters (not trimmed)
- `amount` — required, must be **> 0**
- `currency` — required, 3-letter uppercase ISO code
- `transactionDate` — required, cannot be in the **future**

**FX conversion** uses [Treasury `rates_of_exchange`](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/):
- Most recent `record_date` **on or before** the transaction date
- Rate must be within the **last 6 months**
- Cross-currency conversion routes through USD
- **400** when no qualifying rate exists; **503** when Treasury is unavailable

---

### Invoices (Bill Processing)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/invoices` | Ingest invoice (returns **201**, status `RECEIVED`) |
| `GET` | `/invoices/{id}` | Get by id |

```json
{
  "externalReference": "INV-1001",
  "amount": 250.00,
  "currency": "USD"
}
```

Duplicate `externalReference` → **409 Conflict**.

---

### Payment sessions (Payment Automation)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/payment-sessions` | Create session (returns **201**, status `INITIATED`) |
| `GET` | `/payment-sessions/{id}` | Get by id |

```json
{
  "channel": "WEB",
  "merchantId": "merchant-42"
}
```

`channel`: `WEB` (browser autofill) or `IVR` (phone automation).

---

## Database migrations

Flyway migrations in `billpay-app/src/main/resources/db/migration/`:

| Version | Description |
|---------|-------------|
| V1 | Initial schema |
| V2 | Transactions |
| V3 | Invoices |
| V4 | Payment sessions |

## Project layout

```
billpay-platform/
├── billpay-common/              # ApiResponse, ApiException, GlobalExceptionHandler
├── billpay-bill-processing/     # Transactions, invoices, Treasury FX
├── billpay-payment-automation/  # Payment sessions (WEB / IVR)
├── billpay-app/                 # Spring Boot app, Flyway, docker-compose target
├── docker-compose.yml           # Postgres 16 for local runtime
└── .github/workflows/ci.yml     # mvn verify on push/PR
```

## License

MIT — see [LICENSE](LICENSE).
