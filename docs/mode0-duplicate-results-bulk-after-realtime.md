# Mode 0: duplicate test results (TP-bound + orphan) — Java adapter

**Related:** [mode0-orphan-testpoint-inprogress.md](./mode0-orphan-testpoint-inprogress.md) (broader contract + sync-storage Work X)  
**Seen with:** JUnit5 + SyncStorage, `adapterMode=0`, `importRealtime=false` (default), parameterized / work-item–linked autotest  
**Example:** `createRBDocClientN_local_1956` — two Passed rows in one test run for the same autotest

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

## Correct behaviour

For mode 0 + SyncStorage success:

1. Store the final cut in SyncStorage (optional for Work X).
2. **PUT** the existing TP-bound result (`writeTestRealtime`).
3. On bulk finalize: refresh autotest metadata if needed, but **do not** call `sendTestResults` again for that test.

If SyncStorage did **not** accept the cut, keep the usual path: `writeTest` / bulk as configured by `importRealtime`.

---

## Fix (Java commons)

In `HttpWriter.writeTestsAfterAll`: if `testResults` already contains the test UUID (filled by a successful `writeTestRealtime`), **skip** adding that row to bulk `sendTestResults`. Optionally still update the autotest (setup/teardown) without creating a run result.

Expected log on a fixed build:

```text
Bulk import: skip sendTestResults for <externalId> — already finalized via realtime (resultId=<uuid>)
```

After redeploy, verify on a **new** test run (old orphans stay in the old run). You should see a single TP-bound result.

---

## How to confirm a regression

1. Mode 0 run from plan, one autotest, SyncStorage enabled.
2. Adapter log: `Updated existing test result …` then **no** `sendTestResults` for the same externalId (or explicit skip line).
3. API: one search hit for that `externalId` in the run; `testPointId` present and non-zero.

If you again see PUT + `sendTestResults` for the same UUID without skip, the double-write path is back.
