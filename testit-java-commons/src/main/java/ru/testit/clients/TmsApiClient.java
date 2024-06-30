package ru.testit.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.api.AttachmentsApi;
import ru.testit.client.api.AutoTestsApi;
import ru.testit.client.api.TestResultsApi;
import ru.testit.client.api.TestRunsApi;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TmsApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TmsApiClient.class);
    private static final String AUTH_PREFIX = "PrivateToken";
    private static final boolean INCLUDE_STEPS = true;
    private static final boolean INCLUDE_LABELS = true;
    private static final boolean INCLUDE_LINKS = true;
    private static final int MAX_TRIES = 10;
    private static final int WAITING_TIME = 100;

    private final TestRunsApi testRunsApi;
    private final AutoTestsApi autoTestsApi;
    private final AttachmentsApi attachmentsApi;
    private final TestResultsApi testResultsApi;

    private final ClientConfiguration clientConfiguration;

    public TmsApiClient(ClientConfiguration config) {
        ru.testit.client.invoker.ApiClient apiClient = new ru.testit.client.invoker.ApiClient();
        apiClient.setBasePath(config.getUrl());
        apiClient.setApiKeyPrefix(AUTH_PREFIX);
        apiClient.setApiKey(config.getPrivateToken());
        apiClient.setVerifyingSsl(config.getCertValidation());

        clientConfiguration = config;
        testRunsApi = new TestRunsApi(apiClient);
        autoTestsApi = new AutoTestsApi(apiClient);
        attachmentsApi = new AttachmentsApi(apiClient);
        testResultsApi = new TestResultsApi(apiClient);
    }

    @Override
    public TestRunV2GetModel createTestRun() throws ApiException {
        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(clientConfiguration.getProjectId()));

        if (!Objects.equals(this.clientConfiguration.getTestRunName(), "null")) {
            model.setName(this.clientConfiguration.getTestRunName());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create new test run: {}", model);
        }

        TestRunV2GetModel response = testRunsApi.createEmpty(model);
        testRunsApi.startTestRun(response.getId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The test run created: {}", response);
        }

        return response;
    }

    @Override
    public TestRunV2GetModel getTestRun(String uuid) throws ApiException {
        return testRunsApi.getTestRunById(UUID.fromString(uuid));
    }

    @Override
    public void completeTestRun(String uuid) throws ApiException {
        testRunsApi.completeTestRun(UUID.fromString(uuid));
    }

    @Override
    public void updateAutoTest(AutoTestPutModel model) throws ApiException {
        autoTestsApi.updateAutoTest(model);
    }

    @Override
    public String createAutoTest(AutoTestPostModel model) throws ApiException {
        return Objects.requireNonNull(autoTestsApi.createAutoTest(model).getId()).toString();
    }

    @Override
    public AutoTestModel getAutoTestByExternalId(String externalId) throws ApiException {
        AutotestFilterModel filter = new AutotestFilterModel();

        Set<UUID> projectIds = new HashSet<>();
        projectIds.add(UUID.fromString(this.clientConfiguration.getProjectId()));
        filter.setProjectIds(projectIds);
        filter.setIsDeleted(false);

        Set<String> externalIds = new HashSet<>();
        externalIds.add(externalId);
        filter.externalIds(externalIds);

        SearchAutoTestsQueryIncludesModel includes = new SearchAutoTestsQueryIncludesModel();
        includes.setIncludeLabels(INCLUDE_LABELS);
        includes.setIncludeSteps(INCLUDE_STEPS);
        includes.setIncludeLinks(INCLUDE_LINKS);

        AutotestsSelectModel model = new AutotestsSelectModel();
        model.setFilter(filter);
        model.setIncludes(includes);

        List<AutoTestModel> tests = autoTestsApi.apiV2AutoTestsSearchPost(null,
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
            LOGGER.debug("Link autotest {} to workitem {}", id, workItemId);

            for (int attempts = 0; attempts < MAX_TRIES; attempts++) {
                try {
                    autoTestsApi.linkAutoTestToWorkItem(id, new WorkItemIdModel().id(workItemId));
                    LOGGER.debug("Link autotest {} to workitem {} is successfully", id, workItemId);

                    attempts = MAX_TRIES;
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
    }

    @Override
    public void unlinkAutoTestToWorkItem(String id, String workItemId) {
        LOGGER.debug("Unlink autotest {} from workitem {}", id, workItemId);

        for (int attempts = 0; attempts < MAX_TRIES; attempts++) {
            try {
                autoTestsApi.deleteAutoTestLinkFromWorkItem(id, workItemId);
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
    public List<WorkItemIdentifierModel> getWorkItemsLinkedToTest(String id) throws ApiException {
        return autoTestsApi.getWorkItemsLinkedToAutoTest(id, false, false);
    }

    @Override
    public List<UUID> sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException {
        return testRunsApi.setAutoTestResultsForTestRun(UUID.fromString(testRunUuid), models);
    }

    @Override
    public String addAttachment(String path) throws ApiException {
        File file = new File(path);
        AttachmentModel model = attachmentsApi.apiV2AttachmentsPost(file);

        return model.getId().toString();
    }

    public List<String> getTestFromTestRun(String testRunUuid, String configurationId) throws ApiException {
        TestRunV2GetModel model = testRunsApi.getTestRunById(UUID.fromString(testRunUuid));
        UUID configUUID = UUID.fromString(configurationId);

        if (Objects.requireNonNull(model.getTestResults()).size() == 0) {
            return new ArrayList<>();
        }

        return model.getTestResults().stream()
                .filter(result -> Objects.equals(result.getConfigurationId(), configUUID))
                .map(result -> Objects.requireNonNull(result.getAutoTest()).getExternalId()).collect(Collectors.toList());
    }

    @Override
    public TestResultModel getTestResult(UUID uuid) throws ApiException {
        return testResultsApi.apiV2TestResultsIdGet(uuid);
    }

    @Override
    public void updateTestResult(UUID uuid, TestResultUpdateModel model) throws ApiException {
        testResultsApi.apiV2TestResultsIdPut(uuid, model);
    }
}
