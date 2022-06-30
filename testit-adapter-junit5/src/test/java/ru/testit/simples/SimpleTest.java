package ru.testit.simples;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.tms.client.TMSClient;

public class SimpleTest {

    @BeforeAll
    @Title("Before")
    @Description("Before")
    public static void init() {
        doBeforeSt();
    }

    @BeforeEach
    @Title("Before")
    @Description("BeforeDesc")
    public void setUp() {
        doBefore();
    }

    @AfterAll
    @Title("AfterClass")
    @Description("AfterClassDesc")
    public static void finish() {
        doAfterSt();
    }

    @AfterEach
    @Title("After")
    @Description("AfterDesc")
    public void tearDown() {
        doAfter();
    }


    @ExternalId("DemoN3333")
    @Test
    @WorkItemId("504424")
    @DisplayName("testDemoN3333")
    @Title("2")
    @Description("Description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void itsTrueReallyTrue() {
        doSomething();
        for(int i=0; i<50; i++){
            doSomething();
        }
        Assertions.assertTrue(true);
    }

    @ExternalId("DemoN5555")
    @Test
    @WorkItemId("504424")
    @DisplayName("testDemoN3333")
    @Title("2")
    @Description("Description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void oneAgainTrueReallyTrue() {
        doSomething();
        Assertions.assertTrue(true);
    }

    @Step
    @Title("doSomething")
    @Description("doSomethingDesc")
    private void doSomething() {
        Assertions.assertTrue(true);
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
    @Title("doBeforeSt")
    @Description("doBeforeStDesc")
    private static void doBeforeSt() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("doBefore")
    @Description("doBeforeDesc")
    private void doBefore() {
    }

    @Step
    @Title("do")
    @Description("do")
    private static void doAfterSt() {
        int x = 2 + 5 + 6;

    }

    @Step
    @Title("do")
    @Description("do")
    private void doAfter() {
        int x = 2 + 5 + 6;
    }
}