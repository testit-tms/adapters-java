package ru.testit.writers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.testit.clients.ApiClient;
import ru.testit.clients.ClientConfiguration;
import ru.testit.invoker.ApiException;
import ru.testit.model.*;
import ru.testit.models.*;
import ru.testit.services.ResultStorage;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class HttpWriterTest {
    private final static String TEST_RUN_ID = "5819479d-e38b-40d0-9e35-c5b2dab50158";

    private ClientConfiguration config;
    private ApiClient client;
    private ResultStorage storage;

    @BeforeEach
    void init() {
        this.client = Mockito.mock(ApiClient.class);
        this.config = Mockito.mock(ClientConfiguration.class);
        this.storage = Mockito.mock(ResultStorage.class);

        when(config.getUrl()).thenReturn("https://example.test/");
        when(config.getProjectId()).thenReturn("d7defd1e-c1ed-400d-8be8-091ebfdda744");
        when(config.getConfigurationId()).thenReturn("b09d7164-d58c-41a5-9780-89c30e0cc0c7");
        when(config.getPrivateToken()).thenReturn("QwertyT0kentPrivate");
        when(config.getTestRunId()).thenReturn(TEST_RUN_ID);
    }

    @Test
    void startLaunch_WithTestRunId_NoInvokeCreateHandler() throws ApiException {
        // arrange
        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.startLaunch();

        // assert
        verify(client, never()).createTestRun(new TestRunV2PostShortModel());
    }

    @Test
    void startLaunch_WithoutTestRunId_InvokeCreateHandler() throws ApiException {
        // arrange
        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(config.getProjectId()));

        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setId(UUID.fromString(TEST_RUN_ID));

        when(client.createTestRun(model)).thenReturn(response);
        when(config.getTestRunId()).thenReturn("null");

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.startLaunch();

        // assert
        verify(client, times(1)).createTestRun(model);
        verify(config, times(1)).setTestRunId(TEST_RUN_ID);
    }

    @Test
    void finishLaunch_WithCompletedTestRun_NoInvokeCompleteHandler() throws ApiException {
        // arrange
        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setStateName(TestRunStateTypeModel.COMPLETED);

        when(client.getTestRun(TEST_RUN_ID)).thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.finishLaunch();

        // assert
        verify(client, never()).completeTestRun(anyString());
    }

    @Test
    void finishLaunch_WithInProgressTestRun_InvokeCompleteHandler() throws ApiException {
        // arrange
        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setStateName(TestRunStateTypeModel.INPROGRESS);

        when(client.getTestRun(TEST_RUN_ID)).thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.finishLaunch();

        // assert
        verify(client, times(1)).completeTestRun(TEST_RUN_ID);
    }

    @Test
    void writeTest_WithExistingAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        StepResult stepResult = Helper.generateStepResult();

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);
        when(storage.getStep(testResult.getSteps().get(0)))
                .thenReturn(Optional.of(stepResult));

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
        StepResult stepResult = Helper.generateStepResult();

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);
        when(storage.getStep(testResult.getSteps().get(0)))
                .thenReturn(Optional.of(stepResult));

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).createAutoTest(request);
    }

    @Test
    void writeClass_WithoutAutoTest_NoInvokeUpdateHandler() throws ApiException {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();

        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);

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

        StepResult stepResult = Helper.generateStepResult();
        FixtureResult fixtureResultBeforeEach = Helper.generateBeforeEachFixtureResult();
        FixtureResult fixtureResultAfterEach = Helper.generateAfterEachFixtureResult();

        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);
        when(storage.getStep(testResult.getSteps().get(0)))
                .thenReturn(Optional.of(stepResult));
        when(storage.getFixture(container.getBeforeEachTest().get(0)))
                .thenReturn(Optional.of(fixtureResultBeforeEach));
        when(storage.getFixture(container.getAfterEachTest().get(0)))
                .thenReturn(Optional.of(fixtureResultAfterEach));

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

        when(storage.getClassContainer(classContainer.getUuid()))
                .thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTests(container);

        // assert
        verify(client, never()).updateAutoTest(any(AutoTestPutModel.class));
        verify(client, times(1)).sendTestResults(eq(TEST_RUN_ID), any());
    }

    @Test
    void writeTests_WithAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        ClassContainer classContainer = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        response.getSetup().add(Helper.generateBeforeEachSetup());
        response.getTeardown().add(Helper.generateAfterEachSetup());

        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.getSetup().add(Helper.generateBeforeAllSetup());
        request.getSetup().add(Helper.generateBeforeEachSetup());
        request.getTeardown().add(Helper.generateAfterAllSetup());
        request.getTeardown().add(Helper.generateAfterEachSetup());

        StepResult stepResult = Helper.generateStepResult();
        FixtureResult fixtureResultBeforeEach = Helper.generateBeforeEachFixtureResult();
        FixtureResult fixtureResultAfterEach = Helper.generateAfterEachFixtureResult();
        FixtureResult fixtureResultBeforeAll = Helper.generateBeforeAllFixtureResult();
        FixtureResult fixtureResultAfterAll = Helper.generateAfterAllFixtureResult();

        when(storage.getClassContainer(classContainer.getUuid()))
                .thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);
        when(storage.getStep(testResult.getSteps().get(0)))
                .thenReturn(Optional.of(stepResult));
        when(storage.getFixture(classContainer.getBeforeEachTest().get(0)))
                .thenReturn(Optional.of(fixtureResultBeforeEach));
        when(storage.getFixture(classContainer.getAfterEachTest().get(0)))
                .thenReturn(Optional.of(fixtureResultAfterEach));
        when(storage.getFixture(container.getBeforeMethods().get(0)))
                .thenReturn(Optional.of(fixtureResultBeforeAll));
        when(storage.getFixture(container.getAfterMethods().get(0)))
                .thenReturn(Optional.of(fixtureResultAfterAll));

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTests(container);

        // assert
        verify(client, times(1)).updateAutoTest(request);
        verify(client, times(1)).sendTestResults(eq(TEST_RUN_ID), any());
    }
}