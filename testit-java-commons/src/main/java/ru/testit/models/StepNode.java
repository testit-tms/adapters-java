package ru.testit.models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StepNode
{
    private StepNode parent;
    private String title;
    private String description;
    private Date startedOn;
    private Date completedOn;
    private String outcome;
    private Throwable failureReason;
    private List<LinkItem> linkItems;
    private List<StepNode> children;
    
    public StepNode() {
        this.linkItems = new LinkedList<LinkItem>();
        this.children = new LinkedList<StepNode>();
    }
    
    public StepNode getParent() {
        return this.parent;
    }
    
    public void setParent(final StepNode parent) {
        this.parent = parent;
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
    
    public List<StepNode> getChildren() {
        return this.children;
    }
    
    public void setChildren(final List<StepNode> children) {
        this.children = children;
    }
    
    public String getOutcome() {
        return this.outcome;
    }
    
    public void setOutcome(final String outcome) {
        this.outcome = outcome;
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
    
    public Throwable getFailureReason() {
        return this.failureReason;
    }
    
    public void setFailureReason(final Throwable failureReason) {
        this.failureReason = failureReason;
    }
    
    public List<LinkItem> getLinkItems() {
        return this.linkItems;
    }
    
    public void setLinkItems(final List<LinkItem> linkItems) {
        this.linkItems = linkItems;
    }
}
