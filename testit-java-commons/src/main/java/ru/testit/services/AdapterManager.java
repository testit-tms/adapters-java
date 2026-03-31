package ru.testit.services;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.ITmsApiClient;
import ru.testit.listener.AdapterListener;
import ru.testit.listener.ListenerManager;
import ru.testit.listener.ServiceLoaderListener;
import ru.testit.models.*;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AdapterMode;
import ru.testit.services.core.AdapterCoreFacade;
import ru.testit.services.core.AdapterSupportFacade;
import ru.testit.writers.Writer;

/**
 * This class manages data from a test framework.
 */
public class AdapterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterManager.class);
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final Writer writer;
    private final ITmsApiClient client;
    private final ClientConfiguration clientConfiguration;
    private final AdapterConfig adapterConfig;

    private final AdapterCoreFacade coreFacade;
    private final AdapterSupportFacade supportFacade;

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
        this(
                clientConfiguration,
                adapterConfig,
                AdapterManagerDefaultsFactory.create(clientConfiguration),
                listenerManager
        );
    }

    private AdapterManager(
            ClientConfiguration clientConfiguration,
            AdapterConfig adapterConfig,
            AdapterManagerDefaultsFactory.ConstructorDefaults defaults,
            ListenerManager listenerManager
    ) {
        this(
                clientConfiguration,
                adapterConfig,
                defaults.threadContext,
                defaults.storage,
                defaults.writer,
                defaults.client,
                listenerManager
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
        AdapterManagerWiringFactory.HelperWiring wiring = AdapterManagerWiringFactory.create(
                this.adapterConfig,
                this.clientConfiguration,
                this.threadContext,
                this.storage,
                this.writer,
                this.client,
                listenerManager,
                LOGGER
        );
        this.coreFacade = wiring.coreFacade;
        this.supportFacade = wiring.supportFacade;
    }

    public void startTests() {
        supportFacade.startTests();
    }

    /**
     * Starts main container.
     *
     * @param container the main container.
     */
    public void startMainContainer(final MainContainer container) {
        coreFacade.startMainContainer(container);
    }

    /**
     * Stops main container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopMainContainer(final String uuid) {
        coreFacade.stopMainContainer(uuid);
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
        coreFacade.startClassContainer(parentUuid, container);
    }

    /**
     * Stops class container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopClassContainer(final String uuid) {
        coreFacade.stopClassContainer(uuid);
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
        coreFacade.updateClassContainer(uuid, update);
    }

    /**
     * Starts test case with given uuid.
     *
     * @param uuid the uuid of test case to start.
     */
    public void startTestCase(final String uuid) {
        coreFacade.startTestCase(uuid);
    }

    /**
     * Schedule given test case.
     *
     * @param result the test case to schedule.
     */
    public void scheduleTestCase(final TestResult result) {
        coreFacade.scheduleTestCase(result);
    }

    /**
     * Updates current test case
     *
     * @param update the update function.
     */
    public void updateTestCase(final Consumer<TestResult> update) {
        coreFacade.updateTestCase(update);
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
        coreFacade.updateTestCase(uuid, update);
    }

    /**
     * Stops test case by given uuid.
     *
     * @param uuid the uuid of test case to stop.
     */
    public void stopTestCase(final String uuid) {
        coreFacade.stopTestCase(uuid);
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
        coreFacade.startPrepareFixtureAll(parentUuid, uuid, result);
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
        coreFacade.startTearDownFixtureAll(parentUuid, uuid, result);
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
        coreFacade.startPrepareFixture(parentUuid, uuid, result);
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
        coreFacade.startTearDownFixture(parentUuid, uuid, result);
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
        coreFacade.startPrepareFixtureEachTest(parentUuid, uuid, result);
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
        coreFacade.startTearDownFixtureEachTest(parentUuid, uuid, result);
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
        coreFacade.updateFixture(uuid, update);
    }

    /**
     * Stops fixture by given uuid.
     *
     * @param uuid the uuid of fixture.
     */
    public void stopFixture(final String uuid) {
        coreFacade.stopFixture(uuid);
    }

    /**
     * Start a new step as child step of current running test case or step.
     *
     * @param uuid   the uuid of step.
     * @param result the step.
     */
    public void startStep(final String uuid, final StepResult result) {
        coreFacade.startStep(uuid, result);
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
        coreFacade.startStep(parentUuid, uuid, result);
    }

    /**
     * Updates current step.
     *
     * @param update the update function.
     */
    public void updateStep(final Consumer<StepResult> update) {
        coreFacade.updateStep(update);
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
        coreFacade.updateStep(uuid, update);
    }

    /**
     * Stops current running step.
     */
    public void stopStep() {
        coreFacade.stopStep();
    }

    /**
     * Stops step by given uuid.
     *
     * @param uuid the uuid of step to stop.
     */
    public void stopStep(final String uuid) {
        coreFacade.stopStep(uuid);
    }

    public void addAttachments(List<String> attachments) {
        supportFacade.addAttachments(attachments);
    }

    public void addParameters(Map<String, String> parameters) {
        supportFacade.addParameters(parameters);
    }

    public void addTitle(String title) {
        supportFacade.addTitle(title);
    }

    public void addDescription(String description) {
        supportFacade.addDescription(description);
    }

    public boolean isFilteredMode() {
        return adapterConfig.getMode() == AdapterMode.USE_FILTER;
    }

    public List<String> getTestFromTestRun() {
        return supportFacade.getTestFromTestRun();
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

    public void setWorkerStatus(String status) {
        supportFacade.setWorkerStatus(status);
    }

    /**
     * Sets the status of a worker by its PID
     *
     * @param pid    the PID of the worker
     * @param status the status to set
     */
    public void setWorkerStatus(String pid, String status) {
        supportFacade.setWorkerStatus(pid, status);
    }

}
