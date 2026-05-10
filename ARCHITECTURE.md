# EU DECIDO — System Architecture

> **EU DECIDO** is an experimental civic technology platform designed to explore democratic participation, transparent governance and AI-assisted collective decision-making.

---

# 1. Vision

EU DECIDO aims to create a digital civic infrastructure where citizens can:

* create proposals,
* participate in debates,
* vote anonymously,
* elect representatives,
* monitor political decisions,
* and collaboratively shape governance structures.

The platform combines:

* mobile-first participation,
* transparent governance mechanisms,
* offline-first architecture,
* anonymous voting systems,
* and modular AI agents designed to assist — not replace — democratic processes.

---

# 2. High-Level Architecture

> The following architecture includes both implemented and planned components.

## Core Layers

```text
Citizens
   │
   ▼
Mobile App / Web Platform
   │
   ▼
Backend API (FastAPI)
   │
   ├── Authentication
   ├── Proposals
   ├── Elections
   ├── Voting
   ├── Notifications
   ├── Identity Verification
   │
   ▼
AI Agent Orchestrator
   │
   ├── Moderation Agents
   ├── Analysis Agents
   ├── Governance Agents
   └── Specialized Agents
   │
   ▼
PostgreSQL / Supabase
   │
   ├── Audit Logs
   ├── Votes
   ├── Proposals
   ├── Elections
   └── Governance Data

---

# 3. Mobile Architecture

## Stack

* Kotlin Multiplatform
* Jetpack Compose Multiplatform
* Voyager Navigation
* Koin Dependency Injection
* SQLDelight Local Database

## Principles

### Offline-First

The mobile application is designed to work even with unstable connectivity.

Actions are stored locally and synchronized when connectivity becomes available.

### Shared Business Logic

Core business logic is shared between Android and iOS through Kotlin Multiplatform.

### Modular UI

Each screen uses isolated `ScreenModels` and dependency injection for maintainability and scalability.

---

# 4. Backend Architecture

## Main Technologies

* Python 3.11
* FastAPI
* PostgreSQL
* Supabase
* JWT Authentication
* SQL migrations
* Async processing

## Core Responsibilities

The backend handles:

* authentication,
* proposals,
* comments,
* elections,
* anonymous voting,
* notifications,
* audit logs,
* and AI orchestration.

---

# 5. Database Architecture

## Main Tables

| Table | Purpose |
|---|---|
| users | Registered users |
| identity | Identity verification hashes |
| proposals | Civic proposals |
| comments | Debate and discussion |
| elections | Elections management |
| candidacies | Candidate registration |
| representatives | Elected representatives |
| votes | Anonymous votes |
| voting_tokens | Burn-on-use vote tokens |
| notifications | User notifications |
| agent_results | AI audit logs |

---

# 6. AI Multi-Agent System

The AI system follows a modular orchestration model.

AI agents are designed to:

* assist participation,
* improve information accessibility,
* increase transparency,
* and support democratic processes.

## Fundamental Principle

> “Inform and contextualize — never censor, except in clearly illegal situations.”

---

# 6.1 Orchestrator

The orchestrator coordinates all AI agents.

## Responsibilities

* deterministic routing,
* event processing,
* conflict prevention,
* auditability,
* resource management.

The orchestrator never uses LLMs to decide governance rules autonomously.

Human governance remains central.

---

# 6.2 Currently Implemented Agents

## Moderation Agent

* Detects insults, threats and illegal content
* First synchronous validation layer
* Can temporarily block content when necessary

## Proposal Agent

* Analyzes proposals
* Suggests structure improvements
* Detects proposal categories

## Summary Agent

Generates multiple summaries:

* TLDR
* technical summary
* local impact summary
* accessible explanation

---

# 6.3 Planned Interaction Agents

## Debate Agent

* Organizes arguments
* Detects repetition and logical fallacies
* Improves discussion clarity

## Impact Agent

* Evaluates social and economic impact
* Helps contextualize proposals

## Consensus Agent

* Detects agreement patterns
* Identifies common ground

## Representative Agent

* Generates briefings for representatives
* Assists democratic accountability

---

# 6.4 Governance & Election Agents

## Authentication Agent

Supports secure authentication workflows.

## Identity Agent

Assists identity validation and anti-duplication systems.

## Voting Integrity Agent

Monitors:

* suspicious voting behavior,
* manipulation attempts,
* coordinated abuse,
* and electoral integrity risks.

## Transparency Agent

Generates:

* public reports,
* decision histories,
* and governance audit trails.

## Participation Agent

Encourages civic participation through reminders and engagement mechanisms.

---

# 6.5 Future Research Agents

These agents remain experimental and research-oriented.

## Minority Protection Agent

Detects risks of discrimination or democratic exclusion.

## Democratic Balance Agent

Attempts to ensure plurality and diversity of viewpoints.

## Educational Agent

Explains proposals and governance concepts in accessible language.

## Democratic Memory Agent

Maintains historical context of decisions and governance evolution.

---

# 7. Voting Architecture

The voting system prioritizes:

* anonymity,
* auditability,
* anti-fraud protections,
* and transparency.

## Current Mechanisms

* JWT authentication
* anonymous vote tokens
* burn-on-use voting tokens
* atomic vote registration
* audit logging

No direct association exists between a citizen identity and the stored vote.

---

# 8. Forum & Governance Layer

The platform includes collaborative governance mechanisms such as:

* constitutional drafting,
* policy discussions,
* proposal debates,
* and participatory governance processes.

The governance layer is expected to evolve collaboratively with contributors and citizens.

---

# 9. AI Infrastructure

The architecture is designed to support both:

* commercial LLM providers,
* and sovereign open-source AI infrastructure.

## Supported Approaches

### Local / Self-Hosted Models

* Ollama
* Mistral
* Mixtral
* Llama
* EuroLLM
* other European open-source models

### Production Inference

Future infrastructure may include:

* GPU servers,
* vLLM inference,
* distributed AI services,
* and regional hosting.

---

# 10. Transparency & Auditability

Transparency is a core architectural principle.

## Audit System

All AI agent outputs may be stored in structured audit logs.

This allows:

* reproducibility,
* public accountability,
* governance analysis,
* and debugging.

---

# 11. Security Principles

The platform follows:

* privacy-by-design,
* least privilege access,
* encrypted communications,
* secure authentication,
* and GDPR-oriented practices.

---

# 12. Experimental Status

⚠️ EU DECIDO is currently an experimental and research-stage platform.

Architectures, governance structures and AI systems may evolve significantly over time.

The project is being developed collaboratively and remains open to technical, ethical and democratic contributions.


