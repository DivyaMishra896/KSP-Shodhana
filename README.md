# KSP Shodhana (ಶೋಧನೆ)

> **Ask. Analyze. Act.** AI-Powered Crime Intelligence & Investigation Workspace for the Karnataka State Police.

[![Next.js](https://img.shields.io/badge/Next.js-14.2-FDFCF8?style=flat-square&logo=next.js&logoColor=2C2C24)](https://nextjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-5D7052?style=flat-square&logo=springboot&logoColor=F3F4F1)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.115-C18C5D?style=flat-square&logo=fastapi&logoColor=FFFFFF)](https://fastapi.tiangolo.com/)
[![Gemini AI](https://img.shields.io/badge/Gemini_AI-Flash_Lite-E6DCCD?style=flat-square&logo=google-gemini&logoColor=4A4A40)](https://deepmind.google/technologies/gemini/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-5D7052?style=flat-square&logo=typescript&logoColor=F3F4F1)](https://www.typescriptlang.org/)
[![Java 17-25](https://img.shields.io/badge/Java-17_--_25-C18C5D?style=flat-square&logo=openjdk&logoColor=FFFFFF)](https://openjdk.org/)
[![Python 3.10+](https://img.shields.io/badge/Python-3.10+-E6DCCD?style=flat-square&logo=python&logoColor=4A4A40)](https://www.python.org/)
[![PostgreSQL PostGIS](https://img.shields.io/badge/PostgreSQL-PostGIS-336791?style=flat-square&logo=postgresql&logoColor=FFFFFF)](https://postgis.net/)

---

## Technical Overview

**KSP Shodhana (ಶೋಧನೆ)** is a full-stack AI-assisted crime intelligence workspace built for police officers and crime investigators across Karnataka State.

Investigators can query crime records and suspect networks using natural language in **English or Kannada**. The system parses query intent, extracts structured entities, and renders interactive spatial heatmaps, suspect co-accused network graphs, investigation timelines, and citation-backed evidence panels.

---

## Connected Multi-Service Architecture

The system consists of three seamlessly interconnected microservices operating together:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER (Next.js 14)                          │
│  - App Router, TailwindCSS, Zustand, Leaflet Maps, React Force Graph 2D          │
│  - Real-Time SSE Stream Receiver & Devanagari/Kannada Web Speech Engine          │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         │  REST / SSE Proxy (Port 3000 -> 8080)
                                         ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT CORE BACKEND ENGINE (Port 8080)                  │
│  - Spring Security with JwtAuthenticationFilter & SecurityFilterChain            │
│  - Spring Data JPA + H2 In-Memory / PostgreSQL PostGIS DataSource                │
│  - GraphService (Neo4j Driver with In-Memory BFS Fallback)                       │
│  - AuditLedgerService (WORM Cryptographic SHA-256 Hash Chain)                    │
└───────────────────┬────────────────────┬────────────────────┬────────────────────┘
                    │                    │                    │
          PostgreSQL / PostGIS         Neo4j Graph        FastAPI REST
                    │                    │                    │
                    ▼                    ▼                    ▼
┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────────────┐
│ POSTGIS CONTAINER     │  │ NEO4J GRAPH DB        │  │ FASTAPI AI SERVICE (8000)│
│ - PostGIS (Point 4326)│  │ - Cypher Multi-Hop    │  │ - PII Anonymizer Masker  │
│ - RLS Migrations V1/V2│  │ - Suspect Graph       │  │ - RAG Vector Store       │
└───────────────────────┘  └───────────────────────┘  └────────────┬─────────────┘
                                                                   │
                                                                   ▼
                                                       ┌───────────────────────────┐
                                                       │ GOOGLE GEMINI CLOUD API   │
                                                       └───────────────────────────┘
```

---

## Step-by-Step Running Guide (All Servers Connected)

To run all servers locally with full inter-service connectivity, follow these steps across 3 terminal windows.

### 1️⃣ Step 1: Start Python AI Microservice (Port 8000)
```bash
cd ai-service

# Create and activate virtual environment
# Windows PowerShell:
python -m venv .venv
.\.venv\Scripts\activate

# Linux / macOS:
# python3 -m venv .venv && source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Set Gemini API Key (Optional — fallback engine active if omitted)
# Windows PowerShell: $env:GEMINI_API_KEY="your_gemini_api_key_here"
# Linux/macOS: export GEMINI_API_KEY="your_gemini_api_key_here"

# Start Uvicorn AI Server
uvicorn app.main:app --host 0.0.0.0 --port 8000
```
*Health Check*: Open `http://localhost:8000/health` (Returns `{"status": "ok"}`).

---

### 2️⃣ Step 2: Start Spring Boot Core Backend Engine (Port 8080)
```bash
cd backend

# Set cryptographic JWT signing key
# Windows PowerShell:
$env:JWT_SECRET="ksp_shodhana_local_dev_jwt_secret_key_2026_super_secure_vault"

# Linux / macOS:
# export JWT_SECRET="ksp_shodhana_local_dev_jwt_secret_key_2026_super_secure_vault"

# Run Spring Boot backend
mvn spring-boot:run
```
*Verification*: Backend initializes JPA data store (`Loaded 16 crimes, 16 criminals`) and connects to AI Service at `http://localhost:8000`.

---

### 3️⃣ Step 3: Start Next.js Presentation UI (Port 3000)
```bash
cd frontend

# Install Node dependencies
npm install

# Start Next.js dev server
npm run dev
```
*Access UI*: Open **`http://localhost:3000`** in your browser. Next.js proxy routes `/api/proxy/*` requests directly to Spring Boot backend on `http://localhost:8080`.

---

## 🧪 Automated End-to-End API Audit Test

To verify that all 21 REST API endpoints across all three servers are running and connected perfectly:

```bash
python scratch/test_all_endpoints.py
```
*Expected Output*: `AUDIT RESULTS: 21 PASSED, 0 FAILED OUT OF 21 ENDPOINTS (100% SUCCESS)`.

---

## 🔬 Foundational Feature Implementations (Hackathon Scope)

To maintain absolute clarity and technical accuracy for hackathon evaluation, the following 5 capabilities are implemented using clean, transparent methods over real synthetic seed data. Each method is labeled as a **"foundational implementation, scoped for hackathon timeline"**:

### 0. Cryptographic JJWT Token Signing & Signature Verification
- **Implementation**: Uses `io.jsonwebtoken` (JJWT 0.12.5) with HMAC-SHA256 (`getSigningKey()`).
- **Signature Verification**: `validateToken(token)` parses claims with `Jwts.parser().verifyWith(...)`, rejecting expired, malformed, or forged tokens.
- **Role Enforcement**: Issues signed tokens via `POST /api/v1/auth/token` with green `JWT ✓` indicator badge in UI.

### 1. Criminology-Based Offender Profiling
- **Implementation**: Computed fields on `Criminal` (`priorOffenseCount`, `isRepeatOffender`, `riskScore`, `riskExplanation`).
- **Explainable Formula**:
  $$\text{RiskScore} = \min(100, (\text{priorOffenses} \times 15) + (\text{criticalCount} \times 20) + (\text{highCount} \times 10) + (\text{recentOffenses} \times 15))$$
- **UI Rendering**: Interactive `CriminalProfileModal.tsx` displaying `Risk Score: 85/100` badge, `⚠️ REPEAT OFFENDER` tag, and explainable risk description banner.

### 2. Sociological Crime Insights
- **Implementation**: `SociologicalInsightsService.java` aggregates seed crime and offender records by demographic age group (`18-25`, `26-35`, `36-50`, `50+`) and locality area type (`Urban`, `Semi-Urban`, `Rural`).
- **UI Rendering**: `SociologicalInsightsPanel.tsx` visualizes distribution bar charts with clear methodology disclaimer.

### 3. Financial Crime & Transaction Link Analysis
- **Implementation**: `FinancialTransaction` domain model (`@Entity`, `@Table(name = "financial_transactions")`) + `FinancialTransactionRepository` + `LocalDataStore` fallback with 18 synthetic transactions (4 rule-flagged).
- **Network Graph Integration**: `NetworkService.java` dynamically attaches `financial_transaction` graph nodes and `TRANSFERRED_FUNDS` links to linked criminals in the graph visualization.

### 4. Crime Forecasting & Early Warning
- **Implementation**: `ForecastingService.java` parses `dateOccurred`/`dateReported` timestamps from filtered crime records and dynamically aggregates incident counts into continuous monthly buckets.
- **Dynamic Trend Detection**: Computes trailing moving average over historical months and compares against recent month volume to detect trend direction (`INCREASING`, `STABLE`, `DECREASING`).
- **Emerging Cluster Rule**: If recent period count > 1.4x trailing average, triggers `isEmergingCluster = true` and generates warning message: *"EMERGING CLUSTER WARNING: Recent incident volume (9) exceeds trailing average (3.5) by +157.1%"*.

---

## 🐳 Full Containerized Stack (`docker-compose.yml`)

Runs PostgreSQL/PostGIS, Neo4j, Spring Boot, FastAPI, and Next.js as Docker containers.

```bash
# 1. Copy the environment template and fill in required values
cp .env.example .env

# 2. Edit .env:
#    JWT_SECRET     — openssl rand -base64 48
#    GEMINI_API_KEY  — https://aistudio.google.com/app/apikey

# 3. Build and start all services
docker-compose up --build
```

---

## Repository Structure

```
├── frontend/             # Next.js 14 App Router, Leaflet, React Force Graph UI
├── backend/              # Spring Boot 3.3, Spring Data JPA, Security & Graph Services
├── ai-service/           # FastAPI, Gemini Client, PII Anonymizer, Vector RAG
├── docs/                 # System Architecture & Technical Specifications
├── .env.example          # Root environment template (all Docker vars)
└── docker-compose.yml    # Multi-container orchestration (PostGIS, Neo4j, App Services)
```

---

## License & Accreditation

Developed for **Karnataka State Police**. Built for KSP Hackathon 2026.
