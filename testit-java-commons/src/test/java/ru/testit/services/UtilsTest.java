package ru.testit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.testit.Helper;
import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class UtilsTest {
    private final static String TEXT_WITHOUT_PARAMETERS = "{Text without} {parameters}";
    private final static String[] LABELS_WITHOUT_PARAMETERS = new String[] {"{Labels", "without}", "{parameters}"};

    private Method atomicTest;

    @BeforeEach
    void init() {
        this.atomicTest = mock(Method.class);
    }

    @Test
    void extractExternalID_WithExternalIDWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);
        ExternalId annotation = mock(ExternalId.class);

        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

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
        ExternalId annotation = mock(ExternalId.class);

        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        String externalId = Utils.extractExternalID(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, externalId);
    }

    @Test
    void extractExternalID_WithExternalIDWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        ExternalId annotation = mock(ExternalId.class);

        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(TEXT_WITHOUT_PARAMETERS);

        // act
        String externalId = Utils.extractExternalID(atomicTest, parameters);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, externalId);
    }

    @Test
    void extractExternalID_WithoutExternalID() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(ExternalId.class)).thenReturn(null);

        // act
        String externalIdWithoutInputParameters = Utils.extractExternalID(atomicTest, null);
        String externalIdWithInputParameters = Utils.extractExternalID(atomicTest, parameters);

        // assert
        Assertions.assertNull(externalIdWithoutInputParameters);
        Assertions.assertNull(externalIdWithInputParameters);
    }

    @Test
    void extractDisplayName_WithDisplayNameWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);
        DisplayName annotation = mock(DisplayName.class);

        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

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
        DisplayName annotation = mock(DisplayName.class);

        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        String displayName = Utils.extractDisplayName(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, displayName);
    }

    @Test
    void extractDisplayName_WithDisplayNameWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        DisplayName annotation = mock(DisplayName.class);


        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(TEXT_WITHOUT_PARAMETERS);

        // act
        String displayName = Utils.extractDisplayName(atomicTest, parameters);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, displayName);
    }

    @Test
    void extractDisplayName_WithoutDisplayName() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(DisplayName.class)).thenReturn(null);

        // act
        String displayNameWithoutInputParameters = Utils.extractDisplayName(atomicTest, null);
        String displayNameWithInputParameters = Utils.extractDisplayName(atomicTest, parameters);

        // assert
        Assertions.assertNull(displayNameWithoutInputParameters);
        Assertions.assertNull(displayNameWithInputParameters);
    }

    @Test
    void extractWorkItemId_WithWorkItemIdWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);
        WorkItemId annotation = mock(WorkItemId.class);

        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

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
        WorkItemId annotation = mock(WorkItemId.class);

        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        List<String> workItemIds = Utils.extractWorkItemId(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, workItemIds.get(0));
    }

    @Test
    void extractWorkItemId_WithWorkItemIdWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        WorkItemId annotation = mock(WorkItemId.class);

        when(atomicTest.getAnnotation(WorkItemId.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(TEXT_WITHOUT_PARAMETERS);

        // act
        List<String> workItemIds = Utils.extractWorkItemId(atomicTest, parameters);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, workItemIds.get(0));
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
        List<LinkItem> putLinks = UtilsHelper.generateLinkItemsBeforeSetParameters(parameters);
        List<LinkItem> expectedLinks = UtilsHelper.generateLinkItemsAfterSetParameters(parameters);
        Links annotation = mock(Links.class);
        Link[] arrayLinks = new Link[putLinks.size()];

        for (int i = 0; i < putLinks.size(); i++) {
            arrayLinks[i] = mock(Link.class);
            when(arrayLinks[i].url()).thenReturn(putLinks.get(i).getUrl());
            when(arrayLinks[i].title()).thenReturn(null);
            when(arrayLinks[i].description()).thenReturn(null);
            when(arrayLinks[i].type()).thenReturn(null);
        }

        when(atomicTest.getAnnotation(Links.class)).thenReturn(annotation);
        when(annotation.links()).thenReturn(arrayLinks);

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, parameters);

        // assert
        Assertions.assertEquals(expectedLinks.size(), links.size());

        for (int i = 0; i < expectedLinks.size(); i++) {
            Assertions.assertEquals(expectedLinks.get(i).getUrl(), links.get(i).getUrl());
            Assertions.assertNull(links.get(i).getTitle());
            Assertions.assertNull(links.get(i).getDescription());
            Assertions.assertNull(links.get(i).getType());
        }
    }

    @Test
    void extractLinks_WithFullLinksWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        List<LinkItem> putLinks = UtilsHelper.generateLinkItemsBeforeSetParameters(parameters);
        Links annotation = mock(Links.class);
        Link[] arrayLinks = new Link[putLinks.size()];

        for (int i = 0; i < putLinks.size(); i++) {
            arrayLinks[i] = mock(Link.class);
            when(arrayLinks[i].url()).thenReturn(putLinks.get(i).getUrl());
            when(arrayLinks[i].title()).thenReturn(putLinks.get(i).getTitle());
            when(arrayLinks[i].description()).thenReturn(putLinks.get(i).getDescription());
            when(arrayLinks[i].type()).thenReturn(putLinks.get(i).getType());
        }

        when(atomicTest.getAnnotation(Links.class)).thenReturn(annotation);
        when(annotation.links()).thenReturn(arrayLinks);

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, null);

        // assert
        Assertions.assertEquals(putLinks.size(), links.size());

        for (int i = 0; i < putLinks.size(); i++) {
            Assertions.assertEquals(putLinks.get(i).getUrl(), links.get(i).getUrl());
            Assertions.assertEquals(putLinks.get(i).getDescription(), links.get(i).getDescription());
            Assertions.assertEquals(putLinks.get(i).getTitle(), links.get(i).getTitle());
            Assertions.assertEquals(putLinks.get(i).getType(), links.get(i).getType());
        }
    }

    @Test
    void extractLinks_WithoutLinks_WithOnlyUrlLinkWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        LinkItem putLink = UtilsHelper.generateLinkItemsBeforeSetParameters(parameters).get(0);
        Link annotation = mock(Link.class);

        when(atomicTest.getAnnotation(Links.class)).thenReturn(null);
        when(atomicTest.getAnnotation(Link.class)).thenReturn(annotation);
        when(annotation.url()).thenReturn(putLink.getUrl());
        when(annotation.title()).thenReturn(null);
        when(annotation.description()).thenReturn(null);
        when(annotation.type()).thenReturn(null);

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, null);

        // assert
        Assertions.assertEquals(1, links.size());
        Assertions.assertEquals(putLink.getUrl(), links.get(0).getUrl());
        Assertions.assertNull(links.get(0).getTitle());
        Assertions.assertNull(links.get(0).getDescription());
        Assertions.assertNull(links.get(0).getType());
    }

    @Test
    void extractLinks_WithoutLinks_WithFullLinkWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        LinkItem link = Helper.generateLinkItem();
        Link annotation = mock(Link.class);

        when(atomicTest.getAnnotation(Links.class)).thenReturn(null);
        when(atomicTest.getAnnotation(Link.class)).thenReturn(annotation);
        when(annotation.url()).thenReturn(link.getUrl());
        when(annotation.title()).thenReturn(link.getTitle());
        when(annotation.description()).thenReturn(link.getDescription());
        when(annotation.type()).thenReturn(link.getType());

        // act
        List<LinkItem> links = Utils.extractLinks(atomicTest, parameters);

        // assert
        Assertions.assertEquals(1, links.size());
        Assertions.assertEquals(link.getUrl(), links.get(0).getUrl());
        Assertions.assertEquals(link.getDescription(), links.get(0).getDescription());
        Assertions.assertEquals(link.getTitle(), links.get(0).getTitle());
        Assertions.assertEquals(link.getType(), links.get(0).getType());
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
        String[] putLabels = UtilsHelper.generateLabelsBeforeSetParameters(parameters);
        List<Label> expectedLabels = UtilsHelper.generateLabelsAfterSetParameters(parameters);
        Labels annotation = mock(Labels.class);

        when(atomicTest.getAnnotation(Labels.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(putLabels);

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
        Labels annotation = mock(Labels.class);

        when(atomicTest.getAnnotation(Labels.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(putLabels);

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
        Labels annotation = mock(Labels.class);

        when(atomicTest.getAnnotation(Labels.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(LABELS_WITHOUT_PARAMETERS);

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
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);
        Description annotation = mock(Description.class);

        when(atomicTest.getAnnotation(Description.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

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
        Description annotation = mock(Description.class);

        when(atomicTest.getAnnotation(Description.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        String description = Utils.extractDescription(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, description);
    }

    @Test
    void extractDescription_WithDescriptionWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        Description annotation = mock(Description.class);

        when(atomicTest.getAnnotation(Description.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(TEXT_WITHOUT_PARAMETERS);

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
        Assertions.assertNull(descriptionWithoutInputParameters);
        Assertions.assertNull(descriptionWithInputParameters);
    }

    @Test
    void extractTitle_WithTitleWithParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        String textAfterSetParameters = UtilsHelper.generateTextAfterSetParameters(parameters);
        Title annotation = mock(Title.class);

        when(atomicTest.getAnnotation(Title.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        String title = Utils.extractTitle(atomicTest, parameters);

        // assert
        Assertions.assertEquals(textAfterSetParameters, title);
    }

    @Test
    void extractTitle_WithTitleWithParameters_WithoutInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        String textBeforeSetParameters = UtilsHelper.generateTextBeforeSetParameters(parameters);
        Title annotation = mock(Title.class);

        when(atomicTest.getAnnotation(Title.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(textBeforeSetParameters);

        // act
        String title = Utils.extractTitle(atomicTest, null);

        // assert
        Assertions.assertEquals(textBeforeSetParameters, title);
    }

    @Test
    void extractTitle_WithTitleWithoutParameters_WithInputParameters() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();
        Title annotation = mock(Title.class);

        when(atomicTest.getAnnotation(Title.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(TEXT_WITHOUT_PARAMETERS);

        // act
        String title = Utils.extractTitle(atomicTest, parameters);

        // assert
        Assertions.assertEquals(TEXT_WITHOUT_PARAMETERS, title);
    }

    @Test
    void extractTitle_WithoutTitle() {
        // arrange
        Map<String, String> parameters = UtilsHelper.generateParameters();

        when(atomicTest.getAnnotation(Title.class)).thenReturn(null);

        // act
        String titleWithoutInputParameters = Utils.extractTitle(atomicTest, null);
        String titleWithInputParameters = Utils.extractTitle(atomicTest, parameters);

        // assert
        Assertions.assertNull(titleWithoutInputParameters);
        Assertions.assertNull(titleWithInputParameters);
    }
}
