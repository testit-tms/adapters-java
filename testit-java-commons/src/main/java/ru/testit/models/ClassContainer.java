package ru.testit.models;

import ru.testit.services.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model describes class container.
 */
public class ClassContainer implements Serializable {
    private String uuid;
    private String name;
    private List<FixtureResult> beforeEachTest = new ArrayList<>();
    private List<FixtureResult> afterEachTest = new ArrayList<>();
    private List<FixtureResult> beforeClassMethods = new ArrayList<>();
    private List<FixtureResult> afterClassMethods = new ArrayList<>();
    private List<String> children = new ArrayList<>();
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
    public ClassContainer setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets getBeforeEachTest.
     *
     * @return the beforeEachTest
     */
    public List<FixtureResult> getBeforeEachTest() {
        return beforeEachTest;
    }

    /**
     * Sets getBeforeEachTest.
     *
     * @param beforeEachTest the beforeEachTest
     * @return self for method chaining
     */
    public ClassContainer setBeforeEachTest(List<FixtureResult> beforeEachTest) {
        this.beforeEachTest = beforeEachTest;
        return this;
    }

    /**
     * Gets getAfterEachTest.
     *
     * @return the afterEachTest
     */
    public List<FixtureResult> getAfterEachTest() {
        return afterEachTest;
    }

    /**
     * Sets afters.
     *
     * @param afterEachTest the afters
     * @return self for method chaining
     */
    public ClassContainer setAfterEachTest(List<FixtureResult> afterEachTest) {
        this.afterEachTest = afterEachTest;
        return this;
    }

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
    public ClassContainer setUuid(String uuid) {
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
    public ClassContainer setChildren(List<String> children) {
        this.children = children;
        return this;
    }

    /**
     * Gets beforeClassMethods.
     *
     * @return the beforeClassMethods
     */
    public List<FixtureResult> getBeforeClassMethods() {
        return beforeClassMethods;
    }

    /**
     * Sets beforeClassMethods.
     *
     * @param beforeClassMethods the beforeClassMethods
     * @return self for method chaining
     */
    public ClassContainer setBeforeClassMethods(List<FixtureResult> beforeClassMethods) {
        this.beforeClassMethods = beforeClassMethods;
        return this;
    }

    /**
     * Gets afterClassMethods.
     *
     * @return the afterClassMethods
     */
    public List<FixtureResult> getAfterClassMethods() {
        return afterClassMethods;
    }

    /**
     * Sets afterClassMethods.
     *
     * @param afterClassMethods the afterClassMethods
     * @return self for method chaining
     */
    public ClassContainer setAfterClassMethods(List<FixtureResult> afterClassMethods) {
        this.afterClassMethods = afterClassMethods;
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
    public ClassContainer setStart(Long start) {
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
    public ClassContainer setStop(Long stop) {
        this.stop = stop;
        return this;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClassContainer {\n");
        sb.append("    uuid: ").append(Utils.toIndentedString(this.uuid)).append("\n");
        sb.append("    name: ").append(Utils.toIndentedString(this.name)).append("\n");
        sb.append("    beforeEachTest: ").append(Utils.toIndentedString(this.beforeEachTest)).append("\n");
        sb.append("    afterEachTest: ").append(Utils.toIndentedString(this.afterEachTest)).append("\n");
        sb.append("    beforeClassMethods: ").append(Utils.toIndentedString(this.beforeClassMethods)).append("\n");
        sb.append("    afterClassMethods: ").append(Utils.toIndentedString(this.afterClassMethods)).append("\n");
        sb.append("    children: ").append(Utils.toIndentedString(this.children)).append("\n");
        sb.append("    start: ").append(Utils.toIndentedString(this.start)).append("\n");
        sb.append("    stop: ").append(Utils.toIndentedString(this.stop)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
