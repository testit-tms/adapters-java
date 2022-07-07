package ru.testit.listener;

import org.testng.*;
import ru.testit.models.*;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.services.ExecutableTest;
import ru.testit.services.TmsFactory;
import ru.testit.services.TmsManager;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class BaseTestNgListener implements
        ISuiteListener,
        ITestListener,
        IClassListener,
        IInvokedMethodListener,
        IConfigurationListener {

    /**
     * Store current executable test.
     */
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);

    /**
     * Store uuid for current executable fixture.
     */
    private final ThreadLocal<String> executableFixture = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    /**
     * Store current executable context of tests.
     */
    private final String launcherUUID = UUID.randomUUID().toString();

    /**
     * Store class container uuids.
     */
    private final Map<ITestClass, String> classContainers = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final TmsManager tmsManager;

    public BaseTestNgListener() {
        tmsManager = TmsFactory.getTmsManager();
    }

    // ISuiteListener
    @Override
    public void onStart(final ISuite suite) {
        tmsManager.startTests();
    }

    @Override
    public void onFinish(final ISuite suite) {
        tmsManager.stopTests();
    }

    // ITestListener
    @Override
    public void onStart(final ITestContext context) {
        final MainContainer container = new MainContainer()
                .setUuid(launcherUUID);

        tmsManager.startMainContainer(container);
    }

    @Override
    public void onFinish(final ITestContext context) {
        tmsManager.stopMainContainer(launcherUUID);
    }

    @Override
    public void onTestStart(final ITestResult testResult) {
        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();

        startTestCase(testResult, uuid);

        Optional.of(testResult)
                .map(ITestResult::getMethod)
                .map(ITestNGMethod::getTestClass)
                .ifPresent(cl -> addClassContainerChild(cl, uuid));
    }

    protected void startTestCase(final ITestResult testResult,
                                 final String uuid) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setLabels(Utils.extractLabels(method))
                .setExternalId(Utils.extractExternalID(method))
                .setWorkItemId(Utils.extractWorkItemId(method))
                .setTitle(Utils.extractTitle(method))
                .setName(Utils.extractDisplayName(method))
                .setClassName(method.getDeclaringClass().getSimpleName())
                .setSpaceName((method.getDeclaringClass().getPackage() == null)
                        ? null : method.getDeclaringClass().getPackage().getName())
                .setLinkItems(Utils.extractLinks(method))
                .setDescription(Utils.extractDescription(method));

        tmsManager.scheduleTestCase(result);
        tmsManager.startTestCase(uuid);
    }

    @Override
    public void onTestSuccess(final ITestResult testResult) {
        final ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        tmsManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        tmsManager.stopTestCase(executableTest.getUuid());
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        //if testng has failed without any setup
        if (!executableTest.isStarted()) {
            createTestResultForTestWithoutSetup(result);
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), result.getThrowable(), ItemStatus.FAILED);
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
        ExecutableTest executableTest = this.executableTest.get();

        //testng is being skipped as dependent on failed testng, closing context for previous testng here
        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        //if testng was skipped without any setup
        if (!executableTest.isStarted()) {
            createTestResultForTestWithoutSetup(result);
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), result.getThrowable(), ItemStatus.SKIPPED);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
        //do nothing
    }

    // IClassListener
    @Override
    public void onBeforeClass(ITestClass testClass) {
        final String uuid = UUID.randomUUID().toString();
        final ClassContainer container = new ClassContainer()
                .setUuid(uuid)
                .setName(testClass.getName());

        tmsManager.startClassContainer(launcherUUID, container);

        setClassContainer(testClass, uuid);
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        getClassContainer(testClass).ifPresent(tmsManager::stopClassContainer);
    }

    // IInvokedMethodListener
    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        final ITestNGMethod testMethod = method.getTestMethod();
        if (isSupportedConfigurationFixture(testMethod)) {
            ifTestFixtureStarted(testMethod);
            ifClassFixtureStarted(testMethod);
            ifMethodFixtureStarted(testMethod);
        }
    }

    private void ifTestFixtureStarted(final ITestNGMethod testMethod) {
        if (testMethod.isBeforeTestConfiguration()) {
            final String uuid = executableFixture.get();
            tmsManager.startPrepareFixtureAll(launcherUUID, uuid, getFixtureResult(testMethod));
        }
        if (testMethod.isAfterTestConfiguration()) {
            final String uuid = executableFixture.get();
            tmsManager.startTearDownFixtureAll(launcherUUID, uuid, getFixtureResult(testMethod));
        }
    }

    private void ifClassFixtureStarted(final ITestNGMethod testMethod) {
        if (testMethod.isBeforeClassConfiguration()) {
            getClassContainer(testMethod.getTestClass())
                    .ifPresent(parentUuid -> {
                        final String uuid = executableFixture.get();
                        tmsManager.startPrepareFixture(parentUuid, uuid, getFixtureResult(testMethod));
                    });
        }
        if (testMethod.isAfterClassConfiguration()) {
            getClassContainer(testMethod.getTestClass())
                    .ifPresent(parentUuid -> {
                        final String uuid = executableFixture.get();
                        tmsManager.startTearDownFixture(parentUuid, uuid, getFixtureResult(testMethod));
                    });
        }
    }

    private void ifMethodFixtureStarted(final ITestNGMethod testMethod) {
        final FixtureResult fixture = getFixtureResult(testMethod);
        final String uuid = executableFixture.get();

        getClassContainer(testMethod.getTestClass())
                .ifPresent(parentUuid -> {
                    ExecutableTest test = executableTest.get();

                    if (testMethod.isBeforeMethodConfiguration())
                    {
                        if (test.isStarted()){
                            executableTest.remove();
                            test = executableTest.get();
                        }

                        fixture.setParent(test.getUuid());
                        tmsManager.startPrepareFixtureEachTest(parentUuid, uuid, fixture);
                    }

                    if (testMethod.isAfterMethodConfiguration()) {
                        fixture.setParent(test.getUuid());
                        tmsManager.startTearDownFixtureEachTest(parentUuid, uuid, fixture);
                    }
                });
    }

    private FixtureResult getFixtureResult(final ITestNGMethod testMethod) {
        final Method method = testMethod.getConstructorOrMethod().getMethod();

        return new FixtureResult()
                .setName(Utils.extractTitle(method))
                .setDescription(Utils.extractDescription(method))
                .setStart(System.currentTimeMillis())
                .setItemStage(ItemStage.RUNNING);
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        final ITestNGMethod testMethod = method.getTestMethod();
        if (isSupportedConfigurationFixture(testMethod)) {
            final String executableUuid = executableFixture.get();
            executableFixture.remove();
            if (testResult.isSuccess()) {
                tmsManager.updateFixture(executableUuid, result -> result.setItemStatus(ItemStatus.PASSED));
            } else {
                tmsManager.updateFixture(executableUuid, result -> result
                        .setItemStatus(ItemStatus.FAILED));
            }
            tmsManager.stopFixture(executableUuid);
        }
    }

    private void addClassContainerChild(final ITestClass cl, final String childUuid) {
        this.addChildToContainer(classContainers.get(cl), childUuid);
    }

    private void addChildToContainer(final String containerUuid, final String childUuid) {
        lock.writeLock().lock();
        try {
            if (nonNull(containerUuid)) {
                tmsManager.updateClassContainer(
                        containerUuid,
                        container -> container.getChildren().add(childUuid)
                );
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void setClassContainer(final ITestClass cl, final String uuid) {
        lock.writeLock().lock();
        try {
            classContainers.put(cl, uuid);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Optional<String> getClassContainer(final ITestClass cl) {
        lock.readLock().lock();
        try {
            return java.util.Optional.ofNullable(classContainers.get(cl));
        } finally {
            lock.readLock().unlock();
        }
    }

    private Consumer<TestResult> setStatus(final ItemStatus status, final Throwable throwable) {
        return result -> {
            result.setItemStatus(status);
            if (nonNull(throwable)) {
                result.setThrowable(throwable);
            }
        };
    }

    private boolean isSupportedConfigurationFixture(final ITestNGMethod testMethod) {
        return testMethod.isBeforeMethodConfiguration() || testMethod.isAfterMethodConfiguration()
                || testMethod.isBeforeTestConfiguration() || testMethod.isAfterTestConfiguration()
                || testMethod.isBeforeClassConfiguration() || testMethod.isAfterClassConfiguration();
    }

    private void createTestResultForTestWithoutSetup(final ITestResult result) {
        onTestStart(result);
        executableTest.remove();
    }

    private void stopTestCase(final String uuid, final Throwable throwable, final ItemStatus status) {
        tmsManager.updateTestCase(uuid, setStatus(status, throwable));
        tmsManager.stopTestCase(uuid);
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }
}
