package ru.testit.syncstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.TestResult;
import ru.testit.syncstorage.api.TestResultsApi;
import ru.testit.syncstorage.api.WorkersApi;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.model.RegisterRequest;
import ru.testit.syncstorage.model.RegisterResponse;
import ru.testit.syncstorage.model.SetWorkerStatusRequest;
import ru.testit.syncstorage.model.TestResultCutApiModel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWrapper.class);
    private static final int API_CONNECT_TIMEOUT_MS = 10000;
    private static final int API_READ_TIMEOUT_MS = 10000;

    public RegistrationResult registerWorker(String url, String testRunId) {
        try {
            String workerPid =
                    "worker-" +
                            Thread.currentThread().getId() +
                            "-" +
                            System.currentTimeMillis();

            LOGGER.info("register on {}/register", url);

            WorkersApi workersApi = new WorkersApi(buildApiClient(url));
            RegisterResponse response = workersApi.registerPost(
                    new RegisterRequest()
                            .pid(workerPid)
                            .testRunId(testRunId)
            );
            boolean isMaster = Boolean.TRUE.equals(response.getIsMaster());

            return new RegistrationResult(workerPid, isMaster);
        } catch (Exception e) {
            LOGGER.error("Error on worker registering: {}", e.getMessage());
            return null;
        }
    }

    public boolean setWorkerStatus(
            String url,
            String pid,
            String status,
            String testRunId
    ) {
        try {
            WorkersApi workersApi = new WorkersApi(buildApiClient(url));
            workersApi.setWorkerStatusPost(
                    new SetWorkerStatusRequest()
                            .pid(pid)
                            .status(status)
                            .testRunId(testRunId)
            );
            return true;
        } catch (Exception e) {
            LOGGER.warn("Failed to set worker status: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendTestResultToSyncStorage(
            String url,
            String testRunId,
            TestResult testResult
    ) {
        try {
            TestResultsApi testResultsApi = new TestResultsApi(buildApiClient(url));
            testResultsApi.inProgressTestResultPost(
                    testRunId,
                    toTestResultCutApiModel(testResult)
            );
            return true;
        } catch (Exception e) {
            LOGGER.warn("Failed to send test result to SyncStorage: {}", e.getMessage());
            return false;
        }
    }

    private ApiClient buildApiClient(String url) {
        return new ApiClient()
                .setBasePath(url)
                .setConnectTimeout(API_CONNECT_TIMEOUT_MS)
                .setReadTimeout(API_READ_TIMEOUT_MS);
    }

    private TestResultCutApiModel toTestResultCutApiModel(TestResult testResult) {
        TestResultCutApiModel model = new TestResultCutApiModel()
                .autoTestExternalId(testResult.getExternalId())
                .statusCode(testResult.getItemStatus().value())
                .statusType(testResult.getItemStatus().value());

        if (testResult.getStart() != null) {
            model.startedOn(
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(testResult.getStart()),
                            ZoneOffset.UTC
                    )
            );
        }
        return model;
    }

    public static class RegistrationResult {
        private final String workerPid;
        private final boolean master;

        public RegistrationResult(String workerPid, boolean master) {
            this.workerPid = workerPid;
            this.master = master;
        }

        public String getWorkerPid() {
            return workerPid;
        }

        public boolean isMaster() {
            return master;
        }
    }
}
