# Improvements specification — 2026-05-19

Technical specification of adapter, CI, and bulk-import improvements delivered or prepared on **2026-05-19**.  
Audience: maintainers and reviewers integrating changes across branches.

---

## 1. Summary

| Area | Goal | Status |
|------|------|--------|
| `importRealtime` defaults & realtime safety | Align config with docs; avoid NPE on fixture updates | Merged (`feat/import-realtime-improvements`) |
| E2E CI: `importRealtime=true` pass | Cover realtime path in addition to bulk default | Merged |
| Bulk diagnostics: `mainContainerUuid` | Scope tree-vs-storage warnings per main container | Merged |
| Cucumber bulk lifecycle | One `writeTests` per run; retain class `children` | In codebase (see linked doc) |
| JBehave bulk lifecycle | One main container per embedder run | Local change on `BaseJbehaveListener` |
| Selenide / WebDriver message stability | Reduce flaky `api-validator-dotnet` failures in CI | Branch `fix/TMS-38717-selenide-serenity` |
| Serenity Scenario Outline `externalId` | Unique autotest id per Examples row | Branch `fix/TMS-38717-selenide-serenity` + `java-examples` PR |

---

## 2. `importRealtime` configuration and realtime writer

### 2.1 Problem

- `AppProperties.validateProperties` defaulted missing/invalid `importRealtime` to **`true`**, while README, `ClientConfiguration`, and E2E workflow used **`false`** (bulk mode).
- With `importRealtime=true`, `HttpWriter.updateTestResults` could call TMS APIs with a **null** `testResultId` when setup/teardown updates ran before the result id was registered → **NPE**.

### 2.2 Changes

**`AppProperties.java`**

- Invalid or missing `importRealtime` now defaults to **`false`** (bulk import), consistent with documentation and `TMS_IMPORT_REALTIME: false` in `.github/workflows/test.yml`.

**`HttpWriter.java`**

- Before `apiClient.getTestResult(testResultId)`, guard:
  - if `testResultId == null` → log warning with `testUuid` and `externalId`, **skip** setup/teardown update (no NPE).

### 2.3 Acceptance criteria

- Adapter starts with no `importRealtime` property → bulk mode.
- Realtime run with a missing result id logs a warning and continues instead of failing the worker.

---

## 3. E2E CI: second pass with `importRealtime=true`

### 3.1 Problem

Only the bulk path (`importRealtime=false`) was validated in CI. Realtime regressions were undetected.

### 3.2 Changes (`.github/workflows/test.yml`)

After the default bulk run and `api-validator-dotnet` validation:

1. **Create a new test run** (`testit testrun create`).
2. **`Test importRealtime=true`**
   - `export TMS_IMPORT_REALTIME=true`
   - Reuse sync-storage binary from `.caches/syncstorage-linux-amd64` (no second `wget`).
   - `pkill` previous sync-storage process; restart with the new `TMS_TEST_RUN_ID`.
   - Run `./gradlew test` with the same TMS properties.
3. **`Validate importRealtime=true`** — run `api-validator-dotnet` again.

Default workflow env remains `TMS_IMPORT_REALTIME: false` for the first pass.

### 3.3 Acceptance criteria

- Matrix projects pass validator for both bulk and realtime modes.
- Sync-storage is restarted cleanly between passes (no port/process leak).

---

## 4. Bulk import diagnostics — `mainContainerUuid`

### 4.1 Problem

`HttpWriter.logBulkImportTreeVsStorageDiagnostics` compared the class-container tree to **all** test results in storage. With parallel classes or multiple mains, warnings were **false positives**.

### 4.2 Changes

**`TestResult`**

- Field `mainContainerUuid` (already present) is now populated by listeners when scheduling tests.

**Listeners** — `.setMainContainerUuid(launcherUUID.get())` (or run-level uuid for JBehave) on `TestResult`:

- `BaseCucumber4Listener` … `BaseCucumber7Listener`
- `BaseJunit4Listener`
- `BaseJbehaveListener` (run-level `runMainUuid`)

**`HttpWriter.storedTestUuidsForBulkDiagnostics`**

- Prefer `storage.getTestResultUuidsForMainContainer(container.getUuid())`.
- Fall back to global storage only when no scoped results exist (legacy adapters).

### 4.3 Notes

- **Does not affect TMS export** — diagnostics only.
- Reduces noise when debugging “tests not linked under class tree” bulk skips.

---

## 5. Cucumber — bulk container lifecycle

Documented in detail: [cucumber-bulk-import-lifecycle.md](./cucumber-bulk-import-lifecycle.md).

### 5.1 Before

- `stopMainContainer` after **every scenario** → `writeTests` called N times (quadratic bulk work).
- `startClassContainer` replaced storage entry each scenario → **lost** accumulated `children`.
- Duplicate class UUIDs in `MainContainer.children` → duplicate bulk processing.

### 5.2 After (Cucumber 4–7)

| Event | Action |
|-------|--------|
| `TestRunStarted` | Clear `MAIN_UUIDS_PENDING_FINALIZE` |
| `TestCaseStarted` | `startMainContainer` (idempotent per thread uuid), `startClassContainer`, schedule test |
| `TestCaseFinished` | `stopTestCase`, `stopClassContainer` — **no** `stopMainContainer` |
| `TestRunFinished` | `stopMainContainer` once per registered main uuid (parallel-safe set) |

**`AdapterContainerHelper`**

