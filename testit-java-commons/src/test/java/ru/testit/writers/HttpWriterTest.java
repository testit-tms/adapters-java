package ru.testit.writers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.testit.Helper;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.model.AutoTestModel;
import ru.testit.client.model.AutoTestPostModel;
import ru.testit.client.model.LinkPutModel;
import ru.testit.client.model.AutoTestPutModel;
import ru.testit.clients.ApiClient;
import ru.testit.clients.ClientConfiguration;
import ru.testit.models.*;
import ru.testit.services.ResultStorage;

import java.util.*;

import static org.mockito.Mockito.*;

class HttpWriterTest {
    private final static String TEST_RUN_ID = "5819479d-e38b-40d0-9e35-c5b2dab50158";

    private ClientConfiguration config;
    private ApiClient client;
    private ResultStorage storage;


    @BeforeEach
    void init() {
        this.client = mock(ApiClient.class);
        this.config = mock(ClientConfiguration.class);
        this.storage = mock(ResultStorage.class);

        when(config.getUrl()).thenReturn("https://example.test/");
        when(config.getProjectId()).thenReturn("d7defd1e-c1ed-400d-8be8-091ebfdda744");
        when(config.getConfigurationId()).thenReturn("b09d7164-d58c-41a5-9780-89c30e0cc0c7");
        when(config.getPrivateToken()).thenReturn("QwertyT0kentPrivate");
        when(config.getTestRunId()).thenReturn(TEST_RUN_ID);
    }

    @Test
    void writeTest_WithExistingAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        List<UUID> uuids = Helper.generateListUuid();
        request.setId(null);

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).updateAutoTest(request);
    }

    @Test
    void writeTest_WithCreatingAutoTest_InvokeCreateHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestPostModel request = Helper.generateAutoTestPostModel(config.getProjectId());
        List<UUID> uuids = Helper.generateListUuid();

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(null);
        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).createAutoTest(request);
    }

    @Test
    void writeTest_WithWorkItemId_InvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        String autotestId = response.getId().toString();
        List<String> workItemGlobalId = testResult.getWorkItemId();
        List<UUID> uuids = Helper.generateListUuid();

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).tryLinkAutoTestToWorkItem(autotestId, Collections.singletonList(workItemGlobalId.get(0)));
    }

    @Test
    void writeTest_WithoutWorkItemId_NoInvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult().setWorkItemId(new ArrayList<>());
        List<UUID> uuids = Helper.generateListUuid();

        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, never()).tryLinkAutoTestToWorkItem(anyString(), Collections.singletonList(anyString()));
    }

    @Test
    void writeTest_FiledExistingAutoTest_NoInvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult().setItemStatus(ItemStatus.FAILED);
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        List<UUID> uuids = Helper.generateListUuid();

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, never()).tryLinkAutoTestToWorkItem(anyString(), Collections.singletonList(anyString()));
    }

    @Test
    void writeTest_FiledExistingAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        List<LinkItem> links = new ArrayList<>();
        LinkItem link = new LinkItem();
        link.setTitle("Title").setDescription("Description").setType(LinkType.DEFECT).setUrl("http://test.example/bug123");
        links.add(link);
        List<UUID> uuids = Helper.generateListUuid();

        TestResult testResult = Helper.generateTestResult().setItemStatus(ItemStatus.FAILED).setLinkItems(links);
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());

        List<LinkPutModel> putLinks = new ArrayList<>();
        LinkPutModel putLink = new LinkPutModel();
        putLink.setTitle("Title");
        putLink.setDescription("Description");
        putLink.setUrl("http://test.example/bug123");
        putLink.setType(ru.testit.client.model.LinkType.DEFECT);
        putLinks.add(putLink);

        AutoTestPutModel putModel = Helper.generateAutoTestPutModel(config.getProjectId());
        putModel.links(putLinks);

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.sendTestResults(any(), any())).thenReturn(uuids);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client).updateAutoTest(putModel);
    }

    @Test
    void writeTest_Failed_InvokeSendTestResult() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.setId(null);

        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.sendTestResults(any(), any())).thenThrow(new ApiException());

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).updateAutoTest(request);
    }

    @Test
    void writeClass_WithoutAutoTest_NoInvokeUpdateHandler() throws ApiException {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();

        when(storage.getTestResult(testResult.getUuid())).thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeClass(container);

        // assert
        verify(client, never()).updateAutoTest(any(AutoTestPutModel.class));
    }

    @Test
    void writeClass_WithAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.getSetup().add(Helper.generateBeforeEachSetup());
        request.getTeardown().add(Helper.generateAfterEachSetup());

        when(storage.getTestResult(testResult.getUuid())).thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeClass(container);

        // assert
        verify(client, times(1)).updateAutoTest(request);
    }

    @Test
    void writeTests_WithoutAutoTest_NoInvokeUpdateHandler() throws ApiException {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        ClassContainer classContainer = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();

        when(storage.getClassContainer(classContainer.getUuid())).thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid())).thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTests(container);

        // assert
        verify(client, never()).updateAutoTest(any(AutoTestPutModel.class));
        verify(client, never()).updateTestResult(any(), any());
    }

    @Test
    void writeTests_WithAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        UUID uuid = UUID.randomUUID();

        MainContainer container = Helper.generateMainContainer();
        ClassContainer classContainer = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        response.getSetup().add(Helper.generateBeforeEachSetup());
        response.getTeardown().add(Helper.generateAfterEachSetup());

        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.getSetup().add(Helper.generateBeforeAllSetup());
        request.getSetup().add(Helper.generateBeforeEachSetup());
        request.getTeardown().add(Helper.generateAfterEachSetup());
        request.getTeardown().add(Helper.generateAfterAllSetup());

        when(storage.getClassContainer(classContainer.getUuid())).thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid())).thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(testResult.getExternalId())).thenReturn(response);
        when(client.getTestResult(uuid)).thenReturn(Helper.generateTestResultModel());


        HttpWriter httpWriter = new HttpWriter(config, client, storage);
        httpWriter.addUuid(testResult.getUuid(), uuid);

        // act
        ((Writer) httpWriter).writeTests(container);

        // assert
        verify(client, times(1)).updateAutoTest(request);
        verify(client, times(1)).updateTestResult(any(), any());
    }

    @Test
    void writeAttachment_withValue_InvokeAddHandler() throws ApiException {
        // arrange
        Writer writer = new HttpWriter(config, client, storage);
        String path = "C:/test.txt";

        // act
        writer.writeAttachment(path);

        // assert
        verify(client).addAttachment(path);
    }
}