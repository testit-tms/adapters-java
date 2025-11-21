package ru.testit.models;

import ru.testit.services.HtmlEscapeUtils;
import ru.testit.services.Utils;

/**
 * Model object to pass links to external resources.
 */
public class LinkItem
{
    private String title;
    private String url;
    private String description;
    private LinkType type;

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets title.
     *
     * @param title the value
     * @return self for method chaining
     */
    public LinkItem setTitle(final String title) {
        this.title = HtmlEscapeUtils.escapeHtmlTags(title);
        return this;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets url.
     *
     * @param url the value
     * @return self for method chaining
     */
    public LinkItem setUrl(final String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets description.
     *
     * @param description the value
     * @return self for method chaining
     */
    public LinkItem setDescription(final String description) {
        this.description = HtmlEscapeUtils.escapeHtmlTags(description);
        return this;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public LinkType getType() {
        return this.type;
    }

    /**
     * Sets type.
     *
     * @param type the value
     * @return self for method chaining
     */
    public LinkItem setType(final LinkType type) {
        this.type = type;
        return this;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class LinkItem {\n");
        sb.append("    title: ").append(Utils.toIndentedString(this.title)).append("\n");
        sb.append("    url: ").append(Utils.toIndentedString(this.url)).append("\n");
        sb.append("    description: ").append(Utils.toIndentedString(this.description)).append("\n");
        sb.append("    type: ").append(Utils.toIndentedString(this.type)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
