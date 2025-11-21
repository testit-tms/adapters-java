package ru.testit.models;

import ru.testit.services.HtmlEscapeUtils;
import ru.testit.services.Utils;

import java.io.Serializable;

/**
 * The model object that could be used to pass additional metadata to test results.
 */
public class Label implements Serializable {
    private String name;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets name.
     *
     * @param name the value
     * @return self for method chaining
     */
    public Label setName(final String name) {
        this.name = HtmlEscapeUtils.escapeHtmlTags(name);
        return this;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class Label {\n");
        sb.append("    name: ").append(Utils.toIndentedString(this.name)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
