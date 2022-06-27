package ru.testit.services;

import ru.testit.models.Outcome;
import ru.testit.models.StepNode;
import ru.testit.models.TestMethod;
import ru.testit.testit.client.TestITClient;
import ru.testit.testit.models.request.CreateTestItemRequest;
import ru.testit.testit.models.request.InnerItem;

import java.util.*;

public class CreateTestItemRequestFactory
{
    private Map<String, CreateTestItemRequest> createTestItemRequests;
    
    public CreateTestItemRequestFactory() {
        this.createTestItemRequests = new HashMap<String, CreateTestItemRequest>();
    }
    
    public void processTest(final TestMethod method) {
        final CreateTestItemRequest createTestItemRequest = new CreateTestItemRequest();

        createTestItemRequest.setExternalId(method.getExternalId());
        createTestItemRequest.setProjectId(TestITClient.getProjectID());
        createTestItemRequest.setName(method.getDisplayName());
        createTestItemRequest.setClassName(method.getClassName());
        createTestItemRequest.setNameSpace(method.getSpaceName());
        createTestItemRequest.setTestPlanId(method.getWorkItemId());
        createTestItemRequest.setLinks(method.getLinks());
        createTestItemRequest.setLabels(method.getLabels());

        this.createTestItemRequests.put(method.getExternalId(), createTestItemRequest);
    }
    
    public void processFinishLaunch(final HashMap<TestMethodType, StepNode> utilsMethodSteps, final HashMap<String, StepNode> includedTests) {
        for (final String externalId : this.createTestItemRequests.keySet()) {
            final CreateTestItemRequest createTestItemRequest = this.createTestItemRequests.get(externalId);
            final StepNode testParentStepNode = includedTests.get(externalId);
            createTestItemRequest.setOutcome(Outcome.getByValue(testParentStepNode.getOutcome()));
            this.processTestSteps(createTestItemRequest, testParentStepNode);
            this.processUtilsSteps(createTestItemRequest, utilsMethodSteps);
        }
    }
    
    private void processTestSteps(final CreateTestItemRequest createTestItemRequest, final StepNode parentStep) {
        createTestItemRequest.setTitle(parentStep.getTitle());
        createTestItemRequest.setDescription(parentStep.getDescription());
        this.processStep(parentStep.getChildren(), createTestItemRequest.getSteps());
    }
    
    private void processUtilsSteps(final CreateTestItemRequest createTestItemRequest, final HashMap<TestMethodType, StepNode> utilsMethodSteps) {
        for (final TestMethodType methodType : utilsMethodSteps.keySet()) {
            if (methodType == TestMethodType.BEFORE_CLASS || methodType == TestMethodType.BEFORE_METHOD) {
                this.processSetUpSteps(createTestItemRequest, utilsMethodSteps.get(methodType));
            }
            else {
                this.processTearDownSteps(createTestItemRequest, utilsMethodSteps.get(methodType));
            }
        }
    }
    
    private void processSetUpSteps(final CreateTestItemRequest createTestItemRequest, final StepNode stepNode) {
        final InnerItem setUp = new InnerItem();
        setUp.setTitle(stepNode.getTitle());
        setUp.setDescription(stepNode.getDescription());
        this.processStep(stepNode.getChildren(), setUp.getSteps());
        createTestItemRequest.getSetUp().add(setUp);
    }
    
    private void processTearDownSteps(final CreateTestItemRequest createTestItemRequest, final StepNode stepNode) {
        final InnerItem tearDown = new InnerItem();
        tearDown.setTitle(stepNode.getTitle());
        tearDown.setDescription(stepNode.getDescription());
        this.processStep(stepNode.getChildren(), tearDown.getSteps());
        createTestItemRequest.getTearDown().add(tearDown);
    }
    
    private void processStep(final List<StepNode> childrens, final List<InnerItem> steps) {
        for (final StepNode children : childrens) {
            final InnerItem step = new InnerItem();
            step.setTitle(children.getTitle());
            step.setDescription(children.getDescription());
            steps.add(step);
            if (!children.getChildren().isEmpty()) {
                this.processStep(children.getChildren(), step.getSteps());
            }
        }
    }

    public Collection<CreateTestItemRequest> getCreateTestRequests() {
        return this.createTestItemRequests.values();
    }
}
