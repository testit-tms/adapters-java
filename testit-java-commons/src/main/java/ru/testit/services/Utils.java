package ru.testit.services;

import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class Utils {
    public static String extractExternalID(final Method atomicTest, Map<String, String> parameters) {
        final ExternalId annotation = atomicTest.getAnnotation(ExternalId.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : null;
    }

    public static String extractDisplayName(final Method atomicTest, Map<String, String> parameters) {
        final DisplayName annotation = atomicTest.getAnnotation(DisplayName.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : null;
    }

    public static List<String> extractWorkItemId(final Method atomicTest, Map<String, String> parameters) {
        final List<String> workItemIds = new ArrayList<>();
        final WorkItemId workItem = atomicTest.getAnnotation(WorkItemId.class);
        if (workItem != null) {
            workItemIds.add(setParameters(workItem.value(), parameters));
            return workItemIds;
        }

        final WorkItemIds workItems = atomicTest.getAnnotation(WorkItemIds.class);
        if (workItems != null) {
            for (final String workItemId : workItems.value()) {
                workItemIds.add(setParameters(workItemId, parameters));
            }
        }

        return workItemIds;
    }

    public static List<LinkItem> extractLinks(final Method atomicTest, Map<String, String> parameters) {
        final List<LinkItem> links = new LinkedList<>();
        final Links linksAnnotation = atomicTest.getAnnotation(Links.class);
        if (linksAnnotation != null) {
            for (final Link link : linksAnnotation.links()) {
                links.add(makeLink(link, parameters));
            }
        } else {
            final Link linkAnnotation = atomicTest.getAnnotation(Link.class);
            if (linkAnnotation != null) {
                links.add(makeLink(linkAnnotation, parameters));
            }
        }
        return links;
    }

    public static List<Label> extractLabels(final Method atomicTest, Map<String, String> parameters) {
        final List<Label> labels = new LinkedList<>();
        final Labels annotation = atomicTest.getAnnotation(Labels.class);
        if (annotation != null) {
            for (final String s : annotation.value()) {
                final Label label = new Label()
                        .setName(setParameters(s, parameters));
                labels.add(label);
            }
        }
        return labels;
    }

    public static String extractDescription(final Method atomicTest, Map<String, String> parameters) {
        final Description annotation = atomicTest.getAnnotation(Description.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : null;
    }

    public static String extractTitle(final Method atomicTest, Map<String, String> parameters) {
        final Title annotation = atomicTest.getAnnotation(Title.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : null;
    }

    private static LinkItem makeLink(final Link linkAnnotation, Map<String, String> parameters) {
        return new LinkItem()
                .setTitle(setParameters(linkAnnotation.title(), parameters))
                .setDescription(setParameters(linkAnnotation.description(), parameters))
                .setUrl(setParameters(linkAnnotation.url(), parameters))
                .setType(linkAnnotation.type());
    }

    public static String urlTrim(String url) {
        if (url.endsWith("/")) {
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

    private static String setParameters(String value, Map<String, String> parameters) {
        if (!isNull(parameters) && !isNull(value)) {
            Pattern pattern = Pattern.compile("\\{\\s*(\\w+)}");
            Matcher matcher = pattern.matcher(value);

            while (matcher.find()) {
                String parameterName = matcher.group(1);
                String parameterValue = parameters.get(parameterName);

                if (!isNull(parameterValue)) {
                    value = value.replace(String.format("{%s}", parameterName), parameters.get(parameterName));
                }
            }
        }

        return value;
    }
}
