package ru.testit.samples;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.logevents.SelenideLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.testit.selenide.LogType;
import ru.testit.selenide.SelenideListener;
import ru.testit.annotations.Step;
import ru.testit.annotations.Title;

import java.util.logging.Level;

import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import static com.codeborne.selenide.Condition.text;

public class SampleTests {

    @BeforeEach
    public void setUp() {
        SelenideLogger.addListener(
                "TmsSelenide",
                new SelenideListener()
                        .saveScreenshots(true)
                        .savePageSource(true)
                        .includeSelenideSteps(true)
                        .saveLogs(LogType.BROWSER, Level.ALL));
    }

    @Test
    public void TestFailed() {
        openPage();

        SelenideElement searchField = getElementByXpath("//h1[contains(@class,\"title\")]");

        searchField.shouldHave(text("Система для управления тестированием"));
    }

    @Test
    public void TestSuccess() {
        openPage();

        SelenideElement searchField = getElementByXpath("//h1[contains(@class,\"title\")]");

        searchField.shouldHave(text("Система управления тестированием"));
    }

    @Step
    public void openPage() {
        open("https://testit.software/");
    }

    @Step
    @Title("Search element by xpath")
    public SelenideElement getElementByXpath(String xpath) {
        return $(byXpath(xpath));
    }
}
