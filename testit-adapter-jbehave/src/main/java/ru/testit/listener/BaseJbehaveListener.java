package ru.testit.listener;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;

public class BaseJbehaveListener extends NullStoryReporter {
    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<Story> executableStory = new InheritableThreadLocal<>();
    private final ThreadLocal<Scenario> executableScenario = new InheritableThreadLocal<>();
    private final List<String> exampleUuids = new ArrayList<>();
    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private boolean adapterLaunchIsStarted = false;

    public BaseJbehaveListener() {
        adapterManager = Adapter.getAdapterManager();
    }

    private void startAdapterLaunch() {
        adapterManager.startTests();

        adapterLaunchIsStarted = true;
    }

    @Override
    public void beforeStory(final Story story, final boolean givenStory) {
        if (!adapterLaunchIsStarted) {
            startAdapterLaunch();
        }

        if (!givenStory) {
            executableStory.set(story);
        }
    }

    @Override
    public void afterStory(final boolean givenStory) {
        if (!givenStory) {
            executableStory.remove();
        }
    }

    @Override
    public void beforeScenario(final Scenario scenario) {
        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());
        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startMainContainer(mainContainer);
        adapterManager.startClassContainer(launcherUUID.get(), classContainer);

        executableScenario.set(scenario);

        if (notParameterised(scenario)) {
            ExecutableTest test = executableTest.get();

            if (test.isStarted()) {
                test = refreshContext();
            }

            test.setTestStatus();

            final String uuid = test.getUuid();

            adapterManager.updateClassContainer(classUUID.get(),
                    container -> container.getChildren().add(uuid));

            startTestCase(scenario, uuid, null);
        }
    }

    protected void startTestCase(Scenario scenario, final String uuid, Map<String, String> parameters) {
        final TestResult result = ScenarioParser
                .parseScenario(executableStory.get(), scenario, parameters)
                .setUuid(uuid);

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

        adapterManager.updateClassContainer(classUUID.get(),
                container -> container.getChildren().add(uuid));
        exampleUuids.add(uuid);
        startTestCase(
                executableScenario.get(),
                uuid,
                tableRow);
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

        adapterManager.stopClassContainer(classUUID.get());
        adapterManager.stopMainContainer(launcherUUID.get());
        executableTest.remove();
    }

    @Override
    public void beforeStep(final String step) {
        final StepResult stepResult = new StepResult()
                .setName(step)
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
