package ru.testit.samples.failed;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;
import ru.testit.services.Adapter;

public class OnlyBeforeAfterAllTests {
    @BeforeAll
    @Title("Open browser")
    public static void openBrowser() {
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Log in the system")
    @Description("System authentication")
    public void authorization() {
        Assertions.assertTrue(setLogin("User_1"));
        Assertions.assertTrue(setPassword("Pass123"));
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
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Enter the project")
    @Description("The contents of the project are displayed")
    public void enterProject() {
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Create a section")
    @Description("Section was created")
    public void createSection() {
        Assertions.assertTrue(false);
    }

    @Step
    @Title("Create a test case")
    @Description("Test case was created")
    public void createTestCase() {
        Assertions.assertTrue(true);
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
    @ExternalId("failed_BeforeAll_AfterAll_with_all_annotations")
    @DisplayName("Failed test with all annotations")
    @WorkItemId("123")
    @Title("Title in the autotest card")
    @Description("Test with BeforeAll, AfterAll and all annotations")
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
       Adapter.addLink("https://testit.ru/", "Test 1","Desc 1", LinkType.ISSUE);
        authorization();
        createProject();
        enterProject();
        createSection();
        createTestCase();
        maximumNestingStep(13);
    }

    @Test
    @ExternalId("failed_BeforeAll_AfterAll_with_required_annotations")
    @DisplayName("Failed test with required annotations")
    public void requiredAnnotationsTest() {
        Assertions.assertTrue(true);
    }

    @AfterAll
    @Title("Close browser")
    public static void CloseBrowser() {
        Assertions.assertTrue(false);
    }
}
