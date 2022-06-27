package ru.testit.models;

/**
 * Item statuses.
 */
public enum ItemStatus {
    /**
     * Marks passed tests.
     */
    PASSED("passed"),
    /**
     * Marks tests that have some failed checks.
     */
    FAILED("failed"),
    /**
     * Marks skipped tests.
     */
    SKIPPED("skipped");

    private final String value;

    ItemStatus(final String v) {
        value = v;
    }

    /**
     * From value item status.
     *
     * @param s the status as string
     * @return the item status
     */
    public static ItemStatus fromValue(final String s) {
        for (ItemStatus c : ItemStatus.values()) {
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
