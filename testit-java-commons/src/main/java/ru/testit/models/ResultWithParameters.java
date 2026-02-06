package ru.testit.models;

import java.util.Map;

/**
 * The marker interface for model objects with parameters.
 */
public interface ResultWithParameters {

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    Map<String, String> getParameters();
}
