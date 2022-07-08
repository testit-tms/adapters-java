package ru.testit.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model describes class container.
 */
public class ClassContainer {
    private String uuid;
    private String name;
    private List<String> beforeEachTest = new ArrayList<>();
    private List<String> afterEachTest = new ArrayList<>();
    private List<String> beforeClassMethods = new ArrayList<>();
    private List<String> afterClassMethods = new ArrayList<>();
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
    public List<String> getBeforeEachTest() {
        return beforeEachTest;
    }

    /**
     * Sets getBeforeEachTest.
     *
     * @param beforeEachTest the beforeEachTest
     * @return self for method chaining
     */
    public ClassContainer setBeforeEachTest(List<String> beforeEachTest) {
        this.beforeEachTest = beforeEachTest;
        return this;
    }

    /**
     * Gets getAfterEachTest.
     *
     * @return the afterEachTest
     */
    public List<String> getAfterEachTest() {
        return afterEachTest;
    }

    /**
     * Sets afters.
     *
     * @param afterEachTest the afters
     * @return self for method chaining
     */
    public ClassContainer setAfterEachTest(List<String> afterEachTest) {
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
    public List<String> getBeforeClassMethods() {
        return beforeClassMethods;
    }

    /**
     * Sets beforeClassMethods.
     *
     * @param beforeClassMethods the beforeClassMethods
     * @return self for method chaining
     */
    public ClassContainer setBeforeClassMethods(List<String> beforeClassMethods) {
        this.beforeClassMethods = beforeClassMethods;
        return this;
    }

    /**
     * Gets afterClassMethods.
     *
     * @return the afterClassMethods
     */
    public List<String> getAfterClassMethods() {
        return afterClassMethods;
    }

    /**
     * Sets afterClassMethods.
     *
     * @param afterClassMethods the afterClassMethods
     * @return self for method chaining
     */
    public ClassContainer setAfterClassMethods(List<String> afterClassMethods) {
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
}
