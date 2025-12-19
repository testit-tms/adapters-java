package ru.testit.selenide;

public enum LogType {
    /**
     * This log type pertains to logs from the browser.
     */
    BROWSER(org.openqa.selenium.logging.LogType.BROWSER),

    /**
     * This log type pertains to logs from the client.
     */
    CLIENT(org.openqa.selenium.logging.LogType.CLIENT),

    /**
     * This log pertains to logs from the WebDriver implementation.
     */
    DRIVER(org.openqa.selenium.logging.LogType.DRIVER),

    /**
     * This log type pertains to logs relating to performance timings.
     */
    PERFORMANCE(org.openqa.selenium.logging.LogType.PERFORMANCE),

    /**
     * This log type pertains to logs relating to performance timings.
     */
    PROFILER(org.openqa.selenium.logging.LogType.PROFILER),

    /**
     * This log type pertains to logs from the remote server.
     */
    SERVER(org.openqa.selenium.logging.LogType.SERVER);

    private final String logTypeValue;

    LogType(final String logTypeValue) {
        this.logTypeValue = logTypeValue;
    }

    @Override
    public String toString() {
        return logTypeValue;
    }
}
