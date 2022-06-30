package ru.testit.simples;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;


import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.tms.client.TMSClient;

public class SimpleTest {
    @BeforeClass
    @Title("BeforeClass")
    @Description("newBeforeClass")
    public static void init() {
        doBeforeSt();
    }

    @Before
    @Title("Before")
    @Description("newBefore")
    public void setUp() {
        doBefore();
    }


    @ExternalId("5")
    @Test
    @WorkItemId("3")
    @DisplayName("firstTest")
    @Title("")
    @Description("")
    @Links(links = {@Link( url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.BLOCKED_BY),
            @Link(url = "www.2.ru", title = "NewNewsecondLink", description = "secondLinkDesc", type = LinkType.DEFECT),
            @Link(url = "www.google.com", title = "NewNewnewTitle", description = "LastLink", type = LinkType.ISSUE)})
    @Labels({"newLable1"})
    public void firstTest() {
        doSecond();
        doAfter();
        Assert.assertTrue(true);
    }

    @ExternalId("6")
    @Test
    @WorkItemId("4")
    @DisplayName("secondTest")
    @Title("2")
    @Description("Second Test Description")
    public void secondTest() {
        TMSClient.addLink(new LinkItem("test-addLinkNewNewNew", "www.test.com", "testDesc", LinkType.RELATED));
        doSecond();
        doSomething();
        Assert.assertFalse(false);
    }


    @Step
    @Title("doSomething")
    @Description("netLocalizacii")
    private void doSomething() {
        doSecond();
        doThird();
    }

    @Step
    @Title("doSecond")
    @Description("он делайет что то ")
    private void doSecond() {
        TMSClient.addLink(new LinkItem("doSecondDescNewNewNew", "www.drive.google.com", "testDesc", LinkType.RELATED));
        doThird();
    }

    @Step
    @Title("NewdoThird")
    @Description("NewdoThirdDesc")
    private void doThird() {
        int x = 2 + 5 + 6;

    }

    @Step
    @Title("NewdoBeforeSt")
    @Description("NewdoBeforeStDesc")
    private static void doBeforeSt() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("dNewoBeforeSt")
    @Description("NewdoBeforeStSecond")
    private static void doBeforeSec() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("NewdoBefore")
    @Description("NewdoBeforeDesc")
    private void doBefore() {
    }

    @Step
    @Title("NewdoBefore")
    @Description("NewdoBeforeNEW")
    private void doBeforeSec1() {
    }

    @Step
    @Title("NewdoAfterSt")
    @Description("NewdoAfterStDesc")
    private static void doAfterSt() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("NewdoAfterSt")
    @Description("NewdoAfterStDescNEW")
    private static void doAfterSt12() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("newdoAfter")
    @Description("newdoAfterDesc")
    private void doAfter() {
        int x = 2 + 5 + 6;
    }

    @Step
    @Title("doAfter")
    @Description("doAfterNew")
    private void doAfterSec() {
        int x = 2 + 5 + 6;
    }

    @After
    @Title("After")
    @Description("newAfter")
    public void tearDown() {
        doAfter();
    }

    @AfterClass
    @Title("newAfterClass")
    @Description("NewNewAfterClass")
    public static void finish() {
        doAfterSt12();
    }

}