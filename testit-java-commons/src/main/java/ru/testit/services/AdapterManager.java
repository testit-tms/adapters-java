package ru.testit.services;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.TestRunV2ApiResult;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.Converter;
import ru.testit.clients.ITmsApiClient;
import ru.testit.clients.TmsApiClient;
import ru.testit.listener.AdapterListener;
import ru.testit.listener.ListenerManager;
import ru.testit.listener.ServiceLoaderListener;
import ru.testit.models.*;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AdapterMode;
import ru.testit.syncstorage.ClientWrapper;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.HttpWriter;
import ru.testit.writers.Writer;

/**
 * This class manages data from a test framework.
 */
public class AdapterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AdapterManager.class
    );
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Writer writer;
    private final ITmsApiClient client;
    private final ClientConfiguration clientConfiguration;
    private final AdapterConfig adapterConfig;

    private final ListenerManager listenerManager;
    private final SyncStorageService syncStorageService;

    public AdapterManager(
            ClientConfiguration clientConfiguration,
            AdapterConfig adapterConfig
    ) {
        this(clientConfiguration, adapterConfig, getDefaultListenerManager());
    }

    public AdapterManager(
            ClientConfiguration clientConfiguration,
            AdapterConfig adapterConfig,
            ListenerManager listenerManager
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Client configurations: {}", clientConfiguration);
            LOGGER.debug("Adapter configurations: {}", adapterConfig);
        }

        this.clientConfiguration = clientConfiguration;
        this.adapterConfig = adapterConfig;
        this.storage = Adapter.getResultStorage();
        this.threadContext = new ThreadContext();
        this.client = new TmsApiClient(this.clientConfiguration);
        this.writer = new HttpWriter(
                this.clientConfiguration,
                this.client,
                this.storage
        );
        this.listenerManager = listenerManager;
        this.syncStorageService = new SyncStorageService(
                this.clientConfiguration,
                this.client,
                new ClientWrapper()
        );
    }

    public AdapterManager(
            ClientConfiguration clientConfiguration,
            AdapterConfig adapterConfig,
            ThreadContext threadContext,
            ResultStorage storage,
            Writer writer,
            ITmsApiClient client,
            ListenerManager listenerManager
    ) {
        this.adapterConfig = adapterConfig;
        this.clientConfiguration = clientConfiguration;
        this.threadContext = threadContext;
        this.storage = storage;
        this.writer = writer;
        this.client = client;
        this.listenerManager = listenerManager;
        this.syncStorageService = new SyncStorageService(
                this.clientConfiguration,
                this.client,
                new ClientWrapper()
        );
    }

    public void startTests() {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        LOGGER.debug("Start launch");

        synchronized (this.clientConfiguration) {
            if (
                    Objects.equals(this.clientConfiguration.getTestRunId(), "null")
            ) {
                return;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Test run is exist.");
            }

            try {
                this.updateTestRunName();
            } catch (ApiException e) {
                LOGGER.error(
                        "Can not update the launch: ".concat(e.getMessage())
                );
            }
        }
    }

    private void updateTestRunName() throws ApiException {
        String testRunName = this.clientConfiguration.getTestRunName();

        if (testRunName.isEmpty() || Objects.equals(this.clientConfiguration.getTestRunName(), "null")) {
            return;
        }

        TestRunV2ApiResult testRun = this.client.getTestRun(
                this.clientConfiguration.getTestRunId()
        );

        if (testRun.getName().equals(testRunName)) {
            return;
        }

        testRun.setName(HtmlEscapeUtils.escapeHtmlTags(testRunName));

        this.client.updateTestRun(Converter.buildUpdateEmptyTestRunApiModel(testRun));
    }

    /**
     * Starts main container.
     *
     * @param container the main container.
     */
    public void startMainContainer(final MainContainer container) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start new main container {}", container);
        }
        LOGGER.info("Set in progress worker status");
        setWorkerStatus("in_progress");
    }

    /**
     * Stops main container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopMainContainer(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<MainContainer> found = storage.getTestsContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not stop main container: container with uuid {} not found",
                    uuid
            );
            return;
        }
        final MainContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop main container {}", container);
        }

        writer.writeTests(container);

        LOGGER.info("End of main container, set completed");
        setWorkerStatus("completed");
    }

    /**
     * Starts class container.
     *
     * @param parentUuid the uuid of parent container.
     * @param container  the class container.
     */
    public void startClassContainer(
            final String parentUuid,
            final ClassContainer container
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        storage.getTestsContainer(parentUuid).ifPresent(parent -> {
            storage.updateIfPresent(parentUuid, MainContainer.class, p -> p.getChildren().add(container.getUuid()));
        });
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start new class container {} for parent {}",
                    container,
                    parentUuid
            );
        }
        setWorkerStatus("in_progress");
    }

    /**
     * Stops class container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopClassContainer(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.debug(
                    "Could not stop class container: container with uuid {} not found",
                    uuid
            );
            return;
        }
        final ClassContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop class container {}", container);
        }

        writer.writeClass(container);

        LOGGER.info("End of class container");
    }

    /**
     * Updates class container.
     *
     * @param uuid   the uuid of container.
     * @param update the update function.
     */
    public void updateClassContainer(
            final String uuid,
            final Consumer<ClassContainer> update
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update class container {}", uuid);
        }

        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.debug(
                    "Could not update class container: container with uuid {} not found",
                    uuid
            );
            return;
        }
        final ClassContainer container = found.get();
        update.accept(container);
    }

    /**
     * Starts test case with given uuid.
     *
     * @param uuid the uuid of test case to start.
     */
    public void startTestCase(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        threadContext.clear();
        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not start test case: test case with uuid {} is not scheduled",
                    uuid
            );
            return;
        }
        final TestResult testResult = found.get();

        testResult
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.start(uuid);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start test case {}", testResult);
        }
    }

    /**
     * Schedule given test case.
     *
     * @param result the test case to schedule.
     */
    public void scheduleTestCase(final TestResult result) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        result
                .setItemStage(ItemStage.SCHEDULED)
                .setAutomaticCreationTestCases(
                        adapterConfig.shouldAutomaticCreationTestCases()
                );
        storage.put(result.getUuid(), result);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Schedule test case {}", result);
        }
    }

    /**
     * Updates current test case
     *
     * @param update the update function.
     */
    public void updateTestCase(final Consumer<TestResult> update) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> root = threadContext.getRoot();
        if (!root.isPresent()) {
            LOGGER.error("Could not update test case: no test case running");
            return;
        }

        final String uuid = root.get();
        updateTestCase(uuid, update);
    }

    /**
     * Updates test case by given uuid.
     *
     * @param uuid   the uuid of test case to update.
     * @param update the update function.
     */
    public void updateTestCase(
            final String uuid,
            final Consumer<TestResult> update
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update test case {}", uuid);
        }

        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not update test case: test case with uuid {} not found",
                    uuid
            );
            return;
        }
        final TestResult testResult = found.get();

        update.accept(testResult);
    }

    /**
     * Stops test case by given uuid.
     *
     * @param uuid the uuid of test case to stop.
     */
    public void stopTestCase(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not stop test case: test case with uuid {} not found",
                    uuid
            );
            return;
        }
        final TestResult testResult = found.get();

        listenerManager.beforeTestStop(testResult);

        testResult
                .setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        threadContext.clear();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop test case {}", testResult);
        }

        // Проверяем, если текущий раннер - мастер, и в раннере стоит флаг (no in progress)
        // если да - мы отправляем тест-результат в sync storage с финальным статусом
        // а в test it пишем его как in progress
        if (
                syncStorageService.shouldSendInProgressResult()
        ) {
            // Отправляем тест-результат в SyncStorage
            sendTestResultToSyncStorage(testResult);
            syncStorageService.markInProgressResultSent();
            // Помечаем тест как in progress для Test IT
            markTestAsInProgress(testResult);
            // Первый всегда прольется в реалтайме либо же фича будет работать криво
            writer.writeTestRealtime(testResult);
            return;
        }

        writer.writeTest(testResult);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startPrepareFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start prepare all fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, MainContainer.class, c -> c.getBeforeMethods().add(result));
        });
        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startTearDownFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start tear down all fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, MainContainer.class, c -> c.getAfterMethods().add(result));
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startPrepareFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start prepare fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, ClassContainer.class, c -> c.getBeforeClassMethods().add(result));
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startTearDownFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start tear down fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, ClassContainer.class, c -> c.getAfterClassMethods().add(result));
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startPrepareFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start prepare for each fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, ClassContainer.class, c -> c.getBeforeEachTest().add(result));
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid       the fixture uuid.
     * @param result     the fixture.
     */
    public void startTearDownFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Start tear down for each fixture {} for parent {}",
                    result,
                    parentUuid
            );
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            storage.updateIfPresent(parentUuid, ClassContainer.class, c -> c.getAfterEachTest().add(result));
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new fixture with given uuid.
     *
     * @param uuid   the uuid of fixture.
     * @param result the test fixture.
     */
    private void startFixture(final String uuid, final FixtureResult result) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        storage.put(uuid, result);

        result
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.clear();
        threadContext.start(uuid);
    }

    /**
     * Updates fixture by given uuid.
     *
     * @param uuid   the uuid of fixture.
     * @param update the update function.
     */
    public void updateFixture(
            final String uuid,
            final Consumer<FixtureResult> update
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update fixture {}", uuid);
        }

        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not update test fixture: test fixture with uuid {} not found",
                    uuid
            );
            return;
        }
        final FixtureResult fixture = found.get();

        update.accept(fixture);
    }

    /**
     * Stops fixture by given uuid.
     *
     * @param uuid the uuid of fixture.
     */
    public void stopFixture(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not stop test fixture: test fixture with uuid {} not found",
                    uuid
            );
            return;
        }
        final FixtureResult fixture = found.get();

        fixture
                .setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        storage.remove(uuid);
        threadContext.clear();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop fixture {}", fixture);
        }
    }

    /**
     * Start a new step as child step of current running test case or step.
     *
     * @param uuid   the uuid of step.
     * @param result the step.
     */
    public void startStep(final String uuid, final StepResult result) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.debug(
                    "Could not start step {}: no test case running",
                    result
            );
            return;
        }
        final String parentUuid = current.get();
        startStep(parentUuid, uuid, result);
    }

    /**
     * Start a new step as child of specified parent.
     *
     * @param parentUuid the uuid of parent test case or step.
     * @param uuid       the uuid of step.
     * @param result     the step.
     */
    public void startStep(
            final String parentUuid,
            final String uuid,
            final StepResult result
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        result
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.start(uuid);

        storage.put(uuid, result);
        storage.get(parentUuid, ResultWithSteps.class).ifPresent(parentStep -> {
            storage.updateIfPresent(parentUuid, ResultWithSteps.class, p -> p.getSteps().add(result));
        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start step {} for parent {}", result, parentUuid);
        }
    }

    /**
     * Updates current step.
     *
     * @param update the update function.
     */
    public void updateStep(final Consumer<StepResult> update) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.debug("Could not update step: no step running");
            return;
        }
        final String uuid = current.get();
        updateStep(uuid, update);
    }

    /**
     * Updates step by specified uuid.
     *
     * @param uuid   the uuid of step.
     * @param update the update function.
     */
    public void updateStep(
            final String uuid,
            final Consumer<StepResult> update
    ) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update step {}", uuid);
        }

        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not update step: step with uuid {} not found",
                    uuid
            );
            return;
        }

        final StepResult step = found.get();

        update.accept(step);
    }

    /**
     * Stops current running step.
     */
    public void stopStep() {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final String root = threadContext.getRoot().orElse(null);
        final Optional<String> current = threadContext
                .getCurrent()
                .filter(uuid -> !Objects.equals(uuid, root));
        if (!current.isPresent()) {
            LOGGER.debug("Could not stop step: no step running");
            return;
        }
        final String uuid = current.get();
        stopStep(uuid);
    }

    /**
     * Stops step by given uuid.
     *
     * @param uuid the uuid of step to stop.
     */
    public void stopStep(final String uuid) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            LOGGER.error(
                    "Could not stop step: step with uuid {} not found",
                    uuid
            );
            return;
        }

        final StepResult step = found.get();

        step.setItemStage(ItemStage.FINISHED);
        step.setStop(System.currentTimeMillis());

        storage.remove(uuid);
        threadContext.stop();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop step {}", step);
        }
    }

    public void addAttachments(List<String> attachments) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        List<String> uuids = new ArrayList<>();
        for (final String attachment : attachments) {
            String attachmentsId = writer.writeAttachment(attachment);
            if (attachmentsId.isEmpty()) {
                return;
            }
            uuids.add(attachmentsId);
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.error("Could not add attachment: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithAttachments.class).ifPresent(
                result -> {
                    storage.updateIfPresent(current.get(), ResultWithAttachments.class, r -> r.getAttachments().addAll(uuids));
                }
        );
    }

    public void addParameters(Map<String, String> parameters) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.error("Could not add parameter: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithParameters.class).ifPresent(
                result -> {
                    storage.updateIfPresent(
                            current.get(), ResultWithParameters.class, r -> r.getParameters().putAll(parameters));
                }
        );
    }

    public void addTitle(String title) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.error("Could not set title: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithTitle.class).ifPresent(
                result -> {
                    storage.updateIfPresent(current.get(), ResultWithTitle.class, r -> r.setTitle(title));
                }
        );
    }

    public void addDescription(String description) {
        if (!adapterConfig.shouldEnableTmsIntegration()) {
            return;
        }

        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.error("Could not set description: no test is running");
            return;
        }

        storage.get(current.get(), ResultWithDescription.class).ifPresent(
                result -> {
                    storage.updateIfPresent(
                            current.get(), ResultWithDescription.class, r -> r.setDescription(description));
                }
        );
    }

    public boolean isFilteredMode() {
        return adapterConfig.getMode() == AdapterMode.USE_FILTER;
    }

    public List<String> getTestFromTestRun() {
        if (adapterConfig.shouldEnableTmsIntegration()) {
            try {
                List<String> externalIds =
                        client.getAutotestExternalIdsFromTestRun();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "List of tests from test run: {}",
                            externalIds
                    );
                }

                return externalIds;
            } catch (ApiException e) {
                LOGGER.error("Could not get tests from test run", e);
            }
        }

        return new ArrayList<>();
    }

    public Optional<String> getCurrentTestCaseOrStep() {
        return threadContext.getCurrent();
    }

    private static ListenerManager getDefaultListenerManager() {
        final ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();
        return new ListenerManager(
                ServiceLoaderListener.load(AdapterListener.class, classLoader)
        );
    }

    private void sendTestResultToSyncStorage(TestResult testResult) {
        syncStorageService.sendTestResultToSyncStorage(testResult);
    }

    /**
     * Mark test as "in progress" for Test IT
     */
    private void markTestAsInProgress(TestResult testResult) {
        // Меняем статус теста на InProgress перед отправкой в Test IT
        testResult.setItemStatus(ItemStatus.INPROGRESS);
    }

    public void setWorkerStatus(String status) {
        syncStorageService.setWorkerStatus(status);
    }

    /**
     * Sets the status of a worker by its PID
     *
     * @param pid    the PID of the worker
     * @param status the status to set
     */
    public void setWorkerStatus(String pid, String status) {
        syncStorageService.setWorkerStatus(pid, status);
    }
}
