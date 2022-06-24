package ru.testit.testit.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestResultRequest
{
    @JsonProperty("configurationId")
    private String configurationId;
    @JsonProperty("failureReasonName")
    private String failureReasonName;
    @JsonProperty("autoTestExternalId")
    private String autoTestExternalId;
    @JsonProperty("outcome")
    private String outcome;
    @JsonProperty("message")
    private String message;
    @JsonProperty("traces")
    private String traces;
    @JsonProperty("startedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date startedOn;
    @JsonProperty("completedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date completedOn;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("stepResults")
    private List<InnerResult> stepResults;
    @JsonProperty("setupResults")
    private List<InnerResult> setupResults;
    @JsonProperty("teardownResults")
    private List<InnerResult> teardownResults;
    @JsonProperty("links")
    private List<InnerLink> links;
    
    public TestResultRequest() {
        this.stepResults = new LinkedList<InnerResult>();
        this.setupResults = new LinkedList<InnerResult>();
        this.teardownResults = new LinkedList<InnerResult>();
        this.links = new LinkedList<InnerLink>();
    }
    
    public String getConfigurationId() {
        return this.configurationId;
    }
    
    public void setConfigurationId(final String configurationId) {
        this.configurationId = configurationId;
    }
    
    public String getFailureReasonName() {
        return this.failureReasonName;
    }
    
    public void setFailureReasonName(final String failureReasonName) {
        this.failureReasonName = failureReasonName;
    }
    
    public String getAutoTestExternalId() {
        return this.autoTestExternalId;
    }
    
    public void setAutoTestExternalId(final String autoTestExternalId) {
        this.autoTestExternalId = autoTestExternalId;
    }
    
    public String getOutcome() {
        return this.outcome;
    }
    
    public void setOutcome(final String outcome) {
        this.outcome = outcome;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(final String message) {
        this.message = message;
    }
    
    public String getTraces() {
        return this.traces;
    }
    
    public void setTraces(final String traces) {
        this.traces = traces;
    }
    
    public Date getStartedOn() {
        return this.startedOn;
    }
    
    public void setStartedOn(final Date startedOn) {
        this.startedOn = startedOn;
    }
    
    public Date getCompletedOn() {
        return this.completedOn;
    }
    
    public void setCompletedOn(final Date completedOn) {
        this.completedOn = completedOn;
    }
    
    public Integer getDuration() {
        return this.duration;
    }
    
    public void setDuration(final Integer duration) {
        this.duration = duration;
    }
    
    public List<InnerResult> getStepResults() {
        return this.stepResults;
    }
    
    public void setStepResults(final List<InnerResult> stepResults) {
        this.stepResults = stepResults;
    }
    
    public List<InnerResult> getSetupResults() {
        return this.setupResults;
    }
    
    public void setSetupResults(final List<InnerResult> setupResults) {
        this.setupResults = setupResults;
    }
    
    public List<InnerResult> getTeardownResults() {
        return this.teardownResults;
    }
    
    public void setTeardownResults(final List<InnerResult> teardownResults) {
        this.teardownResults = teardownResults;
    }
    
    public List<InnerLink> getLinks() {
        return this.links;
    }
    
    public void setLinks(final List<InnerLink> links) {
        this.links = links;
    }
}
