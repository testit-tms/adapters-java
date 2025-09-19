package ru.testit.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.Converter;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.services.ResultStorage;
import ru.testit.writers.helpers.BulkAutotestHelper;

import java.util.*;

public class HttpWriter implements Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpWriter.class);
    private final Map<String, UUID> testResults;
    private final ITmsApiClient apiClient;
    private final ResultStorage storage;
    private final ClientConfiguration config;

    public HttpWriter(ClientConfiguration config, ITmsApiClient client, ResultStorage storage) {
        this.config = config;
        this.apiClient = client;
        this.storage = storage;
        this.testResults = new HashMap<>();
    }

    @Override
    public void writeTest(TestResult testResult) {
        if (!config.shouldImportRealtime())
        {
            return;
        }

        writeTestRealtime(testResult);
    }

    private void writeTestRealtime(TestResult testResult) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Write the auto test {}", testResult.getExternalId());
            }

            AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(testResult.getExternalId());
            String autoTestId;

            AutoTestModel autotest = Converter.convertAutoTestApiResultToAutoTestModel(autoTestApiResult);

            if (autotest != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The auto test {} is exist", testResult.getExternalId());
                }

                AutoTestPutModel autoTestPutModel = Converter.prepareToUpdateAutoTest(
                        testResult,
                        autotest,
                        config.getProjectId()
                );

                apiClient.updateAutoTest(autoTestPutModel);
                autoTestId = autotest.getId().toString();
            } else {
                AutoTestPostModel model = Converter.prepareToCreateAutoTest(testResult, config.getProjectId());
                autoTestId = apiClient.createAutoTest(model);
            }

            List<String> workItemIds = testResult.getWorkItemIds();

            if (!workItemIds.isEmpty()) {
                updateTestLinkToWorkItems(autoTestId, workItemIds);
            }

            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel = Converter.prepareTestResultForTestRun(
                    testResult,
                    config.getConfigurationId()
            );

            List<AutoTestResultsForTestRunModel> results = new ArrayList<>();
            results.add(autoTestResultsForTestRunModel);
            List<UUID> ids = apiClient.sendTestResults(config.getTestRunId(), results);
            testResults.put(testResult.getUuid(), ids.get(0));
        } catch (ApiException e) {
            LOGGER.error("Can not write the autotest: " + (e.getMessage()));
        }
    }

    private List<UUID> prepareWorkItemUuidsForUpdateAutoTest(List<UUID> workItemUuids, String autoTestId) throws ApiException {
        List<WorkItemIdentifierModel> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (WorkItemIdentifierModel linkedWorkItem : linkedWorkItems) {
            UUID linkedWorkItemUuid = linkedWorkItem.getId();

            if (workItemUuids.contains(linkedWorkItemUuid) || config.shouldAutomaticUpdationLinksToTestCases()) {
                continue;
            }

            workItemUuids.add(linkedWorkItemUuid);
        }

        return workItemUuids;
    }

    private void updateTestLinkToWorkItems(String autoTestId, List<String> workItemIds) throws ApiException {
        List<WorkItemIdentifierModel> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (WorkItemIdentifierModel linkedWorkItem : linkedWorkItems) {
            String linkedWorkItemId = linkedWorkItem.getGlobalId().toString();

            if (workItemIds.contains(linkedWorkItemId)) {
                workItemIds.remove(linkedWorkItemId);

                continue;
            }

            if (config.shouldAutomaticUpdationLinksToTestCases()) {
                apiClient.unlinkAutoTestToWorkItem(autoTestId, linkedWorkItemId);
            }
        }

        apiClient.linkAutoTestToWorkItems(autoTestId, workItemIds);
    }

    @Override
    public void writeClass(ClassContainer container) {
        if (!config.shouldImportRealtime())
        {
            return;
        }

        for (final String testUuid : container.getChildren()) {
            storage.getTestResult(testUuid).ifPresent(test -> {
                try {
                    AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                    AutoTestModel autoTestModel = Converter.convertAutoTestApiResultToAutoTestModel(autoTestApiResult);

                    if (autoTestModel == null) {
                        return;
                    }

                    AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                    List<AutoTestStepModel> beforeClass = Converter.convertFixture(container.getBeforeClassMethods(), null);
                    List<AutoTestStepModel> beforeEach = Converter.convertFixture(container.getBeforeEachTest(), testUuid);
                    beforeClass.addAll(beforeEach);

                    List<AutoTestStepModel> afterClass = Converter.convertFixture(container.getAfterClassMethods(), null);
                    List<AutoTestStepModel> afterEach = Converter.convertFixture(container.getAfterEachTest(), testUuid);
                    afterClass.addAll(afterEach);

                    autoTestPutModel.setSetup(beforeClass);
                    autoTestPutModel.setTeardown(afterClass);

                    autoTestPutModel.setIsFlaky(autoTestModel.getIsFlaky());

                    apiClient.updateAutoTest(autoTestPutModel);
                } catch (ApiException e) {
                    LOGGER.error("Can not write the class: " + (e.getMessage()));
                }
            });
        }
    }

    @Override
    public void writeTests(MainContainer container) {
        if (config.shouldImportRealtime())
        {
            updateTestResults(container);

            return;
        }

        writeTestsAfterAll(container);
    }

    private void updateTestResults(MainContainer container) {
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(cl -> {
                List<AutoTestStepModel> afterClass = Converter.convertFixture(cl.getAfterClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                for (final String testUuid : cl.getChildren()) {
                    storage.getTestResult(testUuid).ifPresent(test -> {
                        try {
                            AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                            AutoTestModel autoTestModel = Converter.convertAutoTestApiResultToAutoTestModel(autoTestApiResult);

                            if (autoTestModel == null) {
                                return;
                            }

                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                            List<AutoTestStepModel> beforeFinish = new ArrayList<>(beforeAll);
                            beforeFinish.addAll(autoTestPutModel.getSetup());
                            autoTestPutModel.setSetup(beforeFinish);

                            List<AutoTestStepModel> afterFinish = autoTestPutModel.getTeardown();
                            afterFinish.addAll(afterClass);
                            afterFinish.addAll(afterAll);
                            autoTestPutModel.setTeardown(afterFinish);

                            autoTestPutModel.setIsFlaky(autoTestModel.getIsFlaky());

                            apiClient.updateAutoTest(autoTestPutModel);

                            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel = Converter.testResultToAutoTestResultsForTestRunModel(test);

                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultEach = Converter.convertResultFixture(cl.getBeforeEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultFinish = new ArrayList<>();
                            beforeResultFinish.addAll(beforeResultAll);
                            beforeResultFinish.addAll(beforeResultClass);
                            beforeResultFinish.addAll(beforeResultEach);

                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultEach = Converter.convertResultFixture(cl.getAfterEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultFinish = new ArrayList<>();
                            afterResultFinish.addAll(afterResultEach);
                            afterResultFinish.addAll(afterResultClass);
                            afterResultFinish.addAll(afterResultAll);

                            autoTestResultsForTestRunModel.setSetupResults(beforeResultFinish);
                            autoTestResultsForTestRunModel.setTeardownResults(afterResultFinish);

                            UUID testResultId = testResults.get(test.getUuid());

                            TestResultResponse resultModel = apiClient.getTestResult(testResultId);
                            TestResultUpdateV2Request model = Converter.testResultToTestResultUpdateModel(resultModel);
                            model.setSetupResults(Converter.stepResultsToRequests(beforeResultFinish));
                            model.setTeardownResults(Converter.stepResultsToRequests(afterResultFinish));

                            apiClient.updateTestResult(testResultId, model);

                        } catch (ApiException e) {
                            LOGGER.error("Can not update the autotest: " + (e.getMessage()));
                        }
                    });
                }
            });
        }
    }

    private void writeTestsAfterAll(MainContainer container) {
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        BulkAutotestHelper bulkHelper = new BulkAutotestHelper(apiClient, config);

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(cl -> {
                List<AutoTestStepModel> beforeClass = Converter.convertFixture(cl.getBeforeClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                List<AutoTestStepModel> afterClass = Converter.convertFixture(cl.getAfterClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                for (final String testUuid : cl.getChildren()) {
                    storage.getTestResult(testUuid).ifPresent(test -> {
                        try {
                            List<AutoTestStepModel> beforeEach = Converter.convertFixture(cl.getBeforeEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultEach = Converter.convertResultFixture(cl.getBeforeEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultFinish = new ArrayList<>();
                            beforeResultFinish.addAll(beforeResultAll);
                            beforeResultFinish.addAll(beforeResultClass);
                            beforeResultFinish.addAll(beforeResultEach);

                            List<AutoTestStepModel> afterEach = Converter.convertFixture(cl.getAfterEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultEach = Converter.convertResultFixture(cl.getAfterEachTest(), testUuid);
                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultFinish = new ArrayList<>();
                            afterResultFinish.addAll(afterResultEach);
                            afterResultFinish.addAll(afterResultClass);
                            afterResultFinish.addAll(afterResultAll);

                            List<AutoTestStepModel> beforeFinish = new ArrayList<>();
                            beforeFinish.addAll(beforeAll);
                            beforeFinish.addAll(beforeClass);
                            beforeFinish.addAll(beforeEach);

                            List<AutoTestStepModel> afterFinish = new ArrayList<>();
                            afterFinish.addAll(afterEach);
                            afterFinish.addAll(afterClass);
                            afterFinish.addAll(afterAll);

                            AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                            AutoTestModel autoTestModel = Converter.convertAutoTestApiResultToAutoTestModel(autoTestApiResult);

                            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel = Converter.prepareTestResultForTestRun(
                                    test,
                                    config.getConfigurationId()
                            );

                            autoTestResultsForTestRunModel.setSetupResults(beforeResultFinish);
                            autoTestResultsForTestRunModel.setTeardownResults(afterResultFinish);

                            if (autoTestModel == null) {
                                AutoTestPostModel model = Converter.prepareToCreateAutoTest(
                                        test,
                                        config.getProjectId()
                                );

                                model.setSetup(beforeFinish);
                                model.setTeardown(afterFinish);
                                bulkHelper.addForCreate(model, autoTestResultsForTestRunModel);
                            } else {
                                AutoTestPutModel model = Converter.prepareToUpdateAutoTest(
                                        test,
                                        autoTestModel,
                                        config.getProjectId()
                                );

                                model.setSetup(beforeFinish);
                                model.setTeardown(afterFinish);

                                String id = autoTestModel.getGlobalId().toString();
                                List<String> wi = test.getWorkItemIds();

                                Map<String, List<String>> autotestLinksToWIForUpdate = Map.of(
                                        id, wi
                                );

                                bulkHelper.addForUpdate(
                                        model,
                                        autoTestResultsForTestRunModel,
                                        autotestLinksToWIForUpdate
                                );
                            }
                        } catch (ApiException e) {
                            LOGGER.error(e.getMessage());
                        }
                    });
                }
            });
        }

        try {
            bulkHelper.teardown();
        } catch (ApiException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String writeAttachment(String path) {
        try {
            return apiClient.addAttachment(path);
        } catch (ApiException e) {
            LOGGER.error("Can not write attachment: " + (e.getMessage()));

            return "";
        }
    }

    void addUuid(String key, UUID uuid) {
        this.testResults.put(key, uuid);
    }
}
