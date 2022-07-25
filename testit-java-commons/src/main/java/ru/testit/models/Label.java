package ru.testit.models;

/**
 * The model object that could be used to pass additional metadata to test results.
 */
public class Label
{
    private String name;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets name.
     *
     * @param name the value
     * @return self for method chaining
     */
    public Label setName(final String name) {
        this.name = name;
        return this;
    }
}
