package ru.testit.models;

/**
 * Item statuses.
 */
public enum ItemStatus {
    /**
     * Marks passed tests.
     */
    PASSED("Passed"),
    /**
     * Marks tests that have some failed checks.
     */
    FAILED("Failed"),
    /**
     * Marks skipped tests.
     */
    SKIPPED("Skipped"),
    /**
     * Marks in progress tests.
     */
    INPROGRESS("InProgress"),
    /**
     * Marks in blocked tests.
     */
    BLOCKED("Blocked");
    private final String value;

    ItemStatus(final String s) {
        value = s;
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
