package ru.testit.models;

import java.util.List;

/**
 * The marker interface for model objects with steps.
 */
public interface ResultWithSteps {

    /**
     * Gets steps.
     *
     * @return the steps
     */
    List<String> getSteps();
}
