package ru.testit.samples;

import org.junit.jupiter.api.*;
import ru.testit.annotations.*;
import ru.testit.annotations.DisplayName;
import ru.testit.models.LinkType;

public class SampleTest {
    @BeforeAll
    @Title("BeforeTest")
    @Description("BeforeTestDesc")
    public static void befAll(){

    }

    @Step
    @Title("Step doBeforeSt")
    @Description("Step doBeforeSt desc")
    public void doBeforeSt(){

    }

    @BeforeEach
    @Title("Before method")
    @Description("Desc bef met")
    public void befMethod(){

        doBeforeSt();
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
//        doSecond();
        Assertions.assertTrue(true);
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
//        doSomething();
        Assertions.assertTrue(true);
    }

    @AfterEach
    @Title("After method")
    @Description("Desc aft met")
    public void aftMethod(){

    }

    @AfterAll
    @Title("AfterTest")
    @Description("AfterTestDesc")
    public static void aftAll(){

    }
}