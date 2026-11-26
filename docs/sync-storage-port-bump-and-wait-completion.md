# Sync-storage: port bump + wait-completion (cross-adapter porting)

> **Audience:** Python / .NET / JS / Go / Java adapter maintainers.  
> **Related:** [sync-storage-adapter-compat.md](./sync-storage-adapter-compat.md), [sync-storage-0.4-cross-adapter-guide.md](./sync-storage-0.4-cross-adapter-guide.md).  
> **Hard rule:** never finalize outcome via `PUT /api/v2/testResults/{id}`. Finals = Work X / send-test-result only.

**Context:** TMS-43088 (foreign `testRunId` on fixed port) + clean-run InProgress after early `destroy` + review after Java port-bump/wait.

**Java reference:** `adapters-java` — `SyncStorageRunner`, `ClientWrapper.waitCompletion`, `SyncStorageService`.

---

## Goals (must in every language)

1. Clean process exit with owned sync-storage child → TMS results are **final** (not stuck **InProgress**).
2. Preferred port busy with **another** `testRunId` → start/bind on **port+1** (+2…), do not abort the whole run and do not attach to the foreign instance.
3. Two processes racing for a **free** port → loser bumps; **never** silently continue the suite without sync-storage after a failed claim.
4. Multi-class / multi-suite in **one** process → do **not** run Work X / kill SS after the first intermediate `completed`.

Mode-2 empty TMS run created before port resolution fails is **non-critical** (out of scope to auto-delete).

---

## 1. Wait for Work X before destroying owned SS

### Problem

Owned child (typical Windows/macOS): shutdown/`Kill` right after last worker `completed` → `/health` dies → Work X never finishes → cuts stay **InProgress**.

### Correct behaviour

```text
on process/JVM exit ONLY (atexit / shutdown-hook / finally of process owner):

  if owns_ss_child and not external/detached:
      GET /wait-completion?testRunId={id}   # timeout 60–120s
      then destroy/kill child if still alive
  else:
      do not kill SS
```

### Critical regression to avoid (review)

Calling `/wait-completion` (or any “finalize now”) on **every** worker `completed` is wrong when the same process may still run more tests/classes:

```text
stop main/class 1 → remaining==0 → completed → wait-completion → Work X / SS exits
start class 2 → SS already dead
```

| Event | Allowed |
|-------|---------|
| Worker `completed` mid-run | Stop keep-alive for that worker; **no** wait-completion; **no** destroy |
| Process exit, owned child | wait-completion → then destroy |
| Attached / `setsid` shared SS | **Never** destroy from one worker |

### Acceptance

- [ ] Single-process clean exit → finals in TMS without external waiter.
- [ ] Two sequential test classes in one process → SS stays up until process end; both results finalize correctly.
- [ ] External/shared SS not killed by adapter teardown.

---

## 2. Port selection: foreign id + start race

### Problem A — foreign `testRunId` (TMS-43088)

Fail-fast on busy port blocks the second run until the first frees the port (and the second does not resume).

### Problem B — free-port race (review)

Both adapters see `/health` down → both start on `P` → one wins bind/register, the other must **not** “continue without sync-storage”.

### Required loop

```text
preferred = SYNC_STORAGE_PORT (default 49152)
maxBump   = SYNC_STORAGE_PORT_MAX_BUMP (default 10)

for port in preferred .. preferred+maxBump:
  health = GET /health
  if health != 200:
      if try_start(port, myTestRunId):
          use effectivePort=port; return
      else:
          cleanup_failed_spawn(); continue   # bind/startup/register race
  runningId = health.testRunId
  if runningId missing:          # legacy binary
      WARN; try_attach+register; on fail → continue
  else if runningId == myTestRunId:
      try_attach+register; on fail → continue   # race
  else:
      INFO port busy with runningId; continue   # foreign — never attach, never kill

FAIL: no usable port in range
```

`try_start` success criteria:

1. Process spawned (or detached start issued).
2. `/health` 200 within timeout.
3. If `health.testRunId` present → must equal `myTestRunId` (else lost race → cleanup + next port).
4. `/register` succeeds (else cleanup + next port).
5. Only then register process-exit hook that waits + destroys (**owned** child only).

