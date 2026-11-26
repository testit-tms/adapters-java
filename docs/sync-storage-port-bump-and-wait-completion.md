# Sync-storage: port+1 при чужом testRunId + ожидание Work X до destroy

> Cross-adapter / cross-repo porting note (Java reference → Python / .NET / JS / Go clients).  
> Related: [sync-storage-adapter-compat.md](./sync-storage-adapter-compat.md), [sync-storage-0.4-cross-adapter-guide.md](./sync-storage-0.4-cross-adapter-guide.md).  
> **Hard rule unchanged:** do **not** finalize outcome via `PUT /api/v2/testResults/{id}`; Work X / send-test-result only.

**Context:** feedback after TMS-43088 (port reuse by foreign `testRunId`) + clean-run hang leaving results **InProgress**.

---

## Goals (must)

1. **No stuck InProgress** after a successful test JVM exit when SyncStorage was used.
2. **If configured port is busy with another `testRunId`**, start a **new** sync-storage on **port + 1** (then +2, …) instead of aborting the run.

Mode-2 empty run left in UI after a failed second start is **acceptable / non-critical** (run created before port check). Do not block this work on that UX.

---

## 1. Stuck InProgress after clean exit

### Symptoms

| Observation | Meaning |
|-------------|---------|
| Tests exit **0**, cuts accepted, last log line is worker **`completed`** | Adapter side OK |
| Immediately after JVM exit, `GET /health` is dead | Child sync-storage was killed too early |
| Work X never finished → held results stay **InProgress** in TMS | Failure mode to fix |
| Same suite with in-process `GET /wait-completion` → ~seconds later Work X sends final status, `completed: true` | Proves Work X needs wall-clock after last `completed` |

### Root cause (Java today)

`SyncStorageRunner.registerShutdownHook()`:

```text
shutdown hook → if child process alive → process.destroy()
                 // no wait-completion, no waitFor, no grace period
```

On non-Linux the adapter owns the SS child. JVM teardown runs the hook **before** (or instead of) giving SS time to run Work X after the last worker `completed`. Detached Linux (`setsid`) already survives the worker — different path; this bug hits **owned child** (Windows / macOS / non-detached).

### Required behaviour (all adapters that spawn SS as child)

```text
on run end / before killing owned sync-storage process:

  1. Ensure last worker status is completed (already done in happy path).
  2. If this process owns the SS child (not external attach, not shared detached):
       GET /wait-completion?testRunId={id}
         with timeout (recommend 60–120s; configurable)
       OR poll until /health gone AND Work X success is known
  3. Only then destroy / terminate the child (if still alive).
  4. If wait-completion fails / times out:
       log ERROR with testRunId + port;
       still destroy to avoid orphan ports;
       surface failure to CI when possible (non-zero or explicit warning).
```

#### Rules

| Case | Action |
|------|--------|
| Adapter **spawned** SS (owned child) | **Must** wait for Work X (`/wait-completion`) before `destroy` |
| Adapter **attached** to external / foreign SS | **Must not** destroy; optional wait-completion only if this process is the last master / CI orchestration |
| Linux **setsid** detached (shared across workers) | **Must not** kill from one worker; CI/`wait-completion` owns finalization |
| `/wait-completion` returns `completed: true` | Safe to exit; InProgress must not remain for cuts of this run |
| SS already dead before wait | Treat as OK **only if** logs/TMS show finals; otherwise warn (same as CI health-before-wait) |

#### Anti-patterns

- `destroy()` / `Kill()` in shutdown hook with **zero** wait.
- Relying only on idle-timeout (`10m`) to “eventually” finalize — too late for CI UX; leaves InProgress until timeout.
- Calling `PUT /testResults/{id}` to “force” finals — **forbidden**.

#### Acceptance

- [ ] Clean JUnit/pytest/etc. exit **0** → TMS results **Passed/Failed**, not InProgress, without external `wait-completion` wrapper.
- [ ] With artificial Work X delay (~5–10s), owned-child adapter still waits and finals land.
- [ ] Attached/external SS is not killed by a worker shutdown hook.

---

## 2. Port occupied by another testRunId → try port+1

### Symptoms (after TMS-43088 fail-fast)

1. Run A holds sync-storage on port `P` with `testRunId=A`.
2. Run B starts with `testRunId=B`, same configured port `P`.
3. Adapter sees foreign `testRunId` on `/health` → **fails** (current Java: `IllegalStateException`).
4. Second SS never starts; when A frees the port, B does **not** resume — must be relaunched.
5. Mode 2: TMS run B may already exist as **InProgress** (created before port check) while local process already exited — empty “running” run in UI (known nuance, non-blocking).

Fail-fast fixed silent reuse; it is too harsh for sequential local/CI runs on a fixed default port.

