package ru.testit.samples;

import org.testng.Assert;
import org.testng.annotations.*;
import ru.testit.annotations.*;
import ru.testit.models.LinkType;
import ru.testit.services.TmsFactory;

public class SampleTest {

    @BeforeTest
    @Title("BeforeTest")
    @Description("BeforeTestDesc")
    public void befAll() {

    }

    @Step
    @Title("Step doBeforeSt")
    @Description("Step doBeforeSt desc")
    public void doBeforeSt() {

    }

    @BeforeClass
    @Title("BeforeClass")
    @Description("BeforeClassDesc")
    public void init() {
        doBeforeSt();
    }

    @BeforeMethod
    @Title("Before method")
    @Description("Desc bef met")
    public void befMethod() {

    }

    @Step
    @Title("Step doSomething")
    @Description("Step doSomething desc")
    public void doSomething() {

    }

    @ExternalId("12345")
    @Test
    @WorkItemId("54321")
    @DisplayName("1. Demo test display name")
    @Title("1. Demo test title")
    @Description("1. Demo test description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void firstTest() {
        doSomething();
        TmsFactory.link("Test 1", "Desc 1", LinkType.ISSUE, "https://testit.ru/");
        Assert.assertTrue(true);
    }

    @ExternalId("67890")
    @Test
    @WorkItemId("98765")
    @DisplayName("2. Demo test display name")
    @Title("2. Demo test title")
    @Description("2. Demo test description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void secondTest() {
        TmsFactory.link("Test 2", "Desc 2", LinkType.DEFECT, "https://testit.ru/123");
        Assert.assertTrue(true);
    }

    @AfterMethod
    @Title("After method")
    @Description("Desc aft met")
    public void aftMethod() {

    }

    @AfterClass
    @Title("AfterClass")
    @Description("AfterClassDesc")
    public void finish() {
    }

    @AfterTest
    @Title("AfterTest")
    @Description("AfterTestDesc")
    public void aftAll() {

    }
}

