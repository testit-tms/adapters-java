package ru.testit.selenide;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.codeborne.selenide.logevents.SelenideLog;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.models.ItemStatus;
import ru.testit.models.StepResult;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SelenideListener implements LogEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelenideListener.class);
    private final AdapterManager adapterManager;
    private boolean saveScreenshots;
    private boolean savePageHtml;
    private boolean includeSelenideLocatorsSteps;
    private final Map<LogType, Level> logTypesToSave = new EnumMap<>(LogType.class);

    public SelenideListener() {
        this.adapterManager = Adapter.getAdapterManager();
    }

    public SelenideListener saveScreenshots(final boolean saveScreenshots) {
        this.saveScreenshots = saveScreenshots;
        return this;
    }

    public SelenideListener savePageSource(final boolean savePageHtml) {
        this.savePageHtml = savePageHtml;
        return this;
    }

    public SelenideListener includeSelenideSteps(final boolean includeSelenideSteps) {
        this.includeSelenideLocatorsSteps = includeSelenideSteps;
        return this;
    }

    public SelenideListener saveLogs(final LogType logType, final Level logLevel) {
        logTypesToSave.put(logType, logLevel);

        return this;
    }

    private static Optional<byte[]> getScreenshotBytes() {
        try {
            return WebDriverRunner.hasWebDriverStarted()
                    ? Optional.of(((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES))
                    : Optional.empty();
        } catch (WebDriverException e) {
            LOGGER.warn("Can not save screen shot", e);
            return Optional.empty();
        }
    }

    private static Optional<byte[]> getPageSourceBytes() {
        try {
            return WebDriverRunner.hasWebDriverStarted()
                    ? Optional.of(WebDriverRunner.getWebDriver().getPageSource().getBytes(UTF_8))
                    : Optional.empty();
        } catch (WebDriverException e) {
            LOGGER.warn("Can not save page source", e);
            return Optional.empty();
        }
    }

    private static String getBrowserLogs(final LogType logType, final Level level) {
        return String.join("\n\n", Selenide.getWebDriverLogs(logType.toString(), level));
    }

    @Override
    public void afterEvent(LogEvent currentLog) {
        if (currentLog.getStatus().equals(LogEvent.EventStatus.FAIL)) {
            adapterManager.getCurrentTestCaseOrStep().ifPresent(parentUuid -> {
                if (saveScreenshots) {
                    getScreenshotBytes()
                            .ifPresent(bytes -> Adapter.addAttachments("Screenshot-" + parentUuid + ".png", new ByteArrayInputStream(bytes)));
                }
                if (savePageHtml) {
                    getPageSourceBytes()
                            .ifPresent(bytes -> Adapter.addAttachments("PageSource-" + parentUuid + ".txt", new ByteArrayInputStream(bytes)));
                }
                if (!logTypesToSave.isEmpty()) {
                    logTypesToSave
                            .forEach((logType, level) -> {
                                final byte[] content = getBrowserLogs(logType, level).getBytes(UTF_8);
                                Adapter.addAttachments("Logs-" + logType + ".txt", new ByteArrayInputStream(content));
                            });
                }
            });
        }

        if (stepsShouldBeLogged(currentLog)) {
            adapterManager.getCurrentTestCaseOrStep().ifPresent(parentUuid -> {
                switch (currentLog.getStatus()) {
                    case PASS:
                        adapterManager.updateStep(step -> step.setItemStatus(ItemStatus.PASSED));
                        break;
                    case FAIL:
                        adapterManager.updateStep(stepResult ->
                            stepResult.setItemStatus(ItemStatus.FAILED));
                        adapterManager.updateTestCase(parentUuid, testResult ->
                            testResult.setThrowable(currentLog.getError()));
                        break;
                    default:
                        LOGGER.warn("Unsupported status of step: {}", currentLog.getStatus());
                        break;
                }
                adapterManager.stopStep();
            });
        }
    }

    @Override
    public void beforeEvent(LogEvent currentLog) {
        if (stepsShouldBeLogged(currentLog)) {
            adapterManager.getCurrentTestCaseOrStep().ifPresent(parentUuid -> {
                final String uuid = UUID.randomUUID().toString();
                adapterManager.startStep(parentUuid, uuid, new StepResult().setTitle(currentLog.toString()));
            });
        }
    }

    private boolean stepsShouldBeLogged(final LogEvent currentLog) {
        return includeSelenideLocatorsSteps || !(currentLog instanceof SelenideLog);
    }
}
