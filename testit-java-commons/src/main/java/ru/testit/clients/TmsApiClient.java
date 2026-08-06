package ru.testit.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.testit.adaptersapi.api.*;
import ru.testit.adaptersapi.invoker.ApiClient;
import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.model.*;
import ru.testit.services.HtmlEscapeUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TmsApiClient implements ITmsApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TmsApiClient.class);
    private static final String AUTH_PREFIX = "PrivateToken";
    private static final boolean INCLUDE_STEPS = true;
    private static final boolean INCLUDE_LABELS = true;
    private static final boolean INCLUDE_LINKS = true;
    private static final int MAX_TRIES = 4;
    private static final int WAITING_TIME = 100;
    private static final int TESTS_LIMIT = 100;
    private static final ObjectMapper V2_JSON = new ObjectMapper();

    private final TestRunsApi testRunsApi;
    private final AutoTestsApi autoTestsApi;
    private final AttachmentsApi attachmentsApi;
    private final TestResultsApi testResultsApi;
    private final WorkItemsApi workItemsApi;

    private final ClientConfiguration clientConfiguration;

    public TmsApiClient(ClientConfiguration config) {
        boolean disableCertificateValidation = !config.getCertValidation();
        ApiClient apiClient = new ApiClient(disableCertificateValidation);
        apiClient.setBasePath(config.getUrl());
        apiClient.setApiKeyPrefix(AUTH_PREFIX);
        apiClient.setApiKey(config.getPrivateToken());

        clientConfiguration = config;
        testRunsApi = new TestRunsApi(apiClient);
        autoTestsApi = new AutoTestsApi(apiClient);
        attachmentsApi = new AttachmentsApi(apiClient);
        testResultsApi = new TestResultsApi(apiClient);
        workItemsApi = new WorkItemsApi(apiClient);
    }

    @Override
    public TestRunApiResult createTestRun() throws ApiException {
        CreateEmptyTestRunApiModel model = new CreateEmptyTestRunApiModel();
        model.setProjectId(UUID.fromString(clientConfiguration.getProjectId()));

        if (!Objects.equals(this.clientConfiguration.getTestRunName(), "null")) {
            model.setName(HtmlEscapeUtils.escapeHtmlTags(this.clientConfiguration.getTestRunName()));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create new test run: {}", model);
        }

        TestRunApiResult response = testRunsApi.adaptersTestRunsPost(model);
        testRunsApi.adaptersTestRunsIdStartPost(response.getId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The test run created: {}", response);
        }

        return response;
    }

    @Override
    public TestRunApiResult getTestRun(String uuid) throws ApiException {
        return testRunsApi.adaptersTestRunsIdGet(UUID.fromString(uuid));
    }

    @Override
    public void updateTestRun(UpdateEmptyTestRunApiModel testRun) throws ApiException {
        testRunsApi.adaptersTestRunsPut(testRun);
    }

    @Override
    public void completeTestRun(String uuid) throws ApiException {
        testRunsApi.adaptersTestRunsIdCompletePost(UUID.fromString(uuid));
    }

    @Override
    public void updateAutoTest(AutoTestUpdateApiModel model) throws ApiException {
        // Escape HTML tags in model before sending
        autoTestsApi.adaptersAutoTestsPut(model);
    }

    @Override
    public String createAutoTest(AutoTestCreateApiModel model) throws ApiException {
        // Escape HTML tags in model before sending
        return Objects.requireNonNull(autoTestsApi.adaptersAutoTestsPost(model).getId()).toString();
    }

    @Override
    public void updateAutoTests(List<AutoTestUpdateApiModel> models) throws ApiException {
        // Escape HTML tags in models before sending
        autoTestsApi.adaptersAutoTestsBulkPut(models);
    }

    @Override
    public List<AutoTestApiResult> createAutoTests(List<AutoTestCreateApiModel> models) throws ApiException {
        // Escape HTML tags in models before sending
        return autoTestsApi.adaptersAutoTestsBulkPost(models);
    }

    @Override
    public List<UUID> getWorkItemUuidsByIds(Iterable<String> workItemIds) {
        List<UUID> workItemUuids = new ArrayList<>();

        for (String workItemId : workItemIds) {
            try
            {
                WorkItemApiResult workItem = workItemsApi.adaptersWorkItemsIdGet(workItemId, null, null);

                workItemUuids.add(workItem.getId());
            } catch (ApiException e) {
                LOGGER.error("Cannot get work item by id {} {}", workItemId, e.getMessage());
            }
        }

        return workItemUuids;
    }

    @Override
    public AutoTestApiResult getAutoTestByExternalId(String externalId) throws ApiException {
        AutoTestFilterApiModel filter = new AutoTestFilterApiModel();

        Set<UUID> projectIds = new HashSet<>();
        projectIds.add(UUID.fromString(this.clientConfiguration.getProjectId()));
        filter.setProjectIds(projectIds);
        filter.setIsDeleted(false);

        Set<String> externalIds = new HashSet<>();
        externalIds.add(externalId);
        filter.externalIds(externalIds);

        AutoTestSearchIncludeApiModel includes = new AutoTestSearchIncludeApiModel();
        includes.setIncludeLabels(INCLUDE_LABELS);
        includes.setIncludeSteps(INCLUDE_STEPS);
        includes.setIncludeLinks(INCLUDE_LINKS);

        AutoTestSearchApiModel model = new AutoTestSearchApiModel();
        model.setFilter(filter);
        model.setIncludes(includes);

        List<AutoTestApiResult> tests = autoTestsApi.adaptersAutoTestsSearchPost(null,
                null,
                null,
                null,
                null,
                model);

        if ((long) tests.size() == 0) {
            return null;
        }

        return tests.get(0);
    }

    @Override
    public void linkAutoTestToWorkItems(String id, Iterable<String> workItemIds) {
        for (String workItemId : workItemIds) {
            linkAutoTestToWorkItem(id, workItemId);
        }
    }

    public void linkAutoTestToWorkItem(String id, String workItemId) {
        LOGGER.debug("Link autotest {} from workitem {}", id, workItemId);

        for (int attempts = 0; attempts < MAX_TRIES; attempts++) {
            try {
                autoTestsApi.adaptersAutoTestsIdWorkItemsPost(id, new WorkItemIdApiModel().id(workItemId));
                LOGGER.debug("Link autotest {} to workitem {} is successfully", id, workItemId);

                return;
            } catch (ApiException e) {
                LOGGER.error("Cannot link autotest {} to work item {}", id, workItemId);

                try {
                    Thread.sleep(Duration.ofMillis(100).toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    public void unlinkAutoTestToWorkItem(String id, String workItemId) {
        LOGGER.debug("Unlink autotest {} from workitem {}", id, workItemId);

        for (int attempts = 0; attempts < MAX_TRIES; attempts++) {
            try {
                autoTestsApi.adaptersAutoTestsIdWorkItemsDelete(id, workItemId);
                LOGGER.debug("Unlink autotest {} from workitem {} is successfully", id, workItemId);

                return;
            } catch (ApiException e) {
                LOGGER.error("Cannot unlink autotest {} from work item {}", id, workItemId);

                try {
                    Thread.sleep(Duration.ofMillis(WAITING_TIME).toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    public List<AutoTestWorkItemIdentifierApiResult> getWorkItemsLinkedToTest(String id) throws ApiException {
        return autoTestsApi.adaptersAutoTestsIdWorkItemsGet(id, false, false);
    }

    @Override
    public List<UUID> sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException {
        // Escape HTML tags in models before sending
        return testRunsApi.adaptersTestRunsIdTestResultsPost(UUID.fromString(testRunUuid), models);
    }

    @Override
    public String addAttachment(String path) throws ApiException {
        File file = new File(path);
        AttachmentModel model = attachmentsApi.adaptersAttachmentsPost(file);

        return model.getId().toString();
    }

    @Override
    public List<String> getTestFromTestRun(String testRunUuid, String configurationId) throws ApiException {
        List<TestResultShortResponse> allTestResults = new ArrayList<>();
        TestResultsFilterApiModel model = new TestResultsFilterApiModel();
        model.setTestRunIds(Collections.singletonList(UUID.fromString(testRunUuid)));
        model.setConfigurationIds(Collections.singletonList(UUID.fromString(configurationId)));
        int skip = 0;

        do {
            List<TestResultShortResponse> testResults = testResultsApi.adaptersTestResultsSearchPost(
                    skip,
                    TESTS_LIMIT,
                    null,
                    null,
                    null,
                    model
            );

            allTestResults.addAll(testResults);
            skip += TESTS_LIMIT;

            if (testResults.isEmpty()) {
                skip = -1;
            }
        } while (skip >= 0);

        return allTestResults.stream()
                .map(result -> Objects.requireNonNull(result).getAutotestExternalId())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAutotestExternalIdsFromTestRun() throws ApiException {
        List<TestResultShortResponse> allTestResults = new ArrayList<>();
        TestResultsFilterApiModel model = Converter.buildTestResultsFilterApiModelWithInProgressOutcome(
                UUID.fromString(clientConfiguration.getTestRunId()),
                UUID.fromString(clientConfiguration.getConfigurationId())
        );
        int skip = 0;

        do
        {
            List<TestResultShortResponse> testResults = testResultsApi.adaptersTestResultsSearchPost(
                    skip,
                    TESTS_LIMIT,
                    null,
                    null,
                    null,
                    model
            );

            allTestResults.addAll(testResults);
            skip += TESTS_LIMIT;

            if (testResults.isEmpty()) {
                skip = -1;
            }
        } while(skip >= 0);

        return allTestResults.stream()
                .map(result -> Objects.requireNonNull(result).getAutotestExternalId())
                .collect(Collectors.toList());
    }

    @Override
    public UUID findInProgressTestResultId(String externalId) throws ApiException {
        if (externalId == null || externalId.isEmpty()) {
            return null;
        }

        List<TestResultShortResponse> matches = new ArrayList<>();
        TestResultsFilterApiModel model = Converter.buildTestResultsFilterApiModelWithInProgressOutcome(
                UUID.fromString(clientConfiguration.getTestRunId()),
                UUID.fromString(clientConfiguration.getConfigurationId())
        );
        int skip = 0;

        do {
            List<TestResultShortResponse> page = testResultsApi.adaptersTestResultsSearchPost(
                    skip,
                    TESTS_LIMIT,
                    null,
                    null,
                    null,
                    model
            );
            for (TestResultShortResponse item : page) {
                if (item != null && externalId.equals(item.getAutotestExternalId())) {
                    matches.add(item);
                }
            }
            skip += TESTS_LIMIT;
            if (page.isEmpty()) {
                skip = -1;
            }
        } while (skip >= 0);

        if (matches.isEmpty()) {
            return null;
        }

        UUID orphanId = null;
        for (TestResultShortResponse item : matches) {
            UUID id = item.getId();
            if (id == null) {
                continue;
            }
            // adapters DTO has no testPointId — resolve via raw v2 GET
            if (hasValidTestPointIdV2(id)) {
                return id;
            }
            if (orphanId == null) {
                orphanId = id;
            }
        }
        return orphanId;
    }

    /**
     * Hack: adapters OpenAPI for 5.8 omits testPointId. Read it from GET /api/v2/testResults/{id}.
     */
    private boolean hasValidTestPointIdV2(UUID testResultId) {
        try {
            String base = clientConfiguration.getUrl();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            URL url = new URL(base + "/api/v2/testResults/" + testResultId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty(
                    "Authorization",
                    AUTH_PREFIX + " " + clientConfiguration.getPrivateToken()
            );

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                LOGGER.debug("v2 getTestResult {} HTTP {}", testResultId, code);
                return false;
            }

            try (InputStream in = connection.getInputStream()) {
                JsonNode root = V2_JSON.readTree(in);
                JsonNode tp = root.get("testPointId");
                if (tp == null || tp.isNull()) {
                    return false;
                }
                String value = tp.asText();
                return value != null
                        && !value.isEmpty()
                        && !"00000000-0000-0000-0000-000000000000".equals(value);
            }
        } catch (Exception e) {
            LOGGER.debug("v2 getTestResult {} failed: {}", testResultId, e.getMessage());
            return false;
        }
    }

    @Override
    public TestResultResponse getTestResult(UUID uuid) throws ApiException {
        return testResultsApi.adaptersTestResultsIdGet(uuid);
    }

    @Override
    public void updateTestResult(UUID uuid, TestResultUpdateRequest model) throws ApiException {
        // Escape HTML tags in model before sending
        testResultsApi.adaptersTestResultsIdPut(uuid, model);
    }
}
