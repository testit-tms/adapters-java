package ru.testit.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.testit.clients.ApiClient;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.*;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.clients.ClientConfiguration;
import ru.testit.services.ResultStorage;

import java.util.*;

public class HttpWriter implements Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpWriter.class);
    private final ApiClient apiClient;
    private final ResultStorage storage;
    private final ClientConfiguration config;

    public HttpWriter(ClientConfiguration config, ApiClient client, ResultStorage storage) {
        this.config = config;
        this.apiClient = client;
        this.storage = storage;
    }

    @Override
    public void startLaunch() {
        if (!Objects.equals(this.config.getTestRunId(), "null")) {
            return;
        }

        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(config.getProjectId()));

        try {
            TestRunV2GetModel response = apiClient.createTestRun(model);
            this.config.setTestRunId(response.getId().toString());

        } catch (ApiException e) {
            LOGGER.error("Can not start the launch: ".concat(e.getMessage()));
        }
    }

    @Override
    public void finishLaunch() {
        try {
            TestRunV2GetModel testRun = apiClient.getTestRun(config.getTestRunId());

            if (testRun.getStateName() != TestRunStateTypeModel.COMPLETED) {
                apiClient.completeTestRun(config.getTestRunId());
            }
        } catch (ApiException e) {
            if (e.getResponseBody().contains("the StateName is already Completed")) {
                return;
            }
            LOGGER.error("Can not finish the launch: ".concat(e.getMessage()));
        }
    }

    @Override
    public void writeTest(TestResult testResult) {
        try {
            AutoTestModel test = apiClient.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId());
            String workItemId = testResult.getWorkItemId();
            String autoTestId;

            if (test != null) {
                AutoTestPutModel autoTestPutModel = Converter.testResultToAutoTestPutModel(storage, testResult);
                autoTestPutModel.setProjectId(UUID.fromString(config.getProjectId()));
                apiClient.updateAutoTest(autoTestPutModel);
                autoTestId = test.getId().toString();
            } else {
                AutoTestPostModel model = Converter.testResultToAutoTestPostModel(storage, testResult);
                model.setProjectId(UUID.fromString(config.getProjectId()));
                autoTestId = apiClient.createAutoTest(model);
            }

            if (workItemId != null) {
                apiClient.linkAutoTestToWorkItem(autoTestId, workItemId);
            }
        } catch (ApiException e) {
            LOGGER.error("Can not write the autotest: ".concat(e.getMessage()));
        }
    }

    @Override
    public void writeClass(ClassContainer container) {
        for (final String testUuid : container.getChildren()) {
            storage.getTestResult(testUuid).ifPresent(
                    test -> {
                        try {
                            AutoTestModel autoTestModel = apiClient.getAutoTestByExternalId(config.getProjectId(), test.getExternalId());

                            if (autoTestModel == null) {
                                return;
                            }

                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                            List<AutoTestStepModel> beforeClass = Converter.convertFixture(storage, container.getBeforeClassMethods(), null);
                            List<AutoTestStepModel> beforeEach = Converter.convertFixture(storage, container.getBeforeEachTest(), testUuid);
                            beforeClass.addAll(beforeEach);

                            List<AutoTestStepModel> afterClass = Converter.convertFixture(storage, container.getAfterClassMethods(), null);
                            List<AutoTestStepModel> afterEach = Converter.convertFixture(storage, container.getAfterEachTest(), testUuid);
                            afterClass.addAll(afterEach);

                            autoTestPutModel.setSetup(beforeClass);
                            autoTestPutModel.setTeardown(afterClass);

                            apiClient.updateAutoTest(autoTestPutModel);
                        } catch (ApiException e) {
                            LOGGER.error("Can not write the class: ".concat(e.getMessage()));
                        }
                    }
            );
        }
    }

    @Override
    public void writeTests(MainContainer container) {
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(storage, container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(storage, container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(storage, container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(storage, container.getAfterMethods(), null);

        List<AutoTestResultsForTestRunModel> results = new ArrayList<>();

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(
                    cl -> {
                        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(storage, cl.getBeforeClassMethods(), null);
                        List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(storage, cl.getAfterClassMethods(), null);

                        for (final String testUuid : cl.getChildren()) {
                            storage.getTestResult(testUuid).ifPresent(
                                    test -> {
                                        try {
                                            AutoTestModel autoTestModel = apiClient.getAutoTestByExternalId(config.getProjectId(), test.getExternalId());

                                            if (autoTestModel == null) {
                                                return;
                                            }

                                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                                            List<AutoTestStepModel> beforeFinish = beforeAll;
                                            beforeFinish.addAll(autoTestPutModel.getSetup());
                                            autoTestPutModel.setSetup(beforeFinish);

                                            List<AutoTestStepModel> afterClass = Converter.convertFixture(storage, cl.getAfterClassMethods(), null);

                                            List<AutoTestStepModel> afterFinish = autoTestPutModel.getTeardown();
                                            afterFinish.addAll(afterClass);
                                            afterFinish.addAll(afterAll);
                                            autoTestPutModel.setTeardown(afterFinish);

                                            apiClient.updateAutoTest(autoTestPutModel);


                                            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel =
                                                    Converter.testResultToAutoTestResultsForTestRunModel(storage, test);
                                            autoTestResultsForTestRunModel
                                                    .setConfigurationId(UUID.fromString(config.getConfigurationId()));

                                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultEach =
                                                    Converter.convertResultFixture(storage, cl.getBeforeEachTest(), testUuid);
                                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultFinish = new ArrayList<>();
                                            beforeResultFinish.addAll(beforeResultAll);
                                            beforeResultFinish.addAll(beforeResultClass);
                                            beforeResultFinish.addAll(beforeResultEach);

                                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultEach =
                                                    Converter.convertResultFixture(storage, cl.getAfterEachTest(), testUuid);
                                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultFinish = new ArrayList<>();
                                            afterResultFinish.addAll(afterResultEach);
                                            afterResultFinish.addAll(afterResultClass);
                                            afterResultFinish.addAll(afterResultAll);

                                            autoTestResultsForTestRunModel.setSetupResults(beforeResultFinish);
                                            autoTestResultsForTestRunModel.setTeardownResults(afterResultFinish);

                                            results.add(autoTestResultsForTestRunModel);

                                        } catch (ApiException e) {
                                            LOGGER.error("Can not update the autotest: ".concat(e.getMessage()));
                                        }
                                    }
                            );
                        }
                    }
            );
        }

        try {
            apiClient.sendTestResults(config.getTestRunId(), results);
        } catch (ApiException e) {
            LOGGER.error("Can not write the test results: ".concat(e.getMessage()));
        }
    }

    @Override
    public String writeAttachment(String path) {
        try {
            return apiClient.addAttachment(path);
        } catch (ApiException e) {
            LOGGER.error("Can not write attachment: ".concat(e.getMessage()));

            return "";
        }
    }
}
