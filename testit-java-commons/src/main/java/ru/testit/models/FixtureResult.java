package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model describes fixture.
 */

public class FixtureResult implements ResultWithSteps {
    private String name;
    private ItemStatus itemStatus;
    private ItemStage itemStage;
    private String description;
    private List<String> steps = new ArrayList<>();
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
     * @return self for method chaining
     */
    public FixtureResult setName(String name) {
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
    public FixtureResult setItemStatus(ItemStatus itemStatus) {
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
    public FixtureResult setItemStage(ItemStage itemStage) {
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
    public FixtureResult setDescription(String description) {
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
    public FixtureResult setSteps(List<String> steps) {
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
    public FixtureResult setLinkItems(List<LinkItem> linkItems) {
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
    public FixtureResult setStart(Long start) {
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
    public FixtureResult setStop(Long stop) {
        this.stop = stop;
        return this;
    }
}
