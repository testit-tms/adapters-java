package ru.testit.services.core;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ru.testit.listener.ListenerManager;
import ru.testit.models.ItemStatus;
import ru.testit.models.TestResult;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AppProperties;
import ru.testit.services.ResultStorage;
import ru.testit.services.ThreadContext;
import ru.testit.syncstorage.SyncStorageService;
import ru.testit.writers.Writer;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdapterTestCaseHelperStopTest {

    @Test
    void stopTestCase_afterSync_writesInProgressToTestIt_keepsFinalLocally() {
        AdapterConfig config = new AdapterConfig(enabledProps());
        ResultStorage storage = new ResultStorage();
        Writer writer = mock(Writer.class);
        SyncStorageService sync = mock(SyncStorageService.class);
        when(sync.sendInProgressIfNeeded(any())).thenReturn(true);
        when(writer.writeTestRealtime(any())).thenAnswer(invocation -> {
            TestResult sent = invocation.getArgument(0);
            assertEquals(ItemStatus.INPROGRESS, sent.getItemStatus());
            return true;
        });

        AdapterTestCaseHelper helper = new AdapterTestCaseHelper(
                config,
                new ThreadContext(),
                storage,
                new ListenerManager(Collections.emptyList()),
                writer,
                sync,
                LoggerFactory.getLogger(AdapterTestCaseHelperStopTest.class)
        );

        TestResult result = new TestResult()
                .setUuid("u1")
                .setExternalId("ext-1")
                .setItemStatus(ItemStatus.PASSED);
        helper.scheduleTestCase(result);
        helper.startTestCase("u1");
        helper.stopTestCase("u1");

        verify(sync).sendInProgressIfNeeded(result);
        verify(writer).writeTestRealtime(result);
        verify(writer, never()).writeTest(any());
        assertEquals(ItemStatus.PASSED, result.getItemStatus());
    }

    @Test
    void stopTestCase_syncSkipped_fallsBackToWriteTest() {
        AdapterConfig config = new AdapterConfig(enabledProps());
        ResultStorage storage = new ResultStorage();
        Writer writer = mock(Writer.class);
        SyncStorageService sync = mock(SyncStorageService.class);
        when(sync.sendInProgressIfNeeded(any())).thenReturn(false);

        AdapterTestCaseHelper helper = new AdapterTestCaseHelper(
                config,
                new ThreadContext(),
                storage,
                new ListenerManager(Collections.emptyList()),
                writer,
                sync,
                LoggerFactory.getLogger(AdapterTestCaseHelperStopTest.class)
        );

        TestResult result = new TestResult()
                .setUuid("u2")
                .setExternalId("ext-2")
                .setItemStatus(ItemStatus.FAILED);
        helper.scheduleTestCase(result);
        helper.startTestCase("u2");
        helper.stopTestCase("u2");

        verify(writer).writeTest(result);
        verify(writer, never()).writeTestRealtime(any());
        assertEquals(ItemStatus.FAILED, result.getItemStatus());
    }

    private static Properties enabledProps() {
        Properties p = new Properties();
        p.setProperty(AppProperties.ADAPTER_MODE, "0");
        p.setProperty(AppProperties.TMS_INTEGRATION, "true");
        return p;
    }
}
