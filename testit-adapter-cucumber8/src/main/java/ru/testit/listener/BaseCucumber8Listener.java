package ru.testit.listener;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.TestRunHookFinished;
import io.cucumber.messages.types.TestRunHookStarted;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.ClassContainer;
import ru.testit.models.FixtureResult;
import ru.testit.models.ItemStatus;
import ru.testit.models.MainContainer;
import ru.testit.models.StepResult;
import ru.testit.models.TestResult;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BaseCucumber8Listener implements ConcurrentEventListener {
    private static final Logger log = LoggerFactory.getLogger(BaseCucumber8Listener.class);
    /** Main container UUIDs to finalize once per Cucumber run (bulk import); supports parallel threads. */
    private static final Set<String> MAIN_UUIDS_PENDING_FINALIZE = ConcurrentHashMap.newKeySet();
    private final AdapterManager adapterManager;

    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private final ScenarioParser scenarioParser = new ScenarioParser();

    private final ThreadLocal<Feature> currentFeature = new InheritableThreadLocal<>();
    private final ThreadLocal<URI> currentFeatureFile = new InheritableThreadLocal<>();
    private final ThreadLocal<TestCase> currentTestCase = new InheritableThreadLocal<>();
    private final ThreadLocal<Boolean> forbidTestCaseStatusChange = new InheritableThreadLocal<>();

    /** Shared main UUID for the Cucumber run (BeforeAll/AfterAll attach here). */
    private volatile String runMainUuid;
    private final Map<String, Hook> hooksById = new ConcurrentHashMap<>();

    private final EventHandler<TestRunStarted> testRunStartedEventHandler = this::handleTestRunStartedHandler;
    private final EventHandler<TestRunFinished> testRunFinishedEventHandler = this::handleTestRunFinishedHandler;
    private final EventHandler<TestSourceRead> featureStartedEventHandler = this::handleFeatureStartedHandler;
    private final EventHandler<TestCaseStarted> caseStartedEventHandler = this::testStarted;
    private final EventHandler<TestCaseFinished> caseFinishedEventHandler = this::testFinished;
    private final EventHandler<TestStepStarted> stepStartedEventHandler = this::stepStarted;
    private final EventHandler<TestStepFinished> stepFinishedEventHandler = this::stepFinished;
    private final EventHandler<WriteEvent> writeEventHandler = this::handleWriteEvent;
    private final EventHandler<EmbedEvent> embedEventHandler = this::handleEmbedEvent;
    private final EventHandler<Envelope> envelopeEventHandler = this::handleEnvelope;

    private static final String CUCUMBER_WORKING_DIR = Paths.get("").toUri().toString();

    @SuppressWarnings("unused")
    public BaseCucumber8Listener() {
        this(Adapter.getAdapterManager());
    }

    public BaseCucumber8Listener(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, featureStartedEventHandler);
        publisher.registerHandlerFor(TestRunStarted.class, testRunStartedEventHandler);
        publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedEventHandler);

        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedEventHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedEventHandler);

        publisher.registerHandlerFor(TestStepStarted.class, stepStartedEventHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedEventHandler);

        publisher.registerHandlerFor(WriteEvent.class, writeEventHandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventHandler);
        publisher.registerHandlerFor(Envelope.class, envelopeEventHandler);
    }

    public void handleTestRunStartedHandler(TestRunStarted event) {
        MAIN_UUIDS_PENDING_FINALIZE.clear();
        hooksById.clear();
        adapterManager.startTests();

        runMainUuid = launcherUUID.get();
        final MainContainer mainContainer = new MainContainer().setUuid(runMainUuid);
        adapterManager.startMainContainer(mainContainer);
        MAIN_UUIDS_PENDING_FINALIZE.add(runMainUuid);
    }

    public void handleTestRunFinishedHandler(TestRunFinished event) {
        for (String uuid : new ArrayList<>(MAIN_UUIDS_PENDING_FINALIZE)) {
            adapterManager.stopMainContainer(uuid);
        }
        MAIN_UUIDS_PENDING_FINALIZE.clear();
        hooksById.clear();
        runMainUuid = null;
    }

    public void handleFeatureStartedHandler(TestSourceRead event) {
        scenarioParser.addScenarioEvent(event.getUri(), event);
    }

    public void handleEnvelope(final Envelope envelope) {
        envelope.getHook().ifPresent(hook -> hooksById.put(hook.getId(), hook));
        envelope.getTestRunHookStarted().ifPresent(this::handleTestRunHookStarted);
        envelope.getTestRunHookFinished().ifPresent(this::handleTestRunHookFinished);
    }

    private void handleTestRunHookStarted(final TestRunHookStarted started) {
        final String mainUuid = resolveRunMainUuid();
        if (mainUuid == null) {
            log.warn("TestRunHookStarted before TestRunStarted; skipping fixture {}", started.getId());
            return;
        }

        final Hook hook = hooksById.get(started.getHookId());
        if (hook == null) {
            log.warn("No Hook definition for hookId {}; skipping fixture {}", started.getHookId(), started.getId());
            return;
        }

        final Optional<io.cucumber.messages.types.HookType> maybeHookType = hook.getType();
        if (!maybeHookType.isPresent()) {
            return;
        }
        final io.cucumber.messages.types.HookType hookType = maybeHookType.get();
        if (hookType != io.cucumber.messages.types.HookType.BEFORE_TEST_RUN
                && hookType != io.cucumber.messages.types.HookType.AFTER_TEST_RUN) {
            return;
        }

        final String fixtureUuid = started.getId();
        final FixtureResult fixtureResult = new FixtureResult()
                .setTitle(resolveHookTitle(hook))
                .setStart(System.currentTimeMillis());

        if (hookType == io.cucumber.messages.types.HookType.BEFORE_TEST_RUN) {
            adapterManager.startPrepareFixtureAll(mainUuid, fixtureUuid, fixtureResult);
        } else {
            adapterManager.startTearDownFixtureAll(mainUuid, fixtureUuid, fixtureResult);
        }
    }

    private void handleTestRunHookFinished(final TestRunHookFinished finished) {
        final String fixtureUuid = finished.getTestRunHookStartedId();
        final ItemStatus status = convertMessageStatus(finished.getResult());
        adapterManager.updateFixture(fixtureUuid, result -> result.setItemStatus(status));
        adapterManager.stopFixture(fixtureUuid);

        if (status != null && !ItemStatus.PASSED.equals(status)) {
            final String message = finished.getResult().getMessage().orElse("Test run hook is failed.");
            Adapter.addMessage(message);
        }
    }

    private String resolveRunMainUuid() {
        if (runMainUuid != null) {
            return runMainUuid;
        }
        return launcherUUID.get();
    }

    private static String resolveHookTitle(final Hook hook) {
        final Optional<String> name = hook.getName();
        if (name.isPresent() && !name.get().isEmpty()) {
            return name.get();
        }
        return hook.getSourceReference().getJavaMethod()
                .map(method -> method.getClassName() + "." + method.getMethodName())
                .orElseGet(() -> hook.getSourceReference().getUri().orElse(hook.getId()));
    }

    private static ItemStatus convertMessageStatus(final TestStepResult result) {
        final TestStepResultStatus status = result.getStatus();
        if (status == null) {
            return null;
        }
        switch (status) {
            case FAILED:
                return ItemStatus.FAILED;
            case PASSED:
                return ItemStatus.PASSED;
            case SKIPPED:
            case PENDING:
                return ItemStatus.SKIPPED;
            case AMBIGUOUS:
            case UNDEFINED:
            case UNKNOWN:
            default:
                return null;
        }
    }

    public void testStarted(final TestCaseStarted event) {
        final String mainUuid = resolveRunMainUuid();
        launcherUUID.set(mainUuid);

        final MainContainer mainContainer = new MainContainer()
                .setUuid(mainUuid);

        adapterManager.startMainContainer(mainContainer);
        MAIN_UUIDS_PENDING_FINALIZE.add(mainUuid);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startClassContainer(mainUuid, classContainer);

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
                .setMainContainerUuid(mainUuid)
                .setExternalId(tagParser.getExternalIdValue())
                .setName(tagParser.getDisplayNameValue())
                .setTitle(tagParser.getTitleValue())
                .setDescription(tagParser.getDescriptionValue())
                .setWorkItemIds(tagParser.getWorkItemIdList())
                .setSpaceName(tagParser.getNameSpaceValue())
                .setClassName(tagParser.getClassNameValue())
                .setLabels(tagParser.getScenarioLabels())
                .setTags(tagParser.getScenarioTags())
                .setLayer(tagParser.getLayerValue())
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

    public Map<String, String> getExamplesAsParameters(
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

    public void testFinished(final TestCaseFinished event) {
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
    }

    public void stepStarted(final TestStepStarted event) {
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

    public void stepFinished(final TestStepFinished event) {
        if (event.getTestStep() instanceof HookTestStep) {
            final HookTestStep hookStep = (HookTestStep) event.getTestStep();
            if (hookStep.getHookType() == HookType.BEFORE_STEP
                    || hookStep.getHookType() == HookType.AFTER_STEP) {
                return;
            }
            handleHookStep(event);
        } else {
            handlePickleStep(event);
        }
    }

    public void handleHookStep(final TestStepFinished event) {
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

    public void handlePickleStep(final TestStepFinished event) {
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

    public void handleEmbedEvent(final EmbedEvent event) {
        Adapter.addAttachments("Screenshot", new String(event.getData()));
    }

    public void initHook(final HookTestStep hook) {
        switch (hook.getHookType()) {
            case BEFORE:
                startEachTestFixture(hook, true);
                break;
            case AFTER:
                startEachTestFixture(hook, false);
                break;
            case BEFORE_STEP:
            case AFTER_STEP:
            default:
                break;
        }
    }

    private void startEachTestFixture(final HookTestStep hook, final boolean prepare) {
        final FixtureResult hookResult = new FixtureResult()
                .setTitle(hook.getCodeLocation())
                .setStart(System.currentTimeMillis())
                .setParent(getTestCaseUuid(currentTestCase.get()));

        if (prepare) {
            adapterManager.startPrepareFixtureEachTest(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        } else {
            adapterManager.startTearDownFixtureEachTest(getClassContainerUuid(), getHookStepUuid(hook), hookResult);
        }
    }

    public void handleWriteEvent(final WriteEvent event) {
        Adapter.addAttachments(Objects.toString(event.getText()), "Output.txt");
    }

    public void updateTestCaseStatus(final ItemStatus status) {
        if (!Boolean.TRUE.equals(forbidTestCaseStatusChange.get())) {
            adapterManager.updateTestCase(getTestCaseUuid(currentTestCase.get()),
                    result -> result.setItemStatus(status));
        }
    }

    public String getTestCaseUuid(final TestCase testCase) {
        return testCase.getId().toString();
    }

    public String getTestCaseUri(final TestCase testCase) {
        final String testCaseUri = testCase.getUri().toString();
        if (testCaseUri.startsWith(CUCUMBER_WORKING_DIR)) {
            return testCaseUri.substring(CUCUMBER_WORKING_DIR.length());
        }
        return testCaseUri;
    }

    public String getId(final TestCase testCase) {
        int line = testCase.getLocation().getLine();
        final String testCaseLocation = getTestCaseUri(testCase) + ":" + line;
        return Utils.getHash(testCaseLocation);
    }

    public String getClassContainerUuid() {
        return classUUID.get();
    }

    public String getStepUuid(final PickleStepTestStep step) {
        final String stepPath = currentFeature.get().getName() + currentTestCase.get().getName()
                + step.getStep().getText() + step.getStep().getLine();
        return Utils.getHash(stepPath);
    }

    public String getHookStepUuid(final HookTestStep step) {
        final String stepPath = currentFeature.get().getName() + currentTestCase.get().getName()
                + step.getHookType().toString() + step.getCodeLocation();
        return Utils.getHash(stepPath);
    }

    public ItemStatus convertStatus(final Result testCaseResult) {
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
