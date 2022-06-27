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
     * From value stage.
     *
     * @param s the s
     * @return the stage
     */
    public static ItemStage fromValue(final String s) {
        for (ItemStage c : ItemStage.values()) {
            if (c.value.equals(s)) {
                return c;
            }
        }
        throw new IllegalArgumentException(s);
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
