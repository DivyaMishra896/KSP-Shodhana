# KSP Shodhana — Spring Boot Core Backend Gateway

The Spring Boot backend service is the central core orchestrator of the **KSP-Shodhana Intelligence Platform**. It connects Next.js frontend, FastAPI AI Service, Spring Data JPA (H2 / PostgreSQL PostGIS), and Neo4j Graph Database.

---

## Technical Architecture & Inter-Service Connectivity

```
  Next.js Frontend (Port 3000)
             │
             │ HTTP / SSE Proxy (/api/proxy/*)
             ▼
  Spring Boot Core Backend (Port 8080)
             │
             │ HTTP Client REST
             ▼
  FastAPI AI Service (Port 8000) ──► Google Gemini API
```

---

## Service Specifications

* **Framework**: Spring Boot 3.3.0 (Java 17 - 25)
* **Port**: `8080`
* **Security & Auth**: Spring Security with `JwtAuthenticationFilter`, `SecurityFilterChain`, and JJWT HMAC-SHA256 token issuance (`/api/v1/auth/token`).
* **Real-time Streaming**: Server-Sent Events (SSE) via `SseEmitter` (`/api/v1/ai/stream`).
* **WORM Audit Ledger**: `AuditLedgerService.java` SHA-256 hash-chained immutable logging.
* **Anti-Exfiltration**: `AnomalyDetector.java` rate-limiting and account locking.

---

## Step-by-Step Local Execution

### 1️⃣ Set Environment Variables
```bash
# Windows PowerShell:
$env:JWT_SECRET="ksp_shodhana_local_dev_jwt_secret_key_2026_super_secure_vault"
$env:AI_SERVICE_URL="http://localhost:8000"

# Linux / macOS:
export JWT_SECRET="ksp_shodhana_local_dev_jwt_secret_key_2026_super_secure_vault"
export AI_SERVICE_URL="http://localhost:8000"
```

### 2️⃣ Build and Run
```bash
cd backend
mvn spring-boot:run
```
*Verification*: Backend starts on `http://localhost:8080` and initializes seed data (`16 crimes`, `16 criminals`, `18 financial transactions`).

---

## Key REST API Endpoints

### AI Orchestration
* `POST /api/v1/ai/query`: Primary investigation query processor returning full `WorkspacePayload`.
* `GET /api/v1/ai/stream?query=...`: Real-time SSE token-by-token streaming endpoint.

### Auth & Security
* `POST /api/v1/auth/token`: Issues cryptographic signed JJWT tokens (`ROLE_SUPERINTENDENT`, `ROLE_INSPECTOR`, `ROLE_OFFICER`).
* `GET /api/v1/audit/ledger`: WORM Cryptographic SHA-256 ledger endpoint.

### Record Management & Intelligence
* `GET /api/v1/crimes`: Filterable list of FIR crime records.
* `GET /api/v1/crimes/spatial/radius`: PostGIS spatial radius query endpoint.
* `GET /api/v1/criminals`: Searchable criminal dossier database.
* `GET /api/v1/criminals/{id}`: Offender risk score details.
* `GET /api/v1/network/{criminalId}`: Returns 2D physics force graph network data.
* `GET /api/v1/network/path?sourceId=4&targetId=8`: Multi-hop shortest path search.
* `GET /api/v1/analytics/sociological`: Sociological crime insights (demographic age & area distributions).
* `GET /api/v1/analytics/forecast`: Dynamic monthly crime forecasting & early warning cluster detection.
* `GET /api/v1/analytics/financial`: Financial transaction link analysis.
* `GET /api/v1/timeline/{investigationId}`: Returns chronological investigation event timeline.
* `GET /api/v1/reports/{reportId}/preview`: Generates printable HTML investigation case dossier.
