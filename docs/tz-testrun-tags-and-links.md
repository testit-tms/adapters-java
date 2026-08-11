# Technical assignment: test run tags and links

## Context

TMS supports **tags** and **links** on a **test run** (not on an individual autotest).

Users can already:

- set tags when starting autotests from the UI;
- add tags manually in the UI;
- add tags and links via API.

Backend and public API already accept tags and links when creating or updating a test run.

## Business need

1. **Tags** — pass run-level tags from automation tooling so runs can be filtered and labeled the same way as in the UI.
2. **Links** — attach a CI/job URL (e.g. GitLab pipeline/job link returned by a webhook) to the test run **as soon as the run exists**, so:
   - testers can open the job while the run is still **In progress**;
   - if the build fails or hangs, the link is already visible in TMS;
   - finding the right job does not depend on knowing who started the run.

Adapters alone cannot solve “webhook → GitLab → keep polling until terminal status”. That belongs to TMS/orchestration. This task only covers what tooling can send: **tags** and **links** on the test run via existing API.

## Goal

In every utility that creates or updates a test run for automation, support:

- specifying **test run tags**;
- specifying **links** (especially a CI/job URL).

Links must be applied **immediately after the test run is created** (or at the earliest possible moment when the run id is known), **not** only when the run is completed.

## Scope (all products of this family)

Apply the same behaviour consistently to:

- all Test IT adapters (any language / framework);
- Test IT CLI;
- Allure importer;
- any other entry point that creates a test run or attaches metadata to an existing one.

Do not invent a parallel API if the product already uses the standard create/update test run endpoints that accept `tags` and `links`.

## Functional requirements

### 1. Configuration / CLI surface

Expose configuration for:

| Intent | Suggested names (align across products) |
|--------|----------------------------------------|
| Test run tags | `testRunTags` / `TMS_TEST_RUN_TAGS` / equivalent CLI flag (e.g. `--testruntags`) |
| Test run links | `testRunLinks` / `TMS_TEST_RUN_LINKS` / equivalent CLI flag (e.g. `--testrunlinks`) |

Accept the same semantics everywhere:

- **Tags:** list of strings (comma-separated and/or JSON array — pick one primary format and document it; supporting both is fine if parsing is unambiguous).
- **Links:** list of objects with at least `url`; optional `title`, `description`, `type` (use the same link model/types the API already expects).

Empty / omitted values mean “do not change tags/links”.

### 2. When to send data

| Scenario | Required behaviour |
|----------|-------------------|
| Utility **creates** the test run | Pass `tags` and `links` in the **create** request. They must be stored before (or immediately when) the run is started. |
| Utility uses an **existing** test run id | As early as possible after the process starts (before long test execution), **update** the run to add configured tags/links. Do not wait until completion / final report upload. |
| Utility only imports results into a finished run | If it can still update run metadata, apply tags/links at the start of import; if the API forbids that for completed runs, document the limitation. |

**Hard requirement:** a CI job URL provided via config must be visible on the test run while status is still **In progress**.

### 3. Merge semantics

When updating an existing test run:

- do **not** wipe tags/links that were already set in UI or by another tool;
- **merge**: keep existing items, add new ones;
- avoid duplicates (same tag name; same link URL, case-sensitive as API defines).

When creating a new run, send only the configured tags/links (plus whatever the API requires, e.g. project id, name).

### 4. Independence from autotest metadata

- Test run tags ≠ autotest/result tags (`@Tags`, labels on a test method, etc.).
- Test run links ≠ links attached to a single autotest or test result.
- Do not change existing behaviour of per-test tags/links unless required for compatibility.

### 5. Modes / workflows

Whatever “adapter mode” / “create vs reuse run” model the product has:

- **Create-run path:** tags + links on create.
- **Reuse-run path:** early update with merge.
- Document how to pass values via env, config file, and CLI flags.

Typical CI pattern to document:

1. Create test run (CLI or API) **with** job link (+ optional tags).
2. Export/pass `testRunId` into adapters/importer.
3. Adapters send results into that run; optional extra tags/links from adapter config are merged early.

### 6. Observability

- Log (debug or info) that tags/links were applied to which test run id.
- On API failure: log error; do not crash the whole test suite if product policy is “best effort” for metadata — but creation of the run itself must still follow existing failure rules.
- Prefer clear warnings if the payload is invalid JSON / empty URL.

## Non-goals

- Polling GitLab/GitHub until the job reaches a terminal status.
- Changing webhook handling inside TMS.
- Redesigning UI for tags/links (already exists).
- Replacing result-level attachments/messages with run links.

## Acceptance criteria

1. User can pass test run tags via config/CLI/env; they appear on the test run in TMS.
2. User can pass one or more links (including a CI job URL); they appear on the test run in TMS.
3. When the utility creates the run, tags/links are present **right after creation/start**, not only after all tests finish.
4. When the utility attaches to an existing run, configured tags/links are applied at startup (merge), not only at teardown.
5. Existing tags/links on the run are preserved when merging.
6. Per-autotest tags/links behaviour is unchanged.
7. README / help text documents property names, formats, and an example with a CI job URL.
8. Unit or integration tests cover: create-with-tags/links, update-merge, invalid input handling.

## Suggested implementation outline (language-agnostic)

1. Add config keys for tags and links; parse into typed structures.
2. Extend the “create empty test run” call to set `tags` and `links`.
3. Extend the early “ensure / update test run” path to merge and PUT/PATCH tags and links.
4. Ensure every code path that creates a run (including helpers used by sync/storage/sidecar) uses the same create helper — no second create without metadata.
5. Update docs and examples (CI: pass `CI_JOB_URL` / pipeline URL into `testRunLinks`).
6. Add tests.

## Example payloads (illustrative)

Tags:

```text
smoke,nightly
```

or

```json
["smoke", "nightly"]
```

Links:

```json
[
  {
    "url": "https://gitlab.example.com/group/project/-/jobs/12345",
    "title": "CI Job",
    "type": "Related"
  }
]
```

## Cross-product consistency

Use the same property names and payload shapes across adapters, CLI, and Allure importer so CI scripts can set one env var and reuse it everywhere. If a product already has a different flag name, accept an alias but document the canonical names above.

---

## Java adapters status

Implemented in `testit-java-commons` (all Java adapters share this):

- Config: `testRunTags` / `TMS_TEST_RUN_TAGS` / `tmsTestRunTags`, `testRunLinks` / `TMS_TEST_RUN_LINKS` / `tmsTestRunLinks`
- Create path: tags/links on `createTestRun`
- Existing run: early merge in `AdapterStartupHelper` at startup
