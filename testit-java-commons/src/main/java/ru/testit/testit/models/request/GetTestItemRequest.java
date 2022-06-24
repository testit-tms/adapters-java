package ru.testit.testit.models.request;

public class GetTestItemRequest
{
    private String projectId;
    
    public String getProjectId() {
        return this.projectId;
    }
    
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }
}
