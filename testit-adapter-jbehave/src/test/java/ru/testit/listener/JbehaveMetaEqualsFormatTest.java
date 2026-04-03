package ru.testit.listener;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RegexStoryParser stores {@code Meta: @ExternalId=foo} as a property whose name is {@code ExternalId=foo} and value is blank.
 */
class JbehaveMetaEqualsFormatTest {

    private static final String STORY_WITH_EQUALS_META = ""
            + "Scenario: Parametrized test failed\n"
            + "Meta:\n"
            + "@ExternalId=parametrized_test_{number}_{value}_failed\n"
            + "When get parameters <number> <value>\n"
            + "Then return false\n"
            + "\n"
            + "Examples:\n"
            + "| number | value |\n"
            + "| 1 | string01 |\n";

    @Test
    void tagParser_readsExternalId_whenMetaUsesEqualsSign() {
        Story story = new RegexStoryParser().parseStory(STORY_WITH_EQUALS_META);
        Scenario sc = story.getScenarios().get(0);
        Map<String, String> row = new LinkedHashMap<>();
        row.put("number", "1");
        row.put("value", "string01");
        TagParser p = new TagParser(story, sc, row);
        assertEquals("parametrized_test_1_string01_failed", p.getExternalIdValue());
    }
}
