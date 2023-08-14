package ru.testit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.TestRunState;
import ru.testit.client.model.TestRunV2GetModel;
import ru.testit.clients.ApiClient;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.TmsApiClient;
import ru.testit.listener.AdapterListener;
import ru.testit.listener.ListenerManager;
import ru.testit.listener.ServiceLoaderListener;
import ru.testit.models.*;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AdapterMode;
import ru.testit.writers.HttpWriter;
import ru.testit.writers.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class manages data from a test framework.
 */
public class AdapterManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterManager.class);
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Writer writer;
    private final ApiClient client;
    private final ClientConfiguration clientConfiguration;
    private final AdapterConfig adapterConfig;

    private final ListenerManager listenerManager;

    public AdapterManager(ClientConfiguration clientConfiguration, AdapterConfig adapterConfig) {
        this(clientConfiguration, adapterConfig, getDefaultListenerManager());
    }

    public AdapterManager(ClientConfiguration clientConfiguration, AdapterConfig adapterConfig, ListenerManager listenerManager) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Client configurations: {}", clientConfiguration);
            LOGGER.debug("Adapter configurations: {}", adapterConfig);
        }

        this.clientConfiguration = clientConfiguration;
        this.adapterConfig = adapterConfig;
        validateAdapterConfig();
        this.storage = Adapter.getResultStorage();
        this.threadContext = new ThreadContext();
        this.client = new TmsApiClient(this.clientConfiguration);
        this.writer = new HttpWriter(this.clientConfiguration, this.client, this.storage);
        this.listenerManager = listenerManager;
    }

    public AdapterManager(
            ClientConfiguration clientConfiguration,
            AdapterConfig adapterConfig,
            ThreadContext threadContext,
            ResultStorage storage,
            Writer writer,
            ApiClient client,
            ListenerManager listenerManager
    ) {
        this.adapterConfig = adapterConfig;
        this.clientConfiguration = clientConfiguration;
        this.threadContext = threadContext;
        this.storage = storage;
        this.writer = writer;
        this.client = client;
        this.listenerManager = listenerManager;
    }

    public void startTests() {
        LOGGER.debug("Start launch");

        synchronized (this.clientConfiguration) {
            if (!Objects.equals(this.clientConfiguration.getTestRunId(), "null")) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Test run is exist.");
                }
                return;
            }

            try {
                TestRunV2GetModel response = this.client.createTestRun();
                this.clientConfiguration.setTestRunId(response.getId().toString());

            } catch (ApiException e) {
                LOGGER.error("Can not start the launch: ".concat(e.getMessage()));
            }
        }
    }

    public void stopTests() {
        LOGGER.debug("Stop launch");

        try {
            TestRunV2GetModel testRun = this.client.getTestRun(this.clientConfiguration.getTestRunId());

            if (testRun.getStateName() != TestRunState.COMPLETED) {
                this.client.completeTestRun(this.clientConfiguration.getTestRunId());
            }
        } catch (ApiException e) {
            if (e.getResponseBody().contains("the StateName is already Completed")) {
                return;
            }
            LOGGER.error("Can not finish the launch: ".concat(e.getMessage()));
        }
    }

    /**
     * Starts main container.
     *
     * @param container the main container.
     */
    public void startMainContainer(final MainContainer container) {
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start new main container {}", container);
        }
    }

    /**
     * Stops main container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopMainContainer(final String uuid) {
        final Optional<MainContainer> found = storage.getTestsContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not stop main container: container with uuid {} not found", uuid);
            return;
        }
        final MainContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop main container {}", container);
        }

        writer.writeTests(container);
    }

    /**
     * Starts class container.
     *
     * @param parentUuid the uuid of parent container.
     * @param container  the class container.
     */
    public void startClassContainer(final String parentUuid, final ClassContainer container) {
        storage.getTestsContainer(parentUuid).ifPresent(parent -> {
            synchronized (storage) {
                parent.getChildren().add(container.getUuid());
            }
        });
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start new class container {} for parent {}", container, parentUuid);
        }
    }

    /**
     * Stops class container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopClassContainer(final String uuid) {
        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.debug("Could not stop class container: container with uuid {} not found", uuid);
            return;
        }
        final ClassContainer container = found.get();
        container.setStop(System.currentTimeMillis());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop class container {}", container);
        }

        writer.writeClass(container);
    }

    /**
     * Updates class container.
     *
     * @param uuid   the uuid of container.
     * @param update the update function.
     */
    public void updateClassContainer(final String uuid, final Consumer<ClassContainer> update) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update class container {}", uuid);
        }

        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.debug("Could not update class container: container with uuid {} not found", uuid);
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
        threadContext.clear();
        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not start test case: test case with uuid {} is not scheduled", uuid);
            return;
        }
        final TestResult testResult = found.get();

        testResult.setItemStage(ItemStage.RUNNING)
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
        result.setItemStage(ItemStage.SCHEDULED)
                .setAutomaticCreationTestCases(adapterConfig.shouldAutomaticCreationTestCases());
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
    public void updateTestCase(final String uuid, final Consumer<TestResult> update) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update test case {}", uuid);
        }

        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not update test case: test case with uuid {} not found", uuid);
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
        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not stop test case: test case with uuid {} not found", uuid);
            return;
        }
        final TestResult testResult = found.get();

        listenerManager.beforeTestStop(testResult);

        testResult.setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        threadContext.clear();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Stop test case {}", testResult);
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
    public void startPrepareFixtureAll(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start prepare all fixture {} for parent {}", result, parentUuid);
        }

        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeMethods().add(result);
            }
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
    public void startTearDownFixtureAll(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start tear down all fixture {} for parent {}", result, parentUuid);
        }

        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterMethods().add(result);
            }
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
    public void startPrepareFixture(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start prepare fixture {} for parent {}", result, parentUuid);
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeClassMethods().add(result);
            }
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
    public void startTearDownFixture(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start tear down fixture {} for parent {}", result, parentUuid);
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterClassMethods().add(result);
            }
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
    public void startPrepareFixtureEachTest(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start prepare for each fixture {} for parent {}", result, parentUuid);
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeEachTest().add(result);
            }
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
    public void startTearDownFixtureEachTest(final String parentUuid, final String uuid, final FixtureResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Start tear down for each fixture {} for parent {}", result, parentUuid);
        }

        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterEachTest().add(result);
            }
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
        storage.put(uuid, result);

        result.setItemStage(ItemStage.RUNNING).
                setStart(System.currentTimeMillis());

        threadContext.clear();
        threadContext.start(uuid);
    }

    /**
     * Updates fixture by given uuid.
     *
     * @param uuid   the uuid of fixture.
     * @param update the update function.
     */
    public void updateFixture(final String uuid, final Consumer<FixtureResult> update) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update fixture {}", uuid);
        }

        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not update test fixture: test fixture with uuid {} not found", uuid);
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
        final Optional<FixtureResult> found = storage.getFixture(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not stop test fixture: test fixture with uuid {} not found", uuid);
            return;
        }
        final FixtureResult fixture = found.get();

        fixture.setItemStage(ItemStage.FINISHED)
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
        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.debug("Could not start step {}: no test case running", result);
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
    public void startStep(final String parentUuid, final String uuid, final StepResult result) {
        result.setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.start(uuid);

        storage.put(uuid, result);
        storage.get(parentUuid, ResultWithSteps.class).ifPresent(parentStep -> {
            synchronized (storage) {
                parentStep.getSteps().add(result);
            }
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
    public void updateStep(final String uuid, final Consumer<StepResult> update) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update step {}", uuid);
        }

        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not update step: step with uuid {} not found", uuid);
            return;
        }

        final StepResult step = found.get();

        update.accept(step);
    }

    /**
     * Stops current running step.
     */
    public void stopStep() {
        final String root = threadContext.getRoot().orElse(null);
        final Optional<String> current = threadContext.getCurrent()
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
        final Optional<StepResult> found = storage.getStep(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not stop step: step with uuid {} not found", uuid);
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
        List<String> uuids = new ArrayList<>();
        for (final String attachment : attachments) {
            String attachmentsId = writer.writeAttachment(attachment);
            if (attachmentsId.isEmpty()){
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
                    synchronized (storage) {
                        result.getAttachments().addAll(uuids);
                    }
                }
        );
    }

    public boolean isFilteredMode() {
        return adapterConfig.getMode() == AdapterMode.USE_FILTER;
    }

    public List<String> getTestFromTestRun() {
        try {
            List<String> testsForRun = client.getTestFromTestRun(clientConfiguration.getTestRunId(), clientConfiguration.getConfigurationId());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("List of tests from test run: {}", testsForRun);
            }

            return testsForRun;
        } catch (ApiException e) {
            LOGGER.error("Could not get tests from test run", e);
        }
        return new ArrayList<>();
    }

    public Optional<String> getCurrentTestCaseOrStep() {
        return threadContext.getCurrent();
    }

    private void validateAdapterConfig() {
        switch (adapterConfig.getMode()) {
            case USE_FILTER:
            case RUN_ALL_TESTS:
                if (clientConfiguration.getTestRunId().equals("null") || clientConfiguration.getConfigurationId().equals("null")) {
                    String error = "Adapter works in mode 0. Config should contains test run id and configuration id.";
                    LOGGER.error(error);
                    throw new RuntimeException(error);
                }
                break;
            case NEW_TEST_RUN:
                if (clientConfiguration.getProjectId().equals("null")
                        || clientConfiguration.getConfigurationId().equals("null")
                        || !clientConfiguration.getTestRunId().equals("null")) {
                    String error = "Adapter works in mode 2. Config should contains project id and configuration id. Also doesn't contains test run id.";
                    LOGGER.error(error);
                    throw new RuntimeException(error);
                }
                break;
            default:
                String error = String.format("Unknown mode: %s", adapterConfig.getMode());
                LOGGER.error(error);
                throw new RuntimeException(error);
        }
    }

    private static ListenerManager getDefaultListenerManager() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new ListenerManager(ServiceLoaderListener.load(AdapterListener.class, classLoader));
    }
}
