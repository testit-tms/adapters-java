package ru.testit.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ru.testit.adaptersapi.model.AttachmentApiResult;
import ru.testit.adaptersapi.model.LinkApiResult;
import ru.testit.adaptersapi.model.LinkType;
import ru.testit.adaptersapi.model.TestRunApiResult;
import ru.testit.adaptersapi.model.UpdateEmptyTestRunApiModel;
import ru.testit.clients.ClientConfiguration;
import ru.testit.clients.ITmsApiClient;
import ru.testit.properties.AdapterConfig;
import ru.testit.properties.AppProperties;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdapterStartupHelperTestRunMetaTest {

    private static final String RUN_ID = "5819479d-e38b-40d0-9e35-c5b2dab50158";

    private ITmsApiClient client;
    private AdapterConfig adapterConfig;

    @BeforeEach
    void setUp() {
        client = mock(ITmsApiClient.class);
        Properties p = new Properties();
        p.setProperty(AppProperties.TMS_INTEGRATION, "true");
        adapterConfig = new AdapterConfig(p);
    }

    @Test
    void startTests_mergesTagsAndLinksEarly() throws Exception {
        ClientConfiguration config = configWith(
                RUN_ID,
                null,
                "smoke,nightly",
                "[{\"url\":\"https://ci.example/job/1\",\"title\":\"Job\"}]"
        );

        TestRunApiResult existing = new TestRunApiResult();
        existing.setId(UUID.fromString(RUN_ID));
        existing.setName("Old");
        existing.setDescription("Keep me");
        existing.setLaunchSource("CI");
        existing.setTags(Collections.singletonList("smoke"));
        LinkApiResult oldLink = new LinkApiResult();
        oldLink.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        oldLink.setUrl("https://existing.example");
        oldLink.setType(LinkType.RELATED);
        existing.setLinks(Collections.singletonList(oldLink));
        AttachmentApiResult oldAttachment = new AttachmentApiResult();
        oldAttachment.setId(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        oldAttachment.setFileId("file-1");
        oldAttachment.setType("text/plain");
        oldAttachment.setSize(1f);
        oldAttachment.setName("note.txt");
        existing.setAttachments(Collections.singletonList(oldAttachment));
        when(client.getTestRun(RUN_ID)).thenReturn(existing);

        AdapterStartupHelper helper = new AdapterStartupHelper(
                adapterConfig, config, client, LoggerFactory.getLogger(getClass()));
        helper.startTests();

        org.mockito.ArgumentCaptor<UpdateEmptyTestRunApiModel> captor =
                org.mockito.ArgumentCaptor.forClass(UpdateEmptyTestRunApiModel.class);
        verify(client).updateTestRun(captor.capture());
        UpdateEmptyTestRunApiModel model = captor.getValue();
        assertTrue(model.getTags().contains("smoke"));
        assertTrue(model.getTags().contains("nightly"));
        assertEquals(2, model.getLinks().size());
        assertEquals(1, model.getAttachments().size());
        assertEquals(oldAttachment.getId(), model.getAttachments().get(0).getId());
        assertEquals(oldLink.getId(), model.getLinks().get(0).getId());
        assertEquals("Keep me", model.getDescription());
        assertEquals("CI", model.getLaunchSource());
    }

    @Test
    void startTests_tagsOnly_keepsExistingLinksAndAttachments() throws Exception {
        ClientConfiguration config = configWith(RUN_ID, null, "nightly", null);

        TestRunApiResult existing = new TestRunApiResult();
        existing.setId(UUID.fromString(RUN_ID));
        existing.setName("Old");
        existing.setDescription("Keep me");
        existing.setLaunchSource("CI");
        existing.setTags(Collections.singletonList("smoke"));
        LinkApiResult oldLink = new LinkApiResult();
        oldLink.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        oldLink.setUrl("https://existing.example");
        oldLink.setType(LinkType.RELATED);
        existing.setLinks(Collections.singletonList(oldLink));
        AttachmentApiResult oldAttachment = new AttachmentApiResult();
        oldAttachment.setId(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        oldAttachment.setFileId("file-1");
        oldAttachment.setType("text/plain");
        oldAttachment.setSize(1f);
        oldAttachment.setName("note.txt");
        existing.setAttachments(Collections.singletonList(oldAttachment));
        when(client.getTestRun(RUN_ID)).thenReturn(existing);

        AdapterStartupHelper helper = new AdapterStartupHelper(
                adapterConfig, config, client, LoggerFactory.getLogger(getClass()));
        helper.startTests();

        org.mockito.ArgumentCaptor<UpdateEmptyTestRunApiModel> captor =
                org.mockito.ArgumentCaptor.forClass(UpdateEmptyTestRunApiModel.class);
        verify(client).updateTestRun(captor.capture());
        UpdateEmptyTestRunApiModel model = captor.getValue();
        assertEquals(1, model.getLinks().size());
        assertEquals(1, model.getAttachments().size());
        assertEquals("https://existing.example", model.getLinks().get(0).getUrl());
        assertEquals("Keep me", model.getDescription());
        assertEquals("CI", model.getLaunchSource());
    }

    @Test
    void startTests_noMeta_noUpdate() throws Exception {
        ClientConfiguration config = configWith(RUN_ID, null, null, null);
        AdapterStartupHelper helper = new AdapterStartupHelper(
                adapterConfig, config, client, LoggerFactory.getLogger(getClass()));
        helper.startTests();
        verify(client, never()).getTestRun(any());
        verify(client, never()).updateTestRun(any());
    }

    private static ClientConfiguration configWith(
            String runId, String name, String tags, String links
    ) {
        Properties p = new Properties();
        p.setProperty(AppProperties.URL, "https://example.test");
        p.setProperty(AppProperties.PRIVATE_TOKEN, "token");
        p.setProperty(AppProperties.PROJECT_ID, "d7defd1e-c1ed-400d-8be8-091ebfdda744");
        p.setProperty(AppProperties.CONFIGURATION_ID, "b09d7164-d58c-41a5-9780-89c30e0cc0c7");
        p.setProperty(AppProperties.TEST_RUN_ID, runId);
        if (name != null) {
            p.setProperty(AppProperties.TEST_RUN_NAME, name);
        }
        if (tags != null) {
            p.setProperty(AppProperties.TEST_RUN_TAGS, tags);
        }
        if (links != null) {
            p.setProperty(AppProperties.TEST_RUN_LINKS, links);
        }
        return new ClientConfiguration(p);
    }
}