### Required behaviour

Replace hard-fail on foreign `testRunId` with **port bump**:

```text
preferredPort = configured SYNC_STORAGE_PORT / syncStoragePort (e.g. 49152)
maxBump = N  (recommend 10; document / optional config)

for port in preferredPort .. preferredPort+maxBump:
  if GET /health != 200:
    start new sync-storage --port {port} --testRunId {myId} ...
    bind adapter client base URL to this port
    break
  runningId = health.testRunId
  if runningId missing (old binary):
    WARN + attach OR treat as incompatible per existing policy
    (Java today: WARN + attach — keep unless product decides otherwise)
  else if runningId == myId:
    attach / register on existing instance
    break
  else:
    // foreign run — do NOT attach, do NOT fail yet
    log INFO: port {port} busy with testRunId={runningId}; trying {port+1}
    continue

if no port found:
  FAIL with clear message (ports preferredPort..+maxBump all busy / unusable)
```

#### Rules

| Situation | Action |
|-----------|--------|
| Free port | Start SS with **this** port; remember effective port for all HTTP calls |
| Same `testRunId` | Attach (parallel workers) — unchanged |
| Other `testRunId` | **Skip** to `port+1` (do not destroy the foreign process) |
| All candidates busy | Fail with list of tried ports + last foreign ids |
| Effective port ≠ configured | Log once at INFO so CI/support can see `syncStoragePort effective=49153` |

#### Config (suggested, optional)

| Knob | Default | Notes |
|------|---------|--------|
| `syncStoragePort` / `SYNC_STORAGE_PORT` | `49152` | Preferred start |
| `syncStoragePortMaxBump` / env | `10` | Max offsets to try; `0` = old fail-fast on first foreign |

Adapters must update **runtime** base URL (`http://localhost:{effectivePort}`) everywhere (cuts, register, keep-alive, wait-completion).

#### Acceptance

- [ ] Run A on 49152; Run B with new id starts SS on **49153** (or next free), tests run, finals OK.
- [ ] Two workers of the **same** run still attach to one SS (same id on same port).
- [ ] No silent attach to foreign `testRunId`.
- [ ] Foreign SS process is **not** killed by Run B.

---

## 3. Implementation checklist (per language repo)

### Port bump

- [ ] Parse preferred port as int; loop `+0..maxBump`.
- [ ] On foreign id: log + continue (remove / gate the hard `IllegalStateException`).
- [ ] Persist **effective port** on the runner/client singleton.
- [ ] Spawn command uses effective `--port`.
- [ ] Unit/integration: mock health sequence busy(A) → free → start on +1.

### Wait before destroy

- [ ] Owned-child path: call `/wait-completion` (or equivalent) **before** process destroy in shutdown / explicit stop.
- [ ] Timeout + logging; never skip wait on happy path.
- [ ] Do not destroy external/detached SS.
- [ ] Manual/CI test: exit without external waiter → TMS not InProgress.

### Docs / CI

- [ ] README: port bump behaviour; effective port in logs.
- [ ] CI: still OK to call `wait-completion` externally; must remain idempotent if adapter already waited.
- [ ] Link this note from adapter sync-storage compat doc.

### Java reference touchpoints (adapters-java)

| Area | File / symbol |
|------|----------------|
| Port check / fail today | `SyncStorageRunner.ensureCompatibleExistingInstance` |
| Start / health | `SyncStorageRunner.start`, `isSyncStorageAlreadyRunning` |
| Shutdown kill | `SyncStorageRunner.registerShutdownHook` → `process.destroy()` |
| Wait API | `CompletionApi.waitCompletionGet` / `/wait-completion` |
| Config port | `AdapterConfig.getSyncStoragePort()`, env `SYNC_STORAGE_PORT` |

---

## 4. Out of scope

- Auto-cancel / delete empty mode-2 TMS runs created before port resolution.
- Changing Work X contract or finalization via PUT.
- Killing foreign sync-storage to steal the preferred port.

---

## 5. Short status line for tickets

> After TMS-43088 fail-fast: (1) wait for `/wait-completion` before destroying an owned sync-storage child so results never stay InProgress; (2) if the preferred port serves another `testRunId`, bind the new run to `port+1` (+2, …) instead of aborting.

---

## 6. Java status (adapters-java)

Implemented in `SyncStorageRunner` / `ClientWrapper` / `SyncStorageService`:

- Port loop `preferred .. +SYNC_STORAGE_PORT_MAX_BUMP` (default **10**); foreign id → next port; same id → attach.
- On `completed` for owned child: `awaitWorkXIfOwned()` → `GET /wait-completion` (120s).
- Shutdown hook: await then `destroy()` (idempotent if already awaited).
