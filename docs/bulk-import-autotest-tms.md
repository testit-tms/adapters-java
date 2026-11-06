# Bulk import (`importRealtime=false`): autotest API and test results

Technical notes on how the HTTP writer sends autotests and test run results when **realtime import is disabled**, and why the implementation behaves the way it does.

## Scope

- **Module:** `testit-java-commons`
- **Primary classes:** `HttpWriter`, `BulkAutotestHelper`

## Behaviour overview

When `importRealtime=false`, `HttpWriter.writeTests(MainContainer)` calls `writeTestsAfterAll`, which walks the container tree (main → class → tests), loads each `TestResult` from storage, and for each test:

1. Resolves the autotest on TMS (`getAutoTestByExternalId`).
2. Either queues **create** or **update**, queues **test run result** rows for `sendTestResults`, and reconciles **work-item links** after create and update (including when metadata PUT is skipped).

## TMS constraints

1. **`sendTestResults`** must not include **two results for the same autotest external ID in a single request** (TMS error: external IDs must be unique within the batch).
2. **`DbUpdateConcurrencyException`** can occur under concurrent writes; the client retries with backoff.

## Batching test results

`BulkAutotestHelper.partitionResultsUniqueAutotestPerBatch` groups results by `AutoTestResultsForTestRunModel.getAutoTestExternalId()` and builds batches in a **round-robin** fashion so each batch contains at most one row per autotest. If each autotest appears only once, all rows can fit in **one** batch.

## Parallel create/update

Autotest **create** and **update** calls are executed in a **fixed-size thread pool** (capped, e.g. 16) to reduce wall-clock time while limiting pressure on TMS and concurrency errors.

## Deduplication by `externalId`

Within a single flush (`bulkCreate` / `bulkUpdate`), multiple queued rows can refer to the same logical autotest (same `externalId`). The helper **deduplicates** create/update models by `externalId` (**last row wins**), so each distinct autotest receives at most one `createAutoTest` / `updateAutoTest` per flush. **All** result rows are still sent via `sendTestResults` according to the batching rules above.

## Skipping unchanged autotests (bulk path)

For existing autotests, `HttpWriter` compares the server model with the prepared update using **`hasAutoTestChanged`** (same idea as the realtime path). If nothing relevant changed:

- **No** `updateAutoTest` is queued for that test.
- The test run result is still sent via **`BulkAutotestHelper.addTestRunResultOnly`**, which only enqueues `sendTestResults` rows (no PUT autotest).

Work-item link reconciliation:

- After **`bulkCreate`**: for each created autotest, links from `@WorkItemIds` / `@WorkItemId` are applied using the UUID returned by `createAutoTest`.
- After **`bulkUpdate`** and when metadata is skipped (`addTestRunResultOnly`): pending WI links (keyed by autotest UUID) are flushed in `teardown`, so unchanged autotest metadata does not skip link reconcile.

## Class containers in the main tree

`writeTestsAfterAll` iterates **unique** class container UUIDs (`LinkedHashSet` of `MainContainer.getChildren()`) so the same class is not processed multiple times when the list accidentally contains duplicates.

## Related diagnostics

`logBulkImportTreeVsStorageDiagnostics` uses unique class IDs where appropriate when logging scope.

## Files

| File | Role |
|------|------|
| `writers/HttpWriter.java` | Bulk tree walk, `hasAutoTestChanged`, delegates to helper |
| `writers/helpers/BulkAutotestHelper.java` | Batches, parallel API calls, dedupe, result-only flush |
