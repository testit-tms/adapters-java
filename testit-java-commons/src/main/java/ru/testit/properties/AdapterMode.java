package ru.testit.properties;

public enum AdapterMode {
    USE_FILTER(0),
    RUN_ALL_TESTS(1),
    NEW_TEST_RUN(2);

    private final int value;

    AdapterMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static AdapterMode valueOf(int value) {
        for (AdapterMode e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
