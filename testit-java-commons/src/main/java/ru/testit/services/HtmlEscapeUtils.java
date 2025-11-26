package ru.testit.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class HtmlEscapeUtils {

    private static final String NO_ESCAPE_HTML = System.getenv(
            "NO_ESCAPE_HTML"
    );

    // Regex pattern to detect HTML tags
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<\\S.*?(?:>|/>)"
    );

    // Regex patterns to escape only non-escaped characters
    private static final Pattern LESS_THAN_PATTERN = Pattern.compile("<");
    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(">");

    /**
     * Escapes HTML tags to prevent XSS attacks.
     * First checks if the string contains HTML tags using regex pattern.
     * Only performs escaping if HTML tags are detected.
     * Escapes all &lt; as \&lt; and &gt; as \&gt; only if they are not already escaped.
     * Uses regex with negative lookbehind to avoid double escaping.
     *
     * @param text The text to escape
     * @return Escaped text or original text if no HTML tags found
     */
    public static String escapeHtmlTags(String text) {
        if (text == null) {
            return null;
        }

        // First check if the string contains HTML tags
        if (!HTML_TAG_PATTERN.matcher(text).find()) {
            return text; // No HTML tags found, return original string
        }

        // Use regex with negative lookbehind to escape only non-escaped characters
        String result = LESS_THAN_PATTERN.matcher(text).replaceAll("&lt;");
        result = GREATER_THAN_PATTERN.matcher(result).replaceAll("&gt;");

        return result;
    }

    /**
     * Checks if a type is a simple type that doesn't need HTML escaping
     *
     * @param clazz Type to check
     * @return True if it's a simple type
     */
    private static boolean isSimpleType(Class<?> clazz) {
        return (
                clazz.isPrimitive() ||
                        clazz.isEnum() ||
                        clazz == String.class ||
                        clazz == Boolean.class ||
                        clazz == Byte.class ||
                        clazz == Character.class ||
                        clazz == Short.class ||
                        clazz == Integer.class ||
                        clazz == Long.class ||
                        clazz == Float.class ||
                        clazz == Double.class ||
                        clazz == java.math.BigDecimal.class ||
                        clazz == java.math.BigInteger.class ||
                        clazz == java.util.Date.class ||
                        clazz == java.time.LocalDate.class ||
                        clazz == java.time.LocalDateTime.class ||
                        clazz == java.time.LocalTime.class ||
                        clazz == java.time.ZonedDateTime.class ||
                        clazz == java.time.OffsetDateTime.class ||
                        clazz == java.time.Instant.class ||
                        clazz == java.time.Duration.class ||
                        clazz == java.util.UUID.class ||
                        clazz == java.util.Optional.class
        );
    }

    /**
     * Escapes HTML tags in all String fields of an object using reflection
     * Also processes List fields: if List of objects - calls escapeHtmlInObjectList,
     * Can be disabled by setting NO_ESCAPE_HTML environment variable to "true"
     * if List of Strings - escapes each string
     *
     * @param obj The object to process
     * @return The processed object with escaped strings
     */
    public static <T> T escapeHtmlInObject(T obj) {
        return escapeHtmlInObject(obj, new HashSet<>());
    }

    /**
     * Internal method with cycle detection to prevent infinite recursion
     *
     * @param obj     The object to process
     * @param visited Set of already visited objects to detect cycles
     * @return The processed object with escaped strings
     */
    private static <T> T escapeHtmlInObject(T obj, Set<Object> visited) {
        if (obj == null) {
            return null;
        }

        // Check if escaping is disabled via environment variable
        if ("true".equalsIgnoreCase(NO_ESCAPE_HTML)) {
            return obj;
        }

        // Check for cycles to prevent infinite recursion
        if (!isSimpleType(obj.getClass()) && visited.contains(obj)) {
            // Already processed this object, skip to prevent cycles
            return obj;
        }

        try {
            // Add to visited set to track cycles
            if (!isSimpleType(obj.getClass())) {
                visited.add(obj);
            }

            Class<?> clazz = obj.getClass();

            // Process all declared fields
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (value instanceof String) {
                    // Escape String fields
                    field.set(obj, escapeHtmlTags((String) value));
                } else if (value instanceof java.util.List) {
                    // Process List fields
                    field.set(
                            obj,
                            escapeHtmlInObjectList((List<?>) value, visited)
                    );
                } else if (value != null && !isSimpleType(value.getClass())) {
                    // Process nested objects (non-primitives)
                    escapeHtmlInObject(value, visited);
                }
            }
        } catch (Exception e) {
            // Silently ignore reflection errors
        } finally {
            // Remove from visited set when done processing this object
            if (!isSimpleType(obj.getClass())) {
                visited.remove(obj);
            }
        }

        return obj;
    }

    /**
     * Escapes HTML tags in all String fields of objects in a list using reflection.
     * Can be disabled by setting NO_ESCAPE_HTML environment variable to "true".
     *
     * @param list The list of objects to process
     * @return The processed list with escaped strings in all objects, or null if input is null
     */
    public static <T> List<T> escapeHtmlInObjectList(List<T> list) {
        return escapeHtmlInObjectList(list, new HashSet<>());
    }

    /**
     * Internal method with cycle detection to prevent infinite recursion
     *
     * @param list    The list of objects to process
     * @param visited Set of already visited objects to detect cycles
     * @return The processed list with escaped strings in all objects, or null if input is null
     */
    private static <T> List<T> escapeHtmlInObjectList(
            List<T> list,
            Set<Object> visited
    ) {
        if (list == null) {
            return null;
        }

        // Check if escaping is disabled via environment variable
        if ("true".equalsIgnoreCase(NO_ESCAPE_HTML)) {
            return list;
        }

        Object firstElement = list.get(0);

        if (firstElement instanceof String) {
            // List of Strings - escape each string
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                if (element instanceof String) {
                    list.set(i, (T) escapeHtmlTags((String) element));
                }
            }
        } else if (firstElement != null) {
            // List of objects - process each object
            for (T item : list) {
                escapeHtmlInObject(item, visited);
            }
        }

        return list;
    }
}
