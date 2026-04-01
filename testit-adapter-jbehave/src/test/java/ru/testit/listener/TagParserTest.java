package ru.testit.listener;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagParserTest {

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
