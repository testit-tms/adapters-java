# Serenity Summing CI vs api-validator expected

## Symptom

`serenity2-gradle-junit4` / `serenity3-gradle-junit4` validator fails:

- Expected AutoTest collection size 5, actual 7
- Extra: `sum:1+1=3` (`Summing_1_1_3`), `sum:9+9=18` (`Summing_9_9_18`)
- CheckTestResults: no match for those externalIds

## Cause

1. `java-examples` `parameterized.feature` has  
   `@ExternalId=Summing_{left}_{right}_{result}` + `@DisplayName=...`  
   → adapter correctly creates **two** autotests per Outline.
2. `api-validator-dotnet` Expected still describes the old **hash** autotest `B7731…` / name `Summing`.
3. `CheckAutoTests` filtered by `LastTestRunName == testRun.Name`. CI always uses the same name (`serenity*-gradle-junit4 TestRun`), so the orphan hash AT from older runs stayed in the set forever.

## Fix (two repos)

| Repo | Change |
|------|--------|
| **api-validator-dotnet** | Filter by `LastTestRunId == testRun.Id` (not name). **Merged to main** (PR #51). |
| **java-examples** | Temporarily drop `@ExternalId` / `@DisplayName` on Summing Outline so rows share the hash id again and match current Expected. Branch: `fix/serenity-summing-externalid-align-expected` (not pushed). |

After `LastTestRunId` alone (without examples fix): actual shows `Summing_*` extras and misses hash `Summing` — exactly the serenity2 CI failure.

## Proper follow-up (unique ExternalId)

1. Put `@ExternalId=Summing_{left}_{right}_{result}` back in examples.
2. Run validator once with `TMS_OVERWRITE_EXPECTED=true` for serenity2/3.
3. Commit refreshed Expected JSON.
4. Archive orphan hash autotest `B7731…` in the TMS project.
