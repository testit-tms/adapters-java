package ru.testit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.testit.Helper;
import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;
import ru.testit.models.LinkType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("Mockito compatibility issues with Java 19+ and sealed classes")
public class UtilsTest {
    private final static String TEXT_WITHOUT_PARAMETERS = "{Text without} {parameters}";
    private final static String[] LABELS_WITHOUT_PARAMETERS = new String[]{"{Labels", "without}", "{parameters}"};

    private Method atomicTest;

    @BeforeEach
    void init() {
        this.atomicTest = mock(Method.class);
    }

    @Test
    void extractExternalID_WithExternalIDWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);

        class TestClass {
            @ExternalId("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(testMethod.getAnnotation(ExternalId.class));

        // act
        String externalId = Utils.extractExternalID(atomicTest, parameters);

        // assert
        Assertions.assertEquals(textAfterSetParameters, externalId);
    }

    @Test
    void extractExternalID_WithExternalIDWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);

        class TestClass {
            @ExternalId("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(testMethod.getAnnotation(ExternalId.class));

        // act
        String externalId = Utils.extractExternalID(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, externalId);
    }

    @Test
    void extractExternalID_WithExternalIDWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @ExternalId("Text without parameters")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(testMethod.getAnnotation(ExternalId.class));

        // act
        String externalId = Utils.extractExternalID(atomicTest, parameters);

        // assert
        Assertions.assertEquals("Text without parameters", externalId);
    }

    @Test
    void extractExternalID_WithoutExternalID() throws NoSuchMethodException {
        class MockTests {
            public void allAnnotationsTest() {}
        }
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String hash = "FAC87DA410D46D36EFFAD262B98C6FBC8D0F284E4586FCED13841EBB008FBD47";
        Method testMethod = MockTests.class.getMethod("allAnnotationsTest");

        // act
        String externalIdWithoutInputParameters = Utils.extractExternalID(testMethod, null);
        String externalIdWithInputParameters = Utils.extractExternalID(testMethod, parameters);

        // assert
        Assertions.assertEquals(hash, externalIdWithoutInputParameters);
        Assertions.assertEquals(hash, externalIdWithInputParameters);
    }

    @Test
    void extractDisplayName_WithDisplayNameWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);

        class TestClass {
            @DisplayName("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(testMethod.getAnnotation(DisplayName.class));

        // act
        String displayName = Utils.extractDisplayName(atomicTest, parameters);

        // assert
        Assertions.assertEquals(textAfterSetParameters, displayName);
    }

    @Test
    void extractDisplayName_WithDisplayNameWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);

        class TestClass {
            @DisplayName("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(testMethod.getAnnotation(DisplayName.class));

        // act
        String displayName = Utils.extractDisplayName(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, displayName);
    }

    @Test
    void extractDisplayName_WithDisplayNameWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @DisplayName("Text without parameters")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(testMethod.getAnnotation(DisplayName.class));

        // act
        String displayName = Utils.extractDisplayName(atomicTest, parameters);

        // assert
        Assertions.assertEquals("Text without parameters", displayName);
    }

    @Test
    void extractDisplayName_WithoutDisplayName() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String methodName = "allAnnotationsTest";
        when(atomicTest.getName()).thenReturn(methodName);
        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(null);

        // act
        String displayNameWithoutInputParameters = Utils.extractDisplayName(atomicTest, null);
        String displayNameWithInputParameters = Utils.extractDisplayName(atomicTest, parameters);

        // assert
        Assertions.assertEquals(methodName, displayNameWithoutInputParameters);
        Assertions.assertEquals(methodName, displayNameWithInputParameters);
    }

    @Test
    void extractWorkItemId_WithWorkItemIdWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);

        class TestClass {
            @WorkItemId("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(testMethod.getAnnotation(WorkItemId.class));

        // act
        List<String> workItemIds = Utils.extractWorkItemId(atomicTest, parameters);

        // assert
        Assertions.assertEquals(textAfterSetParameters, workItemIds.get(0));
    }

    @Test
    void extractWorkItemId_WithWorkItemIdWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);

        class TestClass {
            @WorkItemId("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(testMethod.getAnnotation(WorkItemId.class));

        // act
        List<String> workItemIds = Utils.extractWorkItemId(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, workItemIds.get(0));
    }

    @Test
    void extractWorkItemId_WithWorkItemIdWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @WorkItemId("Text without parameters")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(testMethod.getAnnotation(WorkItemId.class));

        // act
        List<String> workItemIds = Utils.extractWorkItemId(atomicTest, parameters);

        // assert
        Assertions.assertEquals("Text without parameters", workItemIds.get(0));
    }

    @Test
    void extractWorkItemId_WithoutWorkItemId() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(null);

        // act
        List<String> workItemIdWithoutInputParameters = Utils.extractWorkItemId(atomicTest, null);
        List<String> workItemIdWithInputParameters = Utils.extractWorkItemId(atomicTest, parameters);

        // assert
        Assertions.assertTrue(workItemIdWithoutInputParameters.isEmpty());
        Assertions.assertTrue(workItemIdWithInputParameters.isEmpty());
    }

    @Test
    void extractLinks_WithOnlyUrlLinksWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        List<LinkItem> expectedLinks = UtilsHelper.generateLinkItemsAfterSetParameters(parameters);

        class TestClass {
            @Links(links = {
                    @Link(
                            url = "{Url date} = {date}; ",
                            title = "",
                            description = "",
                            type = ru.testit.models.LinkType.ISSUE
                    ),
                    @Link(
                            url = "{Url number} = {number}; ",
                            title = "",
                            description = "",
                            type = ru.testit.models.LinkType.ISSUE
                    ),
                    @Link(
                            url = "{Url name} = {name}; ",
                            title = "",
                            description = "",
                            type = ru.testit.models.LinkType.ISSUE
                    )
            })
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Links.class)).thenReturn(testMethod.getAnnotation(Links.class));

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, parameters);

        // assert
        Assertions.assertEquals(expectedLinks.size(), links.size());
        for (int i = 0; i < expectedLinks.size(); i++) {
            Assertions.assertEquals(expectedLinks.get(i).getUrl(), links.get(i).getUrl());
            Assertions.assertEquals("", links.get(i).getTitle());
            Assertions.assertEquals("", links.get(i).getDescription());
            Assertions.assertEquals(ru.testit.models.LinkType.ISSUE, links.get(i).getType());
        }
    }

    @Test
    void extractLinks_WithFullLinksWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        List<LinkItem> putLinks = UtilsHelper.generateLinkItemsBeforeSetParameters(parameters);

        class TestClass {
            @Links(links = {
                    @Link(
                            url = "{Url date} = {date}; ",
                            title = "{Title date} = {date}; ",
                            description = "{Description date} = {date}; ",
                            type = ru.testit.models.LinkType.ISSUE
                    ),
                    @Link(
                            url = "{Url number} = {number}; ",
                            title = "{Title number} = {number}; ",
                            description = "{Description number} = {number}; ",
                            type = ru.testit.models.LinkType.ISSUE
                    ),
                    @Link(
                            url = "{Url name} = {name}; ",
                            title = "{Title name} = {name}; ",
                            description = "{Description name} = {name}; ",
                            type = ru.testit.models.LinkType.ISSUE
                    ),
            })
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Links.class)).thenReturn(testMethod.getAnnotation(Links.class));

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, null);

        // assert
        Assertions.assertEquals(putLinks.size(), links.size());
        for (int i = 0; i < putLinks.size(); i++) {
            Assertions.assertEquals(putLinks.get(i).getUrl(), links.get(i).getUrl());
            Assertions.assertEquals(putLinks.get(i).getTitle(), links.get(i).getTitle());
            Assertions.assertEquals(putLinks.get(i).getDescription(), links.get(i).getDescription());
            Assertions.assertEquals(putLinks.get(i).getType(), links.get(i).getType());
        }
    }

    @Test
    void extractLinks_WithoutLinks_WithOnlyUrlLinkWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        LinkItem putLink = UtilsHelper.generateLinkItemsBeforeSetParameters(parameters).get(0);

        class TestClass {
            @Link(
                    url = "{Url date} = {date}; ",
                    title = "",
                    description = "",
                    type = LinkType.ISSUE
            )
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Links.class)).thenReturn(null);
        when(atomicTest.getAnnotation(Link.class)).thenReturn(testMethod.getAnnotation(Link.class));

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, null);

        // assert
        Assertions.assertEquals(1, links.size());
        Assertions.assertEquals(putLink.getUrl(), links.get(0).getUrl());
        Assertions.assertEquals("", links.get(0).getTitle());
        Assertions.assertEquals("", links.get(0).getDescription());
        Assertions.assertEquals(LinkType.ISSUE, links.get(0).getType());
    }

    @Test
    void extractLinks_WithoutLinks_WithFullLinkWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        LinkItem link = Helper.generateLinkItem();

        class TestClass {
            @Link(
                    url = "https://example.com",
                    title = "Test Title",
                    description = "Test Description",
                    type = ru.testit.models.LinkType.ISSUE
            )
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Links.class)).thenReturn(null);
        when(atomicTest.getAnnotation(Link.class)).thenReturn(testMethod.getAnnotation(Link.class));

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, parameters);

        // assert
        Assertions.assertEquals(1, links.size());
        Assertions.assertEquals("https://example.com", links.get(0).getUrl());
        Assertions.assertEquals("Test Description", links.get(0).getDescription());
        Assertions.assertEquals("Test Title", links.get(0).getTitle());
        Assertions.assertEquals(ru.testit.models.LinkType.ISSUE, links.get(0).getType());
    }

    @Test
    void extractLinks_WithoutLinksAndLink() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(Links.class)).thenReturn(null);
        when(atomicTest.getAnnotation(Link.class)).thenReturn(null);

        // act
        List<LinkItem> linksWithoutInputParameters = Utils.extractLinks(atomicTest, null);
        List<LinkItem> linksWithInputParameters = Utils.extractLinks(atomicTest, parameters);

        // assert
        Assertions.assertEquals(0, linksWithoutInputParameters.size());
        Assertions.assertEquals(0, linksWithInputParameters.size());
    }

    @Test
    void urlTrim_UrlWithSlash() {
        // arrange
        String url = "https://www.google.com";

        // act
        String getUrl = Utils.urlTrim(url + "/");

        // assert
        Assertions.assertEquals(url, getUrl);
    }

    @Test
    void urlTrim_UrlWithoutSlash() {
        // arrange
        String url = "https://www.google.com";

        // act
        String getUrl = Utils.urlTrim(url);

        // assert
        Assertions.assertEquals(url, getUrl);
    }

    @Test
    void extractLabels_WithLabelsWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        List<Label> expectedLabels = UtilsHelper.generateLabelsAfterSetParameters(parameters);

        class TestClass {
            @Labels(value = {
                    "{Param date} = {date}; ",
                    "{Param number} = {number}; ",
                    "{Param name} = {name}; "
            })
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Labels.class)).thenReturn(testMethod.getAnnotation(Labels.class));

        // act
        List<Label> getLabels = Utils.extractLabels(atomicTest, parameters);

        // assert
        Assertions.assertEquals(expectedLabels.size(), getLabels.size());
        for (int i = 0; i < expectedLabels.size(); i++) {
            Assertions.assertEquals(expectedLabels.get(i).getName(), getLabels.get(i).getName());
        }
    }

    @Test
    void extractLabels_WithLabelsWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String[] putLabels = UtilsHelper.generateLabelsBeforeSetParameters(parameters);

        class TestClass {
            @Labels(value = {
                    "{Param date} = {date}; ",
                    "{Param number} = {number}; ",
                    "{Param name} = {name}; "
            })
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Labels.class)).thenReturn(testMethod.getAnnotation(Labels.class));

        // act
        List<Label> getLabels = Utils.extractLabels(atomicTest, null);

        // assert
        Assertions.assertEquals(putLabels.length, getLabels.size());
        for (int i = 0; i < putLabels.length; i++) {
            Assertions.assertEquals(putLabels[i], getLabels.get(i).getName());
        }
    }

    @Test
    void extractLabels_WithLabelsWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @Labels(value = {
                    "{Labels",
                    "without}",
                    "{parameters}"
            })
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Labels.class)).thenReturn(testMethod.getAnnotation(Labels.class));

        // act
        List<Label> getLabels = Utils.extractLabels(atomicTest, parameters);

        // assert
        Assertions.assertEquals(LABELS_WITHOUT_PARAMETERS.length, getLabels.size());
        for (int i = 0; i < LABELS_WITHOUT_PARAMETERS.length; i++) {
            Assertions.assertEquals(LABELS_WITHOUT_PARAMETERS[i], getLabels.get(i).getName());
        }
    }

    @Test
    void extractLabels_WithoutLabels() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(Labels.class)).thenReturn(null);

        // act
        List<Label> labelsWithoutInputParameters = Utils.extractLabels(atomicTest, null);
        List<Label> labelsWithInputParameters = Utils.extractLabels(atomicTest, parameters);

        // assert
        Assertions.assertEquals(0, labelsWithoutInputParameters.size());
        Assertions.assertEquals(0, labelsWithInputParameters.size());
    }

    @Test
    void extractDescription_WithDescriptionWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);

        class TestClass {
            @Description("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Description.class)).thenReturn(testMethod.getAnnotation(Description.class));

        // act
        String description = Utils.extractDescription(atomicTest, parameters);

        // assert
        Assertions.assertEquals(textAfterSetParameters, description);
    }

    @Test
    void extractDescription_WithDescriptionWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);

        class TestClass {
            @Description("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Description.class)).thenReturn(testMethod.getAnnotation(Description.class));

        // act
        String description = Utils.extractDescription(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, description);
    }

    @Test
    void extractDescription_WithDescriptionWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @Description(TEXT_WITHOUT_PARAMETERS)
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Description.class)).thenReturn(testMethod.getAnnotation(Description.class));

        // act
        String description = Utils.extractDescription(atomicTest, parameters);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, description);
    }

    @Test
    void extractDescription_WithoutDescription() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(Description.class)).thenReturn(null);

        // act
        String descriptionWithoutInputParameters = Utils.extractDescription(atomicTest, null);
        String descriptionWithInputParameters = Utils.extractDescription(atomicTest, parameters);

        // assert
        Assertions.assertTrue(descriptionWithoutInputParameters.isEmpty());
        Assertions.assertTrue(descriptionWithInputParameters.isEmpty());
    }

    @Test
    void extractTitle_WithTitleWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);

        class TestClass {
            @Title("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Title.class)).thenReturn(testMethod.getAnnotation(Title.class));

        // act
        String title = Utils.extractTitle(atomicTest, parameters, true);

        // assert
        Assertions.assertEquals(textAfterSetParameters, title);
    }

    @Test
    void extractTitle_WithTitleWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);

        class TestClass {
            @Title("{Param date} = {date}; {Param number} = {number}; {Param name} = {name}; ")
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Title.class)).thenReturn(testMethod.getAnnotation(Title.class));

        // act
        String title = Utils.extractTitle(atomicTest, null, true);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, title);
    }

    @Test
    void extractTitle_WithTitleWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            @Title(TEXT_WITHOUT_PARAMETERS)
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Title.class)).thenReturn(testMethod.getAnnotation(Title.class));

        // act
        String title = Utils.extractTitle(atomicTest, parameters, true);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, title);
    }

    @Test
    void extractTitle_WithoutTitle() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        class TestClass {
            void testMethod() {
            }
        }

        Method testMethod = TestClass.class.getDeclaredMethods()[0];
        when(atomicTest.getAnnotation(Title.class)).thenReturn(null);
        when(atomicTest.getName()).thenReturn(testMethod.getName());

        // act
        String titleWithoutInputParameters = Utils.extractTitle(atomicTest, null, true);
        String titleWithInputParameters = Utils.extractTitle(atomicTest, parameters, true);

        // assert
        Assertions.assertNull(titleWithoutInputParameters);
        Assertions.assertNull(titleWithInputParameters);
    }
}
