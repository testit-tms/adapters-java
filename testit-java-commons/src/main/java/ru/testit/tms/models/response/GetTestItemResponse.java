package ru.testit.tms.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.testit.models.Outcome;
import ru.testit.tms.models.request.InnerItem;
import ru.testit.tms.models.request.Label;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTestItemResponse
{
    @JsonProperty("globalId")
    private Integer globalId;
    @JsonProperty("id")
    private String id;
    @JsonProperty("externalId")
    private String externalId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("projectId")
    private String projectId;
    @JsonProperty("namespace")
    private String nameSpace;
    @JsonProperty("classname")
    private String className;
    @JsonProperty("steps")
    private List<InnerItem> steps;
    @JsonProperty("setup")
    private List<InnerItem> setUp;
    @JsonProperty("teardown")
    private List<InnerItem> tearDown;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("labels")
    private List<Label> labels;
    @JsonIgnore
    private String testPlanId;
    @JsonIgnore
    private Outcome outcome;
    
    public Integer getGlobalId() {
        return this.globalId;
    }
    
    public void setGlobalId(final Integer globalId) {
        this.globalId = globalId;
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
    
    public List<Label> getLabels() {
        return this.labels;
    }
    
    public void setLabels(final List<Label> labels) {
        this.labels = labels;
    }
}
