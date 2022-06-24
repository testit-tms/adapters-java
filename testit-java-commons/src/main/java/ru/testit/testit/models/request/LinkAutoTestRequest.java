package ru.testit.testit.models.request;

public class LinkAutoTestRequest
{
    private String id;
    
    public LinkAutoTestRequest(final String id) {
        this.id = id;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
}
