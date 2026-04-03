# JBehave: Meta, `ExternalId`, and Examples parameters

Technical notes on how **`TagParser`** and **`ScenarioParser`** map JBehave **Meta** and **Examples** rows to `TestResult.externalId` and related fields.

## Passing Examples into the parser

`ScenarioParser.parseScenario(Story, Scenario, Map<String, String> parameters)` passes the **example row** map (from `BaseJbehaveListener.example(...)`) into **`TagParser`**. That map keys match **Examples** column headers (`number`, `value`, etc.).

## Placeholder substitution

`TagParser.substituteExampleParameters` replaces:

- `{columnName}`
- `<columnName>`

using the example row map. This is applied to **ExternalId**, **DisplayName**, **Title**, **Description**, and other meta-driven string fields after raw values are read from Meta.

## How JBehave stores Meta keys (why lookups failed before)

### 1. Space-separated form (`@ExternalId value`)

`Meta.Property` splits on the **first space**. The property **name** stored in `java.util.Properties` may be **`@ExternalId`**, not `ExternalId`. Lookups must try both **`ExternalId`** and **`@ExternalId`**.

### 2. Equals form (`@ExternalId=value` without a space)

There is **no space** between the name and the value. JBehave still uses the same split rule, so the **entire token** becomes the property **name** (e.g. `ExternalId=parametrized_test_{n}_{v}_failed`) and the **value in Properties is empty**.

The adapter therefore also scans **`meta.getPropertyNames()`** for names starting with:

- `ExternalId=` or `@ExternalId=`
- (and the same pattern for other keys: **DisplayName**, **Title**, etc.)

and takes the substring **after** the first `=` as the actual value.

### 3. Fallback when Meta has no ExternalId

If **ExternalId** is still empty after the above, a hash is used. For parameterized runs, the example row is included in the hash input so different rows get **different** IDs.

## Recommended story formats

Both are supported:

```text
Meta:
@ExternalId parametrized_test_{number}_{value}_failed
```

```text
Meta:
@ExternalId=parametrized_test_{number}_{value}_failed
```

Column names in **Examples** must match placeholders (`number`, `value`, …).

## Files

| File | Role |
|------|------|
| `testit-adapter-jbehave/.../TagParser.java` | Meta resolution + substitution |
| `testit-adapter-jbehave/.../ScenarioParser.java` | Wires parameters into `TagParser` |
| `testit-adapter-jbehave/.../BaseJbehaveListener.java` | Passes example row into `startTestCase` |

## Tests

- `TagParserTest` — substitution and `@`-prefixed Meta from the `Meta` constructor.
- `JbehaveMetaEqualsFormatTest` — **`RegexStoryParser`** + `@ExternalId=...` + Examples row → final external ID.
