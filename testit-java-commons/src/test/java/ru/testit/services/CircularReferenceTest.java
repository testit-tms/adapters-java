package ru.testit.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class CircularReferenceTest {

    @Test
    void testEscapeHtmlInObjectWithCircularReference() {
        // Create objects with circular reference
        ParentObject parent = new ParentObject();
        parent.name = "Parent <div>";

        ChildObject child = new ChildObject();
        child.name = "Child <script>";
        child.parent = parent; // Circular reference

        parent.child = child;

        // This should not cause StackOverflowError
        HtmlEscapeUtils.escapeHtmlInObject(parent);

        // Verify that HTML was escaped correctly
        Assertions.assertEquals("Parent &lt;div&gt;", parent.name);
        Assertions.assertEquals("Child &lt;script&gt;", child.name);
    }

    @Test
    void testEscapeHtmlInObjectWithSelfReference() {
        // Create object with self-reference
        SelfReferencingObject obj = new SelfReferencingObject();
        obj.name = "Self <object>";
        obj.self = obj; // Self-reference

        // This should not cause StackOverflowError
        HtmlEscapeUtils.escapeHtmlInObject(obj);

        // Verify that HTML was escaped correctly
        Assertions.assertEquals("Self &lt;object&gt;", obj.name);
    }

    // Test classes for circular reference testing
    private static class ParentObject {
        String name;
        ChildObject child;
    }

    private static class ChildObject {
        String name;
        ParentObject parent;
    }

    private static class SelfReferencingObject {
        String name;
        SelfReferencingObject self;
    }
}
