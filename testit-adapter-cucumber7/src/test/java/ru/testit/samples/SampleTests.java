package ru.testit.samples;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import ru.testit.models.LinkType;
import ru.testit.services.Adapter;


public class SampleTests {
    @Given("I authorize on the portal")
    public void authorization() {
        Assert.assertTrue(setLogin("User_1"));
        Assert.assertTrue(setPassword("Pass123"));
    }

    private boolean setLogin(String login) {
        return login.equals("User_1");
    }

    private boolean setPassword(String password) {
        return password.equals("Pass123");
    }

    @When("I create a project")
    public void createProject() {
        Assert.assertTrue(true);
    }

    @And("I open the project")
    public void enterProject() {
        Assert.assertTrue(true);
    }

    @And("I create a section")
    public void createSection() {
        Assert.assertTrue(true);
    }

    @And("I create a section - failed")
    public void createFailedSection() {
        Assert.assertTrue(false);
    }

    @Then("I create a test case")
    public void createTestCase() {
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        Assert.assertTrue(true);
    }

    @Then("I check something")
    public void requiredAnnotationsTest() {
        Assert.assertTrue(true);
    }

    @Then("I check something - failed")
    public void requiredAnnotationsFailedTest() {
        Assert.assertTrue(false);
    }
}
