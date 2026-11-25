# Serenity Summing CI vs api-validator expected

## Contract

`@ExternalId=Summing_{left}_{right}_{result}` + `@DisplayName=sum:{left}+{right}={result}`  
→ two autotests: `Summing_1_1_3` / `Summing_9_9_18` (names `sum:1+1=3` / `sum:9+9=18`).

Expected in `api-validator-dotnet` (`serenity2` / `serenity3`) matches those ExternalIds.  
Validator matches AutoTests by ExternalId; TestResults by ExternalId + parameters.

## Do not

Share one hash ExternalId across Outline rows: AT steps become last-writer-wins, and  
`FindExpectedTestResult` used to throw `Sequence contains more than one matching element`.
