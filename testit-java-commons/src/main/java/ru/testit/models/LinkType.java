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
    
    LinkType(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }

    public static LinkType fromString(String text) {
        for (LinkType type : LinkType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
