package ru.testit.syncstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.model.TestRunV2ApiResult;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.ITmsApiClient;
import ru.testit.models.TestResult;
import ru.testit.properties.AdapterConfig;

public class SyncStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncStorageService.class);

    private final AdapterConfig adapterConfig;
    private final ClientConfiguration clientConfiguration;
    private final ITmsApiClient client;
    private final ClientWrapper clientWrapper;
    private final SyncStorageRunner syncStorageRunner;

    public SyncStorageService(
            AdapterConfig adapterConfig,
            ClientConfiguration clientConfiguration,
            ITmsApiClient client,
            ClientWrapper clientWrapper
    ) {
        this.adapterConfig = adapterConfig;
        this.clientConfiguration = clientConfiguration;
        this.client = client;
        this.clientWrapper = clientWrapper;
        this.syncStorageRunner = initializeSyncStorage();
    }

    private boolean shouldSendInProgressResult() {
        return syncStorageRunner != null &&
                syncStorageRunner.isMaster() &&
                !syncStorageRunner.isAlreadyInProgress();
    }

    private void markInProgressResultSent() {
        if (syncStorageRunner != null) {
            syncStorageRunner.setIsAlreadyInProgress(true);
        }
    }

    public synchronized boolean sendInProgressIfNeeded(TestResult testResult) {
        if (!shouldSendInProgressResult()) {
            return false;
        }
        if (!sendTestResultToSyncStorage(testResult)) {
            return false;
        }
        markInProgressResultSent();
        return true;
    }

    /**
     * @return {@code false} if SyncStorage is not running (fallback to direct Test IT export)
     */
    private boolean sendTestResultToSyncStorage(TestResult testResult) {
        if (syncStorageRunner == null || syncStorageRunner.isNotRunning()) {
            return false;
        }

        LOGGER.debug("trying to send testResult to sync storage");
        boolean isSent = clientWrapper.sendTestResultToSyncStorage(
                syncStorageRunner.getUrl(),
                syncStorageRunner.getTestRunId(),
                testResult,
                clientConfiguration.getProjectId()
        );
        if (isSent) {
            LOGGER.debug(
                    "Successfully sent test result to SyncStorage for test: {}",
                    testResult.getExternalId()
            );
            return true;
        }

        throw new IllegalStateException(
                "Failed to send test result to SyncStorage after retry attempts"
        );
    }

    public void setWorkerStatus(String status) {
        LOGGER.debug("Set worker status to {}", status);
        if (syncStorageRunner == null) {
            LOGGER.warn("No runner");
            return;
        }
        try {
            setWorkerStatus(syncStorageRunner.getWorkerPid(), status);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to set worker status {}, continue without sync-storage: {}", status, e.getMessage());
        }
    }

    public void setWorkerStatus(String pid, String status) {
        if (syncStorageRunner == null || syncStorageRunner.isNotRunning()) {
            LOGGER.debug("not running!");
            return;
        }

        LOGGER.debug("{}:{}", pid, status);
        try {
            boolean isUpdated = clientWrapper.setWorkerStatus(
                    syncStorageRunner.getUrl(),
                    pid,
                    status,
                    syncStorageRunner.getTestRunId()
            );
            if (isUpdated) {
                LOGGER.debug(
                        "Successfully set status {} for worker with PID: {}",
                        status,
                        pid
                );
            } else {
                LOGGER.warn(
                        "Failed to set status {} for worker with PID: {}, continue without sync-storage",
                        status,
                        pid
                );
            }
        } catch (RuntimeException e) {
            LOGGER.warn(
                    "Unexpected error setting worker status {} for PID {}, continue without sync-storage: {}",
                    status,
                    pid,
                    e.getMessage()
            );
        }
    }

    private SyncStorageRunner initializeSyncStorage() {
        try {
            String port = adapterConfig.getSyncStoragePort();
            String testRunId = clientConfiguration.getTestRunId();
            if (testRunId == null || "null".equals(testRunId)) {
                TestRunV2ApiResult response = this.client.createTestRun();
                this.clientConfiguration.setTestRunId(response.getId().toString());
                testRunId = response.getId().toString();
            }

            SyncStorageRunner runner = new SyncStorageRunner(
                    testRunId,
                    port,
                    clientConfiguration.getUrl(),
                    clientConfiguration.getPrivateToken(),
                    adapterConfig.getSyncStoragePath()
            );
            runner.start();
            return runner;
        } catch (Exception e) {
            LOGGER.warn(
                    "Failed to initialize SyncStorage: {}",
                    e.getMessage(),
                    e
            );
            return null;
        }
    }
}
