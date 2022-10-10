package ru.testit.samples;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Test;


@CucumberOptions(
        features = "src/test/resources/features",
        plugin = {
                "ru.testit.listener.BaseCucumber7Listener",
                "progress",
                "summary"
        }
)
@Test
public class CucumberTest extends AbstractTestNGCucumberTests {
}
