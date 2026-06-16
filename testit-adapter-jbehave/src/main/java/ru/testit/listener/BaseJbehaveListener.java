package ru.testit.listener;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

public class BaseJbehaveListener extends NullStoryReporter {
    private static final Set<String> MAIN_UUIDS_PENDING_FINALIZE = ConcurrentHashMap.newKeySet();

    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<Story> executableStory = new InheritableThreadLocal<>();
    private final ThreadLocal<Scenario> executableScenario = new InheritableThreadLocal<>();
    /** Set in {@link #beforeScenario} — {@code afterScenario} may run on another thread. */
    private final ThreadLocal<String> scenarioClassUuid = new ThreadLocal<>();
    private final List<String> exampleUuids = new ArrayList<>();
    private final ConcurrentHashMap<String, String> storyClassUuids = new ConcurrentHashMap<>();
    private final AtomicReference<String> runMainUuid = new AtomicReference<>();
    private boolean adapterLaunchIsStarted = false;

    public BaseJbehaveListener() {
        adapterManager = Adapter.getAdapterManager();
    }

    private void startAdapterLaunch() {
        adapterManager.startTests();
        adapterLaunchIsStarted = true;
    }

    @Override
    public void beforeStoriesSteps(final Stage stage) {
        if (stage == Stage.BEFORE) {
            MAIN_UUIDS_PENDING_FINALIZE.clear();
            runMainUuid.set(null);
            storyClassUuids.clear();
        }
    }

    @Override
    public void afterStoriesSteps(final Stage stage) {
        if (stage == Stage.AFTER) {
            finalizeRunMainContainers();
        }
    }

    private void finalizeRunMainContainers() {
        for (String uuid : new ArrayList<>(MAIN_UUIDS_PENDING_FINALIZE)) {
            adapterManager.stopMainContainer(uuid);
        }
        MAIN_UUIDS_PENDING_FINALIZE.clear();
        runMainUuid.set(null);
        storyClassUuids.clear();
    }

    @Override
    public void beforeStory(final Story story, final boolean givenStory) {
        if (!adapterLaunchIsStarted) {
            startAdapterLaunch();
        }

        if (!givenStory) {
            executableStory.set(story);

            final String mainUuid = runMainUuid.updateAndGet(
                    existing -> existing != null ? existing : UUID.randomUUID().toString());
            if (MAIN_UUIDS_PENDING_FINALIZE.add(mainUuid)) {
                adapterManager.startMainContainer(new MainContainer().setUuid(mainUuid));
            }

            final String classUuid = UUID.randomUUID().toString();
            storyClassUuids.put(story.getPath(), classUuid);
            adapterManager.startClassContainer(mainUuid, new ClassContainer().setUuid(classUuid));
        }
    }

    @Override
    public void afterStory(final boolean givenStory) {
        if (!givenStory) {
            final Story story = executableStory.get();
            if (story != null) {
                final String classUuid = storyClassUuids.remove(story.getPath());
                if (classUuid != null) {
                    adapterManager.stopClassContainer(classUuid);
                }
                if (storyClassUuids.isEmpty()) {
                    finalizeRunMainContainers();
                }
            }
            executableStory.remove();
        }
    }

    @Override
    public void beforeScenario(final Scenario scenario) {
        final String classUuid = storyClassUuids.get(executableStory.get().getPath());
        scenarioClassUuid.set(classUuid);
        executableScenario.set(scenario);

        if (notParameterised(scenario)) {
            ExecutableTest test = executableTest.get();

            if (test.isStarted()) {
                test = refreshContext();
            }

            test.setTestStatus();

            final String uuid = test.getUuid();

            adapterManager.updateClassContainer(classUuid,
                    container -> container.getChildren().add(uuid));

            startTestCase(scenario, uuid, null, runMainUuid.get());
        }
    }

    protected void startTestCase(
            Scenario scenario,
            final String uuid,
            Map<String, String> parameters,
            String mainContainerUuid
    ) {
        final TestResult result = ScenarioParser
                .parseScenario(executableStory.get(), scenario, parameters)
                .setUuid(uuid)
                .setMainContainerUuid(mainContainerUuid);

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(uuid);
    }

    protected boolean notParameterised(final Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() == 0;
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }

    @Override
    public void example(final Map<String, String> tableRow, final int exampleIndex) {
        ExecutableTest test = executableTest.get();

        if (test.isStarted()) {
            test = refreshContext();
        }

        test.setTestStatus();

        final String uuid = test.getUuid();
        final String classUuid = scenarioClassUuid.get();

        adapterManager.updateClassContainer(classUuid,
                container -> container.getChildren().add(uuid));
        exampleUuids.add(uuid);
        startTestCase(
                executableScenario.get(),
                uuid,
                tableRow,
                runMainUuid.get());
    }

    @Override
    public void afterScenario() {
        final ExecutableTest test = executableTest.get();
        final String uuid = test.getUuid();

        if (exampleUuids.isEmpty() && !test.isAfter()) {
            adapterManager.stopTestCase(uuid);
        } else {
            for (String exampleUuid : exampleUuids) {
                adapterManager.stopTestCase(exampleUuid);
            }

            exampleUuids.clear();
        }

        scenarioClassUuid.remove();
        executableTest.remove();
    }

    @Override
    public void beforeStep(final String step) {
        final StepResult stepResult = new StepResult()
                .setTitle(step)
                .setStart(System.currentTimeMillis());

        adapterManager.startStep(
                executableTest.get().getUuid(),
                UUID.randomUUID().toString(),
                stepResult);
    }

    @Override
    public void successful(final String step) {
        final String uuid = executableTest.get().getUuid();

        adapterManager.updateStep(stepResult -> stepResult.setItemStatus(ItemStatus.PASSED));
        adapterManager.stopStep();
        adapterManager.updateTestCase(
                uuid,
                result -> result.setItemStatus(ItemStatus.PASSED));
    }

    @Override
    public void pending(final String step) {
        beforeStep(step);
        adapterManager.updateStep(stepResult -> stepResult.setItemStatus(ItemStatus.SKIPPED));
        adapterManager.stopStep();
    }

    @Override
    public void failed(final String step, final Throwable cause) {
        final String uuid = executableTest.get().getUuid();
        adapterManager.updateStep(stepResult -> stepResult.setItemStatus(ItemStatus.FAILED));
        adapterManager.stopStep();
        adapterManager.updateTestCase(
                uuid,
                result -> {
                    result.setItemStatus(ItemStatus.FAILED);
                    if (nonNull(cause)) {
                        result.setThrowable(cause);
                    }
                }
        );

        adapterManager.stopTestCase(uuid);
        executableTest.get().setAfterStatus();

        if (exampleUuids.contains(uuid)) {
            exampleUuids.remove(uuid);
        }
    }
}
