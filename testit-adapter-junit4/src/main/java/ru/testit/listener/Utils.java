package ru.testit.listener;

import org.junit.runner.Description;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import java.util.LinkedList;
import java.util.List;
import ru.testit.models.Label;

public class Utils {

    public static String extractExternalID(final Description atomicTest) {
        final ExternalId annotation = atomicTest.getAnnotation(ExternalId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractDisplayName(final Description atomicTest) {
        final DisplayName annotation = atomicTest.getAnnotation(DisplayName.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractWorkItemId(final Description method) {
        final WorkItemId annotation = method.getAnnotation(WorkItemId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static List<LinkItem> extractLinks(final Description method) {
        final List<LinkItem> links = new LinkedList<LinkItem>();
        final Links linksAnnotation = method.getAnnotation(Links.class);
        if (linksAnnotation != null) {
            for (final Link link : linksAnnotation.links()) {
                links.add(makeLink(link));
            }
        }
        else {
            final Link linkAnnotation = method.getAnnotation(Link.class);
            if (linkAnnotation != null) {
                links.add(makeLink(linkAnnotation));
            }
        }
        return links;
    }

    public static List<Label> extractLabels(final Description method) {
        final List<Label> labels = new LinkedList<Label>();
        final Labels annotation = method.getAnnotation(Labels.class);
        if (annotation != null) {
            for (final String s : annotation.value()) {
                final Label label = new Label();
                label.setName(s);
                labels.add(label);
            }
        }
        return labels;
    }

    public static String extractTitle(final Description currentTest) {
        final Title annotation = currentTest.getAnnotation(Title.class);
        return (annotation != null) ? annotation.value() : null;
    }

    private static LinkItem makeLink(final Link linkAnnotation) {
        return new LinkItem()
                .setTitle(linkAnnotation.title())
                .setDescription(linkAnnotation.description())
                .setUrl(linkAnnotation.url())
                .setType(linkAnnotation.type());
    }

    public static String extractDescription(final Description currentTest) {
        final ru.testit.annotations.Description annotation = currentTest.getAnnotation(ru.testit.annotations.Description.class);
        return (annotation != null) ? annotation.value() : null;
    }
}
