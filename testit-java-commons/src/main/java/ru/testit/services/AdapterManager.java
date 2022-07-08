package ru.testit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.*;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.writers.HttpWriter;
import ru.testit.writers.Writer;

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

    public AdapterManager() {
        storage = Adapter.getResultStorage();
        threadContext = new ThreadContext();
        writer = new HttpWriter();
    }

    public void startTests(){
        writer.startLaunch();
    }

    public void stopTests(){
        writer.finishLaunch();
    }

    /**
     * Starts main container.
     *
     * @param container the main container.
     */
    public void startMainContainer(final MainContainer container) {
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);
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
        writer.writeTests(container);
    }

    /**
     * Starts class container.
     *
     * @param parentUuid the uuid of parent container.
     * @param container     the class container.
     */
    public void startClassContainer(final String parentUuid, final ClassContainer container) {
        storage.getTestsContainer(parentUuid).ifPresent(parent -> {
            synchronized (storage) {
                parent.getChildren().add(container.getUuid());
            }
        });
        container.setStart(System.currentTimeMillis());
        storage.put(container.getUuid(), container);
    }

    /**
     * Stops class container by given uuid.
     *
     * @param uuid the uuid of container.
     */
    public void stopClassContainer(final String uuid) {
        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not stop class container: container with uuid {} not found", uuid);
            return;
        }
        final ClassContainer container = found.get();
        container.setStop(System.currentTimeMillis());
        writer.writeClass(container);
    }

    /**
     * Updates class container.
     *
     * @param uuid   the uuid of container.
     * @param update the update function.
     */
    public void updateClassContainer(final String uuid, final Consumer<ClassContainer> update) {
        final Optional<ClassContainer> found = storage.getClassContainer(uuid);
        if (!found.isPresent()) {
            LOGGER.error("Could not update class container: container with uuid {} not found", uuid);
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
    }

    /**
     * Schedule given test case.
     *
     * @param result the test case to schedule.
     */
    public void scheduleTestCase(final TestResult result) {
        result.setItemStage(ItemStage.SCHEDULED);
        storage.put(result.getUuid(), result);
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

        testResult.setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        threadContext.clear();
        writer.writeTest(testResult);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startPrepareFixtureAll(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeMethods().add(uuid);
            }
        });
        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startTearDownFixtureAll(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getTestsContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterMethods().add(uuid);
            }
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startPrepareFixture(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeClassMethods().add(uuid);
            }
        });
        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startTearDownFixture(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterClassMethods().add(uuid);
            }
        });

        startFixture(uuid, result);
    }

    /**
     * Start a new prepare fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startPrepareFixtureEachTest(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getBeforeEachTest().add(uuid);
            }
        });
        startFixture(uuid, result);
    }

    /**
     * Start a new tear down fixture with given parent.
     *
     * @param parentUuid the uuid of parent container.
     * @param uuid          the fixture uuid.
     * @param result        the fixture.
     */
    public void startTearDownFixtureEachTest(final String parentUuid, final String uuid, final FixtureResult result) {
        storage.getClassContainer(parentUuid).ifPresent(container -> {
            synchronized (storage) {
                container.getAfterEachTest().add(uuid);
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

        threadContext.clear();
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
            LOGGER.error("Could not start step: no test case running");
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
                parentStep.getSteps().add(uuid);
            }
        });
    }

    /**
     * Updates current step.
     *
     * @param update the update function.
     */
    public void updateStep(final Consumer<StepResult> update) {
        final Optional<String> current = threadContext.getCurrent();
        if (!current.isPresent()) {
            LOGGER.error("Could not update step: no step running");
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
            LOGGER.error("Could not stop step: no step running");
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

        threadContext.stop();
    }
}
