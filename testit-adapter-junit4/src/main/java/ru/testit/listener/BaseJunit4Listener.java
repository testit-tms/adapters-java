package ru.testit.listener;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.testit.models.ClassContainer;
import ru.testit.models.ItemStatus;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.ExecutableTest;

import java.util.UUID;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@RunListener.ThreadSafe
public class BaseJunit4Listener extends RunListener {
    private final AdapterManager adapterManager;
    private final ThreadLocal<ExecutableTest> executableTest = ThreadLocal.withInitial(ExecutableTest::new);
    private final ThreadLocal<String> launcherUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<String> classUUID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    private final ThreadLocal<Boolean> isFinished = ThreadLocal.withInitial(() -> false);

    public BaseJunit4Listener() {
        adapterManager = Adapter.getAdapterManager();
    }

    @Override
    public void testRunStarted(final Description description) {
        adapterManager.startTests();

        final MainContainer mainContainer = new MainContainer()
                .setUuid(launcherUUID.get());

        adapterManager.startMainContainer(mainContainer);

        final ClassContainer classContainer = new ClassContainer()
                .setUuid(classUUID.get());

        adapterManager.startClassContainer(launcherUUID.get(), classContainer);
    }

    @Override
    public void testRunFinished(final Result result) {
        if (isFinished.get()) {
            return;
        }

        adapterManager.stopClassContainer(classUUID.get());
        adapterManager.stopMainContainer(launcherUUID.get());
        isFinished.set(true);
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

        adapterManager.updateClassContainer(classUUID.get(),
                container -> container.getChildren().add(uuid));
    }

    @Override
    public void testFailure(final Failure failure) {
        ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        stopTestCase(executableTest.getUuid(), failure.getException(), ItemStatus.FAILED);
    }

    @Override
    public void testAssumptionFailure(final Failure failure) {
        ExecutableTest executableTest = this.executableTest.get();
        executableTest.setAfterStatus();
        stopTestCase(executableTest.getUuid(), failure.getException(), ItemStatus.FAILED);
    }

    @Override
    public void testIgnored(final Description description) {
        final ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isAfter() || executableTest.isBefore()) {
            return;
        }
        executableTest.setAfterStatus();
        stopTestCase(executableTest.getUuid(), null, ItemStatus.SKIPPED);
    }

    @Override
    public void testFinished(final Description description) {
        final ExecutableTest executableTest = this.executableTest.get();
        if (executableTest.isAfter()) {
            return;
        }
        executableTest.setAfterStatus();
        adapterManager.updateTestCase(executableTest.getUuid(), setStatus(ItemStatus.PASSED, null));
        adapterManager.stopTestCase(executableTest.getUuid());
    }

    protected void startTestCase(Description method, final String uuid) {
        String fullName = method.getClassName();
        int index = fullName.lastIndexOf(".");
        String testNode = fullName + "." + method.getMethodName();
        String spaceName = Utils.extractNamespace(method, (index != -1) ? fullName.substring(0, index) : null);
        String className = Utils.extractClassname(method, (index != -1) ? fullName.substring(index + 1) : fullName);

        final TestResult result = new TestResult()
                .setUuid(uuid)
                .setLabels(Utils.extractLabels(method))
                .setExternalId(Utils.extractExternalID(method))
                .setWorkItemIds(Utils.extractWorkItemId(method))
                .setTitle(Utils.extractTitle(method))
                .setName(Utils.extractDisplayName(method))
                .setClassName(ru.testit.services.Utils.nullOnEmptyString(className))
                .setSpaceName(ru.testit.services.Utils.nullOnEmptyString(spaceName))
                .setLinkItems(Utils.extractLinks(method))
                .setDescription(Utils.extractDescription(method))
                .setExternalKey(testNode);

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
