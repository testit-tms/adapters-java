package ru.testit.listener;

import org.junit.jupiter.api.extension.*;
import ru.testit.models.*;
import ru.testit.services.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class BaseJunit5Listener implements Extension, BeforeAllCallback, AfterAllCallback, InvocationInterceptor, TestWatcher {
    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    public BaseJunit5Listener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        adapterManager.startTests();

        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        adapterManager.stopClassContainer(classUUID.get());
        adapterManager.stopMainContainer(launcherUUID.get());
    }

    @Override
    public void interceptBeforeAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
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
        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startPrepareFixtureEachTest(classUUID.get(), uuid, fixture);
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
    ) throws Exception {
        Map<String, String> parameters = getParameters(invocationContext);

        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(extensionContext.getRequiredTestMethod(), uuid, parameters);

        adapterManager.updateClassContainer(classUUID.get(),
                container -> container.getChildren().add(uuid));

        try {
            invocation.proceed();
        } catch (Throwable throwable) {
            stopTestCase(executableTest.getUuid(), throwable, ItemStatus.FAILED);
            throw new Exception(throwable.getMessage());
        }
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
    ) throws Exception {
        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(extensionContext.getRequiredTestMethod(), uuid, null);

        adapterManager.updateClassContainer(classUUID.get(),
                container -> container.getChildren().add(uuid));

        try {
            invocation.proceed();
        } catch (Throwable throwable) {
            stopTestCase(executableTest.getUuid(), throwable, ItemStatus.FAILED);
            throw new Exception(throwable.getMessage());
        }
    }

    protected void startTestCase(Method method, final String uuid, Map<String, String> parameters) {
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

    @Override
    public void testSuccessful(ExtensionContext context) {
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
        ExecutableTest executableTest = this.executableTest.get();

        if (executableTest.isAfter()) {
            executableTest = refreshContext();
        }

        executableTest.setAfterStatus();

        stopTestCase(executableTest.getUuid(), cause, ItemStatus.SKIPPED);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
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
        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        adapterManager.startTearDownFixtureEachTest(classUUID.get(), uuid, fixture);
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
