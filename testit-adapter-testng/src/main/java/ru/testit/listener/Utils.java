package ru.testit.listener;

import ru.testit.annotations.*;
import ru.testit.testit.models.request.InnerLink;
import ru.testit.testit.models.request.Label;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Utils {
    public static String extractExternalID(final Method atomicTest) {
        final ExternalId annotation = atomicTest.getAnnotation(ExternalId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractDisplayName(final Method atomicTest) {
        final DisplayName annotation = atomicTest.getAnnotation(DisplayName.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractWorkItemId(final Method method) {
        final WorkItemId annotation = method.getAnnotation(WorkItemId.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static List<InnerLink> extractLinks(final Method method) {
        final List<InnerLink> links = new LinkedList<InnerLink>();
        final Links linksAnnotation = method.getAnnotation(Links.class);
        if (linksAnnotation != null) {
            for (final Link link : linksAnnotation.links()) {
                links.add(makeInnerLink(link));
            }
        }
        else {
            final Link linkAnnotation = method.getAnnotation(Link.class);
            if (linkAnnotation != null) {
                links.add(makeInnerLink(linkAnnotation));
            }
        }
        return links;
    }

    public static List<Label> extractLabels(final Method method) {
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

    public static String extractDescription(final Method currentTest) {
        final Description annotation = currentTest.getAnnotation(Description.class);
        return (annotation != null) ? annotation.value() : null;
    }

    public static String extractTitle(final Method currentTest) {
        final Title annotation = currentTest.getAnnotation(Title.class);
        return (annotation != null) ? annotation.value() : null;
    }

    private static InnerLink makeInnerLink(final Link linkAnnotation) {
        final InnerLink innerLink = new InnerLink();
        innerLink.setTitle(linkAnnotation.title());
        innerLink.setDescription(linkAnnotation.description());
        innerLink.setUrl(linkAnnotation.url());
        innerLink.setType(linkAnnotation.type().getValue());
        return innerLink;
    }
}
