# KSP Shodhana — FastAPI AI Gateway Service

The Python FastAPI AI Gateway service handles structured natural language intent extraction, semantic RAG vector search, and pre-inference PII data anonymization for **KSP-Shodhana**.

---

## Technical Architecture & Service Integration

```
  Spring Boot Backend (Port 8080)
             │
             │ HTTP POST Requests
             ▼
  FastAPI AI Service (Port 8000) ──► Google Gemini API (gemini-flash-lite-latest)
```

The AI service receives natural language queries forwarded by the Spring Boot backend, executes PII data redaction (`pii_anonymizer.py`), parses intent and entities via Gemini structured JSON output (`app/schemas/query.py`), and returns analysis insights and explainable evidence cards (`app/schemas/analysis.py`).

---

## Technical Specifications

* **Framework**: FastAPI + Uvicorn (`0.115.0`)
* **Python Version**: `3.10+`
* **Port**: `8000`
* **AI Model**: Google Gemini (`gemini-flash-lite-latest`)
* **PII Redaction**: `pii_anonymizer.py` pre-inference masking of Aadhaar, phone numbers, and license plates
* **Vector Store**: `vector_store.py` RAG cosine similarity vector search across FIR reports
* **Schema Validation**: Pydantic v2 structured output contracts

---

## Endpoint Specification

* `GET /health`: Service health check (Returns `{"status": "ok"}`).
* `POST /ai/v1/understand`: Parses natural language text into structured intent, extracted entities, and visualization recommendations.
* `POST /ai/v1/analyze`: Generates analytical insights and explainable evidence cards backed by FIR citations.
* `POST /ai/v1/search/vector`: RAG semantic vector search endpoint across crime documents.
* `GET/POST /ai/v1/settings`: Fetches and updates in-memory model settings.

---

## Step-by-Step Local Execution

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

# Set Gemini API Key (Optional — fallback heuristic engine active if omitted)
# Windows PowerShell: $env:GEMINI_API_KEY="your_gemini_api_key_here"
# Linux/macOS: export GEMINI_API_KEY="your_gemini_api_key_here"

# Start Uvicorn AI Server
uvicorn app.main:app --host 0.0.0.0 --port 8000
```
*Service starts on*: `http://localhost:8000`.
