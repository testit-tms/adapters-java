# Technical notes (adapters-java)

Supplementary documentation for behaviour and performance changes that are not fully covered elsewhere. Written for maintainers.

| Document | Topic |
|----------|--------|
| [mode0-orphan-testpoint-inprogress.md](./mode0-orphan-testpoint-inprogress.md) | **mode=0 orphan InProgress** (no testPoint): problem, Java/sync-storage fix, cross-adapter checklist |
| [mode0-duplicate-results-bulk-after-realtime.md](./mode0-duplicate-results-bulk-after-realtime.md) | **mode=0 duplicate Passed** (TP + orphan): Java bulk `sendTestResults` after realtime PUT |
| [tz-testrun-tags-and-links.md](./tz-testrun-tags-and-links.md) | TZ: test run tags & links (create/early merge); Java status at end |
| [improvements-2026-05-19.md](./improvements-2026-05-19.md) | **2026-05-19** changelog spec: `importRealtime`, CI, bulk lifecycle (Cucumber/JBehave), Selenide, Serenity |
| [bulk-import-autotest-tms.md](./bulk-import-autotest-tms.md) | `importRealtime=false`: bulk autotest create/update, `sendTestResults` batching, parallelism, dedupe, skipping unchanged autotests |
| [cucumber-bulk-import-lifecycle.md](./cucumber-bulk-import-lifecycle.md) | Cucumber: one `stopMainContainer` per run, class container retention, deduped class list |
| [jbehave-meta-external-id-and-parameters.md](./jbehave-meta-external-id-and-parameters.md) | JBehave: Meta key shapes (`@Key`, `Key=value`), Examples substitution, `ExternalId` |

**Also see:** Sync Storage Work X / search notes in this doc (shared binary). Other Sync Storage topics: `sync_storage/adapters-core/docs/`.
