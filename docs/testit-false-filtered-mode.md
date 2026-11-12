# `testIt=false` must not filter tests (mode 0)

## Problem

With `testIt=false` and `adapterMode=0` (or with no `adapterMode` — default is also `USE_FILTER`), discovery filters still ran and excluded every test (`Tests run: 0`).

Cause: `AdapterManager.isFilteredMode()` looked only at `adapterMode == 0`. Filters (JUnit4/5/6, TestNG) then called `getTestFromTestRun()`, which returns an empty list when TMS integration is off, so every test was excluded.

`PostDiscoveryFilter` is loaded via Java `ServiceLoader` (`META-INF/services`), not as a JUnit Jupiter extension, so `junit.jupiter.extensions.autodetection.enabled=false` does not disable it.

## Fix

`AdapterManager.isFilteredMode()` requires both:

- `shouldEnableTmsIntegration()` is true (`testIt` not `false`);
- mode is `USE_FILTER` (`adapterMode=0`).

One change in commons covers JUnit4, JUnit5, JUnit6, and TestNG.
