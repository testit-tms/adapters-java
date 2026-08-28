# Technical assignment: autotest layer (test pyramid)

## Context

TMS supports a **test pyramid layer** on an **autotest** card. The layer can be assigned manually, by rules, or from automation runs.

Adapters can set the layer when creating or updating an autotest during a test run. The layer source in API is **`Run`** (set by the adapter).

## Business need

As an automation engineer, I want to declare the pyramid level of each autotest **in test code** so that after a run the autotest card shows:

- the layer name (e.g. `API`, `E2E`);
- the source **Run** (adapter), distinct from Manual / Rule / Report.

## Goal

In every adapter that creates or updates autotests, support an optional **layer** metadata field:

- read from test code only (annotation / mark / tag — per language);
- send on **create** and **update** autotest API calls when present;
- **omit** the field when the user did not specify a layer.

## Scope

Apply consistently to all Test IT adapters (any language / framework) and any entry point that upserts autotests via the standard adapters API.

**In scope:** autotest `layer` on create/update.

**Out of scope:**

- layer on **test run** entity;
- layer on **test result** as a separate API field (UI may show layer from the linked autotest);
- configuration / env / CLI default layer for a whole run;
- runtime API to set layer during test execution;
- validating layer names against a fixed whitelist (adapters pass the string as-is).

## API contract

Use existing autotest endpoints and models:

```json
{
  "layer": {
    "name": "API",
    "source": "Run"
  }
}
```

| Operation | Behaviour |
|-----------|-----------|
| **Create autotest** | Include `layer` only when the test declares a non-empty layer |
| **Update autotest** | Always send `resetLayer: false` (required by API). Include `layer` only when the test declares a non-empty layer |
| **Update autotest, no layer in test** | Send `resetLayer: false`; omit `layer` |

Recommended layer names (constants in client libraries, not enforced): `E2E`, `UI`, `API`, `Contract`, `Integration`, `Component`, `Unit`.

Any other non-empty string is valid.

`LayerSource` values: `Manual`, `Report`, `Run`, `Rule`. Adapters always use **`Run`**.

## Functional requirements

### 1. Declaration in test code only

The user sets layer on the **test method / scenario**, not via adapter config.

| Stack | Suggested syntax |
|-------|------------------|
| Java (JUnit / TestNG) | `@Layer("API")` or `@Layer(TestLayers.API)` on test method |
| Python (pytest) | `@pytest.mark.layer("api")` |
| C# (NUnit / xUnit) | `[Layer("API")]` on test method |
| JavaScript (Jest / Mocha) | `@layer('API')` or framework-specific decorator |
| Cucumber (Gherkin) | `@Layer=API` on scenario |
| JBehave | Meta: `@Layer API` on scenario |

Parameter substitution (e.g. `{param}` in Java) should work if the framework already supports it for other annotations.

### 2. Mapping flow

```
test code (annotation / tag)
    → internal test result model (optional layer field)
    → Converter / mapper
    → AutoTestCreateApiModel.layer / AutoTestUpdateApiModel.layer
    → TMS
```

### 3. Independence from other metadata

- Layer ≠ labels / tags on autotest.
- Layer ≠ test run tags.
- Do not change existing labels/tags behaviour.

### 4. Failed-test update path

Some adapters send a minimal update when a test fails (copy existing autotest from TMS). Still apply `layer` from the test annotation when present.

## Non-goals

- Default layer for all tests in a run via config.
- `Adapter.addLayer()` or similar dynamic API.
- OpenAPI / client regeneration in adapter repos (use existing generated `LayerApiModel`).

## Acceptance criteria

1. User adds layer in test code → autotest in TMS shows that layer with source **Run** after create/update.
2. User omits layer → adapter does not send `layer` on create; on update sends `resetLayer: false`.
3. Custom layer string is accepted without validation.
4. Recommended constants documented; arbitrary strings work.
5. Cross-language doc describes the same rules with per-language syntax examples.
6. Unit tests: with layer / without layer / custom string / failed-update path.

## Suggested implementation outline (language-agnostic)

1. Add `@Layer` (or equivalent) in shared annotations package.
2. Add optional `layer` field to internal `TestResult` (or equivalent).
3. Extract layer from test method in each framework listener.
4. On every update: `resetLayer: false`; if layer non-empty → also `LayerApiModel { name, source: Run }`.
5. BDD adapters: parse `@Layer=` / meta `Layer` like other scenario tags.
6. Document in adapter README; add samples and tests.

## Example (Java)

```java
import ru.testit.annotations.Layer;
import ru.testit.models.TestLayers;

@Layer(TestLayers.API)
@Test
void createUser() {
    // ...
}

@Layer("my-custom-layer")
@Test
void customLayer() {
    // ...
}
```

## Example (pytest)

```python
import pytest

@pytest.mark.layer("api")
def test_create_user():
    ...
```

---

## Java adapters status

Implemented in `testit-java-commons` (shared by all Java adapters):

- `@Layer` on test method (`ru.testit.annotations.Layer`)
- `TestLayers` constants (`ru.testit.models.TestLayers`)
- `TestResult.layer`, `Utils.extractLayer`
- `Converter`: `resetLayer: false` on every update; `layer` + `LayerSource.RUN` only when set in test code
- Framework listeners: JUnit 4/5, TestNG, Cucumber 4–7 (`@Layer=`), JBehave (meta `Layer`)
