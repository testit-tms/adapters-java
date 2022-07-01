package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model object that stores information about test that was run.
 */
public class TestResult implements ResultWithSteps {
    private String uuid;
    private String externalId;
    private String workItemId;
    private String className;
    private String spaceName;
    private List<Label> labels = new ArrayList<>();
    private List<LinkItem> linkItems = new ArrayList<>();
    private String name;
    private ItemStatus itemStatus;
    private ItemStage itemStage;
    private String description;
    private List<String> steps = new ArrayList<>();
    private Long start;
    private Long stop;
    private Throwable throwable;

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
    public String getWorkItemId() {
        return workItemId;
    }

    /**
     * Sets work item id.
     *
     * @param workItemId the value
     * @return self for method chaining
     */
    public TestResult setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
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
    public List<String> getSteps() {
        return steps;
    }

    /**
     * Sets steps.
     *
     * @param steps the steps
     * @return self for method chaining
     */
    public TestResult setSteps(List<String> steps) {
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
     * @return the stop
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
}
