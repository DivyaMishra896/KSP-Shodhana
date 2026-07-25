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

### Auth & Security (RBAC)
* `POST /api/v1/auth/token`: Issues cryptographic signed JJWT tokens (`ROLE_SUPERINTENDENT`, `ROLE_INSPECTOR`, `ROLE_OFFICER`). ⚠️ **No credential check** — any badge + role combination is accepted; JWT signature/RBAC enforcement downstream is genuine.
* `GET /api/v1/admin/unredacted-dossier/{crimeId}`: **Restricted Vault PII Endpoint** (Requires `ROLE_SUPERINTENDENT`). Returns Aadhaar, phone, and offshore account string deterministically derived from a hash of the FIR number — illustrates the RBAC gate; values are hash-seeded synthetic constants, not real PII unmasking.
* `POST /api/v1/admin/purge-case/{crimeId}`: **Restricted Admin Action** (Requires `ROLE_SUPERINTENDENT`). Deletes the crime record from JPA repository and writes a cryptographic SHA-256 audit entry. Audit note body is a fixed template string.
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

---

## ⚠️ Known Limitations

| Area | What is Real | What is Simplified |
|---|---|---|
| Token issuance | HMAC-SHA256 JWT signed; signature verification & RBAC genuine | No password check — any badge+role accepted |
| Admin Vault PII | RBAC (`ROLE_SUPERINTENDENT`) enforced | Values are hash-seeded synthetic constants, not real field decryption |
| Case Purge | Deletes record from JPA + writes SHA-256 audit entry | Audit note is a fixed template string |
| Graph traversal | Genuine multi-hop BFS over seeded co-accused graph | Neo4j Cypher only fires when a live Neo4j container is connected |
| Row-Level Security | Real PostgreSQL RLS SQL defined in `V2__row_level_security.sql` | Only enforced in `docker-compose.yml` PostgreSQL mode; H2 has none |