- Reuse existing class container instance (refresh `start` only).
- Append class uuid to main **once** (dedupe).

**`HttpWriter`**

- Walk unique class ids (`LinkedHashSet`) during bulk import.

---

## 6. JBehave — bulk container lifecycle (new)

### 6.1 Problem

`BaseJbehaveListener` previously created and finalized **main + class containers per scenario** — same bulk cost as pre-fix Cucumber (one `writeTests` per scenario).

### 6.2 Target behaviour (aligned with Cucumber)

| Event | Action |
|-------|--------|
| `beforeStoriesSteps(BEFORE)` | Reset run state |
| `beforeStory` | One shared `runMainUuid` for embedder run; new `ClassContainer` per story file |
| `beforeScenario` / `example` | Schedule/start test; `updateClassContainer` only |
| `afterScenario` | `stopTestCase` only |
| `afterStory` | `stopClassContainer` for that story |
| `afterStoriesSteps(AFTER)` **or** last `afterStory` | `finalizeRunMainContainers()` → single `writeTests` |

### 6.3 Thread safety

- `afterScenario` may run on a **different thread** than `beforeScenario` (documented JBehave behaviour).
- Container uuids stored in `ConcurrentHashMap` keyed by `story.getPath()`.
- `scenarioClassUuid` set in `ThreadLocal` during `beforeScenario` for `example` / `afterScenario`.

### 6.4 Example impact

`multiStory.story` (3 scenarios): **3× `writeTests`** → **1× `writeTests`** + **3× `writeClass`** (one class stop per story file).

### 6.5 File

- `testit-adapter-jbehave/src/main/java/ru/testit/listener/BaseJbehaveListener.java`

---

## 7. Selenide / WebDriver — stable failure messages (CI)

Branch: **`fix/TMS-38717-selenide-serenity`** (`41fafa1`).

### 7.1 Problem

`selenide-gradle-junit5` E2E failed intermittently in `api-validator-dotnet` because Chrome session startup errors differed between CI runs:

- Host name / IP in “Host info”
- Varying “session not created” reason text
- Different `java.version` / `os.version` in stack traces

### 7.2 Changes

**`Utils.normalizeWebDriverMessage(String)`**

- Applied only when message contains `Could not start a new session`.
- Strips host info; normalizes session-not-created line; masks Java/OS version strings.

**`Converter`**

- Normalizes `throwable.getMessage()` and stack trace before HTML escape and TMS export.

**`NormalizeWebDriverMessageTest`**

- Unit tests for normalization rules.

**`.github/workflows/test.yml` (selenide matrix row)**

- `needs_chrome: true` → `browser-actions/setup-chrome@v1`.
- Headless Selenide opts with isolated `--user-data-dir` per run (`run1` / `run2` for adapter mode 2).
- `pkill` chromedriver/chrome and cleanup between test steps.

### 7.3 Follow-up

- Update expected artifacts in `api-validator-dotnet` for selenide after stable Chrome messages.
- Optional: `closeWebDriver()` in `java-examples` selenide project.

---

## 8. Serenity / Cucumber Scenario Outline — unique `externalId`

Branch: **`fix/TMS-38717-selenide-serenity`**.

### 8.1 Problem

Scenario Outline “Summing” used `@ExternalId={result}` — **same** `externalId` for all Examples rows → multiple test results shared one `autoTestId` → **non-deterministic order** in validator.

**Constraint:** do **not** change `TagParser` / `externalId` derivation logic in commons.

### 8.2 Solution

Explicit per-row tag in feature files:

```gherkin
@ExternalId=Summing_{left}_{right}_{result}
Scenario Outline: Summing
```

**In-repo example updated (branch):**

- `testit-adapter-cucumber7/src/test/resources/features/parameterized.feature`

**Required in `java-examples`:**

- `serenity2-gradle-junit4` and `serenity3-gradle-junit4` feature files (separate PR).

### 8.3 Cucumber listener hardening (already in codebase)

- `testFinished`: `updateTestCase` with `setThrowable` when scenario fails.
- Hook failures: propagate throwable via `updateTestCase` (not only `Adapter.addMessage`).

---

## 9. Related existing documentation

| Document | Topic |
|----------|--------|
| [bulk-import-autotest-tms.md](./bulk-import-autotest-tms.md) | Bulk autotest create/update, batching, dedupe |
| [cucumber-bulk-import-lifecycle.md](./cucumber-bulk-import-lifecycle.md) | Cucumber main/class lifecycle |
| [jbehave-meta-external-id-and-parameters.md](./jbehave-meta-external-id-and-parameters.md) | JBehave Meta / `ExternalId` syntax |

---

## 10. Integration checklist

- [ ] Merge `feat/import-realtime-improvements` (importRealtime default, CI realtime pass, `mainContainerUuid`).
- [ ] Merge `fix/TMS-38717-selenide-serenity` (WebDriver normalization, CI Chrome, parameterized `externalId`).
- [ ] Merge JBehave `BaseJbehaveListener` bulk lifecycle.
- [ ] PR to `java-examples`: Serenity `@ExternalId=Summing_{left}_{right}_{result}`.
- [ ] Refresh `api-validator-dotnet` expected data for selenide if needed after Chrome stabilization.

---

## 11. Out of scope (not changed here)

- Python `@testit.step("write: {text}")` parameter substitution (`adapters-python`).
- Sync Storage hybrid import path beyond existing worker lifecycle.
- Changing global `externalId` hashing in `TagParser` for Scenario Outlines.
