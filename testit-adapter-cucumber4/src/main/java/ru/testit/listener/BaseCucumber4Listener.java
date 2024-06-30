package ru.testit.listener;

import cucumber.api.*;
import cucumber.api.event.*;
import gherkin.ast.*;
import gherkin.pickles.PickleTag;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BaseCucumber4Listener implements ConcurrentEventListener {
    private final AdapterManager adapterManager;

    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private final ConcurrentHashMap<String, String> scenarioUuids = new ConcurrentHashMap<>();
    private final ScenarioParser scenarioParser = new ScenarioParser();

    private final ThreadLocal<Feature> currentFeature = new InheritableThreadLocal<>();
    private final ThreadLocal<String> currentFeatureFile = new InheritableThreadLocal<>();
    private final ThreadLocal<TestCase> currentTestCase = new InheritableThreadLocal<>();
    private final ThreadLocal<Boolean> forbidTestCaseStatusChange = new InheritableThreadLocal<>();

    private final EventHandler<TestRunStarted> testRunStartedEventHandler = this::handleTestRunStartedHandler;
    private final EventHandler<TestSourceRead> featureStartedEventHandler = this::handleFeatureStartedHandler;
    private final EventHandler<TestCaseStarted> caseStartedEventHandler = this::testStarted;
    private final EventHandler<TestCaseFinished> caseFinishedEventHandler = this::testFinished;
    private final EventHandler<TestStepStarted> stepStartedEventHandler = this::stepStarted;
    private final EventHandler<TestStepFinished> stepFinishedEventHandler = this::stepFinished;
    private final EventHandler<WriteEvent> writeEventHandler = this::handleWriteEvent;
    private final EventHandler<EmbedEvent> embedEventHandler = this::handleEmbedEvent;

    private static final String CUCUMBER_WORKING_DIR = Paths.get("").toUri().toString();

    @SuppressWarnings("unused")
    public BaseCucumber4Listener() {
        this(Adapter.getAdapterManager());
    }

    public BaseCucumber4Listener(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, featureStartedEventHandler);
        publisher.registerHandlerFor(TestRunStarted.class, testRunStartedEventHandler);

        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedEventHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedEventHandler);

        publisher.registerHandlerFor(TestStepStarted.class, stepStartedEventHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedEventHandler);

        publisher.registerHandlerFor(WriteEvent.class, writeEventHandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventHandler);
    }

    private void handleTestRunStartedHandler(TestRunStarted event) {
        adapterManager.startTests();
    }

    private void handleFeatureStartedHandler(TestSourceRead event) {
        scenarioParser.addScenarioEvent(event.uri, event);
    }

    private void testStarted(final TestCaseStarted event) {
        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);

        currentFeatureFile.set(event.testCase.getUri());
        currentFeature.set(scenarioParser.getFeature(currentFeatureFile.get()));
        currentTestCase.set(event.getTestCase());
        forbidTestCaseStatusChange.set(false);

        final ScenarioDefinition scenarioDefinition =
                scenarioParser.getScenarioDefinition(currentFeatureFile.get(), currentTestCase.get().getLine());

        Map<String, String> parameters = new HashMap<>();

        if (scenarioDefinition instanceof ScenarioOutline) {
            parameters =
                    getParameters((ScenarioOutline) scenarioDefinition, currentTestCase.get());
        }

        final Deque<PickleTag> tags = new LinkedList<>(currentTestCase.get().getTags());

        final Feature feature = currentFeature.get();
        final TagParser tagParser = new TagParser(feature, currentTestCase.get(), tags, parameters);

        final String featureName = feature.getName();
        final String uuid = getTestCaseUuid(currentTestCase.get());

        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setExternalId(tagParser.getExternalId())
                .setName(tagParser.getDisplayName())
                .setTitle(tagParser.getTitle())
                .setDescription(tagParser.getDescription())
                .setWorkItemIds(tagParser.getWorkItemIds())
                .setClassName(featureName)
                .setLabels(tagParser.getScenarioLabels())
                .setLinkItems(tagParser.getScenarioLinks())
                .setParameters(parameters);

        final String description = Stream.of(feature.getDescription(), scenarioDefinition.getDescription())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        if (!description.isEmpty()) {
            result.setDescription(description);
        }

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(getTestCaseUuid(currentTestCase.get()));
        adapterManager.updateClassContainer(classUUID.get(),
                container -> container.getChildren().add(uuid));
    }

    private Map<String, String> getParameters(
            final ScenarioOutline scenarioOutline, final TestCase localCurrentTestCase
    ) {
        final Optional<Examples> examplesBlock =
                scenarioOutline.getExamples().stream()
                        .filter(example -> example.getTableBody().stream()
                                .anyMatch(row -> row.getLocation().getLine() == localCurrentTestCase.getLine())
                        ).findFirst();

        if (examplesBlock.isPresent()) {
            final TableRow row = examplesBlock.get().getTableBody().stream()
                    .filter(example -> example.getLocation().getLine() == localCurrentTestCase.getLine())
                    .findFirst().get();
            final Map<String, String> parameters = new HashMap<>();

            IntStream.range(0, examplesBlock.get().getTableHeader().getCells().size()).forEach(index -> {
                final String name = examplesBlock.get().getTableHeader().getCells().get(index).getValue();
                final String value = row.getCells().get(index).getValue();
                parameters.put(name, value);
            });

            return parameters;
        } else {
            return Collections.emptyMap();
        }
    }

    private void testFinished(final TestCaseFinished event) {
        final String uuid = getTestCaseUuid(event.getTestCase());
        Throwable throwable = event.result.getError();
        if (throwable != null) {
            adapterManager.updateTestCase(
                    uuid,
                    testResult -> testResult.setThrowable(throwable)
            );
        }
        adapterManager.stopTestCase(uuid);
        adapterManager.stopClassContainer(classUUID.get());
        adapterManager.stopMainContainer(launcherUUID.get());
    }

    private void stepStarted(final TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            final PickleStepTestStep pickleStep = (PickleStepTestStep) event.testStep;
            final String stepKeyword = Optional.ofNullable(
                    scenarioParser.getAction(currentFeatureFile.get(), pickleStep.getStepLine())
            ).orElse("UNDEFINED");

            final StepResult stepResult = new StepResult()
                    .setName(String.format("%s %s", stepKeyword, pickleStep.getPickleStep().getText()))
                    .setStart(System.currentTimeMillis());

            adapterManager.startStep(getTestCaseUuid(currentTestCase.get()), getStepUuid(pickleStep), stepResult);
        } else if (event.testStep instanceof HookTestStep) {
            initHook((HookTestStep) event.testStep);
        }
    }

    private void stepFinished(final TestStepFinished event) {
        if (event.testStep instanceof HookTestStep) {
            handleHookStep(event);
        } else {
            handlePickleStep(event);
        }
    }

    private void handleHookStep(final TestStepFinished event) {
        final HookTestStep hookStep = (HookTestStep) event.testStep;
        final String uuid = getHookStepUuid(hookStep);
        final FixtureResult fixtureResult = new FixtureResult()
                .setItemStatus(convertStatus(event.result));

        if (!ItemStatus.PASSED.equals(fixtureResult.getItemStatus())) {
            final TestResult testResult = new TestResult().setItemStatus(convertStatus(event.result));
            Throwable throwable = event.result.getError();
            if (throwable != null) {
                testResult.setThrowable(throwable);
            }

            final String errorMessage = event.result.getError() == null ? hookStep.getHookType()
                    .name() + " is failed." : hookStep.getHookType()
                    .name() + " is failed: " + event.result.getError().getLocalizedMessage();

            Adapter.addMessage(errorMessage);

            if (hookStep.getHookType() == HookType.Before) {
                testResult.setItemStatus(ItemStatus.SKIPPED);
                updateTestCaseStatus(testResult.getItemStatus());
                forbidTestCaseStatusChange.set(true);
            }

            fixtureResult.setItemStatus(testResult.getItemStatus());
        }

        adapterManager.updateFixture(uuid, result -> result.setItemStatus(fixtureResult.getItemStatus()));
        adapterManager.stopFixture(uuid);
    }

    private void handlePickleStep(final TestStepFinished event) {
        final ItemStatus stepStatus = convertStatus(event.result);

        if (event.result.getStatus() == Result.Type.UNDEFINED) {
            updateTestCaseStatus(ItemStatus.PASSED);

            adapterManager.updateTestCase(getTestCaseUuid(currentTestCase.get()), scenarioResult ->
                    scenarioResult
                            .setThrowable(new IllegalStateException("Undefined Step. Please add step definition")));
        } else {
            updateTestCaseStatus(stepStatus);
        }

        if (!ItemStatus.PASSED.equals(stepStatus) && stepStatus != null) {
            forbidTestCaseStatusChange.set(true);
        }

        adapterManager.updateStep(getStepUuid((PickleStepTestStep) event.testStep),
                stepResult -> stepResult.setItemStatus(stepStatus));
        adapterManager.stopStep(getStepUuid((PickleStepTestStep) event.testStep));
    }

    private void handleEmbedEvent(final EmbedEvent event) {
        Adapter.addAttachments("Screenshot", new String(event.data));
    }

    private void initHook(final HookTestStep hook) {
        final FixtureResult hookResult = new FixtureResult()
                .setName(hook.getCodeLocation())
                .setStart(System.currentTimeMillis());

        if (hook.getHookType() == HookType.Before) {
            adapterManager.startPrepareFixture(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        } else {
            adapterManager.startTearDownFixture(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        }
    }

    private void handleWriteEvent(final WriteEvent event) {
        Adapter.addAttachments(Objects.toString(event.text), "Output.txt");
    }

    private void updateTestCaseStatus(final ItemStatus status) {
        if (!forbidTestCaseStatusChange.get()) {
            adapterManager.updateTestCase(getTestCaseUuid(currentTestCase.get()),
                    result -> result.setItemStatus(status));
        }
    }

    private String getTestCaseUuid(final TestCase testCase) {
        return scenarioUuids.computeIfAbsent(getId(testCase), it -> UUID.randomUUID().toString());
    }

    private String getTestCaseUri(final TestCase testCase) {
        final String testCaseUri = testCase.getUri();
        if (testCaseUri.startsWith(CUCUMBER_WORKING_DIR)) {
            return testCaseUri.substring(CUCUMBER_WORKING_DIR.length());
        }
        return testCaseUri;
    }

    private String getId(final TestCase testCase) {
        final String testCaseLocation = getTestCaseUri(testCase) + ":" + testCase.getLine();
        return Utils.getHash(testCaseLocation);
    }

    private String getClassContainerUuid() {
        return classUUID.get();
    }

    private String getStepUuid(final PickleStepTestStep step) {
        final String stepPath = currentFeature.get().getName() + currentTestCase.get().getName()
                + step.getPickleStep().getText() + step.getStepLine();
        return Utils.getHash(stepPath);
    }

    private String getHookStepUuid(final HookTestStep step) {
        final String stepPath = currentFeature.get().getName() + currentTestCase.get().getName()
                + step.getHookType().toString() + step.getCodeLocation();
        return Utils.getHash(stepPath);
    }

    private ItemStatus convertStatus(final Result testCaseResult) {
        switch (testCaseResult.getStatus()) {
            case FAILED:
                return ItemStatus.FAILED;
            case PASSED:
                return ItemStatus.PASSED;
            case SKIPPED:
            case PENDING:
                return ItemStatus.SKIPPED;
            case AMBIGUOUS:
            case UNDEFINED:
            default:
                return null;
        }
    }
}
