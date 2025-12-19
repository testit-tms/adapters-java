package ru.testit.listener;

import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.testit.models.ClassContainer;
import ru.testit.models.FixtureResult;
import ru.testit.models.ItemStage;
import ru.testit.models.ItemStatus;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;

@RunListener.ThreadSafe
public class BaseJunit4Listener extends RunListener {

    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest =
        ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(
        () -> UUID.randomUUID().toString()
    );
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() ->
        UUID.randomUUID().toString()
    );
    private final ThreadLocal<Boolean> isFinished = ThreadLocal.withInitial(
        () -> false
    );

    // Fields to track fixture execution
    private final ThreadLocal<String> beforeFixtureId = new ThreadLocal<>();
    private final ThreadLocal<String> afterFixtureId = new ThreadLocal<>();

    public BaseJunit4Listener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void testRunStarted(final Description description) {
        adapterManager.startTests();

        final MainContainer mainContainer = new MainContainer().setUuid(
            launcherUUID.get()
        );

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer().setUuid(
            classUUID.get()
        );

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);

        // Create and complete fixtures for @BeforeClass methods (execute once at start)
        createAndCompleteBeforeClassFixtures(description);

        // Create fixtures for @Before methods (one per test)
        createBeforeFixtures(description);

        // Create fixtures for @After methods (one per test)
        createAfterFixtures(description);

        // @AfterClass fixtures will be created and completed at the end
    }

    private void createAndCompleteBeforeClassFixtures(
        final Description description
    ) {
        // Get the test class
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            return;
        }

        // Look for @BeforeClass methods and create+complete fixtures for them
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeClass.class)) {
                // Found a @BeforeClass method, create and complete fixture
                String fixtureId = UUID.randomUUID().toString();

                FixtureResult fixture = getFixtureResult(method);
                adapterManager.startPrepareFixtureAll(
                    launcherUUID.get(),
                    fixtureId,
                    fixture
                );

                // Mark as passed immediately and complete (assume successful since tests are running)
                adapterManager.updateFixture(fixtureId, f ->
                    f.setItemStatus(ItemStatus.PASSED)
                );
                adapterManager.stopFixture(fixtureId);

                // Note: Not storing fixtureId since it's immediately completed
                break; // Only handle one for now, as in JUnit 5 implementation
            }
        }
    }

    private void createBeforeFixtures(final Description description) {
        // Get the test class
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            return;
        }

        // Look for @Before methods (create fixture for first one found)
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Before.class)) {
                // Found a @Before method, create fixture if not already created
                if (beforeFixtureId.get() == null) {
                    String fixtureId = UUID.randomUUID().toString();
                    beforeFixtureId.set(fixtureId);

                    FixtureResult fixture = getFixtureResult(method);
                    // Set parent to associate with current test
                    fixture.setParent(executableTest.get().getUuid());
                    adapterManager.startPrepareFixtureEachTest(
                        classUUID.get(),
                        fixtureId,
                        fixture
                    );
                    // Don't mark as passed yet - will be marked when each test finishes
                }
                break; // Only handle one for now, as in JUnit 5 implementation
            }
        }
    }

    private void createAfterFixtures(final Description description) {
        // Get the test class
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            return;
        }

        // Look for @After methods (create fixture for first one found)
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(After.class)) {
                // Found a @After method, create fixture if not already created
                if (afterFixtureId.get() == null) {
                    String fixtureId = UUID.randomUUID().toString();
                    afterFixtureId.set(fixtureId);

                    FixtureResult fixture = getFixtureResult(method);
                    // Set parent to associate with current test
                    fixture.setParent(executableTest.get().getUuid());
                    adapterManager.startTearDownFixtureEachTest(
                        classUUID.get(),
                        fixtureId,
                        fixture
                    );
                    // Don't mark as passed yet - will be marked when each test finishes
                }
                break; // Only handle one for now, as in JUnit 5 implementation
            }
        }
    }

    private void createAndCompleteAfterClassFixtures(
        final Description description
    ) {
        // Get the test class
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            // Fallback to a basic description if needed
            Description fallbackDesc = Description.createSuiteDescription(
                "TestSuite"
            );
            testClass = fallbackDesc.getTestClass();
            if (testClass == null) {
                return;
            }
        }

        // Look for @AfterClass methods and create+complete fixtures for them
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(AfterClass.class)) {
                // Found a @AfterClass method, create and complete fixture
                String fixtureId = UUID.randomUUID().toString();

                FixtureResult fixture = getFixtureResult(method);
                adapterManager.startTearDownFixtureAll(
                    launcherUUID.get(),
                    fixtureId,
                    fixture
                );

                // Mark as passed immediately and complete (assume successful at end)
                adapterManager.updateFixture(fixtureId, f ->
                    f.setItemStatus(ItemStatus.PASSED)
                );
                adapterManager.stopFixture(fixtureId);

                break; // Only handle one for now, as in JUnit 5 implementation
            }
        }
    }

    @Override
    public void testRunFinished(final Result result) {
        if (isFinished.get()) {
            return;
        }

        // Complete any remaining @Before fixtures with passed status
        completeFixtureWithStatus(beforeFixtureId, ItemStatus.PASSED);

        // Complete any remaining @After fixtures with passed status
        completeFixtureWithStatus(afterFixtureId, ItemStatus.PASSED);

        // Create and complete @AfterClass fixtures (execute once at end)
        createAndCompleteAfterClassFixtures(
            Description.createSuiteDescription("TestSuite")
        );

        // Clean up fixture IDs
        beforeFixtureId.remove();
        afterFixtureId.remove();

        adapterManager.stopClassContainer(classUUID.get());
        adapterManager.stopMainContainer(launcherUUID.get());
        isFinished.set(true);
    }

    @Override
    public void testStarted(final Description description) {
        ExecutableTest test = this.executableTest.get();
        if (test.isStarted()) {
            test = refreshContext();
        }
        test.setTestStatus();

        final String uuid = test.getUuid();
        startTestCase(description, uuid);

        adapterManager.updateClassContainer(classUUID.get(), container ->
            container.getChildren().add(uuid)
        );
    }

    @Override
    public void testFailure(final Failure failure) {
        ExecutableTest test = this.executableTest.get();
        test.setAfterStatus();
        stopTestCase(
            test.getUuid(),
            failure.getException(),
            ItemStatus.FAILED
        );

        // Fail any running fixtures
        completeFixtureWithStatus(beforeFixtureId, ItemStatus.FAILED);
        completeFixtureWithStatus(afterFixtureId, ItemStatus.FAILED);
    }

    @Override
    public void testAssumptionFailure(final Failure failure) {
        testFailure(failure);
    }

    @Override
    public void testIgnored(final Description description) {
        final ExecutableTest test = this.executableTest.get();
        if (test.isAfter() || test.isBefore()) {
            return;
        }
        test.setAfterStatus();
        stopTestCase(test.getUuid(), null, ItemStatus.SKIPPED);
    }

    @Override
    public void testFinished(final Description description) {
        final ExecutableTest test = this.executableTest.get();
        if (test.isAfter()) {
            return;
        }
        test.setAfterStatus();
        adapterManager.updateTestCase(
            test.getUuid(),
            setStatus(ItemStatus.PASSED, null)
        );
        adapterManager.stopTestCase(test.getUuid());

        // Complete @Before and @After fixtures with success status
        completeFixtureWithStatus(beforeFixtureId, ItemStatus.PASSED);
        completeFixtureWithStatus(afterFixtureId, ItemStatus.PASSED);
    }

    protected void startTestCase(Description method, final String uuid) {
        String fullName = method.getClassName();
        int index = fullName.lastIndexOf(".");
        String testNode = fullName + "." + method.getMethodName();

        final TestResult result = new TestResult()
            .setUuid(uuid)
            .setLabels(Utils.extractLabels(method))
            .setExternalId(Utils.extractExternalID(method))
            .setWorkItemIds(Utils.extractWorkItemIds(method))
            .setTitle(Utils.extractTitle(method))
            .setName(Utils.extractDisplayName(method))
            .setClassName(
                Utils.extractClassname(
                    method,
                    (index != -1) ? fullName.substring(index + 1) : fullName
                )
            )
            .setSpaceName(
                Utils.extractNamespace(
                    method,
                    (index != -1) ? fullName.substring(0, index) : null
                )
            )
            .setLinkItems(Utils.extractLinks(method))
            .setDescription(Utils.extractDescription(method))
            .setExternalKey(testNode);

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(uuid);
    }

    private FixtureResult getFixtureResult(final Method method) {
        return new FixtureResult()
            .setName(method.getName())
            .setDescription("") // JUnit 4 doesn't have built-in description for setup methods
            .setStart(System.currentTimeMillis())
            .setItemStage(ItemStage.RUNNING);
    }

    private void completeFixtureWithStatus(
        ThreadLocal<String> fixtureIdHolder,
        ItemStatus status
    ) {
        if (fixtureIdHolder.get() != null) {
            adapterManager.updateFixture(fixtureIdHolder.get(), f ->
                f.setItemStatus(status)
            );

            adapterManager.stopFixture(fixtureIdHolder.get());

            // Remove the fixture ID since it's now complete
            fixtureIdHolder.remove();
        }
    }

    private void stopTestCase(
        final String uuid,
        final Throwable throwable,
        final ItemStatus status
    ) {
        adapterManager.updateTestCase(uuid, setStatus(status, throwable));
        adapterManager.stopTestCase(uuid);

        // If test failed, also fail any running fixtures
        if (status == ItemStatus.FAILED) {
            completeFixtureWithStatus(beforeFixtureId, ItemStatus.FAILED);
            completeFixtureWithStatus(afterFixtureId, ItemStatus.FAILED);
        }
    }

    private Consumer<TestResult> setStatus(
        final ItemStatus status,
        final Throwable throwable
    ) {
        return result -> {
            result.setItemStatus(status);
            if (nonNull(throwable)) {
                result.setThrowable(throwable);
            }
        };
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }
}
