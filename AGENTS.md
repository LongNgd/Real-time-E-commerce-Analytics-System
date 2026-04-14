# AGENTS

---

## Auto-Load Skills

**AUTO-LOAD RULES:**

### 1. clean-code Skill
- **Auto-load when:** Any request involves writing, modifying, or refactoring code (no keyword gating).
- **Location:** `.opencode/skills/clean-code/SKILL.md`
- **Apply to:** All code changes, refactoring, new code writing

### 2. code-review-checklist Skill
- **Auto-load when:** After code changes are completed, before responding with the final answer (always run a review pass).
- **Location:** `.opencode/skills/code-review-checklist/SKILL.md`
- **Apply to:** Pre-completion quality review of changes

**RULE:** If any code is written or changed → Load `clean-code` immediately. When changes are done → Load `code-review-checklist` before completing the task.

**RESPONSE REQUIREMENT:** Every response must include the list of skills used in that response.

---

## Repository Reality (current state)
- This repo is documentation-only right now: `README.md`, `SRS.md`, and `SystemArchitecture.md` are the only project sources.
- There are no verified build/test/lint/typecheck scripts, manifests, lockfiles, CI workflows, or runnable services checked in yet.
- Do not invent commands (e.g., `npm test`, `docker compose up`) unless those files are added first.

## Canonical Sources
- Treat `SRS.md` as the requirements source of truth (functional and non-functional requirements, APIs, data model).
- Treat `SystemArchitecture.md` as the wiring/flow source of truth (service boundaries and event/data flow).
- `README.md` is currently a short project summary, not an operational runbook.

## Verified Architecture Context
- Target system is a microservices e-commerce analytics platform with Kafka event streaming, Redis caching/realtime data, and MongoDB persistence.
- Core services called out across docs: API Gateway, Auth, Event (producer), Analytics (consumer), Product, User, Recommendation.
- Primary event flow: Client -> Event Service -> Kafka -> Analytics Service -> Redis + MongoDB.

## Working Rules For Future Sessions
- When implementing code later, align names/endpoints/topics with `SRS.md` (e.g., Kafka topic `user-events`, APIs under `/auth`, `/events`, `/products`, `/analytics`, `/recommend`).
- If new code introduces behavior that conflicts with docs, update docs in the same change or explicitly document the deviation.
- Keep docs bilingual-aware: existing content mixes English headings with Vietnamese domain details; preserve clarity instead of rewriting tone arbitrarily.
