package ru.testit.listener;

import org.junit.runner.Description;
import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static String extractExternalID(final Description method) {
        final ExternalId annotation = method.getAnnotation(ExternalId.class);
        return (annotation != null) ? annotation.value() : getHash(method.getMethodName());
    }

    public static String extractDisplayName(final Description method) {
        final DisplayName annotation = method.getAnnotation(DisplayName.class);
        return (annotation != null) ? annotation.value() : method.getMethodName();
    }

    public static List<String> extractWorkItemId(final Description method) {
        final List<String> workItemIds = new ArrayList<>();
        final WorkItemId workItem = method.getAnnotation(WorkItemId.class);
        if (workItem != null) {
            workItemIds.add(workItem.value());

            return workItemIds;
        }

        final WorkItemIds workItems = method.getAnnotation(WorkItemIds.class);
        if (workItems != null) {
            for (final String workItemId : workItems.value()) {
                workItemIds.add(workItemId);
            }
        }

        return workItemIds;
    }

    public static List<LinkItem> extractLinks(final Description method) {
        final List<LinkItem> links = new LinkedList<>();
        final Links linksAnnotation = method.getAnnotation(Links.class);
        if (linksAnnotation != null) {
            for (final Link link : linksAnnotation.links()) {
                links.add(makeLink(link));
            }
        } else {
            final Link linkAnnotation = method.getAnnotation(Link.class);
            if (linkAnnotation != null) {
                links.add(makeLink(linkAnnotation));
            }
        }
        return links;
    }

    public static List<Label> extractLabels(final Description method) {
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

    public static String extractTitle(final Description method) {
        final Title annotation = method.getAnnotation(Title.class);

        String title;
        if (annotation == null) {
            title = method.getClassName();
        } else {
            title = annotation.value();
        }

        return title;
    }

    private static LinkItem makeLink(final Link linkAnnotation) {
        return new LinkItem()
                .setTitle(linkAnnotation.title())
                .setDescription(linkAnnotation.description())
                .setUrl(linkAnnotation.url())
                .setType(linkAnnotation.type());
    }

    public static String extractDescription(final Description method) {
        final ru.testit.annotations.Description annotation = method.getAnnotation(ru.testit.annotations.Description.class);
        return (annotation != null) ? annotation.value() : "";
    }

    private static String getHash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
    }
}
