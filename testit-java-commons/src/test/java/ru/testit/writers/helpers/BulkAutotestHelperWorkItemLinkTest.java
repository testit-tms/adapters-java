package ru.testit.writers.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.testit.adaptersapi.model.AutoTestCreateApiModel;
import ru.testit.adaptersapi.model.AutoTestResultsForTestRunModel;
import ru.testit.adaptersapi.model.AutoTestUpdateApiModel;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.ITmsApiClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkAutotestHelperWorkItemLinkTest {

    private static final String RUN_ID = "5819479d-e38b-40d0-9e35-c5b2dab50158";

    private ITmsApiClient apiClient;
    private ClientConfiguration config;
    private BulkAutotestHelper helper;

    @BeforeEach
    void setUp() throws Exception {
        apiClient = mock(ITmsApiClient.class);
        config = mock(ClientConfiguration.class);
        when(config.getTestRunId()).thenReturn(RUN_ID);
        when(config.shouldAutomaticUpdationLinksToTestCases()).thenReturn(false);
        when(apiClient.getWorkItemsLinkedToTest(anyString())).thenReturn(Collections.emptyList());
        when(apiClient.sendTestResults(anyString(), anyList())).thenReturn(Collections.emptyList());
        helper = new BulkAutotestHelper(apiClient, config);
    }

    @Test
    void teardown_threeCreatesWithDifferentWorkItems_linksEachAutotest() throws Exception {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

        when(apiClient.createAutoTest(any(AutoTestCreateApiModel.class))).thenAnswer(inv -> {
            AutoTestCreateApiModel m = inv.getArgument(0);
            switch (m.getExternalId()) {
                case "wi_link_1":
                    return id1.toString();
                case "wi_link_2":
                    return id2.toString();
                case "wi_link_3":
                    return id3.toString();
                default:
                    return UUID.randomUUID().toString();
            }
        });

        helper.addForCreate(createModel("wi_link_1"), result("wi_link_1"), Collections.singletonList("676359"));
        helper.addForCreate(createModel("wi_link_2"), result("wi_link_2"), Collections.singletonList("676360"));
        helper.addForCreate(createModel("wi_link_3"), result("wi_link_3"), Collections.singletonList("676361"));
        helper.teardown();

        verify(apiClient, times(3)).createAutoTest(any(AutoTestCreateApiModel.class));
        verify(apiClient).linkAutoTestToWorkItems(eq(id1.toString()), eq(Collections.singletonList("676359")));
        verify(apiClient).linkAutoTestToWorkItems(eq(id2.toString()), eq(Collections.singletonList("676360")));
        verify(apiClient).linkAutoTestToWorkItems(eq(id3.toString()), eq(Collections.singletonList("676361")));
    }

    @Test
    void teardown_updateUnchangedWithWorkItems_stillLinks() throws Exception {
        String autoTestUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        helper.addTestRunResultOnly(result("existing"));
        helper.queueWorkItemLinks(autoTestUuid, Collections.singletonList("676359"));
        helper.teardown();

        verify(apiClient, never()).updateAutoTest(any(AutoTestUpdateApiModel.class));
        verify(apiClient).linkAutoTestToWorkItems(eq(autoTestUuid), eq(Collections.singletonList("676359")));
    }

    @Test
    void teardown_updateWithWorkItems_linksByUuid() throws Exception {
        String autoTestUuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        AutoTestUpdateApiModel update = new AutoTestUpdateApiModel();
        update.setExternalId("existing");
        update.setProjectId(UUID.randomUUID());
        update.setName("existing");

        Map<String, List<String>> wi = new HashMap<>();
        wi.put(autoTestUuid, Collections.singletonList("676360"));

        helper.addForUpdate(update, result("existing"), wi);
        helper.teardown();

        ArgumentCaptor<Iterable<String>> wiCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(apiClient).updateAutoTest(any(AutoTestUpdateApiModel.class));
        verify(apiClient).linkAutoTestToWorkItems(eq(autoTestUuid), wiCaptor.capture());
        assertTrue(wiCaptor.getValue().iterator().hasNext());
        assertEquals("676360", wiCaptor.getValue().iterator().next());
    }

    private static AutoTestCreateApiModel createModel(String externalId) {
        AutoTestCreateApiModel model = new AutoTestCreateApiModel();
        model.setExternalId(externalId);
        model.setName(externalId);
        model.setProjectId(UUID.randomUUID());
        return model;
    }

    private static AutoTestResultsForTestRunModel result(String externalId) {
        AutoTestResultsForTestRunModel model = new AutoTestResultsForTestRunModel();
        model.setAutoTestExternalId(externalId);
        model.setConfigurationId(UUID.randomUUID());
        return model;
    }
}
