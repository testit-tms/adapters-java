package ru.testit.listener;

import java.util.UUID;

/**
 * Describes executable test.
 */
class ExecutableTest {
    private final String uuid;
    private ExecutableTestStage executableTestStage;

    ExecutableTest() {
        this.uuid = UUID.randomUUID().toString();
        this.executableTestStage = ExecutableTestStage.BEFORE;
    }

    public void setTestStatus() {
        this.executableTestStage = ExecutableTestStage.TEST;
    }

    public void setAfterStatus() {
        this.executableTestStage = ExecutableTestStage.AFTER;
    }

    public boolean isStarted() {
        return this.executableTestStage != ExecutableTestStage.BEFORE;
    }

    public boolean isAfter() {
        return this.executableTestStage == ExecutableTestStage.AFTER;
    }

    public String getUuid() {
        return uuid;
    }
}

/**
 * The stage of executable test.
 */
enum ExecutableTestStage {
    BEFORE,
    TEST,
    AFTER
}
