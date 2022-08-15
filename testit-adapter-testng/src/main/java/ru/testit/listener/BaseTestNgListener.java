package ru.testit.listener;

import org.testng.*;
import org.testng.annotations.Parameters;
import org.testng.xml.XmlTest;
import ru.testit.models.*;
import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.services.ExecutableTest;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    private final AdapterManager adapterManager;

    public BaseTestNgListener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void onStart(final ISuite suite) {
        adapterManager.startTests();
    }

    @Override
    public void onFinish(final ISuite suite) {
        adapterManager.stopTests();
    }

    @Override
    public void onStart(final ITestContext context) {
        final MainContainer container = new MainContainer()
                .setUuid(launcherUUID);

        adapterManager.startMainContainer(container);
    }

    @Override
    public void onFinish(final ITestContext context) {
        adapterManager.stopMainContainer(launcherUUID);
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
        Map<String, String> parameters = getParameters(testResult);

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setLabels(Utils.extractLabels(method, parameters))
                .setExternalId(Utils.extractExternalID(method, parameters))
                .setWorkItemId(Utils.extractWorkItemId(method, parameters))
                .setTitle(Utils.extractTitle(method, parameters))
                .setName(Utils.extractDisplayName(method, parameters))
                .setClassName(method.getDeclaringClass().getSimpleName())
                .setSpaceName((method.getDeclaringClass().getPackage() == null)
                        ? null : method.getDeclaringClass().getPackage().getName())
                .setLinkItems(Utils.extractLinks(method, parameters))
                .setDescription(Utils.extractDescription(method, parameters))
                .setParameters(parameters);

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(uuid);
    }

    private Map<String, String> getParameters(final ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        final Map<String, String> testParameters = new HashMap<>(
                testResult.getTestContext().getCurrentXmlTest().getAllParameters()
        );

        Object[] parameters = testResult.getParameters();
        List<Class<?>> injectedTypes = Arrays.asList(
                ITestContext.class, ITestResult.class, XmlTest.class, Method.class, Object[].class
        );

        final Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != parameters.length) {
            return testParameters;
        }

        final String[] providedNames = Optional.ofNullable(method.getAnnotation(Parameters.class))
                .map(Parameters::value)
                .orElse(new String[]{});

        final String[] reflectionNames = Stream.of(method.getParameters())
                .map(java.lang.reflect.Parameter::getName)
                .toArray(String[]::new);

        int skippedCount = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> parameterType = parameterTypes[i];
            if (injectedTypes.contains(parameterType)) {
                skippedCount++;
                continue;
            }

            final int indexFromAnnotation = i - skippedCount;
            if (indexFromAnnotation < providedNames.length) {
                testParameters.put(providedNames[indexFromAnnotation], parameters[i].toString());
                continue;
            }

            if (i < reflectionNames.length) {
                testParameters.put(reflectionNames[i], parameters[i].toString());
            }
        }

        return testParameters;
    }

    @Override
    public void onTestSuccess(final ITestResult testResult) {
        final ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        adapterManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        adapterManager.stopTestCase(executableTest.getUuid());
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        if (!executableTest.isStarted()) {
            createTestResultForTestWithoutSetup(result);
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), result.getThrowable(), ItemStatus.FAILED);
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

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

    @Override
    public void onBeforeClass(ITestClass testClass) {
        final String uuid = UUID.randomUUID().toString();
        final ClassContainer container = new ClassContainer()
                .setUuid(uuid)
                .setName(testClass.getName());

        adapterManager.startClassContainer(launcherUUID, container);

        setClassContainer(testClass, uuid);
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        getClassContainer(testClass).ifPresent(adapterManager::stopClassContainer);
    }

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
            adapterManager.startPrepareFixtureAll(launcherUUID, uuid, getFixtureResult(testMethod));
        }
        if (testMethod.isAfterTestConfiguration()) {
            final String uuid = executableFixture.get();
            adapterManager.startTearDownFixtureAll(launcherUUID, uuid, getFixtureResult(testMethod));
        }
    }

    private void ifClassFixtureStarted(final ITestNGMethod testMethod) {
        if (testMethod.isBeforeClassConfiguration()) {
            getClassContainer(testMethod.getTestClass())
                    .ifPresent(parentUuid -> {
                        final String uuid = executableFixture.get();
                        adapterManager.startPrepareFixture(parentUuid, uuid, getFixtureResult(testMethod));
                    });
        }
        if (testMethod.isAfterClassConfiguration()) {
            getClassContainer(testMethod.getTestClass())
                    .ifPresent(parentUuid -> {
                        final String uuid = executableFixture.get();
                        adapterManager.startTearDownFixture(parentUuid, uuid, getFixtureResult(testMethod));
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
                        adapterManager.startPrepareFixtureEachTest(parentUuid, uuid, fixture);
                    }

                    if (testMethod.isAfterMethodConfiguration()) {
                        fixture.setParent(test.getUuid());
                        adapterManager.startTearDownFixtureEachTest(parentUuid, uuid, fixture);
                    }
                });
    }

    private FixtureResult getFixtureResult(final ITestNGMethod testMethod) {
        final Method method = testMethod.getConstructorOrMethod().getMethod();

        return new FixtureResult()
                .setName(Utils.extractTitle(method, null))
                .setDescription(Utils.extractDescription(method, null))
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
                adapterManager.updateFixture(executableUuid, result -> result.setItemStatus(ItemStatus.PASSED));
            } else {
                adapterManager.updateFixture(executableUuid, result -> result
                        .setItemStatus(ItemStatus.FAILED));
            }
            adapterManager.stopFixture(executableUuid);
        }
    }

    private void addClassContainerChild(final ITestClass cl, final String childUuid) {
        this.addChildToContainer(classContainers.get(cl), childUuid);
    }

    private void addChildToContainer(final String containerUuid, final String childUuid) {
        lock.writeLock().lock();
        try {
            if (nonNull(containerUuid)) {
                adapterManager.updateClassContainer(
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
        adapterManager.updateTestCase(uuid, setStatus(status, throwable));
        adapterManager.stopTestCase(uuid);
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }
}
