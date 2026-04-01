package ru.testit.listener;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
class TagParserTest {

    @Test
    void jbehaveMetaPropertyNameUsesAtPrefix() {
        Meta fromAt = new Meta(Arrays.asList("@ExternalId parametrized_test_{n}_x"));
        assertEquals("parametrized_test_{n}_x", fromAt.getProperty("@ExternalId"));
        assertEquals("", fromAt.getProperty("ExternalId"));

        Meta fromPlain = new Meta(Arrays.asList("ExternalId parametrized_test_{n}_x"));
        assertEquals("parametrized_test_{n}_x", fromPlain.getProperty("ExternalId"));
    }

    @Test
    void tagParser_readsAtPrefixedMetaAndSubstitutesExampleRow() {
        Meta scenarioMeta = new Meta(Arrays.asList("@ExternalId parametrized_test_{number}_{value}_failed"));
        Scenario scenario = new Scenario("Parametrized test failed", scenarioMeta);
        Story story = new Story("folder/MyFeature.story");
        Map<String, String> row = new LinkedHashMap<>();
        row.put("number", "1");
        row.put("value", "x");
        TagParser p = new TagParser(story, scenario, row);
        assertEquals("parametrized_test_1_x_failed", p.getExternalIdValue());
    }

    @Test
    void substituteExampleParameters_replacesCurlyAndAngleBrackets() {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("number", "1");
        row.put("value", "x");
        assertEquals(
                "parametrized_test_1_x_failed",
                TagParser.substituteExampleParameters(
                        "parametrized_test_{number}_{value}_failed",
                        row
                )
        );
        assertEquals(
                "parametrized_test_1_x_failed",
                TagParser.substituteExampleParameters(
                        "parametrized_test_<number>_<value>_failed",
                        row
                )
        );
    }
}
