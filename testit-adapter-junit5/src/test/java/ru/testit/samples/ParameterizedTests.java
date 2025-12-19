package ru.testit.samples;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.testit.annotations.*;
import ru.testit.models.LinkType;

import java.util.stream.Stream;

class ParameterizedTests {

    @ParameterizedTest
    @ValueSource(shorts = {1, 2, 3})
    @ExternalId("Parameterized_test_with_one_parameter_{number}")
    @DisplayName("Test with number = {number} parameter")
    @WorkItemIds("{number}")
    @Title("Title in the autotest card {number}")
    @Description("Test with BeforeEach, AfterEach and all annotations {number}")
    @Labels({"Tag{number}"})
    void testWithOneParameter(int number) {
        // empty
    }

    @ParameterizedTest
    @MethodSource("arguments")
    @ExternalId("Parameterized_test_with_multiple_parameters_{number}")
    @DisplayName("Parameterized test with number = {number}, title = {title}, expected = {expected}, url = {url}")
    @Links(links = {
            @Link(url = "https://{url}/module/repository", title = "{title} Repository", description = "Example of repository", type = LinkType.REPOSITORY),
            @Link(url = "https://{url}/module/projects", title = "{title} Projects", type = LinkType.REQUIREMENT),
            @Link(url = "https://{url}/module/", type = LinkType.BLOCKED_BY),
            @Link(url = "https://{url}/module/docs", title = "{title} Documentation", type = LinkType.RELATED),
            @Link(url = "https://{url}/module/JCP-777", title = "{title} JCP-777", type = LinkType.DEFECT),
            @Link(url = "https://{url}/module/issue/5", title = "{title} Issue-5", type = LinkType.ISSUE),
    })
    void testWithMultipleParameters(int number, String title, boolean expected, String url) {
        // empty
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(1, "Test version 1", true, "google.com"),
                Arguments.of(2, "Test version 2", false, "yandex.ru")
        );
    }
}
