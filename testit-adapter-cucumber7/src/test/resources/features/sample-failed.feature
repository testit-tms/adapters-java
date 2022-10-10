Feature: SampleFailed

  Background:
    Given I authorize on the portal

  @ExternalId=failed_with_all_annotations
  @DisplayName=Failed_test_with_all_annotations
  @WorkItemIds=123
  @Title=Title_in_the_autotest_card
  @Description=Test_with_all_annotations
  @Labels=Tag1,Tag2
  @Links={"url":"https://dumps.example.com/module/repository","title":"Repository","description":"Example_of_repository","type":"Repository"}
  Scenario: Create new project, section and test case (failed)
    When I create a project
    And I open the project
    And I create a section - failed
    Then I create a test case

  Scenario: Check something (failed)
    Then I check something - failed