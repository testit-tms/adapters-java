package ru.testit.tms.models.request;

public class StartTestRunRequest
{
    private String projectId;
    private String name;
    
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
}
