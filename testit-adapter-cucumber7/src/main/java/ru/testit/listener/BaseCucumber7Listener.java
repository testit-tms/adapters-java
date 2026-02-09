package ru.testit.listener;

import io.cucumber.messages.types.*;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BaseCucumber7Listener implements ConcurrentEventListener {
    private static final Logger log = LoggerFactory.getLogger(BaseCucumber7Listener.class);
    private final AdapterManager adapterManager;

    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private final ConcurrentHashMap<String, String> scenarioUuids = new ConcurrentHashMap<>();
    private final ScenarioParser scenarioParser = new ScenarioParser();

    private final ThreadLocal<Feature> currentFeature = new InheritableThreadLocal<>();
    private final ThreadLocal<URI> currentFeatureFile = new InheritableThreadLocal<>();
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
    public BaseCucumber7Listener() {
        this(Adapter.getAdapterManager());
    }

    public BaseCucumber7Listener(AdapterManager adapterManager) {
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
        scenarioParser.addScenarioEvent(event.getUri(), event);
    }

    private void testStarted(final TestCaseStarted event) {
        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);

        currentFeatureFile.set(event.getTestCase().getUri());
        currentFeature.set(scenarioParser.getFeature(currentFeatureFile.get()));
        currentTestCase.set(event.getTestCase());
        forbidTestCaseStatusChange.set(false);

        int line = currentTestCase.get().getLocation().getLine();
        final Scenario scenarioDefinition =
                scenarioParser.getScenarioDefinition(currentFeatureFile.get(), line);

        Map<String, String> parameters = new HashMap<>();

        if (scenarioDefinition.getExamples() != null) {
            parameters =
                    getExamplesAsParameters(scenarioDefinition, currentTestCase.get());
        }

        final Deque<String> tags = new LinkedList<>(currentTestCase.get().getTags());

        final Feature feature = currentFeature.get();
        final TagParser tagParser = new TagParser(feature, currentTestCase.get(), tags, parameters);

        final String uuid = getTestCaseUuid(currentTestCase.get());
        final String scenarioName = currentTestCase.get().getName();

        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setExternalId(tagParser.getExternalIdValue())
                .setName(tagParser.getDisplayNameValue())
                .setTitle(tagParser.getTitleValue())
                .setDescription(tagParser.getDescriptionValue())
                .setWorkItemIds(tagParser.getWorkItemIdList())
                .setSpaceName(tagParser.getNameSpaceValue())
                .setClassName(tagParser.getClassNameValue())
                .setLabels(tagParser.getScenarioLabels())
                .setLinkItems(tagParser.getScenarioLinks())
                .setParameters(parameters)
                .setExternalKey(scenarioName);

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

    private Map<String, String> getExamplesAsParameters(
            final Scenario scenario, final TestCase localCurrentTestCase
    ) {
        final Optional<Examples> maybeExample =
                scenario.getExamples().stream()
                        .filter(example -> example.getTableBody().stream()
                                .anyMatch(row -> row.getLocation().getLine()
                                        == localCurrentTestCase.getLocation().getLine())
                        )
                        .findFirst();

        if (!maybeExample.isPresent()) {
            return Collections.emptyMap();
        }

        final Examples examples = maybeExample.get();

        final Optional<TableRow> maybeRow = examples.getTableBody().stream()
                .filter(example -> example.getLocation().getLine() == localCurrentTestCase.getLocation().getLine())
                .findFirst();

        if (!maybeRow.isPresent()) {
            return Collections.emptyMap();
        }

        final TableRow row = maybeRow.get();
        final Map<String, String> parameters = new HashMap<>();

        Optional<TableRow> headerOptional = examples.getTableHeader();
        if (!headerOptional.isPresent()) {
            log.error("Header is null");
            throw new AssertionError();
        }
        TableRow header = headerOptional.get();
        List<TableCell> headerCells = header.getCells();
        IntStream.range(0, headerCells.size()).forEach
                (index -> {
                    final String name = headerCells.get(index).getValue();
                    final String value = row.getCells().get(index).getValue();
                    parameters.put(name, value);
                });

        return parameters;

    }

    private void testFinished(final TestCaseFinished event) {
        final String uuid = getTestCaseUuid(event.getTestCase());
        Throwable throwable = event.getResult().getError();
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
        if (event.getTestStep() instanceof PickleStepTestStep) {
            final PickleStepTestStep pickleStep = (PickleStepTestStep) event.getTestStep();
            final String stepKeyword = Optional.ofNullable(
                    scenarioParser.getAction(currentFeatureFile.get(), pickleStep.getStep().getLine())
            ).orElse("UNDEFINED");

            Map<String, String> parameters = new HashMap<>();

            for (Argument argument : pickleStep.getDefinitionArgument()) {
                parameters.put("arg" + parameters.size(), argument.getValue());
            }

            parameters = scenarioParser.getParameters(
                    currentFeatureFile.get(),
                    pickleStep.getStep().getLine(),
                    parameters
            );

            final StepResult stepResult = new StepResult()
                    .setTitle(String.format("%s %s", stepKeyword, pickleStep.getStep().getText()))
                    .setStart(System.currentTimeMillis())
                    .setParameters(parameters);

            adapterManager.startStep(getTestCaseUuid(currentTestCase.get()), getStepUuid(pickleStep), stepResult);
        } else if (event.getTestStep() instanceof HookTestStep) {
            initHook((HookTestStep) event.getTestStep());
        }
    }

    private void stepFinished(final TestStepFinished event) {
        if (event.getTestStep() instanceof HookTestStep) {
            handleHookStep(event);
        } else {
            handlePickleStep(event);
        }
    }

    private void handleHookStep(final TestStepFinished event) {
        final HookTestStep hookStep = (HookTestStep) event.getTestStep();
        final String uuid = getHookStepUuid(hookStep);
        final FixtureResult fixtureResult = new FixtureResult()
                .setItemStatus(convertStatus(event.getResult()));

        if (!ItemStatus.PASSED.equals(fixtureResult.getItemStatus())) {
            final TestResult testResult = new TestResult().setItemStatus(convertStatus(event.getResult()));
            Throwable throwable = event.getResult().getError();
            if (throwable != null) {
                testResult.setThrowable(throwable);
            }

            final String errorMessage = event.getResult().getError() == null ? hookStep.getHookType()
                    .name() + " is failed." : hookStep.getHookType()
                    .name() + " is failed: " + event.getResult().getError().getLocalizedMessage();

            Adapter.addMessage(errorMessage);

            if (hookStep.getHookType() == HookType.BEFORE) {
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
        final ItemStatus stepStatus = convertStatus(event.getResult());

        if (event.getResult().getStatus() == io.cucumber.plugin.event.Status.UNDEFINED) {
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

        adapterManager.updateStep(getStepUuid((PickleStepTestStep) event.getTestStep()),
                stepResult -> stepResult.setItemStatus(stepStatus));
        adapterManager.stopStep(getStepUuid((PickleStepTestStep) event.getTestStep()));
    }

    private void handleEmbedEvent(final EmbedEvent event) {
        Adapter.addAttachments("Screenshot", new String(event.getData()));
    }

    private void initHook(final HookTestStep hook) {
        final FixtureResult hookResult = new FixtureResult()
                .setTitle(hook.getCodeLocation())
                .setStart(System.currentTimeMillis());

        if (hook.getHookType() == HookType.BEFORE) {
            adapterManager.startPrepareFixture(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        } else {
            adapterManager.startTearDownFixture(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        }
    }

    private void handleWriteEvent(final WriteEvent event) {
        Adapter.addAttachments(Objects.toString(event.getText()), "Output.txt");
    }

    private void updateTestCaseStatus(final ItemStatus status) {
        if (!Boolean.TRUE.equals(forbidTestCaseStatusChange.get())) {
            adapterManager.updateTestCase(getTestCaseUuid(currentTestCase.get()),
                    result -> result.setItemStatus(status));
        }
    }

    private String getTestCaseUuid(final TestCase testCase) {
        return scenarioUuids.computeIfAbsent(getId(testCase), it -> UUID.randomUUID().toString());
    }

    private String getTestCaseUri(final TestCase testCase) {
        final String testCaseUri = testCase.getUri().toString();
        if (testCaseUri.startsWith(CUCUMBER_WORKING_DIR)) {
            return testCaseUri.substring(CUCUMBER_WORKING_DIR.length());
        }
        return testCaseUri;
    }

    private String getId(final TestCase testCase) {
        int line = testCase.getLocation().getLine();
        final String testCaseLocation = getTestCaseUri(testCase) + ":" + line;
        return Utils.getHash(testCaseLocation);
    }

    private String getClassContainerUuid() {
        return classUUID.get();
    }

    private String getStepUuid(final PickleStepTestStep step) {
        final String stepPath = currentFeature.get().getName() + currentTestCase.get().getName()
                + step.getStep().getText() + step.getStep().getLine();
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
