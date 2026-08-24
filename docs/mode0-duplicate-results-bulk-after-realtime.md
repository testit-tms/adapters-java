# Mode 0: duplicate test results (TP-bound + orphan) — Java adapter

**Related:** [mode0-orphan-testpoint-inprogress.md](./mode0-orphan-testpoint-inprogress.md) (sync-storage Work X, search contract)  
**Typical setup:** JUnit5 + SyncStorage, `adapterMode=0`, fixed `testRunId`, `importRealtime=false` (default)

---

## Symptom (historical bug)

One autotest from a test plan produced **two** Passed rows in the same run for the same `externalId`:

| Row | `testPointId` | Origin |
|-----|---------------|--------|
| Older | Real plan point UUID | Created by TMS when the run started |
| Newer | Missing / `00000000-…` | **Second** `POST …/testRuns/{id}/testResults` from the adapter |

Root cause: the adapter finalized the test twice — once at **test finish**, again at **run finish** (bulk).

---

## Current behaviour (fixed)

**Rule:** final status goes only through **`sendTestResults`** (`setAutoTestResultsForTestRun`).  
**PUT** `/api/v2/testResults/{id}` is **not** used to finalize; if used at all, status is sent **as on the server** (no overwrite with the adapter’s final status).

### End of test — `AdapterTestCaseHelper.stopTestCase`

When SyncStorage accepts the cut (`sendInProgressIfNeeded` → true):

1. `HttpWriter.writeTestRealtime` — update/create autotest metadata, link work items.
2. **`sendTestResults`** — full result body (`statusType`, steps, parameters, message, …).
3. Store `testUuid → resultId` in the writer map.

When SyncStorage is unavailable: `writeTest` → same `writeTestRealtime` path if `importRealtime=true`; otherwise export waits until bulk at run end.

Debug log:

```text
Finalized test result via sendTestResults for <externalId> (resultId=<uuid>)
```

### End of run — `stopMainContainer` → `writeTests`

#### `importRealtime=false` (default) — `writeTestsAfterAll`

- If `testResults` already contains this `testUuid` → **skip** `sendTestResults`; optionally refresh autotest setup/teardown only.
- Otherwise → bulk `sendTestResults` once (e.g. SyncStorage was off and nothing was sent at test finish).

Info log when skipped:

```text
Bulk import: skip sendTestResults for <externalId> (already finalized at test finish)
```

#### `importRealtime=true` — `updateTestResults`

After per-test `sendTestResults`, run end may **PUT** setup/teardown fixtures onto the **same** result id.  
Status in PUT body = current server value (`Converter.testResultToTestResultUpdateModel`); **not** replaced with final adapter status.

### Run completion

When the last main container finishes: `HttpWriter.onAllMainContainersFinished` → `completeTestRun` (mode 0 runs that would otherwise stay open).

---

## Flow (mode 0 + SyncStorage + `importRealtime=false`)

```text
Plan start     → TMS creates TP-bound InProgress
stopTestCase   → SyncStorage cut + sendTestResults (Passed/Failed + full payload)
stopMainContainer → bulk skips sendTestResults for that test; autotest metadata only
last container → completeTestRun
```

Expected API outcome: **one** finalized result row per autotest in the run (the one from `sendTestResults` at test finish), with steps/parameters from the create payload.

---

## Regression checklist

1. Mode 0, one autotest, SyncStorage on, `importRealtime=false`.
2. Log: `Finalized test result via sendTestResults` at test end.
3. Log: `Bulk import: skip sendTestResults …` at run end (not a second batch row for the same test).
4. `testResults/search`: one hit per `externalId`; run status **Completed**.

**Bad signs (bug is back):**

- PUT with explicit final `statusCode` at test finish (`Updated existing test result …` after overwriting InProgress).
- Bulk `sendTestResults` for a test that was already finalized at test finish (no skip line).
- Two Passed rows for the same `externalId` in one run.

---

## Out of scope / not the adapter fix

- Sync-storage Work X may also search/finalize; adapter-side fix is **no double `sendTestResults`**, not Work X alone.
- `POST …/testResults` with `testPointId` still **creates** a new row — not used for finalization.
- Enriching an existing TP-bound row via PUT with `parameters` / `autoTestStepResults` is limited by TMS PUT schema; full payload is on the **create** path (`sendTestResults`).
