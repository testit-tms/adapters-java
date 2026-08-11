package ru.testit.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses {@code testRunTags} / {@code testRunLinks} configuration values.
 */
public final class TestRunMetaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunMetaParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestRunMetaParser() {
    }

    /**
     * Accepts comma-separated tags or a JSON array of strings.
     */
    public static List<String> parseTags(String raw) {
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                JsonNode node = MAPPER.readTree(trimmed);
                if (!node.isArray()) {
                    LOGGER.warn("Invalid testRunTags JSON: expected array");
                    return Collections.emptyList();
                }
                List<String> tags = new ArrayList<>();
                for (JsonNode item : node) {
                    if (item.isTextual()) {
                        String tag = item.asText().trim();
                        if (!tag.isEmpty()) {
                            tags.add(tag);
                        }
                    }
                }
                return tags;
            } catch (Exception e) {
                LOGGER.warn("Invalid testRunTags JSON: {}", e.getMessage());
                return Collections.emptyList();
            }
        }

        List<String> tags = new ArrayList<>();
        for (String part : trimmed.split(",")) {
            String tag = part.trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    /**
     * Accepts a JSON array of link objects: {@code url} required; {@code title}, {@code description}, {@code type} optional.
     */
    public static List<LinkItem> parseLinks(String raw) {
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        try {
            JsonNode node = MAPPER.readTree(trimmed);
            if (!node.isArray()) {
                LOGGER.warn("Invalid testRunLinks JSON: expected array");
                return Collections.emptyList();
            }
            List<LinkItem> links = new ArrayList<>();
            for (JsonNode item : node) {
                if (!item.isObject()) {
                    continue;
                }
                JsonNode urlNode = item.get("url");
                if (urlNode == null || !urlNode.isTextual() || urlNode.asText().trim().isEmpty()) {
                    LOGGER.warn("Skipping test run link without url");
                    continue;
                }
                LinkItem link = new LinkItem().setUrl(urlNode.asText().trim());
                if (item.hasNonNull("title") && item.get("title").isTextual()) {
                    link.setTitle(item.get("title").asText());
                }
                if (item.hasNonNull("description") && item.get("description").isTextual()) {
                    link.setDescription(item.get("description").asText());
                }
                if (item.hasNonNull("type") && item.get("type").isTextual()) {
                    LinkType type = LinkType.fromString(item.get("type").asText());
                    if (type != null) {
                        link.setType(type);
                    }
                }
                links.add(link);
            }
            return links;
        } catch (Exception e) {
            LOGGER.warn("Invalid testRunLinks JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
