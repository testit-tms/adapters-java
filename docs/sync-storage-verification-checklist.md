# Sync-storage: verification checklist (other adapters / languages)

> Use after porting [sync-storage-port-bump-and-wait-completion.md](./sync-storage-port-bump-and-wait-completion.md) and [sync-storage-adapter-compat.md](./sync-storage-adapter-compat.md).  
> **Hard rule:** do not finalize status via `PUT /api/v2/testResults/{id}`.  
> Fill **Adapter / lang / SS version / OS** at the top of each run report.

| Field | Value |
|-------|--------|
| Adapter + version | |
| Language / runtime | |
| Sync-storage version (`/health` or binary) | |
| OS | Windows / Linux / macOS |
| Preferred port | |
| Date / tester | |

Mark each row: **Pass / Fail / N/A** + short note (port, testRunId, log line).

---

## A. Baseline (must)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| A1 | Clean run (one worker): tests exit 0, TMS results **final** (not InProgress), without external `wait-completion` wrapper | | |
| A2 | After exit, preferred/effective port eventually free (or SS idle-exits); no permanent orphan blocking next run forever | | |
| A3 | Log shows worker `completed`; Work X / finals visible in SS or TMS | | |
| A4 | Cuts accepted by SS; finish path still sends **InProgress** to TMS when using SS (Work X does final) — no PUT outcome | | |

---

## B. Port bump / foreign run (must)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| B1 | Start run A on preferred port `P`; while A runs, start run B (new `testRunId`, same `P`) | | |
| B2 | B does **not** attach to A’s SS; starts on `P+1` (or next free); log `effectivePort` | | |
| B3 | B finishes (finals OK) while A still running | | |
| B4 | A finishes finals OK; A’s SS process not killed by B | | |
| B5 | Two workers of the **same** `testRunId` attach to **one** SS on same port | | |

---

## C. Start race / no silent no-SS (must)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| C1 | Two processes start almost together on free preferred port | | |
| C2 | One claims `P`, the other bumps (`P+1`…) — both stay on sync-storage | | |
| C3 | No “continue without sync-storage” after failed register/bind when another port in range is free | | |
| C4 | If entire `P..P+maxBump` busy → clear hard fail (not quiet degrade) | | |

---

## D. Multi-suite / multi-class one process (must)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| D1 | One process, ≥2 sequential classes/suites/modules | | |
| D2 | After first class: SS still alive; Work X did **not** run “for the whole run” too early | | |
| D3 | Second class still sends cuts / registers against the same (or correctly rebound) SS | | |
| D4 | After **process** exit: wait-completion (owned child) → both classes’ results final in TMS | | |

---

## E. Keep-alive / lifecycle (must)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| E1 | Long test (> keep-alive interval × 2): server accepts keep-alive; no Force Work X mid-test | | |
| E2 | No spam WARN `keep_alive: worker not found` on healthy worker | | |
| E3 | Generic 404 / missing route on keep-alive → debug/trace, not false “worker not found” | | |
| E4 | Keep-alive stops on worker `completed` / process end | | |

---

## F. Owned child vs detached (platform)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| F1 | **Owned child** (e.g. Windows): process-exit hook waits `/wait-completion` then destroys child | | |
| F2 | **Detached / shared** (e.g. Linux `setsid`): one worker exit does **not** kill SS used by others | | |
| F3 | Attach-only worker never destroys foreign/external SS | | |

---

## G. Regression / known edges (should)

| # | Check | Pass? | Notes |
|---|--------|-------|-------|
| G1 | Mode 2: if start fails after TMS run create → empty InProgress run possible (document, non-blocker) | | |
| G2 | Hard-kill JVM mid-run → orphan SS may remain; next run bumps port (acceptable) | | |
| G3 | `/health` without `testRunId` (old binary) → WARN + documented policy (attach or fail) | | |
| G4 | Port occupied by **non-SS** process → fail or bump per policy; no infinite hang without log | | |
| G5 | CI optional external `wait-completion`: idempotent if adapter already waited | | |

---

## H. Out of adapter scope (file bugs on sync-storage if fail)

| # | Check | Expected owner |
|---|--------|----------------|
| H1 | TMS send returns HTTP 500 but SS logs “Work X completed successfully” while result stays InProgress | **sync-storage** |
| H2 | Orphan `registered` worker without keep-alive stays forever beyond configured orphan timeout | **sync-storage** config/bug |
| H3 | Idle timeout / master lifetime until Work X done | **sync-storage** (≥ 0.4.1 behaviour) |

---

## Quick smoke (15–20 min)

If time is short, run at least: **A1, B1–B4, C1–C2, D1–D4, E1–E2**.

---

## Evidence to attach

- Adapter log snippet: effective port, register, completed, wait-completion.
- SS log: Work X / keep-alive.
- TMS: test run id(s), result outcomes (not InProgress).
- Commands / env: `SYNC_STORAGE_PORT`, `SYNC_STORAGE_PORT_MAX_BUMP`, adapter mode.
