package ru.testit.listener;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.*;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class BaseJunit5Listener implements Extension, BeforeAllCallback, AfterAllCallback, InvocationInterceptor, TestWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJunit5Listener.class);
    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    public BaseJunit5Listener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Before all: {}", context.getRequiredTestClass().getName());
        }

        adapterManager.startTests();

        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(Utils.getHash(context.getRequiredTestClass().getName()));

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("After all: {}", context.getDisplayName());
        }

        adapterManager.stopClassContainer(Utils.getHash(context.getRequiredTestClass().getName()));
        adapterManager.stopMainContainer(launcherUUID.get());
    }

    @Override
    public void interceptBeforeAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept before all: {}", invocationContext.getExecutable().getName());
        }

        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startPrepareFixtureAll(launcherUUID.get(), uuid, fixture);

        try {
            invocation.proceed();
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        adapterManager.stopFixture(uuid);
    }

    private FixtureResult getFixtureResult(final Method method) {
        return new FixtureResult()
                .setName(Utils.extractTitle(method, null))
                .setDescription(Utils.extractDescription(method, null))
                .setStart(System.currentTimeMillis())
                .setItemStage(ItemStage.RUNNING);
    }

    @Override
    public void interceptBeforeEachMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept before each: {}", invocationContext.getExecutable().getName());
        }

        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startPrepareFixtureEachTest(Utils.getHash(invocationContext.getTargetClass().getName()), uuid, fixture);
        ExecutableTest test = executableTest.get();

        if (test.isStarted()) {
            executableTest.remove();
            test = executableTest.get();
        }
        fixture.setParent(test.getUuid());

        try {
            invocation.proceed();
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        adapterManager.stopFixture(uuid);
    }

    @Override
    public void interceptTestTemplateMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) throws Throwable {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept test template: {}", invocationContext.getExecutable().getName());
        }

        Map<String, String> parameters = getParameters(invocationContext);

        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(extensionContext.getRequiredTestMethod(), uuid, parameters);

        adapterManager.updateClassContainer(Utils.getHash(invocationContext.getTargetClass().getName()),
                container -> container.getChildren().add(uuid));

        invocation.proceed();
    }

    private Map<String, String> getParameters(
            final ReflectiveInvocationContext<Method> invocationContext
    ) {
        final Parameter[] parameters = invocationContext.getExecutable().getParameters();
        Map<String, String> testParameters = new HashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterType = parameter.getType();

            if (parameterType.getCanonicalName().startsWith("org.junit.jupiter.api")) {
                continue;
            }

            String name = parameter.getName();
            String value = invocationContext.getArguments().get(i).toString();

            testParameters.put(name, value);
        }

        return testParameters;
    }

    @Override
    public void interceptTestMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) throws Throwable {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept test: {}", invocationContext.getExecutable().getName());
        }

        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(extensionContext.getRequiredTestMethod(), uuid, null);

        adapterManager.updateClassContainer(Utils.getHash(invocationContext.getTargetClass().getName()),
                container -> container.getChildren().add(uuid));

        invocation.proceed();
    }

    protected void startTestCase(Method method, final String uuid, Map<String, String> parameters) {
        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setLabels(Utils.extractLabels(method, parameters))
                .setExternalId(Utils.extractExternalID(method, parameters))
                .setWorkItemId(Utils.extractWorkItemId(method, parameters))
                .setTitle(Utils.extractTitle(method, parameters))
                .setName(Utils.extractDisplayName(method, parameters))
                .setClassName(Utils.extractClassname(method, method.getDeclaringClass().getSimpleName(), parameters))
                .setSpaceName(Utils.extractNamespace(method,
                        (method.getDeclaringClass().getPackage() == null)
                                ? null : method.getDeclaringClass().getPackage().getName(),
                        parameters)
                )
                .setLinkItems(Utils.extractLinks(method, parameters))
                .setDescription(Utils.extractDescription(method, parameters))
                .setParameters(parameters);

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(uuid);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test successful: {}", context.getDisplayName());
        }

        final ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        adapterManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        adapterManager.stopTestCase(executableTest.getUuid());
    }

    private Consumer<TestResult> setStatus(final ItemStatus status, final Throwable throwable) {
        return result -> {
            result.setItemStatus(status);
            if (nonNull(throwable)) {
                result.setThrowable(throwable);
            }
        };
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test aborted: {}", context.getDisplayName());
        }

        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), cause, ItemStatus.SKIPPED);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test failed: {}", context.getDisplayName());
        }

        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), cause, ItemStatus.FAILED);
    }

    private void stopTestCase(final String uuid, final Throwable throwable, final ItemStatus status) {
        adapterManager.updateTestCase(uuid, setStatus(status, throwable));
        adapterManager.stopTestCase(uuid);
    }

    @Override
    public void interceptAfterEachMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept after each: {}", invocationContext.getExecutable().getName());
        }

        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startTearDownFixtureEachTest(Utils.getHash(invocationContext.getTargetClass().getName()), uuid, fixture);
        ExecutableTest test = executableTest.get();
        fixture.setParent(test.getUuid());
        try {
            invocation.proceed();
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            adapterManager.updateFixture(uuid, result -> result
                    .setItemStatus(ItemStatus.FAILED));
        }

        adapterManager.stopFixture(uuid);
    }

    @Override
    public void interceptAfterAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Intercept after all: {}", invocationContext.getExecutable().getName());
        }

        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startTearDownFixtureAll(launcherUUID.get(), uuid, fixture);

        try {
            invocation.proceed();
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            adapterManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        adapterManager.stopFixture(uuid);
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }
}
