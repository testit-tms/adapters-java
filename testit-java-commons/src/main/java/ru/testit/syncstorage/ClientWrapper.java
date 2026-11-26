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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static ru.testit.clients.Converter.mapStatusType;

public class ClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWrapper.class);
    private static final int API_CONNECT_TIMEOUT_MS = 10000;
    private static final int API_READ_TIMEOUT_MS = 10000;
    private static final int KEEP_ALIVE_TIMEOUT_MS = 5000;
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

    /**
     * Single non-retried ping; failures are logged and ignored (see sync-storage KEEP_ALIVE.md).
     */
    public boolean sendKeepAlive(String url, String pid, String testRunId) {
        if (pid == null || pid.isEmpty() || testRunId == null || testRunId.isEmpty()) {
            return false;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url + "/keep_alive").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(KEEP_ALIVE_TIMEOUT_MS);
            connection.setReadTimeout(KEEP_ALIVE_TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String body = "{\"pid\":\"" + jsonEscape(pid) + "\",\"testRunId\":\"" + jsonEscape(testRunId) + "\"}";
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.trace("keep_alive accepted for pid={}", pid);
                return true;
            }
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                String errorBody = readErrorBody(connection);
                // sync-storage returns "Worker not found"; a missing route is a generic 404.
                if (errorBody != null && errorBody.contains("Worker not found")) {
                    LOGGER.warn("keep_alive: worker not found (pid={})", pid);
                } else {
                    LOGGER.debug("keep_alive endpoint unavailable (HTTP 404, pid={})", pid);
                }
            } else {
                LOGGER.debug("keep_alive failed: HTTP {} (pid={})", responseCode, pid);
            }
            return false;
        } catch (Exception e) {
            LOGGER.debug("keep_alive failed for pid={}: {}", pid, e.getMessage());
            return false;
        }
    }

    private static String jsonEscape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String readErrorBody(HttpURLConnection connection) {
        try (InputStream stream = connection.getErrorStream()) {
            if (stream == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[256];
            int read;
            while ((read = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8.name()).trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Blocks until sync-storage Work X finishes for the run (or timeout / error).
     */
    public boolean waitCompletion(String url, String testRunId, int timeoutMs) {
        if (url == null || url.isEmpty() || testRunId == null || testRunId.isEmpty()) {
            return false;
        }
        try {
            String endpoint = url + "/wait-completion?testRunId=" +
                    URLEncoder.encode(testRunId, StandardCharsets.UTF_8.name());
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(Math.max(timeoutMs, 1000));
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.warn("wait-completion HTTP {} for testRunId={}", responseCode, testRunId);
                return false;
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            Boolean completed = SyncStorageRunner.extractJsonBooleanField(body.toString(), "completed");
            if (Boolean.TRUE.equals(completed)) {
                LOGGER.debug("wait-completion: Work X finished for testRunId={}", testRunId);
                return true;
            }
            LOGGER.warn("wait-completion: not completed for testRunId={}, body={}", testRunId, body);
            return false;
        } catch (Exception e) {
            LOGGER.warn("wait-completion failed for testRunId={}: {}", testRunId, e.getMessage());
            return false;
        }
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
