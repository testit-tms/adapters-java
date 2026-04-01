package ru.testit.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.annotations.Parameters;
import org.testng.xml.XmlTest;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public class BaseTestNgListener implements
        ISuiteListener,
        ITestListener,
        IClassListener,
        IInvokedMethodListener,
        IConfigurationListener,
        IMethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTestNgListener.class);

    /**
     * Store current executable test.
     */
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);

    /**
     * Store uuid for current executable fixture.
     */
    private final ThreadLocal<String> executableFixture = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    /**
     * One MainContainer per {@link ITestContext} — a single shared UUID breaks when suite has multiple &lt;test&gt;
     * nodes or parallel contexts (Gradle / TestNG overwrites the same storage key).
     */
    private final Map<ITestContext, String> mainUuidByContext = new ConcurrentHashMap<>();

    /**
     * Store class container uuids.
     */
    private final Map<ITestClass, String> classContainers = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final AdapterManager adapterManager;

    private static final String PARAM_REGEX = "\\{.*}";

    public BaseTestNgListener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void onStart(final ISuite suite) {
        adapterManager.startTests();
    }

    @Override
    public void onStart(final ITestContext context) {
        final String mainUuid = UUID.randomUUID().toString();
        mainUuidByContext.put(context, mainUuid);
        adapterManager.startMainContainer(new MainContainer().setUuid(mainUuid));
    }

    @Override
    public void onFinish(final ITestContext context) {
        final String mainUuid = mainUuidByContext.remove(context);
        if (mainUuid != null) {
            adapterManager.stopMainContainer(mainUuid);
        }
    }

    @Override
    public void onTestStart(final ITestResult testResult) {
        ensureClassContainerRegistered(testResult);

        ExecutableTest test = this.executableTest.get();
        if (test.isStarted()) {
            test = refreshContext();
        }
        test.setTestStatus();

        final String uuid = test.getUuid();

        startTestCase(testResult, uuid);

        Optional.of(testResult)
                .map(ITestResult::getMethod)
                .map(ITestNGMethod::getTestClass)
                .ifPresent(cl -> addClassContainerChild(cl, uuid));
    }

    protected void startTestCase(final ITestResult testResult,
                                 final String uuid) {
        Map<String, String> parameters = getParameters(testResult);
        String testNode = testResult.getInstanceName() + "." + testResult.getName();

        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        final String mainUuid = mainUuidForTestResult(testResult);
        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setMainContainerUuid(mainUuid)
                .setLabels(Utils.extractLabels(method, parameters))
                .setTags(Utils.extractTags(method, parameters))
                .setExternalId(Utils.extractExternalID(method, parameters))
                .setWorkItemIds(Utils.extractWorkItemIds(method, parameters))
                .setTitle(Utils.extractTitle(method, parameters, true))
                .setName(Utils.extractDisplayName(method, parameters))
                .setClassName(Utils.extractClassname(method, method.getDeclaringClass().getSimpleName(), parameters))
                .setSpaceName(Utils.extractNamespace(method,
                        ((method.getDeclaringClass().getPackage() == null
                                || method.getDeclaringClass().getPackage().getName().equals(""))
                                ? null : method.getDeclaringClass().getPackage().getName()),
                        parameters))
                .setLinkItems(Utils.extractLinks(method, parameters))
                .setDescription(Utils.extractDescription(method, parameters))
                .setParameters(parameters)
                .setExternalKey(testNode);

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

            String value = String.format("%s", parameters[i]);

            if (indexFromAnnotation < providedNames.length) {
                if (parameters[i] != null) {
                    testParameters.put(providedNames[indexFromAnnotation], value);
                }
                continue;
            }

            if (i < reflectionNames.length) {
                testParameters.put(reflectionNames[i], value);
            }
        }

        return testParameters;
    }

    @Override
    public void onTestSuccess(final ITestResult testResult) {
        final ExecutableTest test = this.executableTest.get();
        test.setAfterStatus();
        adapterManager.updateTestCase(test.getUuid(), setStatus(ItemStatus.PASSED, null));
        adapterManager.stopTestCase(test.getUuid());
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        ExecutableTest test = this.executableTest.get();

        if (test.isAfter()) {
            test = refreshContext();
        }

        if (!test.isStarted()) {
            createTestResultForTestWithoutSetup(result);
        }

        test.setAfterStatus();

        stopTestCase(test.getUuid(), result.getThrowable(), ItemStatus.FAILED);
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
        ExecutableTest test = this.executableTest.get();

        if (test.isAfter()) {
            test = refreshContext();
        }

        if (!test.isStarted()) {
            createTestResultForTestWithoutSetup(result);
        }

        test.setAfterStatus();

        stopTestCase(test.getUuid(), result.getThrowable(), ItemStatus.SKIPPED);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
        //do nothing
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        if (getClassContainer(testClass).isPresent()) {
            return;
        }
        final String mainUuid = mainUuidForTestClass(testClass);
        if (mainUuid == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "TestIT: defer class container for {} until onTestStart (ITestContext not available on ITestClass here)",
                        testClass.getName()
                );
            }
            return;
        }
        registerClassContainer(testClass, mainUuid);
    }

    private void registerClassContainer(final ITestClass testClass, final String mainUuid) {
        final String uuid = UUID.randomUUID().toString();
        final ClassContainer container = new ClassContainer()
                .setUuid(uuid)
                .setName(testClass.getName());
        adapterManager.startClassContainer(mainUuid, container);
        setClassContainer(testClass, uuid);
    }

    /**
     * When {@link #onBeforeClass} could not resolve {@link ITestContext}, register using {@link ITestResult#getTestContext()}.
     */
    private void ensureClassContainerRegistered(final ITestResult testResult) {
        final ITestClass testClass = testResult.getMethod().getTestClass();
        if (getClassContainer(testClass).isPresent()) {
            return;
        }
        final String mainUuid = mainUuidForTestResult(testResult);
        if (mainUuid == null) {
            LOGGER.warn("TestIT: cannot resolve MainContainer for class {}; tests may be missing from TMS bulk import", testClass.getName());
            return;
        }
        registerClassContainer(testClass, mainUuid);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TestIT: lazy class container registered for {}", testClass.getName());
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        getClassContainer(testClass).ifPresent(adapterManager::stopClassContainer);
    }

    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        final ITestNGMethod testMethod = method.getTestMethod();
        if (isSupportedConfigurationFixture(testMethod)) {
            ifTestFixtureStarted(testMethod, testResult);
            ifClassFixtureStarted(testMethod);
            ifMethodFixtureStarted(testMethod);
        }
    }

    private void ifTestFixtureStarted(final ITestNGMethod testMethod, final ITestResult testResult) {
        final String mainUuid = mainUuidForTestResult(testResult);
        if (mainUuid == null) {
            return;
        }
        if (testMethod.isBeforeTestConfiguration()) {
            final String uuid = executableFixture.get();
            adapterManager.startPrepareFixtureAll(mainUuid, uuid, getFixtureResult(testMethod));
        }
        if (testMethod.isAfterTestConfiguration()) {
            final String uuid = executableFixture.get();
            adapterManager.startTearDownFixtureAll(mainUuid, uuid, getFixtureResult(testMethod));
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

                    if (testMethod.isBeforeMethodConfiguration()) {
                        if (test.isStarted()) {
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
                .setTitle(Utils.extractTitle(method, null, false))
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

    private String mainUuidForTestResult(final ITestResult testResult) {
        if (testResult != null) {
            ITestContext ctx = testResult.getTestContext();
            if (ctx != null) {
                String u = mainUuidByContext.get(ctx);
                if (u != null) {
                    return u;
                }
            }
        }
        return mainUuidByContext.size() == 1 ? mainUuidByContext.values().iterator().next() : null;
    }

    private String mainUuidForTestClass(final ITestClass testClass) {
        ITestContext ctx = testContextFromTestClass(testClass);
        if (ctx != null) {
            String u = mainUuidByContext.get(ctx);
            if (u != null) {
                return u;
            }
        }
        return mainUuidByContext.size() == 1 ? mainUuidByContext.values().iterator().next() : null;
    }

    /** TestNG does not expose ITestContext on {@link ITestClass}; try non-public methods on implementation class. */
    private static ITestContext testContextFromTestClass(final ITestClass testClass) {
        if (testClass == null) {
            return null;
        }
        for (Class<?> clazz = testClass.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                final java.lang.reflect.Method m = clazz.getDeclaredMethod("getTestContext");
                m.setAccessible(true);
                return (ITestContext) m.invoke(testClass);
            } catch (NoSuchMethodException e) {
                // try superclass
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }
        return null;
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

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (!adapterManager.isFilteredMode()) {
            return methods;
        }

        List<String> testsForRun = adapterManager.getTestFromTestRun();

        return methods.stream().filter(method -> {
            String externalId = Utils.extractExternalID(method.getMethod().getConstructorOrMethod().getMethod(), null);

            final Pattern pattern = Pattern.compile(PARAM_REGEX, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(externalId);

            if (matcher.find()) {
                boolean include = filterTestWithParameters(testsForRun, externalId);

                if (LOGGER.isDebugEnabled()) {
                    if (include) {
                        LOGGER.debug("Test {} include for run", externalId);
                    } else {
                        LOGGER.debug("Test {} exclude for run", externalId);
                    }
                }

                return include;
            }

            boolean include = testsForRun.contains(externalId);

            if (LOGGER.isDebugEnabled()) {
                if (include) {
                    LOGGER.debug("Test {} include for run", externalId);
                } else {
                    LOGGER.debug("Test {} exclude for run", externalId);
                }
            }

            return include;
        }).collect(Collectors.toList());
    }

    private boolean filterTestWithParameters(List<String> testsForRun, String externalId) {
        Pattern pattern = Pattern.compile(externalId.replaceAll(PARAM_REGEX, ".*"));

        for (String test : testsForRun) {
            Matcher matcher = pattern.matcher(test);
            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }
}
