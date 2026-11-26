package ru.testit.syncstorage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.testit.syncstorage.SyncStorageRunner.PortAction;

class SyncStoragePortActionTest {

    @Test
    void resolvePortAction_freePortStartsNew() {
        assertEquals(PortAction.START_NEW, SyncStorageRunner.resolvePortAction(false, null, "run-b"));
    }

    @Test
    void resolvePortAction_sameRunAttaches() {
        assertEquals(PortAction.ATTACH, SyncStorageRunner.resolvePortAction(true, "run-a", "run-a"));
    }

    @Test
    void resolvePortAction_foreignRunTriesNext() {
        assertEquals(PortAction.TRY_NEXT, SyncStorageRunner.resolvePortAction(true, "run-a", "run-b"));
    }

    @Test
    void resolvePortAction_legacyHealthWithoutTestRunIdAttaches() {
        assertEquals(PortAction.ATTACH, SyncStorageRunner.resolvePortAction(true, null, "run-b"));
        assertEquals(PortAction.ATTACH, SyncStorageRunner.resolvePortAction(true, "", "run-b"));
    }

    @Test
    void parsePort_defaultsOnBadInput() {
        assertEquals(49152, SyncStorageRunner.parsePort(null, 49152));
        assertEquals(49152, SyncStorageRunner.parsePort("null", 49152));
        assertEquals(49152, SyncStorageRunner.parsePort("x", 49152));
        assertEquals(50000, SyncStorageRunner.parsePort("50000", 49152));
    }

    @Test
    void extractJsonBooleanField_readsCompleted() {
        assertTrue(SyncStorageRunner.extractJsonBooleanField("{\"completed\":true}", "completed"));
        assertEquals(Boolean.FALSE, SyncStorageRunner.extractJsonBooleanField("{\"completed\": false}", "completed"));
        assertNull(SyncStorageRunner.extractJsonBooleanField("{}", "completed"));
    }
}
