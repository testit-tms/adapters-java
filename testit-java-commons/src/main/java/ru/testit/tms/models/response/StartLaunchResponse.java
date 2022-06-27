package ru.testit.tms.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StartLaunchResponse
{
    private String id;
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
}
