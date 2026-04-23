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

    public boolean sendInProgressIfNeeded(TestResult testResult) {
        if (!shouldSendInProgressResult()) {
            return false;
        }
        sendTestResultToSyncStorage(testResult);
        markInProgressResultSent();
        return true;
    }

    private void sendTestResultToSyncStorage(TestResult testResult) {
        if (syncStorageRunner == null || syncStorageRunner.isNotRunning()) {
            return;
        }

        LOGGER.info("trying to send testResult to sync storage");
        boolean isSent = clientWrapper.sendTestResultToSyncStorage(
                syncStorageRunner.getUrl(),
                syncStorageRunner.getTestRunId(),
                testResult,
                clientConfiguration.getProjectId()
        );
        if (isSent) {
            LOGGER.info(
                    "Successfully sent test result to SyncStorage for test: {}",
                    testResult.getExternalId()
            );
            return;
        }

        throw new IllegalStateException(
                "Failed to send test result to SyncStorage after retry attempts"
        );
    }

    public void setWorkerStatus(String status) {
        LOGGER.info("Set worker status to {}", status);
        if (syncStorageRunner == null) {
            LOGGER.warn("No runner");
            return;
        }
        setWorkerStatus(syncStorageRunner.getWorkerPid(), status);
    }

    public void setWorkerStatus(String pid, String status) {
        if (syncStorageRunner == null || syncStorageRunner.isNotRunning()) {
            LOGGER.info("not running!");
            return;
        }

        LOGGER.info("{}:{}", pid, status);
        boolean isUpdated = clientWrapper.setWorkerStatus(
                syncStorageRunner.getUrl(),
                pid,
                status,
                syncStorageRunner.getTestRunId()
        );
        if (isUpdated) {
            LOGGER.info(
                    "Successfully set status {} for worker with PID: {}",
                    status,
                    pid
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
