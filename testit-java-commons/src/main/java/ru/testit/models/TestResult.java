package ru.testit.models;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The model object that stores information about test that was run.
 */
public class TestResult implements ResultWithSteps, ResultWithAttachments, Serializable {
    private String uuid;
    private String externalId;
    private List<String> workItemIds = new ArrayList<>();
    private String className;
    private String spaceName;
    private List<Label> labels = new ArrayList<>();
    private List<LinkItem> linkItems = new ArrayList<>();
    private List<LinkItem> resultLinks = new ArrayList<>();
    private List<String> attachments = new ArrayList<>();
    private String name;
    private String title;
    private String message;
    private ItemStatus itemStatus;
    private ItemStage itemStage;
    private String description;
    private List<StepResult> steps = new ArrayList<>();
    private Long start;
    private Long stop;
    private Throwable throwable;
    private Map<String, String> parameters;
    private boolean automaticCreationTestCases;

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param uuid the value
     * @return self for method chaining
     */
    public TestResult setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Gets external id.
     *
     * @return the external id
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets external id.
     *
     * @param externalId the value
     * @return self for method chaining
     */
    public TestResult setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Gets work item id.
     *
     * @return the work item id
     */
    public List<String> getWorkItemId() {
        return workItemIds;
    }

    /**
     * Sets work item id.
     *
     * @param workItemIds the value
     * @return self for method chaining
     */
    public TestResult setWorkItemId(List<String> workItemIds) {
        this.workItemIds = workItemIds;
        return this;
    }

    /**
     * Gets class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets class name.
     *
     * @param className the value
     * @return self for method chaining
     */
    public TestResult setClassName(String className) {
        this.className = className;
        return this;
    }

    /**
     * Gets space name.
     *
     * @return the space name
     */
    public String getSpaceName() {
        return spaceName;
    }

    /**
     * Sets space name.
     *
     * @param spaceName the value
     * @return self for method chaining
     */
    public TestResult setSpaceName(String spaceName) {
        this.spaceName = spaceName;
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
    public TestResult setAttachments(List<String> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Gets labels.
     *
     * @return the labels
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Sets labels.
     *
     * @param labels the labels
     * @return self for method chaining
     */
    public TestResult setLabels(List<Label> labels) {
        this.labels = labels;
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
    public TestResult setLinkItems(List<LinkItem> linkItems) {
        this.linkItems = linkItems;
        return this;
    }

    /**
     * Gets result links.
     *
     * @return the links
     */
    public List<LinkItem> getResultLinks() {
        return resultLinks;
    }

    /**
     * Sets result links.
     *
     * @param resultLinks the test
     * @return self for method chaining
     */
    public TestResult setResultLinks(List<LinkItem> resultLinks) {
        this.resultLinks = resultLinks;
        return this;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     * @return self for method chaining
     */
    public TestResult setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the value
     * @return self for method chaining
     */
    public TestResult setTitle(String title) {
        this.title = title;
        return this;
    }

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
    public TestResult setName(String name) {
        this.name = name;
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
    public TestResult setItemStatus(ItemStatus itemStatus) {
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
    public TestResult setItemStage(ItemStage itemStage) {
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
    public TestResult setDescription(String description) {
        this.description = description;
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
    public TestResult setSteps(List<StepResult> steps) {
        this.steps = steps;
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
    public TestResult setStart(Long start) {
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
    public TestResult setStop(Long stop) {
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
    public TestResult setThrowable(Throwable throwable) {
        this.throwable = throwable;
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
    public TestResult setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Gets automaticCreationTestCases.
     *
     * @return the automaticCreationTestCases
     */
    public boolean getAutomaticCreationTestCases() {
        return automaticCreationTestCases;
    }

    /**
     * Sets automaticCreationTestCases.
     *
     * @param automaticCreationTestCases the value
     * @return self for method chaining
     */
    public TestResult setAutomaticCreationTestCases(boolean automaticCreationTestCases) {
        this.automaticCreationTestCases = automaticCreationTestCases;
        return this;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TestResult {\n");
        sb.append("    uuid: ").append(Utils.toIndentedString(this.uuid)).append("\n");
        sb.append("    externalId: ").append(Utils.toIndentedString(this.externalId)).append("\n");
        sb.append("    workItemIds: ").append(Utils.toIndentedString(this.workItemIds)).append("\n");
        sb.append("    className: ").append(Utils.toIndentedString(this.className)).append("\n");
        sb.append("    spaceName: ").append(Utils.toIndentedString(this.spaceName)).append("\n");
        sb.append("    labels: ").append(Utils.toIndentedString(this.labels)).append("\n");
        sb.append("    linkItems: ").append(Utils.toIndentedString(this.linkItems)).append("\n");
        sb.append("    resultLinks: ").append(Utils.toIndentedString(this.resultLinks)).append("\n");
        sb.append("    attachments: ").append(Utils.toIndentedString(this.attachments)).append("\n");
        sb.append("    name: ").append(Utils.toIndentedString(this.name)).append("\n");
        sb.append("    title: ").append(Utils.toIndentedString(this.title)).append("\n");
        sb.append("    message: ").append(Utils.toIndentedString(this.message)).append("\n");
        sb.append("    itemStatus: ").append(Utils.toIndentedString(this.itemStatus)).append("\n");
        sb.append("    itemStage: ").append(Utils.toIndentedString(this.itemStage)).append("\n");
        sb.append("    description: ").append(Utils.toIndentedString(this.description)).append("\n");
        sb.append("    steps: ").append(Utils.toIndentedString(this.steps)).append("\n");
        sb.append("    throwable: ").append(Utils.toIndentedString(this.throwable)).append("\n");
        sb.append("    start: ").append(Utils.toIndentedString(this.start)).append("\n");
        sb.append("    stop: ").append(Utils.toIndentedString(this.stop)).append("\n");
        sb.append("    parameters: ").append(Utils.toIndentedString(this.parameters)).append("\n");
        sb.append("    automaticCreationTestCases: ").append(Utils.toIndentedString(this.automaticCreationTestCases)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
