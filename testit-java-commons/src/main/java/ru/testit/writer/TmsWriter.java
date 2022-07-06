package ru.testit.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.AutoTestsApi;
import ru.testit.client.TestResultsApi;
import ru.testit.client.TestRunsApi;
import ru.testit.invoker.ApiClient;
import ru.testit.invoker.ApiException;
import ru.testit.model.*;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.properties.AppProperties;
import ru.testit.services.TmsFactory;
import ru.testit.tms.client.ITMSClient;
import ru.testit.tms.client.TMSClient;
import ru.testit.tms.models.config.ClientConfiguration;

import java.util.*;

public class TmsWriter {
    private static final String AUTH_PREFIX = "PrivateToken";
    private static final Logger LOGGER = LoggerFactory.getLogger(TmsWriter.class);

    private ClientConfiguration config;
    private ITMSClient tmsClient;
    private TestRunsApi testRunsApi;
    private AutoTestsApi autoTestsApi;
    private TestResultsApi testResultsApi;

    public TmsWriter() {
        Properties appProperties = AppProperties.loadProperties();
        this.config = new ClientConfiguration(appProperties);
        this.tmsClient = new TMSClient(config);

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(config.getUrl());
        apiClient.setApiKeyPrefix(AUTH_PREFIX);
        apiClient.setApiKey(config.getPrivateToken());

        testRunsApi = new TestRunsApi(apiClient);
        autoTestsApi = new AutoTestsApi(apiClient);
        testResultsApi = new TestResultsApi(apiClient);
    }

    public void startLaunch() {
        if (this.config.getTestRunId() != "null") {
            return;
        }

        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(config.getProjectId()));

        try {
            TestRunV2GetModel response = testRunsApi.createEmpty(model);
            this.config.setTestRunId(response.getId().toString());

        } catch (ApiException e) {
            LOGGER.error("Can not start the launch: ".concat(e.getMessage()));
        }
    }

    public void finishLaunch() {
        try {
            testRunsApi.completeTestRun(UUID.fromString(config.getTestRunId()));
        } catch (ApiException e) {
            if (e.getResponseBody().contains("the StateName is already Completed")) {
                return;
            }
            LOGGER.error("Can not finish the launch: ".concat(e.getMessage()));
        }
    }

    public void writeTest(TestResult testResult) {
        try {
            AutoTestModel test = getAutoTestByExternalId(testResult.getExternalId());

            if (test != null) {
                AutoTestPutModel autoTestPutModel = Converter.testResultToAutoTestPutModel(testResult);
                autoTestPutModel.setProjectId(UUID.fromString(config.getProjectId()));
                autoTestsApi.updateAutoTest(autoTestPutModel);

                return;
            }

            AutoTestPostModel model = Converter.testResultToAutoTestPostModel(testResult);
            model.setProjectId(UUID.fromString(config.getProjectId()));

            autoTestsApi.createAutoTest(model);
        } catch (ApiException e) {
            LOGGER.error("Can not write the autotest: ".concat(e.getMessage()));
        }
    }

    private AutoTestModel getAutoTestByExternalId(String externalId) throws ApiException {
        List<AutoTestModel> tests = autoTestsApi.getAllAutoTests(UUID.fromString(config.getProjectId()),
                externalId, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                true, true, null, null, null, null, null);

        if (tests.stream().count() == 0) {
            LOGGER.warn("Can not find the autotest with external ID ".concat(externalId));
            return null;
        }

        return tests.get(0);
    }

    public void writeClass(ClassContainer container) {
        for (final String testUuid : container.getChildren()) {
            TmsFactory.getResultStorage().getTestResult(testUuid).ifPresent(
                    test -> {
                        try {
                            AutoTestModel autoTestModel = getAutoTestByExternalId(test.getExternalId());

                            if (autoTestModel == null){
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

                            autoTestsApi.updateAutoTest(autoTestPutModel);
                        } catch (ApiException e) {
                            LOGGER.error("Can not write the class: ".concat(e.getMessage()));
                        }
                    }
            );
        }
    }

    public void writeTests(MainContainer container) {
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        List<AutoTestResultsForTestRunModel> results = new ArrayList<>();

        for (final String classUuid : container.getChildren()) {
            TmsFactory.getResultStorage().getClassContainer(classUuid).ifPresent(
                    cl -> {
                        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                        List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                        for (final String testUuid : cl.getChildren()) {
                            TmsFactory.getResultStorage().getTestResult(testUuid).ifPresent(
                                    test -> {
                                        try {
                                            AutoTestModel autoTestModel = getAutoTestByExternalId(test.getExternalId());

                                            if (autoTestModel == null){
                                                return;
                                            }

                                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                                            List<AutoTestStepModel> beforeFinish = beforeAll;
                                            beforeFinish.addAll(autoTestPutModel.getSetup());
                                            autoTestPutModel.setSetup(beforeFinish);

                                            List<AutoTestStepModel> afterFinish = afterAll;
                                            afterFinish.addAll(autoTestPutModel.getTeardown());
                                            autoTestPutModel.setTeardown(afterFinish);

                                            autoTestsApi.updateAutoTest(autoTestPutModel);


                                            AutoTestResultsForTestRunModel autoTestResultsForTestRunModel =
                                                    Converter.testResultToAutoTestResultsForTestRunModel(test);
                                            autoTestResultsForTestRunModel
                                                    .setConfigurationId(UUID.fromString(config.getConfigurationId()));

                                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultEach =
                                                    Converter.convertResultFixture(cl.getBeforeEachTest(), testUuid);
                                            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultFinish = new ArrayList<>();
                                            beforeResultFinish.addAll(beforeResultAll);
                                            beforeResultFinish.addAll(beforeResultClass);
                                            beforeResultFinish.addAll(beforeResultEach);

                                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultEach =
                                                    Converter.convertResultFixture(cl.getAfterEachTest(), testUuid);
                                            List<AttachmentPutModelAutoTestStepResultsModel> afterResultFinish = new ArrayList<>();
                                            afterResultFinish.addAll(afterResultAll);
                                            afterResultFinish.addAll(afterResultClass);
                                            afterResultFinish.addAll(afterResultEach);

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
            testRunsApi.setAutoTestResultsForTestRun(UUID.fromString(config.getTestRunId()), results);
        } catch (ApiException e) {
            LOGGER.error("Can not write the test results: ".concat(e.getMessage()));
        }
    }
}
