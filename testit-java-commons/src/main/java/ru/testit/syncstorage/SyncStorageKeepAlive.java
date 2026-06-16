package ru.testit.syncstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background keep-alive pings for sync-storage while the worker stays {@code in_progress}.
 * Resets stuck-detection timers without changing worker status.
 */
final class SyncStorageKeepAlive {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncStorageKeepAlive.class);
    static final int DEFAULT_INTERVAL_SEC = 25;

    private final ClientWrapper clientWrapper;
    private final String url;
    private final String pid;
    private final String testRunId;
    private final int intervalSec;

    private volatile boolean running;
    private Thread thread;

    SyncStorageKeepAlive(
            ClientWrapper clientWrapper,
            String url,
            String pid,
            String testRunId
    ) {
        this(clientWrapper, url, pid, testRunId, DEFAULT_INTERVAL_SEC);
    }

    SyncStorageKeepAlive(
            ClientWrapper clientWrapper,
            String url,
            String pid,
            String testRunId,
            int intervalSec
    ) {
        this.clientWrapper = clientWrapper;
        this.url = url;
        this.pid = pid;
        this.testRunId = testRunId;
        this.intervalSec = intervalSec > 0 ? intervalSec : DEFAULT_INTERVAL_SEC;
    }

    synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this::loop, "sync-storage-keep-alive");
        thread.setDaemon(true);
        thread.start();
        LOGGER.debug("SyncStorage keep-alive started (interval {}s, pid={})", intervalSec, pid);
    }

    synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        LOGGER.debug("SyncStorage keep-alive stopped (pid={})", pid);
    }

    private void loop() {
        while (running) {
            clientWrapper.sendKeepAlive(url, pid, testRunId);
            try {
                Thread.sleep(intervalSec * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
