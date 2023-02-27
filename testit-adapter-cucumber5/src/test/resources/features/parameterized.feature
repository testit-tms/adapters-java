Feature: Rule
  Tests that use Rule

  @DisplayName=sum:{left}+{right}={result}
  @ExternalId={result}
  Scenario Outline: Summing
    When Summing <left>+<right>
    Then Result is <result>

    Examples:
      | left | right | result |
      | 1    | 1     | 3      |
      | 9    | 9     | 18     |