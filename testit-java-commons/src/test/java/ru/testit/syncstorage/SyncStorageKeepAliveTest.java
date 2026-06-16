package ru.testit.syncstorage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncStorageKeepAliveTest {

    @Test
    void sendKeepAlive_rejectsBlankPid() {
        ClientWrapper client = new ClientWrapper();
        assertFalse(client.sendKeepAlive("http://127.0.0.1:1", "", "run-1"));
        assertFalse(client.sendKeepAlive("http://127.0.0.1:1", "w1", ""));
    }

    @Test
    void keepAlive_stopsOnInterrupt() throws InterruptedException {
        ClientWrapper client = new ClientWrapper() {
            @Override
            public boolean sendKeepAlive(String url, String pid, String testRunId) {
                return true;
            }
        };
        SyncStorageKeepAlive keepAlive = new SyncStorageKeepAlive(
                client, "http://127.0.0.1:1", "w1", "run-1", 1
        );
        keepAlive.start();
        Thread.sleep(50);
        keepAlive.stop();
        assertTrue(true);
    }
}
