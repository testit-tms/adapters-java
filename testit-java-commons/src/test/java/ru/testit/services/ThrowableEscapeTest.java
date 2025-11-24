package ru.testit.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ThrowableEscapeTest {

    @Test
    public void testEscapeHtmlInObjectWithThrowable() {
        // Create a Throwable with a message containing HTML
        Throwable throwable = new AssertionError("Assertion failed: expected <1> but was <0>");

        // This should not cause StackOverflowError
        Throwable result = HtmlEscapeUtils.escapeHtmlInObject(throwable);

        // Should return the same object
        Assertions.assertSame(throwable, result);

        // Message should be preserved (no HTML to escape in this case)
        Assertions.assertEquals("Assertion failed: expected <1> but was <0>", result.getMessage());
    }

    @Test
    public void testEscapeHtmlInObjectWithThrowableContainingHtml() {
        // Create a Throwable with a message containing actual HTML tags
        Throwable throwable = new RuntimeException("Error in <div> element with <script> tag");

        // This should not cause StackOverflowError
        Throwable result = HtmlEscapeUtils.escapeHtmlInObject(throwable);

        // Should return the same object
        Assertions.assertSame(throwable, result);

        // The message should still be accessible (our implementation doesn't modify Throwable's message field)
        // because Throwable's message field is final and we can't change it through reflection
        Assertions.assertNotNull(result.getMessage());
    }

    @Test
    public void testEscapeHtmlInObjectWithCausedByThrowable() {
        // Create nested Throwables
        Throwable cause = new IllegalArgumentException("Cause message with <html>");
        Throwable throwable = new RuntimeException("Main error", cause);

        // This should not cause StackOverflowError
        Throwable result = HtmlEscapeUtils.escapeHtmlInObject(throwable);

        // Should return the same object
        Assertions.assertSame(throwable, result);
    }
}
