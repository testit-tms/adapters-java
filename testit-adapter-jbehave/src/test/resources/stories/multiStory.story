Scenario: First
Meta:
@ExternalId first_scenario
@DisplayName First scenario
@WorkItemIds 123
@Title Title_in_the_autotest_card
@Description Test_with_all_annotations
@Labels Tag1,Tag2
@Links {"url":"https://dumps.example.com/module/repository","title":"Repository","description":"Example_of_repository","type":"Repository"}
Given a is 2
And b is 2
When I add a to b
Then result is 4

Scenario: Second

Given a is 3
And b is 7
When I add a to b
Then result is 10

Scenario: Third

Given a is 5
And b is -5
When I add a to b
Then result is 0
