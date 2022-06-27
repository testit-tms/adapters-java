package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model object that stores information about executed test fixtures (set up and tear down methods).
 * In order to link test fixture to test result {@link TestResultContainer} is used.
 */

public class FixtureResult {
    private String name;
    private ItemStatus itemStatus;
    private ItemStage itemStage;
    private String description;
    private List<StepResult> steps = new ArrayList<>();
    private List<LinkItem> linkItems = new ArrayList<>();
    private Long start;
    private Long stop;

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
