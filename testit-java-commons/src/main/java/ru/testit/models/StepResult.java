package ru.testit.models;

import ru.testit.services.HtmlEscapeUtils;
import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model describes step.
 */

public class StepResult implements ResultWithSteps, ResultWithAttachments, Serializable {
    private String name;
    private ItemStatus itemStatus;
    private ItemStage itemStage;
    private String description;
    private List<StepResult> steps = new ArrayList<>();
    private List<LinkItem> linkItems = new ArrayList<>();
    private List<String> attachments = new ArrayList<>();
    private Throwable throwable;
    private Long start;
    private Long stop;
    private Map<String, String> parameters;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the value
     * @return self for method chaining
     */
    public StepResult setName(String name) {
        this.name = HtmlEscapeUtils.escapeHtmlTags(name);
        return this;
    }

    /**
     * Gets item status.
     *
     * @return the item status
     */
    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    /**
     * Sets item status.
     *
     * @param itemStatus the value
     * @return self for method chaining
     */
    public StepResult setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
        return this;
    }

    /**
     * Gets item stage.
     *
     * @return the item stage
     */
    public ItemStage getItemStage() {
        return itemStage;
    }

    /**
     * Sets item stage.
     *
     * @param itemStage the value
     * @return self for method chaining
     */
    public StepResult setItemStage(ItemStage itemStage) {
        this.itemStage = itemStage;
        return this;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the value
     * @return self for method chaining
     */
    public StepResult setDescription(String description) {
        this.description = HtmlEscapeUtils.escapeHtmlTags(description);
        return this;
    }

    /**
     * Gets attachments.
     *
     * @return the attachments
     */
    public List<String> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the attachments
     * @return self for method chaining
     */
    public StepResult setAttachments(List<String> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Gets steps.
     *
     * @return the steps
     */
    public List<StepResult> getSteps() {
        return steps;
    }

    /**
     * Sets steps.
     *
     * @param steps the steps
     * @return self for method chaining
     */
    public StepResult setSteps(List<StepResult> steps) {
        this.steps = steps;
        return this;
    }

    /**
     * Gets links.
     *
     * @return the links
     */
    public List<LinkItem> getLinkItems() {
        return linkItems;
    }

    /**
     * Sets links.
     *
     * @param linkItems the steps
     * @return self for method chaining
     */
    public StepResult setLinkItems(List<LinkItem> linkItems) {
        this.linkItems = linkItems;
        return this;
    }

    /**
     * Gets start.
     *
     * @return the start
     */
    public Long getStart() {
        return start;
    }

    /**
     * Sets start.
     *
     * @param start the value
     * @return self for method chaining
     */
    public StepResult setStart(Long start) {
        this.start = start;
        return this;
    }

    /**
     * Gets stop.
     *
     * @return the stop
     */
    public Long getStop() {
        return stop;
    }

    /**
     * Sets stop.
     *
     * @param stop the value
     * @return self for method chaining
     */
    public StepResult setStop(Long stop) {
        this.stop = stop;
        return this;
    }

    /**
     * Gets throwable.
     *
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets throwable.
     *
     * @param throwable the value
     * @return self for method chaining
     */
    public StepResult setThrowable(Throwable throwable) {
        this.throwable = HtmlEscapeUtils.escapeHtmlInObject(throwable);
        return this;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the value
     * @return self for method chaining
     */
    public StepResult setParameters(Map<String, String> parameters) {
        this.parameters = HtmlEscapeUtils.escapeHtmlInObject(parameters);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StepResult {\n");
        sb.append("    name: ").append(Utils.toIndentedString(this.name)).append("\n");
        sb.append("    itemStatus: ").append(Utils.toIndentedString(this.itemStatus)).append("\n");
        sb.append("    itemStage: ").append(Utils.toIndentedString(this.itemStage)).append("\n");
        sb.append("    description: ").append(Utils.toIndentedString(this.description)).append("\n");
        sb.append("    steps: ").append(Utils.toIndentedString(this.steps)).append("\n");
        sb.append("    linkItems: ").append(Utils.toIndentedString(this.linkItems)).append("\n");
        sb.append("    attachments: ").append(Utils.toIndentedString(this.attachments)).append("\n");
        sb.append("    throwable: ").append(Utils.toIndentedString(this.throwable)).append("\n");
        sb.append("    start: ").append(Utils.toIndentedString(this.start)).append("\n");
        sb.append("    stop: ").append(Utils.toIndentedString(this.stop)).append("\n");
        sb.append("    parameters: ").append(Utils.toIndentedString(this.parameters)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
