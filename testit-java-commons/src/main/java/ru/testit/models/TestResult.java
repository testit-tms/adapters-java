package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model object that stores information about test that was run.
 */
public class TestResult {
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
    private List<StepResult> steps = new ArrayList<>();
    private Long start;
    private Long stop;

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
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
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
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
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
     */
    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
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
     */
    public void setClassName(String className) {
        this.className = className;
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
     */
    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
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
     */
    public void setLabels(List<Label> labels) {
        this.labels = labels;
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
     */
    public void setLinkItems(List<LinkItem> linkItems) {
        this.linkItems = linkItems;
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
     */
    public void setName(String name) {
        this.name = name;
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
     */
    public void setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
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
     */
    public void setItemStage(ItemStage itemStage) {
        this.itemStage = itemStage;
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
     */
    public void setDescription(String description) {
        this.description = description;
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
     */
    public void setSteps(List<StepResult> steps) {
        this.steps = steps;
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
     */
    public void setStart(Long start) {
        this.start = start;
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
     */
    public void setStop(Long stop) {
        this.stop = stop;
    }
}
