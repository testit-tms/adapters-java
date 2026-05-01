package ru.testit.services.core;

import org.slf4j.Logger;
import ru.testit.listener.ListenerManager;
import ru.testit.models.ItemStage;
import ru.testit.models.ItemStatus;
import ru.testit.models.TestResult;
import ru.testit.properties.AdapterConfig;
import ru.testit.services.ResultStorage;
import ru.testit.services.ThreadContext;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

import java.util.Optional;
import java.util.function.Consumer;

public class AdapterTestCaseHelper {

    private final AdapterConfig adapterConfig;
    private final ThreadContext threadContext;
    private final ResultStorage storage;
    private final ListenerManager listenerManager;
    private final Writer writer;
    private final SyncStorageService syncStorageService;
    private final Logger logger;

    public AdapterTestCaseHelper(
            AdapterConfig adapterConfig,
            ThreadContext threadContext,
            ResultStorage storage,
            ListenerManager listenerManager,
            Writer writer,
            SyncStorageService syncStorageService,
            Logger logger
    ) {
        this.adapterConfig = adapterConfig;
        this.threadContext = threadContext;
        this.storage = storage;
        this.listenerManager = listenerManager;
        this.writer = writer;
        this.syncStorageService = syncStorageService;
        this.logger = logger;
    }

    public void startTestCase(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        threadContext.clear();
        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not start test case: test case with uuid {} is not scheduled",
                    uuid
            );
            return;
        }
        final TestResult testResult = found.get();

        testResult
                .setItemStage(ItemStage.RUNNING)
                .setStart(System.currentTimeMillis());

        threadContext.start(uuid);

        if (logger.isDebugEnabled()) {
            logger.debug("Start test case {}", testResult);
        }
    }

    public void scheduleTestCase(final TestResult result) {
        if (!isTmsEnabled()) {
            return;
        }

        result
                .setItemStage(ItemStage.SCHEDULED)
                .setAutomaticCreationTestCases(
                        adapterConfig.shouldAutomaticCreationTestCases()
                );
        storage.put(result.getUuid(), result);

        if (logger.isDebugEnabled()) {
            logger.debug("Schedule test case {}", result);
        }
    }

    public void updateTestCase(final Consumer<TestResult> update) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<String> root = threadContext.getRoot();
        if (!root.isPresent()) {
            logger.error("Could not update test case: no test case running");
            return;
        }

        updateTestCase(root.get(), update);
    }

    public void updateTestCase(
            final String uuid,
            final Consumer<TestResult> update
    ) {
        if (!isTmsEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Update test case {}", uuid);
        }

        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not update test case: test case with uuid {} not found",
                    uuid
            );
            return;
        }

        update.accept(found.get());
    }

    public void stopTestCase(final String uuid) {
        if (!isTmsEnabled()) {
            return;
        }

        final Optional<TestResult> found = storage.getTestResult(uuid);
        if (!found.isPresent()) {
            logger.error(
                    "Could not stop test case: test case with uuid {} not found",
                    uuid
            );
            return;
        }
        final TestResult testResult = found.get();

        listenerManager.beforeTestStop(testResult);

        testResult
                .setItemStage(ItemStage.FINISHED)
                .setStop(System.currentTimeMillis());

        threadContext.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("Stop test case {}", testResult);
        }

        boolean inProgressSent = false;
        try {
            inProgressSent = syncStorageService.sendInProgressIfNeeded(testResult);
        } catch (Exception e) {
            logger.warn(
                    "Failed to send in-progress result to SyncStorage, fallback to final Test IT result: {}",
                    e.getMessage()
            );
        }

        // InProgress in Test IT only after SyncStorage write; if realtime fails, fall back to final status
        if (inProgressSent) {
            ItemStatus finalStatus = testResult.getItemStatus();
            markTestAsInProgress(testResult);
            if (writer.writeTestRealtime(testResult)) {
                return;
            }
            logger.warn(
                    "Test IT realtime write failed after SyncStorage success; exporting final status for {}",
                    testResult.getExternalId()
            );
            testResult.setItemStatus(finalStatus);
        }

        writer.writeTest(testResult);
    }

    private void markTestAsInProgress(TestResult testResult) {
        testResult.setItemStatus(ItemStatus.INPROGRESS);
    }

    private boolean isTmsEnabled() {
        return adapterConfig.shouldEnableTmsIntegration();
    }
}
