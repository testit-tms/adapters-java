package ru.testit.samples;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.tms.client.TMSClient;

public class SampleTest {

    @Step
    @Title("doSomething")
    @Description("doSomethingDesc")
    private void doSomething() {
        Assert.assertTrue(true);
        doThird();
    }

    @Step
    @Title("doSecond")
    @Description("doSecondDesc")
    private void doSecond() {
        TMSClient.addLink(new LinkItem("doSecondLink", "www.test.com", "testDesc", LinkType.RELATED));
        doThird();
    }

    @Step
    @Title("doThird")
    @Description("doThirdDesc")
    private void doThird() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("do")
    @Description("do")
    private static void doAfterSt() {
        int x = 2 + 5 + 6;
    }

    @BeforeClass
    @Title("BeforeClass")
    @Description("BeforeClassDesc")
    public void init() {
        doBeforeSt();
    }

    @Step
    @Title("doBeforeSt")
    @Description("doBeforeStDesc")
    private static void doBeforeSt() {
        int x = 2 + 5 + 6;
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
        doSecond();
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
        doSomething();
        Assert.assertTrue(true);
    }

    @AfterClass
    @Title("AfterClass")
    @Description("AfterClassDesc")
    public void finish() {
        doAfterSt();
    }
}

