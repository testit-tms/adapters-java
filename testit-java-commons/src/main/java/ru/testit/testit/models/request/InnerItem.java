package ru.testit.testit.models.request;

import java.util.LinkedList;
import java.util.List;

public class InnerItem
{
    private String title;
    private String description;
    private List<InnerItem> steps;
    
    public InnerItem() {
        this.steps = new LinkedList<InnerItem>();
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
    
    public List<InnerItem> getSteps() {
        return this.steps;
    }
    
    public void setSteps(final List<InnerItem> steps) {
        this.steps = steps;
    }
}
