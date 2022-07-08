package ru.testit.samples;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.testit.annotations.*;
import ru.testit.listener.BaseJunit4Runner;
import ru.testit.models.LinkType;

@RunWith(BaseJunit4Runner.class)
public class SampleTest {

    @Test
    @ExternalId("Simple_test_1")
    @DisplayName("Simple test 12")
    public void simpleTest1() {
        Assert.assertTrue(true);
    }

    @Test
    @ExternalId("Simple_test_2")
    @WorkItemId("1")
    @DisplayName("Simple test 22")
    @Title("test №2")
    @Description("Description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void itsTrueReallyTrue() {
        step1();
        Assert.assertTrue(true);
    }

    @Step
    @Title("Step 1")
    @Description("Step 1 description")
    private void step1() {
        step2();
        Assert.assertTrue(true);
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void step2() {
        Assert.assertTrue(true);
    }
}
