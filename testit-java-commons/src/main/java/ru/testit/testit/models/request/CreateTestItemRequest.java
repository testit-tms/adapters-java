package ru.testit.testit.models.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import ru.testit.models.Outcome;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTestItemRequest
{
    private String id;
    private String externalId;
    private String projectId;
    private String name;
    private String nameSpace;
    private String className;
    private List<InnerItem> steps;
    private List<InnerItem> setUp;
    private List<InnerItem> tearDown;
    private String title;
    private String description;
    private List<InnerLink> links;
    private List<Label> labels;
    @JsonIgnore
    private String testPlanId;
    @JsonIgnore
    private Outcome outcome;
    
    public CreateTestItemRequest() {
        this.steps = new LinkedList<InnerItem>();
        this.setUp = new LinkedList<InnerItem>();
        this.tearDown = new LinkedList<InnerItem>();
        this.links = new LinkedList<InnerLink>();
        this.labels = new LinkedList<Label>();
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public String getExternalId() {
        return this.externalId;
    }
    
    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }
    
    public String getProjectId() {
        return this.projectId;
    }
    
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getNameSpace() {
        return this.nameSpace;
    }
    
    public void setNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(final String className) {
        this.className = className;
    }
    
    public List<InnerItem> getSteps() {
        return this.steps;
    }
    
    public void setSteps(final List<InnerItem> steps) {
        this.steps = steps;
    }
    
    public List<InnerItem> getSetUp() {
        return this.setUp;
    }
    
    public void setSetUp(final List<InnerItem> setUp) {
        this.setUp = setUp;
    }
    
    public List<InnerItem> getTearDown() {
        return this.tearDown;
    }
    
    public void setTearDown(final List<InnerItem> tearDown) {
        this.tearDown = tearDown;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getTestPlanId() {
        return this.testPlanId;
    }
    
    public void setTestPlanId(final String testPlanId) {
        this.testPlanId = testPlanId;
    }
    
    public Outcome getOutcome() {
        return this.outcome;
    }
    
    public void setOutcome(final Outcome outcome) {
        this.outcome = outcome;
    }
    
    public List<InnerLink> getLinks() {
        return this.links;
    }
    
    public void setLinks(final List<InnerLink> links) {
        this.links = links;
    }
    
    public List<Label> getLabels() {
        return this.labels;
    }
    
    public void setLabels(final List<Label> labels) {
        this.labels = labels;
    }
}
