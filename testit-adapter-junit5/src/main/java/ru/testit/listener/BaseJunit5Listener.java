package ru.testit.listener;

import org.junit.jupiter.api.extension.*;
import ru.testit.models.*;
import ru.testit.services.*;

import java.lang.reflect.*;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class BaseJunit5Listener implements Extension, BeforeAllCallback, AfterAllCallback, InvocationInterceptor, TestWatcher {
    private final TmsManager tmsManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final String launcherUUID = UUID.randomUUID().toString();
    private final String classUUID = UUID.randomUUID().toString();

    public BaseJunit5Listener() {
        tmsManager = TmsFactory.getTmsManager();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        tmsManager.startTests();

        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID);

        tmsManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID);

        tmsManager.startClassContainer(launcherUUID, classContainer);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        tmsManager.stopClassContainer(classUUID);
        tmsManager.stopMainContainer(launcherUUID);
        tmsManager.stopTests();
    }

    @Override
    public void interceptBeforeAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        tmsManager.startPrepareFixtureAll(launcherUUID, uuid, fixture);

        try {
            invocation.proceed();
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        tmsManager.stopFixture(uuid);
    }

    private FixtureResult getFixtureResult(final Method method) {
        return new FixtureResult()
                .setName(Utils.extractTitle(method))
                .setDescription(Utils.extractDescription(method))
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
        tmsManager.startPrepareFixtureEachTest(classUUID, uuid, fixture);
        ExecutableTest test = executableTest.get();

        if (test.isStarted()) {
            executableTest.remove();
            test = executableTest.get();
        }
        fixture.setParent(test.getUuid());

        try {
            invocation.proceed();
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        tmsManager.stopFixture(uuid);
    }

    @Override
    public void interceptTestMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(extensionContext.getRequiredTestMethod(), uuid);

        tmsManager.updateClassContainer(classUUID,
                container -> container.getChildren().add(uuid));

        try {
            invocation.proceed();
        } catch (Throwable throwable) {
            stopTestCase(executableTest.getUuid(), throwable, ItemStatus.FAILED);
        }
    }

    protected void startTestCase(Method method, final String uuid) {
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
    public void testSuccessful(ExtensionContext context) {
        final ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        tmsManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        tmsManager.stopTestCase(executableTest.getUuid());
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
        tmsManager.updateTestCase(uuid, setStatus(status, throwable));
        tmsManager.stopTestCase(uuid);
    }

    @Override
    public void interceptAfterEachMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        tmsManager.startTearDownFixtureEachTest(classUUID, uuid, fixture);
        ExecutableTest test = executableTest.get();
        fixture.setParent(test.getUuid());
        try {
            invocation.proceed();
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            tmsManager.updateFixture(uuid, result -> result
                    .setItemStatus(ItemStatus.FAILED));
        }

        tmsManager.stopFixture(uuid);
    }

    @Override
    public void interceptAfterAllMethod(
            InvocationInterceptor.Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        final String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = getFixtureResult(invocationContext.getExecutable());
        tmsManager.startTearDownFixtureAll(launcherUUID, uuid, fixture);

        try {
            invocation.proceed();
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.PASSED));
        } catch (Throwable throwable) {
            tmsManager.updateFixture(uuid, result -> result.setItemStatus(ItemStatus.FAILED));
        }

        tmsManager.stopFixture(uuid);
    }

    private ExecutableTest refreshContext() {
        executableTest.remove();
        return executableTest.get();
    }
}
