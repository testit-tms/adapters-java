package ru.testit.services.core;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ru.testit.listener.ListenerManager;
import ru.testit.models.ClassContainer;
import ru.testit.models.FixtureResult;
import ru.testit.models.StepResult;
import ru.testit.models.TestResult;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AppProperties;
import ru.testit.services.ResultStorage;
import ru.testit.services.ThreadContext;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AdapterFixtureThreadContextTest {

    @Test
    void afterEachTestFixture_testCaseRemainsRoot_andUpdateTestCaseWorksDuringStep() {
        AdapterConfig config = new AdapterConfig(enabledProps());
        ResultStorage storage = new ResultStorage();
        ThreadContext threadContext = new ThreadContext();
        Writer writer = mock(Writer.class);
        SyncStorageService sync = mock(SyncStorageService.class);

        AdapterTestCaseHelper testCaseHelper = new AdapterTestCaseHelper(
                config,
                threadContext,
                storage,
                new ListenerManager(Collections.emptyList()),
                writer,
                sync,
                LoggerFactory.getLogger(AdapterFixtureThreadContextTest.class)
        );
        AdapterFixtureHelper fixtureHelper = new AdapterFixtureHelper(
                config,
                threadContext,
                storage,
                LoggerFactory.getLogger(AdapterFixtureThreadContextTest.class)
        );
        AdapterStepHelper stepHelper = new AdapterStepHelper(
                config,
                threadContext,
                storage,
                LoggerFactory.getLogger(AdapterFixtureThreadContextTest.class)
        );

        String testCaseUuid = "test-case-uuid";
        String fixtureUuid = "fixture-uuid";
        String stepUuid = "step-uuid";
        String classContainerUuid = "class-container-uuid";

        storage.put(classContainerUuid, new ClassContainer().setUuid(classContainerUuid));
        testCaseHelper.scheduleTestCase(new TestResult().setUuid(testCaseUuid));
        testCaseHelper.startTestCase(testCaseUuid);

        fixtureHelper.startPrepareFixtureEachTest(
                classContainerUuid,
                fixtureUuid,
                new FixtureResult().setParent(testCaseUuid)
        );
        assertEquals(fixtureUuid, threadContext.getCurrent().orElse(null));
        assertEquals(testCaseUuid, threadContext.getRoot().orElse(null));

        fixtureHelper.stopFixture(fixtureUuid);
        assertEquals(testCaseUuid, threadContext.getRoot().orElse(null));
        assertEquals(testCaseUuid, threadContext.getCurrent().orElse(null));

        stepHelper.startStep(testCaseUuid, stepUuid, new StepResult().setTitle("Given something"));
        assertEquals(stepUuid, threadContext.getCurrent().orElse(null));
        assertEquals(testCaseUuid, threadContext.getRoot().orElse(null));

        AtomicReference<String> updatedMessage = new AtomicReference<>();
        testCaseHelper.updateTestCase(result -> updatedMessage.set("ok"));
        assertEquals("ok", updatedMessage.get());
        assertTrue(storage.getTestResult(testCaseUuid).isPresent());
    }

    private static Properties enabledProps() {
        Properties p = new Properties();
        p.setProperty(AppProperties.ADAPTER_MODE, "0");
        p.setProperty(AppProperties.TMS_INTEGRATION, "true");
        return p;
    }
}
