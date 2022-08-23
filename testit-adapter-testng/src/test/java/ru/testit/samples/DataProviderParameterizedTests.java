package ru.testit.samples;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.testit.annotations.*;
import ru.testit.models.LinkType;

public class DataProviderParameterizedTests {

    @DataProvider
    public static Object[][] allParameters() {
        return new Object[][] {
                {"Test version 1", 1, "google.com"},
                {"Test version 2", 2, "yandex.ru"}
        };
    }

    @Test(dataProvider = "allParameters")
    @ExternalId("Parameterized_test_with_data_provider_parameters_{number}")
    @DisplayName("Test with title = {title}, number = {number}, url = {url} parameters")
    @WorkItemIds("{number}")
    @Title("Title in the autotest card {number}")
    @Description("{title}")
    @Labels({"Tag{number}"})
    @Links(links = {
            @Link(url = "https://{url}/module/repository", title = "{title} Repository", description = "Example of repository", type = LinkType.REPOSITORY),
            @Link(url = "https://{url}/module/projects", title = "{title} Projects", type = LinkType.REQUIREMENT),
            @Link(url = "https://{url}/module/", type = LinkType.BLOCKED_BY),
            @Link(url = "https://{url}/module/docs", title = "{title} Documentation", type = LinkType.RELATED),
            @Link(url = "https://{url}/module/JCP-777", title = "{title} JCP-777", type = LinkType.DEFECT),
            @Link(url = "https://{url}/module/issue/5", title = "{title} Issue-5", type = LinkType.ISSUE),
    })
    void testWithDataProviderParameters(String title, int number, String url) {

    }
}
