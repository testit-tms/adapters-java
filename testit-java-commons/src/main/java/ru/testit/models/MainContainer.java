package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model describes main container with.
 */
public class MainContainer {
    private String uuid;
    private List<String> beforeMethods = new ArrayList<>();
    private List<String> afterMethods = new ArrayList<>();
    private List<String> children = new ArrayList<>();
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
     * @return self for method chaining
     */
    public MainContainer setUuid(String uuid) {
        this.uuid = uuid;
        return this;
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
     * @return self for method chaining
     */
    public MainContainer setChildren(List<String> children) {
        this.children = children;
        return this;
    }

    /**
     * Gets beforeMethods.
     *
     * @return the beforeMethods
     */
    public List<String> getBeforeMethods() {
        return beforeMethods;
    }

    /**
     * Sets beforeMethods.
     *
     * @param beforeMethods the beforeMethods
     * @return self for method chaining
     */
    public MainContainer setBeforeMethods(List<String> beforeMethods) {
        this.beforeMethods = beforeMethods;
        return this;
    }

    /**
     * Gets afterMethods.
     *
     * @return the afterMethods
     */
    public List<String> getAfterMethods() {
        return afterMethods;
    }

    /**
     * Sets afterMethods.
     *
     * @param afterMethods the afterMethods
     * @return self for method chaining
     */
    public MainContainer setAfterMethods(List<String> afterMethods) {
        this.afterMethods = afterMethods;
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
    public MainContainer setStart(Long start) {
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
    public MainContainer setStop(Long stop) {
        this.stop = stop;
        return this;
    }
}
