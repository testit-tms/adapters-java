package ru.testit.services;

import ru.testit.annotations.*;
import ru.testit.models.Label;
import ru.testit.models.LinkItem;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class Utils {

    private Utils() {}

    public static String extractExternalID(final Method atomicTest, Map<String, String> parameters) {
        final ExternalId annotation = atomicTest.getAnnotation(ExternalId.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : getHash(atomicTest.getDeclaringClass().getName() + atomicTest.getName());
    }

    public static String extractDisplayName(final Method atomicTest, Map<String, String> parameters) {
        final DisplayName annotation = atomicTest.getAnnotation(DisplayName.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : atomicTest.getName();
    }

    public static List<String> extractWorkItemIds(final Method atomicTest, Map<String, String> parameters) {
        final List<String> workItemIds = new ArrayList<>();
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

    public static String extractClassname(final Method atomicTest, String className, Map<String, String> parameters) {
        Classname annotation = atomicTest.getAnnotation(Classname.class);

        if (annotation == null) {
            annotation = atomicTest.getDeclaringClass().getAnnotation(Classname.class);
        }

        return (annotation != null) ? setParameters(annotation.value(), parameters) : setParameters(className, parameters);
    }

    public static String extractNamespace(final Method atomicTest, String nameSpace, Map<String, String> parameters) {
        Namespace annotation = atomicTest.getAnnotation(Namespace.class);

        if (annotation == null) {
            annotation = atomicTest.getDeclaringClass().getAnnotation(Namespace.class);
        }

        return (annotation != null) ? setParameters(annotation.value(), parameters) : setParameters(nameSpace, parameters);
    }

    public static String extractDescription(final Method atomicTest, Map<String, String> parameters) {
        final Description annotation = atomicTest.getAnnotation(Description.class);
        return (annotation != null) ? setParameters(annotation.value(), parameters) : "";
    }

    public static String extractTitle(final Method atomicTest, Map<String, String> parameters, boolean isTestMehhod) {
        final Title annotation = atomicTest.getAnnotation(Title.class);

        String title;
        if (annotation != null) {
            title = annotation.value();
        } else if (isTestMehhod) {
            title = null;
        }
        else {
            title = atomicTest.getName();
        }

        return setParameters(title, parameters);
    }

    public static String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
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

    public static String setParameters(String value, Map<String, String> parameters) {
        if (!isNull(parameters) && !isNull(value)) {
            Pattern pattern = Pattern.compile("\\{\\s*([\\w\\s]+)}");
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

    public static String getHash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return convertToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
    }

    private static String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText.toUpperCase();
    }
}
