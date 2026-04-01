# Cucumber: main container lifecycle and bulk import performance

Technical notes on why Cucumber runs could be slow with `importRealtime=false` and what was changed in the **Cucumber 4–7** listeners and **container helper**.

## Problem (before)

1. **`stopMainContainer` ran after every scenario** (`TestCaseFinished`). That invoked `writer.writeTests` once per scenario, so TMS bulk work grew **1 + 2 + … + N** over a suite.
2. **`startClassContainer` was called every scenario** with the same thread-local class UUID. Each call did `storage.put(classUuid, new ClassContainer())`, **replacing** the stored class container and **dropping previously accumulated `children`**, so only the **last** scenario’s test UUID remained in the class tree.
3. **`MainContainer.children` could list the same class UUID many times** (one append per scenario). `HttpWriter` walked that list as-is, so the same class (and tests) could be processed **multiple times** in one bulk pass.

## Changes

### 1. Finalize the main container once per test run

- **Handlers:** `TestRunFinished` / `cucumber.api.event.TestRunFinished` (Cucumber 4).
- **Removed:** `stopMainContainer` from the per-scenario `testFinished` handler.
- **Added:** A **static concurrent set** of main container UUIDs (one per launcher/thread). On each scenario start, the launcher UUID is registered. On test run finished, **`stopMainContainer` is called once per registered UUID** (supports parallel Cucumber threads).
- **`TestRunStarted`** clears the set at the beginning of a run.

This aligns behaviour with frameworks that call **start/stop main once** (e.g. JUnit Jupiter after all tests).

### 2. Do not replace an existing class container in storage

In `AdapterContainerHelper.startClassContainer`, if a **class container with the same UUID already exists**, only its **start timestamp** is refreshed; the stored object (and its **`children`**) is **not** replaced. New scenarios **append** test UUIDs via `updateClassContainer` on the same instance.

### 3. Deduplicate class UUID when linking to the main container

When registering a class under a main container, the class UUID is appended **only if not already present**, avoiding duplicate entries in `MainContainer.getChildren()`.

### 4. HttpWriter bulk walk

`writeTestsAfterAll` iterates **unique** class container IDs (`LinkedHashSet`) so each class is visited once per bulk run.

## Modules / files

| Area | Files |
|------|--------|
| Cucumber listeners | `testit-adapter-cucumber4` … `testit-adapter-cucumber7`: `BaseCucumber*Listener.java` |
| Container registration | `testit-java-commons`: `services/core/AdapterContainerHelper.java` |
| Bulk writer | `testit-java-commons`: `writers/HttpWriter.java` |

## Operational note

Sync worker status (`in_progress` / `completed`) now transitions **once per test run** at main stop rather than after every scenario, which is usually what you want for a single TMS test run.
