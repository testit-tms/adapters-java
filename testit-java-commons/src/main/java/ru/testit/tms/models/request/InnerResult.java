package ru.testit.tms.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class InnerResult
{
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("startedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date startedOn;
    @JsonProperty("completedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date completedOn;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("outcome")
    private String outcome;
    @JsonProperty("stepResults")
    private List<InnerResult> stepResults;
    
    public InnerResult() {
        this.stepResults = new LinkedList<InnerResult>();
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
    
    public String getOutcome() {
        return this.outcome;
    }
    
    public void setOutcome(final String outcome) {
        this.outcome = outcome;
    }
    
    public List<InnerResult> getStepResults() {
        return this.stepResults;
    }
    
    public void setStepResults(final List<InnerResult> stepResults) {
        this.stepResults = stepResults;
    }
}
