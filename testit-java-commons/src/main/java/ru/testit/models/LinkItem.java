package ru.testit.models;

import ru.testit.annotations.LinkType;

public class LinkItem
{
    private String title;
    private String url;
    private String description;
    private LinkType type;
    
    public LinkItem(final String title) {
        this(title, null, null, null);
    }
    
    public LinkItem(final String title, final String url) {
        this(title, url, null, null);
    }
    
    public LinkItem(final String title, final String url, final String description) {
        this(title, url, description, null);
    }
    
    public LinkItem(final String title, final String url, final String description, final LinkType type) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.type = type;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public LinkType getType() {
        return this.type;
    }
}
