package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The model object that stores links between test results and test fixtures.
 * <p>
 * During report generation all {@link #befores} and {@link #afters} is added to each
 * test result that {@link TestResult#getUuid()} matches values, specified in {@link #children}.
 * <p>
 * Containers that have empty {@link #children} are simply ignored.
 */
public class TestResultContainer {
    private String uuid;
    private String name;
    private List<String> children = new ArrayList<>();
    private String description;
    private List<FixtureResult> befores = new ArrayList<>();
    private List<FixtureResult> afters = new ArrayList<>();
    private List<LinkItem> linkItems;
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
     * Gets children.
     *
     * @return the children
     */
    public List<String> getChildren() {
        return children;
    }

    /**
     * Sets children.
     *
     * @param children the children
     */
    public void setChildren(List<String> children) {
        this.children = children;
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
     * Gets befores.
     *
     * @return the befores
     */
    public List<FixtureResult> getBefores() {
        return befores;
    }

    /**
     * Sets befores.
     *
     * @param befores the befores
     */
    public void setBefores(List<FixtureResult> befores) {
        this.befores = befores;
    }

    /**
     * Gets afters.
     *
     * @return the afters
     */
    public List<FixtureResult> getAfters() {
        return afters;
    }

    /**
     * Sets afters.
     *
     * @param afters the afters
     */
    public void setAfters(List<FixtureResult> afters) {
        this.afters = afters;
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
