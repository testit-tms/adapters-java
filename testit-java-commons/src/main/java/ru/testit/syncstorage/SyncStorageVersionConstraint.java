package ru.testit.syncstorage;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Floor version + channel for Sync Storage release selection.
 * <ul>
 *   <li>{@code v0.4.4} — only numeric tags {@code v0.4.*} (no {@code -tms-*})</li>
 *   <li>{@code v0.4.3-tms-6.0} — only tags {@code v0.4.*-tms-6.0}</li>
 * </ul>
 * Runtime picks the newest patch on the same major.minor line that matches the channel.
 */
final class SyncStorageVersionConstraint {

    private static final Pattern TAG = Pattern.compile(
            "^v?(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)(?:-tms-(?<tms>[\\d.]+))?$"
    );

    private final int major;
    private final int minor;
    private final int patch;
    /** Empty = numeric-only channel. */
    private final String tmsChannel;
    private final String raw;

    private SyncStorageVersionConstraint(int major, int minor, int patch, String tmsChannel, String raw) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.tmsChannel = tmsChannel == null ? "" : tmsChannel;
        this.raw = raw;
    }

    static SyncStorageVersionConstraint parse(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Sync Storage version is empty");
        }
        String trimmed = tag.trim();
        Matcher m = TAG.matcher(trimmed);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unsupported Sync Storage version: " + trimmed);
        }
        return new SyncStorageVersionConstraint(
                Integer.parseInt(m.group("major")),
                Integer.parseInt(m.group("minor")),
                Integer.parseInt(m.group("patch")),
                m.group("tms"),
                trimmed.startsWith("v") ? trimmed : "v" + trimmed
        );
    }

    boolean matchesChannel(SyncStorageVersionConstraint other) {
        return Objects.equals(tmsChannel, other.tmsChannel);
    }

    boolean sameMinorLine(SyncStorageVersionConstraint other) {
        return major == other.major && minor == other.minor;
    }

    boolean isAtLeastFloor(SyncStorageVersionConstraint floor) {
        return compareSemver(this, floor) >= 0;
    }

    static int compareSemver(SyncStorageVersionConstraint a, SyncStorageVersionConstraint b) {
        if (a.major != b.major) {
            return Integer.compare(a.major, b.major);
        }
        if (a.minor != b.minor) {
            return Integer.compare(a.minor, b.minor);
        }
        return Integer.compare(a.patch, b.patch);
    }

    String normalizedTag() {
        if (tmsChannel.isEmpty()) {
            return "v" + major + "." + minor + "." + patch;
        }
        return "v" + major + "." + minor + "." + patch + "-tms-" + tmsChannel;
    }

    String raw() {
        return raw;
    }

    boolean hasTmsChannel() {
        return !tmsChannel.isEmpty();
    }

    String tmsChannel() {
        return tmsChannel;
    }
}
