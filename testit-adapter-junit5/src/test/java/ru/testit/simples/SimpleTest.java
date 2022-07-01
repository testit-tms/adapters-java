package ru.testit.simples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.tms.client.TMSClient;

public class SimpleTest {

    @Test
    @ExternalId("Simple_test_1")
    @DisplayName("Simple test 1")
    public void simpleTest1() {
        Assertions.assertTrue(true);
    }

    @Test
    @ExternalId("Simple_test_2")
    @WorkItemId("1")
    @DisplayName("Simple test 2")
    @Title("test №2")
    @Description("Description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void itsTrueReallyTrue() {
        step1();
        TMSClient.addLink(new LinkItem("doSecondLink", "www.test.com", "testDesc", LinkType.RELATED));
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Step 1")
    @Description("Step 1 description")
    private void step1() {
        step2();
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void step2() {
        Assertions.assertTrue(true);
    }
}