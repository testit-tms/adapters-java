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
import java.util.concurrent.atomic.AtomicInteger;

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
        if (!config.shouldImportRealtime()) {
            return;
        }

        writeTestRealtime(testResult);
    }

    @Override
    public void writeTestRealtime(TestResult testResult) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Write the auto test {}", testResult.getExternalId());
            }

            AutoTestApiResult autotest = apiClient.getAutoTestByExternalId(testResult.getExternalId());
            String autoTestId;


            if (autotest != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The auto test {} is exist", testResult.getExternalId());
                }

                AutoTestUpdateApiModel AutoTestUpdateApiModel = Converter.prepareToUpdateAutoTest(
                        testResult,
                        autotest,
                        config.getProjectId()
                );

                apiClient.updateAutoTest(AutoTestUpdateApiModel);
                autoTestId = autotest.getId().toString();
            } else {
                AutoTestCreateApiModel model = Converter.prepareToCreateAutoTest(testResult, config.getProjectId());
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
            LOGGER.error("Can not write the autotest: {}", e.getMessage());
        }
    }

    // TODO: use after refactoring
    private List<UUID> prepareWorkItemUuidsForUpdateAutoTest(List<UUID> workItemUuids, String autoTestId) throws ApiException {
        List<AutoTestWorkItemIdentifierApiResult> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (AutoTestWorkItemIdentifierApiResult linkedWorkItem : linkedWorkItems) {
            UUID linkedWorkItemUuid = linkedWorkItem.getId();

            if (workItemUuids.contains(linkedWorkItemUuid) || config.shouldAutomaticUpdationLinksToTestCases()) {
                continue;
            }

            workItemUuids.add(linkedWorkItemUuid);
        }

        return workItemUuids;
    }

    private void updateTestLinkToWorkItems(String autoTestId, List<String> workItemIds) throws ApiException {
        List<AutoTestWorkItemIdentifierApiResult> linkedWorkItems = apiClient.getWorkItemsLinkedToTest(autoTestId);

        for (AutoTestWorkItemIdentifierApiResult linkedWorkItem : linkedWorkItems) {
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
        if (!config.shouldImportRealtime()) {
            return;
        }

        for (final String testUuid : container.getChildren()) {
            storage.getTestResult(testUuid).ifPresent(test -> {
                try {
                    AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                    if (autoTestApiResult == null) {
                        return;
                    }

                    AutoTestUpdateApiModel autoTestUpdateApiModel = Converter.AutoTestApiResultToAutoTestUpdateApiModel(autoTestApiResult);

                    List<AutoTestStepApiModel> beforeClass = Converter.convertFixtureToApi(container.getBeforeClassMethods(), null);
                    List<AutoTestStepApiModel> beforeEach = Converter.convertFixtureToApi(container.getBeforeEachTest(), testUuid);
                    beforeClass.addAll(beforeEach);

                    List<AutoTestStepApiModel> afterClass = Converter.convertFixtureToApi(container.getAfterClassMethods(), null);
                    List<AutoTestStepApiModel> afterEach = Converter.convertFixtureToApi(container.getAfterEachTest(), testUuid);
                    afterClass.addAll(afterEach);

                    autoTestUpdateApiModel.setSetup(beforeClass);
                    autoTestUpdateApiModel.setTeardown(afterClass);

                    autoTestUpdateApiModel.setIsFlaky(autoTestApiResult.getIsFlaky());

                    apiClient.updateAutoTest(autoTestUpdateApiModel);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("AutoTest {} updated", test.getExternalId());
                    }
                } catch (ApiException e) {
                    LOGGER.error("Can not write the class: {}", (e.getMessage()));
                }
            });
        }
    }

    @Override
    public void writeTests(MainContainer container) {
        if (config.shouldImportRealtime()) {
            updateTestResults(container);

            return;
        }

        // realtime false: all in one
        writeTestsAfterAll(container);
    }

    private void updateTestResults(MainContainer container) {
        List<AutoTestStepApiModel> beforeAll = Converter.convertFixtureToApi(container.getBeforeMethods(), null);
        List<AutoTestStepApiModel> afterAll = Converter.convertFixtureToApi(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        for (final String classUuid : container.getChildren()) {
            storage.getClassContainer(classUuid).ifPresent(cl -> {
                List<AutoTestStepApiModel> afterClass = Converter.convertFixtureToApi(cl.getAfterClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
                List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

                for (final String testUuid : cl.getChildren()) {
                    storage.getTestResult(testUuid).ifPresent(test -> {
                        try {
                            AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                            if (autoTestApiResult == null) {
                                return;
                            }

                            AutoTestUpdateApiModel autoTestUpdateApiModel = Converter.AutoTestApiResultToAutoTestUpdateApiModel(autoTestApiResult);

                            List<AutoTestStepApiModel> beforeFinish = new ArrayList<>(beforeAll);
                            beforeFinish.addAll(autoTestUpdateApiModel.getSetup());
                            autoTestUpdateApiModel.setSetup(beforeFinish);

                            List<AutoTestStepApiModel> afterFinish = autoTestUpdateApiModel.getTeardown();
                            afterFinish.addAll(afterClass);
                            afterFinish.addAll(afterAll);
                            autoTestUpdateApiModel.setTeardown(afterFinish);

                            autoTestUpdateApiModel.setIsFlaky(autoTestApiResult.getIsFlaky());

                            apiClient.updateAutoTest(autoTestUpdateApiModel);

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
                            LOGGER.error("Can not update the autotest: {}", (e.getMessage()));
                        }
                    });
                }
            });
        }
    }

    private void writeTestsAfterAll(MainContainer container) {
        logBulkImportTreeVsStorageDiagnostics(container);

        List<AutoTestStepApiModel> beforeAll = Converter.convertFixtureToApi(container.getBeforeMethods(), null);
        List<AutoTestStepApiModel> afterAll = Converter.convertFixtureToApi(container.getAfterMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> beforeResultAll = Converter.convertResultFixture(container.getBeforeMethods(), null);
        List<AttachmentPutModelAutoTestStepResultsModel> afterResultAll = Converter.convertResultFixture(container.getAfterMethods(), null);

        BulkAutotestHelper bulkHelper = new BulkAutotestHelper(apiClient, config);

        AtomicInteger testsInTree = new AtomicInteger();
        AtomicInteger missingTestResult = new AtomicInteger();
        AtomicInteger bulkApiErrors = new AtomicInteger();

        LinkedHashSet<String> classUuids = new LinkedHashSet<>(container.getChildren());
        for (final String classUuid : classUuids) {
            Optional<ClassContainer> ocl = storage.getClassContainer(classUuid);
            if (!ocl.isPresent()) {
                LOGGER.warn("Bulk import: MainContainer references classUuid={} but ClassContainer is missing in storage", classUuid);
                continue;
            }
            ClassContainer cl = ocl.get();
            List<AutoTestStepApiModel> beforeClass = Converter.convertFixtureToApi(cl.getBeforeClassMethods(), null);
            List<AttachmentPutModelAutoTestStepResultsModel> beforeResultClass = Converter.convertResultFixture(cl.getBeforeClassMethods(), null);
            List<AutoTestStepApiModel> afterClass = Converter.convertFixtureToApi(cl.getAfterClassMethods(), null);
            List<AttachmentPutModelAutoTestStepResultsModel> afterResultClass = Converter.convertResultFixture(cl.getAfterClassMethods(), null);

            for (final String testUuid : cl.getChildren()) {
                testsInTree.incrementAndGet();
                Optional<TestResult> otr = storage.getTestResult(testUuid);
                if (!otr.isPresent()) {
                    missingTestResult.incrementAndGet();
                    LOGGER.warn("Bulk import: class tree lists uuid={} but TestResult is absent in storage (skipped)", testUuid);
                    continue;
                }
                TestResult test = otr.get();
                try {
                    List<AutoTestStepApiModel> beforeEach = Converter.convertFixtureToApi(cl.getBeforeEachTest(), testUuid);
                    List<AttachmentPutModelAutoTestStepResultsModel> beforeResultEach = Converter.convertResultFixture(cl.getBeforeEachTest(), testUuid);
                    List<AttachmentPutModelAutoTestStepResultsModel> beforeResultFinish = new ArrayList<>();
                    beforeResultFinish.addAll(beforeResultAll);
                    beforeResultFinish.addAll(beforeResultClass);
                    beforeResultFinish.addAll(beforeResultEach);

                    List<AutoTestStepApiModel> afterEach = Converter.convertFixtureToApi(cl.getAfterEachTest(), testUuid);
                    List<AttachmentPutModelAutoTestStepResultsModel> afterResultEach = Converter.convertResultFixture(cl.getAfterEachTest(), testUuid);
                    List<AttachmentPutModelAutoTestStepResultsModel> afterResultFinish = new ArrayList<>();
                    afterResultFinish.addAll(afterResultEach);
                    afterResultFinish.addAll(afterResultClass);
                    afterResultFinish.addAll(afterResultAll);

                    List<AutoTestStepApiModel> beforeFinish = new ArrayList<>();
                    beforeFinish.addAll(beforeAll);
                    beforeFinish.addAll(beforeClass);
                    beforeFinish.addAll(beforeEach);

                    List<AutoTestStepApiModel> afterFinish = new ArrayList<>();
                    afterFinish.addAll(afterEach);
                    afterFinish.addAll(afterClass);
                    afterFinish.addAll(afterAll);

                    AutoTestApiResult autoTestApiResult = apiClient.getAutoTestByExternalId(test.getExternalId());

                    AutoTestResultsForTestRunModel autoTestResultsForTestRunModel = Converter.prepareTestResultForTestRun(
                            test,
                            config.getConfigurationId()
                    );

                    autoTestResultsForTestRunModel.setSetupResults(beforeResultFinish);
                    autoTestResultsForTestRunModel.setTeardownResults(afterResultFinish);

                    if (autoTestApiResult == null) {
                        AutoTestCreateApiModel model = Converter.prepareToCreateAutoTest(
                                test,
                                config.getProjectId()
                        );

                        model.setSetup(beforeFinish);
                        model.setTeardown(afterFinish);
                        bulkHelper.addForCreate(model, autoTestResultsForTestRunModel);
                    } else {
                        AutoTestUpdateApiModel model = Converter.prepareToUpdateAutoTest(
                                test,
                                autoTestApiResult,
                                config.getProjectId()
                        );

                        model.setSetup(beforeFinish);
                        model.setTeardown(afterFinish);

                        String id = autoTestApiResult.getGlobalId().toString();
                        List<String> wi = test.getWorkItemIds();

                        Map<String, List<String>> autotestLinksToWIForUpdate = new HashMap<>();
                        autotestLinksToWIForUpdate.put(id, wi);

                        if (hasAutoTestChanged(autoTestApiResult, model)) {
                            bulkHelper.addForUpdate(
                                    model,
                                    autoTestResultsForTestRunModel,
                                    autotestLinksToWIForUpdate
                            );
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Bulk import: autotest unchanged, skip PUT for {}", test.getExternalId());
                            }
                            bulkHelper.addTestRunResultOnly(autoTestResultsForTestRunModel);
                        }
                    }
                } catch (ApiException e) {
                    bulkApiErrors.incrementAndGet();
                    LOGGER.error(
                            "Bulk import API error for externalId={}, testUuid={}: {}",
                            test.getExternalId(),
                            test.getUuid(),
                            e.getMessage(),
                            e
                    );
                }
            }
        }

        try {
            bulkHelper.teardown();
        } catch (ApiException e) {
            LOGGER.error("Bulk import teardown failed: {}", e.getMessage(), e);
        }

        LOGGER.info(
                "Bulk import finished: mainUuid={}, testsInTree={}, missingTestResultInStorage={}, bulkApiErrors={}",
                container.getUuid(),
                testsInTree.get(),
                missingTestResult.get(),
                bulkApiErrors.get()
        );
    }

    /**
     * Diagnostics for importRealtime=false: bulk only sends tests reachable via MainContainer → ClassContainer.children.
     */
    private void logBulkImportTreeVsStorageDiagnostics(MainContainer container) {
        Set<String> linkedTestUuids = new HashSet<>();
        for (String classUuid : new LinkedHashSet<>(container.getChildren())) {
            storage.getClassContainer(classUuid).ifPresent(cl -> linkedTestUuids.addAll(cl.getChildren()));
        }
        Set<String> storedTestUuids = storedTestUuidsForBulkDiagnostics(container);
        Set<String> inStorageNotInTree = new HashSet<>(storedTestUuids);
        inStorageNotInTree.removeAll(linkedTestUuids);
        Set<String> inTreeNotInStorage = new HashSet<>(linkedTestUuids);
        inTreeNotInStorage.removeAll(storedTestUuids);
        if (!inTreeNotInStorage.isEmpty()) {
            LOGGER.warn(
                    "Bulk import (importRealtime=false): {} UUID(s) in class container children but no TestResult in storage: {}",
                    inTreeNotInStorage.size(),
                    inTreeNotInStorage
            );
        }
        if (!inStorageNotInTree.isEmpty()) {
            LOGGER.warn(
                    "Bulk import (importRealtime=false): {} TestResult(s) for this main container are not linked under the class tree and will NOT be sent: {}. "
                            + "Typical cause: updateClassContainer did not run (see WARN).",
                    inStorageNotInTree.size(),
                    inStorageNotInTree
            );
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Bulk import scope: mainUuid={}, classContainerCount={}, linkedTestCount={}, storedTestResultCount={}",
                    container.getUuid(),
                    new LinkedHashSet<>(container.getChildren()).size(),
                    linkedTestUuids.size(),
                    storedTestUuids.size()
            );
        }
    }

    /** Per-main scope when TestResult.mainContainerUuid is set; otherwise global (legacy adapters, noisy with parallel classes). */
    private Set<String> storedTestUuidsForBulkDiagnostics(MainContainer container) {
        Set<String> scoped = storage.getTestResultUuidsForMainContainer(container.getUuid());
        if (!scoped.isEmpty()) {
            return scoped;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Bulk import diagnostics: no TestResult with mainContainerUuid; comparing tree to global storage (parallel classes may false-positive)"
            );
        }
        return storage.getAllTestResultUuids();
    }

    @Override
    public String writeAttachment(String path) {
        try {
            return apiClient.addAttachment(path);
        } catch (ApiException e) {
            LOGGER.error("Can not write attachment: {}", e.getMessage());

            return "";
        }
    }

    void addUuid(String key, UUID uuid) {
        this.testResults.put(key, uuid);
    }


    /**
     * Сравнивает авто-тест с сервера с моделью для обновления
     *
     * @param serverModel модель с сервера
     * @param updateModel модель для обновления
     * @return true, если модели отличаются и нужно выполнить обновление
     */
    private boolean hasAutoTestChanged(AutoTestApiResult serverModel, AutoTestUpdateApiModel updateModel) {
        if (serverModel == null || updateModel == null) {
            return true;
        }
        // Сравниваем основные поля
        if (!Objects.equals(serverModel.getExternalId(), updateModel.getExternalId())) {
            return true;
        }
        if (!Objects.equals(serverModel.getName(), updateModel.getName())) {
            return true;
        }
        if (!Objects.equals(serverModel.getNamespace(), updateModel.getNamespace())) {
            return true;
        }
        if (!Objects.equals(serverModel.getClassname(), updateModel.getClassname())) {
            return true;
        }
        if (!Objects.equals(serverModel.getTitle(), updateModel.getTitle())) {
            return true;
        }
        if (!Objects.equals(serverModel.getDescription(), updateModel.getDescription())) {
            return true;
        }
        // Сравниваем setup методы
        if (!areStepListsEqual(serverModel.getSetup(), updateModel.getSetup())) {
            return true;
        }
        // Сравниваем teardown методы
        if (!areStepListsEqual(serverModel.getTeardown(), updateModel.getTeardown())) {
            return true;
        }
        // Если дошли до этого места, модели идентичны
        return false;
    }

    /**
     * Сравнивает два списка шагов авто-теста
     *
     * @param serverSteps шаги с сервера
     * @param updateSteps шаги для обновления
     * @return true, если списки отличаются
     */
    private boolean areStepListsEqual(List<AutoTestStepApiResult> serverSteps, List<AutoTestStepApiModel> updateSteps) {

        if (serverSteps == null && updateSteps == null) {
            return false; // Если оба null, то они равны
        }
        if (serverSteps == null || updateSteps == null) {
            return true; // Если один null, а другой нет, то они разные
        }

        if (serverSteps.size() != updateSteps.size()) {
            return true;
        }

        for (int i = 0; i < serverSteps.size(); i++) {
            AutoTestStepApiResult serverStep = serverSteps.get(i);
            AutoTestStepApiModel updateStep = updateSteps.get(i);

            if (!Objects.equals(serverStep.getTitle(), updateStep.getTitle())) {
                return true;
            }

            if (!Objects.equals(serverStep.getDescription(), updateStep.getDescription())) {
                return true;
            }

            // Compare nested steps as well.
            // Previously we compared only title/description for the top-level step list, which could
            // lead to skipping PUT when nested structure differs (e.g. missing setup/teardown details).
            if (areStepListsEqual(serverStep.getSteps(), updateStep.getSteps())) {
                return true;
            }

        }

        return false;
    }

}
