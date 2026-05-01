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

import static ru.testit.clients.Converter.mapStatusType;

public class ClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWrapper.class);
    private static final int API_CONNECT_TIMEOUT_MS = 10000;
    private static final int API_READ_TIMEOUT_MS = 10000;
    private static final int RETRY_MAX_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 1000L;

    public RegistrationResult registerWorker(String url, String testRunId) {
        return executeWithRetry("register worker", () -> {
            String workerPid =
                    "worker-" +
                            Thread.currentThread().getId() +
                            "-" +
                            System.currentTimeMillis();
            LOGGER.debug("register on {}/register", url);
            WorkersApi workersApi = new WorkersApi(buildApiClient(url));
            RegisterResponse response = workersApi.registerPost(
                    new RegisterRequest()
                            .pid(workerPid)
                            .testRunId(testRunId)
            );
            boolean isMaster = Boolean.TRUE.equals(response.getIsMaster());
            return new RegistrationResult(workerPid, isMaster);
        }, null);
    }

    public boolean setWorkerStatus(
            String url,
            String pid,
            String status,
            String testRunId
    ) {
        Boolean result = executeWithRetry("set worker status", () -> {
            WorkersApi workersApi = new WorkersApi(buildApiClient(url));
            workersApi.setWorkerStatusPost(
                    new SetWorkerStatusRequest()
                            .pid(pid)
                            .status(status)
                            .testRunId(testRunId)
            );
            return Boolean.TRUE;
        }, Boolean.FALSE);
        return Boolean.TRUE.equals(result);
    }

    public boolean sendTestResultToSyncStorage(
            String url,
            String testRunId,
            TestResult testResult,
            String projectId
    ) {
        Boolean result = executeWithRetry("send test result to SyncStorage", () -> {
            TestResultsApi testResultsApi = new TestResultsApi(buildApiClient(url));
            testResultsApi.inProgressTestResultPost(
                    testRunId,
                    toTestResultCutApiModel(testResult, projectId)
            );
            return Boolean.TRUE;
        }, Boolean.FALSE);
        return Boolean.TRUE.equals(result);
    }

    private <T> T executeWithRetry(String operationName, RetryOperation<T> operation, T fallbackValue) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                LOGGER.warn(
                        "Failed to {} (attempt {}/{}): {}",
                        operationName,
                        attempt,
                        RETRY_MAX_ATTEMPTS,
                        e.getMessage()
                );
                if (attempt == RETRY_MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn(
                            "Interrupted while retrying SyncStorage request for {}, continue with fallback",
                            operationName
                    );
                    return fallbackValue;
                }
            }
        }

        LOGGER.error(
                "Failed to {} after {} attempts",
                operationName,
                RETRY_MAX_ATTEMPTS,
                lastException
        );
        return fallbackValue;
    }

    @FunctionalInterface
    private interface RetryOperation<T> {
        T execute() throws Exception;
    }

    private ApiClient buildApiClient(String url) {
        return new ApiClient()
                .setBasePath(url)
                .setConnectTimeout(API_CONNECT_TIMEOUT_MS)
                .setReadTimeout(API_READ_TIMEOUT_MS);
    }

    private TestResultCutApiModel toTestResultCutApiModel(TestResult testResult, String projectId) {
        String status = testResult.getItemStatus().value();
        TestResultCutApiModel model = new TestResultCutApiModel()
                .projectId(projectId)
                .autoTestExternalId(testResult.getExternalId())
                .statusCode(status)
                .statusType(mapStatusType(status).getValue());

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
