package ru.testit.syncstorage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SyncStorageReleaseResolverTest {

    private static final List<String> TAGS = Arrays.asList(
            "v0.4.3-tms-6.0",
            "v0.4.3",
            "v0.4.2-tms-5.8",
            "v0.4.2",
            "v0.4.1-tms-5.8",
            "v0.4.1",
            "v0.4.0-tms-5.8",
            "v0.4.0",
            "v0.3.12-tms-5.8",
            "v0.3.12",
            "v0.3.8-rc-tms-5.7"
    );

    @Test
    void numericFloor_picksLatestNumericOnMinorLine() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.4.0");
        assertEquals("v0.4.3", SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }

    @Test
    void numericFloor_ignoresTmsTags() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.4.4");
        // floor patch 4 → nothing >= 0.4.4 numeric in list
        assertNull(SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }

    @Test
    void tms60Floor_picksOnlyTms60() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.4.0-tms-6.0");
        assertEquals("v0.4.3-tms-6.0", SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }

    @Test
    void tms58Floor_doesNotPickTms60OrNumeric() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.4.1-tms-5.8");
        assertEquals("v0.4.2-tms-5.8", SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }

    @Test
    void doesNotCrossMinorLine() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.3.0");
        assertEquals("v0.3.12", SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }

    @Test
    void skipsUnparseablePrereleaseTags() {
        SyncStorageVersionConstraint floor = SyncStorageVersionConstraint.parse("v0.3.0-tms-5.7");
        assertNull(SyncStorageReleaseResolver.selectLatest(floor, TAGS));
    }
}
