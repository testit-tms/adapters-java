package ru.testit.listener;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.testit.models.*;
import ru.testit.services.ExecutableTest;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@RunListener.ThreadSafe
public class BaseJunit4Listener extends RunListener
{
    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final String launcherUUID = UUID.randomUUID().toString();
    private final String classUUID = UUID.randomUUID().toString();

    public BaseJunit4Listener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void testRunStarted(final Description description) {
        adapterManager.startTests();

        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID);

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID);

        adapterManager.startClassContainer(launcherUUID, classContainer);
    }

    @Override
    public void testRunFinished(final Result result) {
        adapterManager.stopClassContainer(classUUID);
        adapterManager.stopMainContainer(launcherUUID);
        adapterManager.stopTests();
    }

    @Override
    public void testStarted(final Description description) {
        ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isStarted()) {
            executableTest = refreshContext();
        }
        executableTest.setTestStatus();

        final String uuid = executableTest.getUuid();
        startTestCase(description, uuid);

        adapterManager.updateClassContainer(classUUID,
                container -> container.getChildren().add(uuid));
    }

    @Override
    public void testFailure(final Failure failure) {
        ExecutableTest executableTest = this.executableTest.get();
        stopTestCase(executableTest.getUuid(), failure.getException(), ItemStatus.FAILED);
    }

    @Override
    public void testAssumptionFailure(final Failure failure) {
        ExecutableTest executableTest = this.executableTest.get();
        stopTestCase(executableTest.getUuid(), failure.getException(), ItemStatus.FAILED);
    }

    @Override
    public void testIgnored(final Description description) {
        ExecutableTest executableTest = this.executableTest.get();
        stopTestCase(executableTest.getUuid(), null, ItemStatus.SKIPPED);
    }

    @Override
    public void testFinished(final Description description) {
        final ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        adapterManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        adapterManager.stopTestCase(executableTest.getUuid());
    }

    private FixtureResult getFixtureResult(final Description method) {
        return new FixtureResult()
                .setName(Utils.extractTitle(method))
                .setDescription(Utils.extractDescription(method))
                .setStart(System.currentTimeMillis())
                .setItemStage(ItemStage.RUNNING);
    }

    protected void startTestCase(Description method, final String uuid) {
        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setLabels(Utils.extractLabels(method))
                .setExternalId(Utils.extractExternalID(method))
                .setWorkItemId(Utils.extractWorkItemId(method))
                .setTitle(Utils.extractTitle(method))
                .setName(Utils.extractDisplayName(method))
                .setClassName(method.getClass().getSimpleName())
                .setSpaceName((method.getClass().getPackage() == null)
                        ? null : method.getClass().getPackage().getName())
                .setLinkItems(Utils.extractLinks(method))
                .setDescription(Utils.extractDescription(method));

        adapterManager.scheduleTestCase(result);
        adapterManager.startTestCase(uuid);
    }

    private void stopTestCase(final String uuid, final Throwable throwable, final ItemStatus status) {
        adapterManager.updateTestCase(uuid, setStatus(status, throwable));
        adapterManager.stopTestCase(uuid);
    }

    private Consumer<TestResult> setStatus(final ItemStatus status, final Throwable throwable) {
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