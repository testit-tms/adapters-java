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
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return value;
    }
}
