package ru.testit.services;

import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;

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

    public static List<LinkItem> extractLinks(final Method method) {
        final List<LinkItem> links = new LinkedList<>();
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

    public static List<Label> extractLabels(final Method method) {
        final List<Label> labels = new LinkedList<>();
        final Labels annotation = method.getAnnotation(Labels.class);
        if (annotation != null) {
            for (final String s : annotation.value()) {
                final Label label = new Label()
                        .setName(s);
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

    private static LinkItem makeLink(final Link linkAnnotation) {
        return new LinkItem()
            .setTitle(linkAnnotation.title())
            .setDescription(linkAnnotation.description())
            .setUrl(linkAnnotation.url())
            .setType(linkAnnotation.type());
    }

    public static String urlTrim(String url){
        if (url.endsWith("/")){
            return removeTrailing(url);
        }

        return url;
    }

    private static String removeTrailing(String s) {
        StringBuilder sb = new StringBuilder(s);
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