### Rules

| Situation | Action |
|-----------|--------|
| Free port, start+register OK | Use it; log `effectivePort` if ≠ preferred |
| Same `testRunId` | Attach (parallel workers) |
| Other `testRunId` | Next port; do not kill foreign SS |
| Start/register fail | Cleanup owned failed child; next port |
| Range exhausted | Hard fail with clear message |
| Failed claim | **Do not** fall back to “run without sync-storage” for this path |

### Config

| Knob | Default |
|------|---------|
| `syncStoragePort` / `SYNC_STORAGE_PORT` | `49152` |
| `SYNC_STORAGE_PORT_MAX_BUMP` | `10` |

All HTTP (cuts, register, keep-alive, wait-completion) must use **effective** `http://localhost:{port}`.

### Acceptance

- [ ] Run A on P, Run B → SS on P+1 (or next free); both Succeeded.
- [ ] Same-run workers attach to one SS.
- [ ] Simultaneous starts on free P → one bumps; neither silently drops SS.
- [ ] Foreign SS process left alone.

---

## 3. Per-language checklist

Copy into the target repo PR:

### Port bump + race

- [ ] Int preferred port; loop `+0..maxBump`.
- [ ] Foreign id → next (no fail-fast-only, no silent attach).
- [ ] Failed start/attach/register → cleanup + next (no silent no-SS).
- [ ] Persist effective port on runner/client.
- [ ] Unit tests: foreign → +1; free→start; attach same id; register fail → try next.

### Wait before destroy

- [ ] wait-completion **only** on process exit for owned child.
- [ ] **Not** on intermediate `completed`.
- [ ] Do not destroy external/detached SS.
- [ ] Manual: multi-class one process + clean exit → no early SS death; no stuck InProgress.

### Docs / CI

- [ ] Log `syncStoragePort effective=…`.
- [ ] External CI `wait-completion` remains OK (idempotent).
- [ ] Link this doc from local sync-storage notes.

---

## 4. Java touchpoints (reference implementation)

| Concern | Where |
|---------|--------|
| Port loop / race / cleanup | `SyncStorageRunner.start`, `tryStartOwnedProcess`, `cleanupFailedSpawn`, `resolvePortAction` |
| wait-completion HTTP | `ClientWrapper.waitCompletion` |
| Await + destroy | `SyncStorageRunner.awaitWorkXIfOwned`, `registerShutdownHook` |
| `completed` (no await) | `SyncStorageService.setWorkerStatus` → `stopKeepAlive` only |
| When worker completed is sent | `AdapterContainerHelper.stopMainContainer` (`remaining == 0`) |

Env: `SYNC_STORAGE_PORT`, `SYNC_STORAGE_PORT_MAX_BUMP`.

---

## 5. Out of scope (do not block porting)

| Item | Owner / note |
|------|----------------|
| Empty mode-2 run in UI after failed start | Product/UX; known |
| Orphan SS after hard-kill of JVM | Idle/orphan timeouts on **sync-storage** |
| Worker stuck `registered` without keep-alive | SS orphan policy + adapter keep-alive |
| `/health` without `testRunId` | Legacy WARN+attach (or stricter policy by product) |
| Work X logs “completed successfully” on TMS HTTP 500 but result stays InProgress | **sync-storage / Work X** bug — not adapter PUT |

---

## 6. Ticket one-liner

> Port+1 when preferred port has another testRunId or start/register loses a race; await `/wait-completion` only in process-exit hook before destroying an owned sync-storage child (never on mid-run `completed`).

---

## 7. Verification

Full Pass/Fail checklist for other adapters: **[sync-storage-verification-checklist.md](./sync-storage-verification-checklist.md)**.

| # | Scenario | Expect |
|---|----------|--------|
| 1 | Clean single run, owned SS | Finals without external wait |
| 2 | Run A then Run B same preferred port | B on P+1; both final |
| 3 | Two workers same testRunId | One SS; both results |
| 4 | Two processes start together on free P | One bumps; both use SS |
| 5 | Two sequential classes / suites one process | No Work X after first; SS alive until exit |
| 6 | Keep-alive during long test | No false `worker not found` spam |
