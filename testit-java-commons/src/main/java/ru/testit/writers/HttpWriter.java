package ru.testit.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.clients.ApiClient;
import ru.testit.clients.ClientConfiguration;
import ru.testit.models.ClassContainer;
import ru.testit.models.ItemStatus;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.services.ResultStorage;

import java.util.*;

public class HttpWriter implements Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpWriter.class);
    private final Map<String, UUID> testResults;
    private final ApiClient apiClient;
    private final ResultStorage storage;
    private final ClientConfiguration config;

    public HttpWriter(ClientConfiguration config, ApiClient client, ResultStorage storage) {
        this.config = config;
        this.apiClient = client;
        this.storage = storage;
        this.testResults = new HashMap<>();
    }

    @Override
    public void writeTest(TestResult testResult) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Write auto test {}", testResult.getExternalId());
            }

            AutoTestModel autotest = apiClient.getAutoTestByExternalId(testResult.getExternalId());
            List<String> workItemId = testResult.getWorkItemId();
            String autoTestId;

            if (autotest != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Auto test is exist. Update auto test {}", testResult.getExternalId());
                }

                AutoTestPutModel autoTestPutModel;

                if (testResult.getItemStatus() == ItemStatus.FAILED) {
                    autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autotest);
                    autoTestPutModel.links(Converter.convertPutLinks(testResult.getLinkItems()));
                } else {
                    autoTestPutModel = Converter.testResultToAutoTestPutModel(testResult);
                    autoTestPutModel.setProjectId(UUID.fromString(config.getProjectId()));
                }

                autoTestPutModel.setIsFlaky(autotest.getIsFlaky());

                apiClient.updateAutoTest(autoTestPutModel);
                autoTestId = autotest.getId().toString();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Create new auto test {}", testResult.getExternalId());
                }

                AutoTestPostModel model = Converter.testResultToAutoTestPostModel(testResult);
                model.setProjectId(UUID.fromString(config.getProjectId()));
                autoTestId = apiClient.createAutoTest(model);
            }

            if (!workItemId.isEmpty()) {
                if (!apiClient.tryLinkAutoTestToWorkItem(autoTestId, workItemId)) {
                    return;
                }
            }

            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel = Converter.testResultToAutoTestResultsForTestRunModel(testResult);
            autoTestResultsForTestRunModel.setConfigurationId(UUID.fromString(config.getConfigurationId()));

            List<AutoTestResultsForTestRunModel> results = new ArrayList<>();
            results.add(autoTestResultsForTestRunModel);
            List<UUID> ids = apiClient.sendTestResults(config.getTestRunId(), results);
            testResults.put(testResult.getUuid(), ids.get(0));
        } catch (ApiException e) {
            LOGGER.error("Can not write the autotest: " + (e.getMessage()));
        }
    }

    @Override
    public void writeClass(ClassContainer container) {
        for (final String testUuid : container.getChildren()) {
            storage.getTestResult(testUuid).ifPresent(test -> {
                try {
                    AutoTestModel autoTestModel = apiClient.getAutoTestByExternalId(test.getExternalId());

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
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

//        List<AutoTestResultsForTestRunModel> results = new ArrayList<>();

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(cl -> {
                List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                for (final String testUuid : cl.getChildren()) {
                    storage.getTestResult(testUuid).ifPresent(test -> {
                        try {
                            AutoTestModel autoTestModel = apiClient.getAutoTestByExternalId(test.getExternalId());

                            if (autoTestModel == null) {
                                return;
                            }

                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                            List<AutoTestStepModel> beforeFinish = new ArrayList<>(beforeAll);
                            beforeFinish.addAll(autoTestPutModel.getSetup());
                            autoTestPutModel.setSetup(beforeFinish);

                            List<AutoTestStepModel> afterClass = Converter.convertFixture(cl.getAfterClassMethods(), null);

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

                            TestResultModel resultModel = apiClient.getTestResult(testResultId);
                            TestResultUpdateModel model = Converter.testResultToTestResultUpdateModel(resultModel);
                            model.setSetupResults(beforeResultFinish);
                            model.setTeardownResults(afterResultFinish);

                            apiClient.updateTestResult(testResultId, model);

                        } catch (ApiException e) {
                            LOGGER.error("Can not update the autotest: " + (e.getMessage()));
                        }
                    });
                }
            });
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
