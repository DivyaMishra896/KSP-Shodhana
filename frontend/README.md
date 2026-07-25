# KSP Shodhana — Next.js 14 Presentation Layer Workspace

The Next.js 14 presentation layer provides an interactive investigation workspace for law enforcement officers operating **KSP-Shodhana**.

---

## Technical Architecture & Proxy Connectivity

```
  Browser UI (http://localhost:3000)
             │
             │ API Proxy Route (/api/proxy/[...path])
             ▼
  Spring Boot Core Backend Engine (http://localhost:8080)
```

The Next.js App Router configures an internal BFF (Backend-For-Frontend) proxy under `src/app/api/proxy/[...path]/route.ts` that automatically forwards requests to the Spring Boot backend on `http://localhost:8080`, attaching cryptographic `Authorization: Bearer <token>` headers stored in local storage.

---

## Technical Specifications

* **Framework**: Next.js 14 (App Router)
* **Language**: TypeScript (`5.0`)
* **Port**: `3000`
* **State Management**: Zustand (`5.0`) global workspace store (`useWorkspaceStore.ts`)
* **Styling**: Tailwind CSS v4 organic design palette (`--color-primary: #5D7052`)
* **Maps & Graphs**: Leaflet (`1.9.4`) spatial heatmaps & React Force Graph 2D (`1.29`) physics suspect graph
* **Forensics Security**: `WatermarkOverlay.tsx` dynamic steganographic watermark overlay
* **Multi-Language Audio**: `translator.ts` script translation engine for Devanagari Hindi (`hi-IN`) and Kannada (`kn-IN`) Web Speech TTS

---

## Key UI Components

* `features/chat/`: AI Copilot input, message bubbles, real-time SSE streaming handler, and speak aloud buttons.
* `features/workspace/components/VisualizationGrid.tsx`: Dynamic dual-pane visualization grid auto-pairing Evidence cards with Network Graphs.
* `features/workspace/components/SociologicalInsightsPanel.tsx`: Demographic age distribution and urban/rural locality charts.
* `features/workspace/components/CriminalProfileModal.tsx`: Offender risk profiling modal displaying Criminology Risk Score (`Risk Score: 85/100`), `⚠️ REPEAT OFFENDER` tag, and plain-language explanation banner.
* `features/workspace/components/WorkspaceHeader.tsx`: Responsive top header toolbar with RBAC role selector, cryptographic JWT badge, and quick visualization toggles.

---

## Quickstart & Local Execution

```bash
cd frontend

# Install Node dependencies
npm install

# Start Next.js development server
npm run dev
```
*Application accessible at*: `http://localhost:3000`.
