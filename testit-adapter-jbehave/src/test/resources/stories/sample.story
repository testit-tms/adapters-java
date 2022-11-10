Scenario: Add a to b
Meta:
@ExternalId failed_with_all_annotations
@DisplayName Failed_test_with_all_annotations
@WorkItemIds 123
@Title Title_in_the_autotest_card
@Description Test_with_all_annotations
@Labels Tag1,Tag2
@Links {"url":"https://dumps.example.com/module/repository","title":"Repository","description":"Example_of_repository","type":"Repository"}
Given a is 5
And b is 10
When I add a to b
Then result is 14
