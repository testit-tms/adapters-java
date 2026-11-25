package ru.testit.syncstorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves the newest Sync Storage release on the floor's major.minor line and channel.
 */
final class SyncStorageReleaseResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncStorageReleaseResolver.class);

    static final String RELEASES_API_URL =
            "https://api.github.com/repos/testit-tms/sync-storage-public/releases?per_page=100";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SyncStorageReleaseResolver() {
    }

    /**
     * @param floorConstraint e.g. {@code v0.4.3-tms-6.0} or {@code v0.4.4}
     * @return normalized tag to download; floor itself if API/list has no better match
     */
    static String resolveLatest(String floorConstraint) {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse(floorConstraint);
        try {
            List<String> tags = fetchReleaseTags();
            String selected = selectLatest(floor, tags);
            if (selected != null) {
                LOGGER.info(
                        "Sync Storage: resolved {} → {} (channel={})",
                        floor.normalizedTag(),
                        selected,
                        floor.hasTmsChannel() ? "tms-" + floor.tmsChannel() : "numeric"
                );
                return selected;
            }
            LOGGER.warn(
                    "Sync Storage: no matching release for {}, using floor",
                    floor.normalizedTag()
            );
        } catch (Exception e) {
            LOGGER.warn(
                    "Sync Storage: failed to resolve latest release for {}, using floor: {}",
                    floor.normalizedTag(),
                    e.getMessage()
            );
        }
        return floor.normalizedTag();
    }

    /**
     * Pure selection for tests. Returns null if nothing matches.
     */
    static String selectLatest(SyncStorageVersionConstraint floor, List<String> tags) {
        SyncStorageVersionConstraint best = null;
        for (String tag : tags) {
            SyncStorageVersionConstraint candidate;
            try {
                candidate = SyncStorageVersionConstraint.parse(tag);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (!floor.matchesChannel(candidate)) {
                continue;
            }
            if (!floor.sameMinorLine(candidate)) {
                continue;
            }
            if (!candidate.isAtLeastFloor(floor)) {
                continue;
            }
            if (best == null || SyncStorageVersionConstraint.compareSemver(candidate, best) > 0) {
                best = candidate;
            }
        }
        return best == null ? null : best.normalizedTag();
    }

    static List<String> fetchReleaseTags() throws IOException {
        URL url = new URL(RELEASES_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(SyncStorageRunner.CONNECT_TIMEOUT);
        connection.setReadTimeout(SyncStorageRunner.CONNECT_TIMEOUT);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "testit-java-adapter");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub releases API HTTP " + code);
            }
            try (InputStream in = connection.getInputStream()) {
                JsonNode root = MAPPER.readTree(in);
                if (!root.isArray()) {
                    return Collections.emptyList();
                }
                List<String> tags = new ArrayList<String>();
                for (JsonNode node : root) {
                    if (node.path("draft").asBoolean(false) || node.path("prerelease").asBoolean(false)) {
                        continue;
                    }
                    String tag = node.path("tag_name").asText(null);
                    if (tag != null && !tag.isEmpty()) {
                        tags.add(tag);
                    }
                }
                return tags;
            }
        } finally {
            connection.disconnect();
        }
    }
}
