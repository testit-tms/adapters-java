package ru.testit.services;

import ru.testit.aspects.StepAspect;
import ru.testit.models.ItemStatus;
import ru.testit.models.Outcome;
import ru.testit.models.StepNode;
import ru.testit.models.TestMethod;
import ru.testit.properties.AppProperties;
import ru.testit.tms.client.ITMSClient;
import ru.testit.tms.client.TMSClient;
import ru.testit.tms.models.config.ClientConfiguration;

import java.util.*;

public class TMSService {
    private ITMSClient tmsClient;
    private CreateTestItemRequestFactory createTestItemRequestFactory;
    private TestResultRequestFactory testResultRequestFactory;
    private LinkedHashMap<TestMethodType, StepNode> utilsMethodSteps;
    private HashMap<String, StepNode> includedTests;
    private List<String> alreadyFinished;
    private ClientConfiguration config;

    public TMSService() {
        this.createTestItemRequestFactory = new CreateTestItemRequestFactory();
        this.testResultRequestFactory = new TestResultRequestFactory();
        this.utilsMethodSteps = new LinkedHashMap<TestMethodType, StepNode>();
        this.includedTests = new HashMap<String, StepNode>();
        this.alreadyFinished = new LinkedList<String>();
        Properties appProperties = AppProperties.loadProperties();
        this.config = new ClientConfiguration(appProperties);
        this.tmsClient = new TMSClient(config);
    }

    public void startLaunch() {
        if (this.config.getTestRunId() != "null") {
            return;
        }
        this.tmsClient.startLaunch();
    }

    public void finishLaunch() {
        this.createTestItemRequestFactory.processFinishLaunch(this.utilsMethodSteps, this.includedTests);
        this.tmsClient.sendTestItems(this.createTestItemRequestFactory.getCreateTestRequests());
        this.testResultRequestFactory.processFinishLaunch(this.utilsMethodSteps, this.includedTests);
        this.tmsClient.finishLaunch(this.testResultRequestFactory.getTestResultRequest());
    }

    public void startTestMethod(final TestMethod method) {
        this.createTestItemRequestFactory.processTest(method);
        final StepNode parentStep = new StepNode();
        parentStep.setTitle(method.getTitle());
        parentStep.setDescription(method.getDescription());
        parentStep.setStartedOn(new Date());
        this.includedTests.put(method.getExternalId(), parentStep);
        StepAspect.setStepNodes(parentStep);
    }

    public void finishTestMethod(ItemStatus status, final TestMethod method) {
        if (this.alreadyFinished.contains(method.getExternalId())) {
            return;
        }
        final StepNode parentStep = this.includedTests.get(method.getExternalId());
        if (parentStep != null) {
            parentStep.setOutcome((status == ItemStatus.PASSED) ? Outcome.PASSED.getValue() : Outcome.FAILED.getValue());
            parentStep.setFailureReason(method.getThrowable());
            parentStep.setCompletedOn(new Date());
        }
        this.alreadyFinished.add(method.getExternalId());
    }

    public void startUtilMethod(final TestMethodType currentMethod, final TestMethod method) {
        final StepNode parentStep = new StepNode();
        parentStep.setTitle(method.getTitle());
        parentStep.setDescription(method.getDescription());
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
