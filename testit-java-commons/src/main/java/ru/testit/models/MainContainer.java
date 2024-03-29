package ru.testit.models;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model describes main container with.
 */
public class MainContainer implements Serializable {
    private String uuid;
    private List<FixtureResult> beforeMethods = new ArrayList<>();
    private List<FixtureResult> afterMethods = new ArrayList<>();
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
    public List<FixtureResult> getBeforeMethods() {
        return beforeMethods;
    }

    /**
     * Sets beforeMethods.
     *
     * @param beforeMethods the beforeMethods
     * @return self for method chaining
     */
    public MainContainer setBeforeMethods(List<FixtureResult> beforeMethods) {
        this.beforeMethods = beforeMethods;
        return this;
    }

    /**
     * Gets afterMethods.
     *
     * @return the afterMethods
     */
    public List<FixtureResult> getAfterMethods() {
        return afterMethods;
    }

    /**
     * Sets afterMethods.
     *
     * @param afterMethods the afterMethods
     * @return self for method chaining
     */
    public MainContainer setAfterMethods(List<FixtureResult> afterMethods) {
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MainContainer {\n");
        sb.append("    uuid: ").append(Utils.toIndentedString(this.uuid)).append("\n");
        sb.append("    beforeMethods: ").append(Utils.toIndentedString(this.beforeMethods)).append("\n");
        sb.append("    afterMethods: ").append(Utils.toIndentedString(this.afterMethods)).append("\n");
        sb.append("    children: ").append(Utils.toIndentedString(this.children)).append("\n");
        sb.append("    start: ").append(Utils.toIndentedString(this.start)).append("\n");
        sb.append("    stop: ").append(Utils.toIndentedString(this.stop)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
