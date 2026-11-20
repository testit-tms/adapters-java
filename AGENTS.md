The project is built with Java 8.

Exception: `testit-adapter-junit6` and `testit-adapter-cucumber8` require Java 17 (JUnit 6 / Cucumber 8 baseline).

You can check ./docs for the feature development history.

## Sync-storage / TMS status finalization (hard rule)

**Never finalize test-result status via `PUT /api/v2/testResults/{id}`.**

Update outcome the same way as before sync-storage 0.4.x: **send test result**
(`SetAutoTestResultsForTestRun` / Work X after InProgress cut). PUT is not an allowed
status-update path. See `.cursor/rules/sync-storage-finalize.mdc` and
`docs/mode0-duplicate-results-bulk-after-realtime.md`.
