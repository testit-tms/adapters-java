package ru.testit.properties;

import org.junit.jupiter.api.Test;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRunMetaParserTest {

    @Test
    void parseTags_commaSeparated() {
        assertEquals(Arrays.asList("smoke", "nightly"), TestRunMetaParser.parseTags("smoke, nightly"));
    }

    @Test
    void parseTags_jsonArray() {
        assertEquals(Arrays.asList("a", "b"), TestRunMetaParser.parseTags("[\"a\",\"b\"]"));
    }

    @Test
    void parseTags_empty() {
        assertTrue(TestRunMetaParser.parseTags(null).isEmpty());
        assertTrue(TestRunMetaParser.parseTags("").isEmpty());
        assertTrue(TestRunMetaParser.parseTags("null").isEmpty());
    }

    @Test
    void parseLinks_json() {
        List<LinkItem> links = TestRunMetaParser.parseLinks(
                "[{\"url\":\"https://ci.example/job/1\",\"title\":\"CI Job\",\"type\":\"Related\"}]");
        assertEquals(1, links.size());
        assertEquals("https://ci.example/job/1", links.get(0).getUrl());
        assertEquals("CI Job", links.get(0).getTitle());
        assertEquals(LinkType.RELATED, links.get(0).getType());
    }

    @Test
    void parseLinks_skipsWithoutUrl() {
        assertTrue(TestRunMetaParser.parseLinks("[{\"title\":\"x\"}]").isEmpty());
    }

    @Test
    void parseLinks_invalidJson() {
        assertTrue(TestRunMetaParser.parseLinks("not-json").isEmpty());
    }
}
