package ru.testit.models;

/**
 * Item stages.
 */
public enum ItemStage {
    /**
     * Running stage.
     */
    RUNNING("running"),
    /**
     * Finished stage.
     */
    FINISHED("finished"),
    /**
     * Scheduled stage.
     */
    SCHEDULED("scheduled"),
    /**
     * Pending stage.
     */
    PENDING("pending");

    private final String value;

    ItemStage(final String s) {
        value = s;
    }

    /**
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return value;
    }
}
