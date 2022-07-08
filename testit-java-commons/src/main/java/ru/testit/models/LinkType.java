package ru.testit.models;

public enum LinkType
{
    RELATED("Related"), 
    BLOCKED_BY("BlockedBy"), 
    DEFECT("Defect"), 
    ISSUE("Issue"), 
    REQUIREMENT("Requirement"), 
    REPOSITORY("Repository");
    
    private String value;
    
    private LinkType(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
