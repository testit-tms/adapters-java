# Mode 0: duplicate test results (TP-bound + orphan) — Java adapter

**Related:** [mode0-orphan-testpoint-inprogress.md](./mode0-orphan-testpoint-inprogress.md) (broader contract + sync-storage Work X)  
**Seen with:** JUnit5 + SyncStorage, `adapterMode=0`, `importRealtime=false` (default), parameterized / work-item–linked autotest  
**Example:** `createRBDocClientN_local_1956` — two Passed rows in one test run for the same autotest

> **Status: temporary workaround (hack).** Current adapter behaviour: PUT existing TP-bound result + skip second `sendTestResults` + `completeTestRun` when all main containers finish. Replace when TMS can merge/enrich a plan result with full steps/parameters (or merge on `testPointId` create), and when the run closes itself without an adapter-side complete.

---

## What you see

You launch **one** autotest from a test plan (`adapterMode=0`, fixed `testRunId`). Build is green. In the TMS run UI (or `testResults/search`) you get **two** results for the same `externalId` / globalId:

| Result | `testPointId` | Typical origin |
|--------|---------------|----------------|
| First (older `createdDate`) | Real UUID from the plan | Created when the run started from the plan; later **updated** (PUT) |
| Second (newer `createdDate`) | Missing / `00000000-…` | **Created** later via `POST …/testRuns/{id}/testResults` — orphan |

Both can be Passed. UI shows a duplicate line; the plan point and the orphan group look like “the same test twice”.

This is **not** TMS inventing a second row by itself. The adapter called create after the TP-bound row was already finalized.

---

## Why it happens (Java)

Two different export paths run for the **same** finished test.

### 1. End of test (`stopTestCase`)

When SyncStorage accepts the cut (`sendInProgressIfNeeded` → true), `AdapterTestCaseHelper` always calls `HttpWriter.writeTestRealtime`:

1. Find an existing InProgress result for this autotest (prefer one with a real `testPointId`).
2. **PUT** `/api/v2/testResults/{id}` with the final status.
3. Remember the mapping `testUuid → resultId` in the writer.

In logs this looks like:

```text
Updated existing test result <uuid> for <externalId>
```

That step is correct for mode 0: the plan already created the TP-bound InProgress; we must update it, not create another.

### 2. End of run (`stopMainContainer` → bulk)

With `importRealtime=false`, finishing the main container runs `writeTestsAfterAll` (bulk import): update/create autotest metadata, then **`sendTestResults`** (`setAutoTestResultsForTestRun`).

That API **creates** a new run result and does **not** attach `testPointId`. If a TP-bound result already exists, TMS keeps both → orphan duplicate.

In logs, right after the PUT:

```text
Bulk import scope: ...
sendTestResults: 1 batch(es), 1 result row(s)
```

Timeline matches the API: PUT updates the older id; a few hundred ms later a new id appears with `createdDate` = bulk time and empty `testPointId`.

### Sync-storage is a red herring here

Work X may also search/finalize, but for this bug the orphan is already created by the adapter’s bulk `sendTestResults` **before** (or independently of) a successful Work X PUT. Fixing only sync-storage search does not remove the double write.

---

## Correct long-term behaviour

For mode 0 + SyncStorage success:

1. Store the final cut in SyncStorage (optional for Work X).
2. **PUT** the existing TP-bound result (`writeTestRealtime`) — ideally with full `parameters` / `autoTestStepResults` supported by TMS.
3. On bulk finalize: refresh autotest metadata if needed, but **do not** call `sendTestResults` again for that test.
4. TMS (or a proper contract) closes the test run; adapter should not need a forced `completeTestRun`.

Tried and rejected as adapter-only fix: `POST …/testResults` with `testPointId` — still **creates** a second row (same point or orphan), does not update the existing InProgress.

---

## Temporary fix (Java commons) — keep until API/product catch up

1. **`TestResultUpdateRequestExt`** — PUT also sends `parameters` and `autoTestStepResults` (OpenAPI update model omits them; server may still ignore them).
2. **`Converter.buildFinalTestResultUpdate`** — same richness as `sendTestResults` body, applied via PUT.
3. **`updateExistingTestResult`** + bulk enrich path — full PUT on the TP-bound id; **never** a second `sendTestResults`.
4. **`onAllMainContainersFinished`** — `completeTestRun` if the run would otherwise stay open (workaround).

Expected log: `enrich existing result via PUT … skip sendTestResults`, then `Completed test run …`.

---

## How to confirm a regression

1. Mode 0 run from plan, one autotest, SyncStorage enabled.
2. Adapter log: realtime PUT, then enrich PUT (no `sendTestResults` for that externalId).
3. API: one search hit for that `externalId`; `testPointId` present; `parameters` non-null when the test had parameters (if TMS applies them on PUT).
4. Test run status is completed (not stuck In Progress).

If you again see PUT + `sendTestResults` for the same UUID without the enrich/skip line, the double-write path is back.
