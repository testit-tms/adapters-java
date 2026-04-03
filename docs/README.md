# Technical notes (adapters-java)

Supplementary documentation for behaviour and performance changes that are not fully covered elsewhere. Written for maintainers.

| Document | Topic |
|----------|--------|
| [bulk-import-autotest-tms.md](./bulk-import-autotest-tms.md) | `importRealtime=false`: bulk autotest create/update, `sendTestResults` batching, parallelism, dedupe, skipping unchanged autotests |
| [cucumber-bulk-import-lifecycle.md](./cucumber-bulk-import-lifecycle.md) | Cucumber: one `stopMainContainer` per run, class container retention, deduped class list |
| [jbehave-meta-external-id-and-parameters.md](./jbehave-meta-external-id-and-parameters.md) | JBehave: Meta key shapes (`@Key`, `Key=value`), Examples substitution, `ExternalId` |

**Out of scope for these notes:** Sync Storage integration and `AdapterManager` structural refactors (see code and other project docs).
