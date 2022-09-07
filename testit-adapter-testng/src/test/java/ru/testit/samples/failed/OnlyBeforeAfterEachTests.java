package ru.testit.samples.failed;

import org.testng.Assert;
import org.testng.annotations.*;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.services.Adapter;

public class OnlyBeforeAfterEachTests {
    @BeforeMethod
    @Title("Log in the system")
    @Description("System authentication")
    public void authorization() {
        Assert.assertTrue(setLogin("User_2"));
        Assert.assertTrue(setPassword("Pass123"));
    }

    @Step
    @Title("Set login")
    public boolean setLogin(String login) {
        return login.equals("User_1");
    }

    @Step
    @Title("Set password")
    public boolean setPassword(String password) {
        return password.equals("Pass123");
    }

    @Step
    @Title("Create a project")
    @Description("Project was created")
    public void createProject() {
        Assert.assertTrue(true);
    }

    @Step
    @Title("Enter the project")
    @Description("The contents of the project are displayed")
    public void enterProject() {
        Assert.assertTrue(true);
    }

    @Step
    @Title("Create a section")
    @Description("Section was created")
    public void createSection() {
        Assert.assertTrue(true);
    }

    @Step
    @Title("Create a test case")
    @Description("Test case was created")
    public void createTestCase() {
        Assert.assertTrue(true);
    }

    @BeforeMethod
    @Title("Maximum nesting in setup step")
    public void beforeStepWithMaximumNesting() {
        maximumNestingStep(13);
    }

    @Step
    @Title("Maximum nesting step")
    @Description("15 nesting levels of step")
    public void maximumNestingStep(int level) {
        if (level > 1) {
            maximumNestingStep(level - 1);
        }
    }

    @Test
    @ExternalId("failed_BeforeEach_AfterEach_with_all_annotations")
    @DisplayName("Failed test with all annotations")
    @WorkItemIds("456")
    @Title("Title in the autotest card")
    @Description("Test with BeforeEach, AfterEach and all annotations")
    @Labels({"Tag1","Tag2"})
    @Links(links = {
            @Link(url = "https://dumps.example.com/module/repository", title = "Repository", description = "Example of repository", type = LinkType.REPOSITORY),
            @Link(url = "https://dumps.example.com/module/projects", title = "Projects", type = LinkType.REQUIREMENT),
            @Link(url = "https://dumps.example.com/module/", type = LinkType.BLOCKED_BY),
            @Link(url = "https://dumps.example.com/module/docs", title = "Documentation", type = LinkType.RELATED),
            @Link(url = "https://dumps.example.com/module/JCP-777", title = "JCP-777", type = LinkType.DEFECT),
            @Link(url = "https://dumps.example.com/module/issue/5", title = "Issue-5", type = LinkType.ISSUE),
    })
    public void allAnnotationsTest() {
       Adapter.addLinks("https://testit.ru/", "Test 1","Desc 1", LinkType.ISSUE);
        createProject();
        enterProject();
        createSection();
        createTestCase();
        maximumNestingStep(13);
    }

    @Test
    @ExternalId("failed_BeforeEach_AfterEach_with_required_annotations")
    @DisplayName("Failed test with required annotations")
    public void requiredAnnotationsTest() {
        Assert.assertTrue(false);
    }

    @AfterMethod
    @Title("Log out the system")
    public void logOut() {
        Assert.assertTrue(true);
    }

    @AfterMethod
    @Title("Maximum nesting in teardown step")
    public void afterStepWithMaximumNesting() {
        maximumNestingStep(13);
    }
}
