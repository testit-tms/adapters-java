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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public void writeTest(TestResult testResult) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Write auto test {}", testResult.getExternalId());
            }

            AutoTestModel test = apiClient.getAutoTestByExternalId(testResult.getExternalId());
            List<String> workItemId = testResult.getWorkItemId();
            String autoTestId;

            if (test != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Auto test is exist. Update auto test {}", testResult.getExternalId());
                }

                AutoTestPutModel autoTestPutModel;

                if (testResult.getItemStatus() == ItemStatus.FAILED) {
                    autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(test);
                    autoTestPutModel.links(Converter.convertPutLinks(testResult.getLinkItems()));
                } else {
                    autoTestPutModel = Converter.testResultToAutoTestPutModel(testResult);
                    autoTestPutModel.setProjectId(UUID.fromString(config.getProjectId()));
                }

                apiClient.updateAutoTest(autoTestPutModel);
                autoTestId = test.getId().toString();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Create new auto test {}", testResult.getExternalId());
                }

                AutoTestPostModel model = Converter.testResultToAutoTestPostModel(testResult);
                model.setProjectId(UUID.fromString(config.getProjectId()));
                autoTestId = apiClient.createAutoTest(model);
            }

            if (workItemId.size() == 0 ||
                    (test != null && testResult.getItemStatus() == ItemStatus.FAILED)) {
                return;
            }

            workItemId.forEach(i -> {
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Link work item {} to auto test {}", i, testResult.getExternalId());
                    }
                    apiClient.linkAutoTestToWorkItem(autoTestId, i);
                } catch (ApiException e) {
                    LOGGER.error("Can not link the autotest: ".concat(e.getMessage()));
                }
            });
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
        List<AutoTestStepModel> beforeAll = Converter.convertFixture(container.getBeforeMethods(), null);
        List<AutoTestStepModel> afterAll = Converter.convertFixture(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        List<AutoTestResultsForTestRunModel> results = new ArrayList<>();

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(
                    cl -> {
                        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                        List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                        for (final String testUuid : cl.getChildren()) {
                            storage.getTestResult(testUuid).ifPresent(
                                    test -> {
                                        try {
                                            AutoTestModel autoTestModel = apiClient.getAutoTestByExternalId(test.getExternalId());

                                            if (autoTestModel == null) {
                                                return;
                                            }

                                            AutoTestPutModel autoTestPutModel = Converter.autoTestModelToAutoTestPutModel(autoTestModel);

                                            List<AutoTestStepModel> beforeFinish = beforeAll;
                                            beforeFinish.addAll(autoTestPutModel.getSetup());
                                            autoTestPutModel.setSetup(beforeFinish);

                                            List<AutoTestStepModel> afterClass = Converter.convertFixture(cl.getAfterClassMethods(), null);

                                            List<AutoTestStepModel> afterFinish = autoTestPutModel.getTeardown();
                                            afterFinish.addAll(afterClass);
                                            afterFinish.addAll(afterAll);
                                            autoTestPutModel.setTeardown(afterFinish);

                                            apiClient.updateAutoTest(autoTestPutModel);


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
            if (results.size() == 0) {
                return;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Write results: {}", results);
            }
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
