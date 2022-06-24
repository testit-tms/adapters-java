package ru.testit.services;

import org.testng.ITestResult;
import ru.testit.annotations.Description;
import ru.testit.annotations.ExternalId;
import ru.testit.annotations.Title;
import ru.testit.aspects.StepAspect;
import ru.testit.models.Outcome;
import ru.testit.models.StepNode;
import ru.testit.properties.AppProperties;
import ru.testit.testit.client.TestITClient;
import ru.testit.testit.models.config.ClientConfiguration;

import java.lang.reflect.Method;
import java.util.*;

public class TestItService {
    private TestITClient testITClient;
    private CreateTestItemRequestFactory createTestItemRequestFactory;
    private TestResultRequestFactory testResultRequestFactory;
    private LinkedHashMap<TestMethodType, StepNode> utilsMethodSteps;
    private HashMap<String, StepNode> includedTests;
    private List<String> alreadyFinished;
    private AppProperties appProperties;

    public TestItService() {
        this.createTestItemRequestFactory = new CreateTestItemRequestFactory();
        this.testResultRequestFactory = new TestResultRequestFactory();
        this.utilsMethodSteps = new LinkedHashMap<TestMethodType, StepNode>();
        this.includedTests = new HashMap<String, StepNode>();
        this.alreadyFinished = new LinkedList<String>();
        this.appProperties = new AppProperties();
        ClientConfiguration config = new ClientConfiguration(appProperties.getPrivateToken(),
                appProperties.getProjectID(),
                appProperties.getUrl(),
                appProperties.getConfigurationId(),
                appProperties.getTestRunId());
        this.testITClient = new TestITClient(config);
    }

    public void startLaunch() {
        if (this.appProperties.getTestRunId() != "null") {
            return;
        }
        this.testITClient.startLaunch();
    }

    public void finishLaunch() {
        this.createTestItemRequestFactory.processFinishLaunch(this.utilsMethodSteps, this.includedTests);
        this.testITClient.sendTestItems(this.createTestItemRequestFactory.getCreateTestRequests());
        this.testResultRequestFactory.processFinishLaunch(this.utilsMethodSteps, this.includedTests);
        this.testITClient.finishLaunch(this.testResultRequestFactory.getTestResultRequest());
    }

    public void startTestMethod(ITestResult testResult) {
        Method m = testResult.getMethod().getConstructorOrMethod().getMethod();
        this.createTestItemRequestFactory.processTest(m);
        final StepNode parentStep = new StepNode();
        parentStep.setTitle(this.extractTitle(m));
        parentStep.setDescription(this.extractDescription(m));
        parentStep.setStartedOn(new Date());
        this.includedTests.put(this.extractExternalID(m), parentStep);
        StepAspect.setStepNodes(parentStep);
    }

    public void finishTestMethod(ItemStatus status, ITestResult testResult) {
        Method m = testResult.getMethod().getConstructorOrMethod().getMethod();
        final String externalId = this.extractExternalID(m);
        if (this.alreadyFinished.contains(externalId)) {
            return;
        }
        final StepNode parentStep = this.includedTests.get(externalId);
        if (parentStep != null) {
            parentStep.setOutcome((status == ItemStatus.PASSED) ? Outcome.PASSED.getValue() : Outcome.FAILED.getValue());
            parentStep.setFailureReason(testResult.getThrowable());
            parentStep.setCompletedOn(new Date());
        }
        this.alreadyFinished.add(externalId);
    }

    private String extractDescription(final Method currentTest) {
        final Description annotation = currentTest.getAnnotation(Description.class);
        return (annotation != null) ? annotation.value() : null;
    }

    private String extractTitle(final Method currentTest) {
        final Title annotation = currentTest.getAnnotation(Title.class);
        return (annotation != null) ? annotation.value() : null;
    }

    private String extractExternalID(final Method currentTest) {
        final ExternalId annotation = currentTest.getAnnotation(ExternalId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public void startUtilMethod(final TestMethodType currentMethod, final Method method) {
        final StepNode parentStep = new StepNode();
        parentStep.setTitle(this.extractTitle(method));
        parentStep.setDescription(this.extractDescription(method));
        parentStep.setStartedOn(new Date());
        this.utilsMethodSteps.putIfAbsent(currentMethod, parentStep);
        StepAspect.setStepNodes(parentStep);
    }

    public void finishUtilMethod(final TestMethodType currentMethod, final Throwable thrown) {
        final StepNode parentStep = this.utilsMethodSteps.get(currentMethod);
        parentStep.setOutcome((thrown == null) ? Outcome.PASSED.getValue() : Outcome.FAILED.getValue());
        parentStep.setCompletedOn(new Date());
        if (currentMethod == TestMethodType.BEFORE_METHOD) {
            StepAspect.returnStepNode();
        }
    }
}
