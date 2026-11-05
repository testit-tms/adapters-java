package ru.testit.samples;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ParameterizedTest {
    private int result;

    @When("Summing {int}+{int}")
    public void sum(int left, int right){
      this.result = left + right;
    }

    @Then("Result is {int}")
    public void result(int result){
        Assert.assertEquals(this.result, result);
    }
}
