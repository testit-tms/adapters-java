package ru.testit.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import java.util.List;

class UtilsEscapeTest {

    @Test
    void testEscapeHtmlTags() {
        // Test escaping < anywhere
        String input1 = "Test <div> here";
        String result1 = HtmlEscapeUtils.escapeHtmlTags(input1);

        Assertions.assertEquals("Test \\<div\\> here", result1);

        // Test escaping self-closing tag
        String input2 = "Test <br/> here";
        String result2 = HtmlEscapeUtils.escapeHtmlTags(input2);

        Assertions.assertEquals("Test \\<br/\\> here", result2);

        // Test escaping at start of string
        String input3 = "<div>";
        String result3 = HtmlEscapeUtils.escapeHtmlTags(input3);

        Assertions.assertEquals("\\<div\\>", result3);

        // Test complex case with multiple tags
        String input4 = "<div>content</div>";
        String result4 = HtmlEscapeUtils.escapeHtmlTags(input4);

        Assertions.assertEquals("\\<div\\>content\\</div\\>", result4);

        // Test just < symbol (should NOT be escaped - no HTML tags)
        String input5 = "value < 10";
        String result5 = HtmlEscapeUtils.escapeHtmlTags(input5);

        Assertions.assertEquals("value < 10", result5);

        // Test just > symbol (should NOT be escaped - no HTML tags)
        String input6 = "value > 5";
        String result6 = HtmlEscapeUtils.escapeHtmlTags(input6);

        Assertions.assertEquals("value > 5", result6);

        // Test null input
        Assertions.assertNull(HtmlEscapeUtils.escapeHtmlTags(null));

        // Test empty string
        Assertions.assertEquals("", HtmlEscapeUtils.escapeHtmlTags(""));

        // Test string without HTML tags
        String input7 = "Normal text without tags";
        Assertions.assertEquals(input7, HtmlEscapeUtils.escapeHtmlTags(input7));
        
        // Test string with HTML-like symbols but no actual tags
        String input8 = "Expression: a < b && b > c";
        Assertions.assertEquals(input8, HtmlEscapeUtils.escapeHtmlTags(input8));
    }

    @Test
    void testEscapeHtmlTagsAvoidDoubleEscaping() {
        // Test that already escaped characters are not escaped again (no HTML tags, so no changes)
        String input1 = "Already escaped \\< and \\>";
        String result1 = HtmlEscapeUtils.escapeHtmlTags(input1);
        Assertions.assertEquals("Already escaped \\< and \\>", result1);

        // Test mixed escaped and non-escaped (no HTML tags, so no changes)
        String input2 = "Mixed \\< escaped < not escaped >";
        String result2 = HtmlEscapeUtils.escapeHtmlTags(input2);
        Assertions.assertEquals("Mixed \\< escaped < not escaped >", result2);

        // Test complex case with tags and already escaped content
        String input3 = "\\<already\\> and <new> tags";
        String result3 = HtmlEscapeUtils.escapeHtmlTags(input3);
        Assertions.assertEquals("\\<already\\> and \\<new\\> tags", result3);

        // Test multiple backslashes before < or > (no HTML tags, so no changes)
        String input4 = "\\\\< and \\\\>";
        String result4 = HtmlEscapeUtils.escapeHtmlTags(input4);
        Assertions.assertEquals("\\\\< and \\\\>", result4);
        
        // Test avoid double escaping WITH HTML tags
        String input5 = "Already escaped \\<tag> and new <div> tags";
        String result5 = HtmlEscapeUtils.escapeHtmlTags(input5);
        Assertions.assertEquals("Already escaped \\<tag\\> and new \\<div\\> tags", result5);
    }

    @Test
    void testHtmlTagDetection() {
        // Test HTML tag detection - these should be escaped
        String input1 = "Text with <div> tag";
        String result1 = HtmlEscapeUtils.escapeHtmlTags(input1);
        Assertions.assertEquals("Text with \\<div\\> tag", result1);
        
        // Test self-closing tag detection
        String input2 = "Text with <br/> tag";
        String result2 = HtmlEscapeUtils.escapeHtmlTags(input2);
        Assertions.assertEquals("Text with \\<br/\\> tag", result2);
        
        // Test tag with attributes
        String input3 = "Text with <a href='link'> tag";
        String result3 = HtmlEscapeUtils.escapeHtmlTags(input3);
        Assertions.assertEquals("Text with \\<a href='link'\\> tag", result3);
        
        // Test non-HTML < and > - these should NOT be escaped
        String input4 = "Math: 5 < 10 and 20 > 15";
        String result4 = HtmlEscapeUtils.escapeHtmlTags(input4);
        Assertions.assertEquals("Math: 5 < 10 and 20 > 15", result4);
        
        // Test mixed content - only HTML parts should be escaped
        String input5 = "Expression: a < b, but also <span>text</span>";
        String result5 = HtmlEscapeUtils.escapeHtmlTags(input5);
        Assertions.assertEquals("Expression: a \\< b, but also \\<span\\>text\\</span\\>", result5);
    }

    @Test
    void testEscapeHtmlTagsWithEnvironmentVariable() {
        // Save original environment (not possible to modify in runtime for this test)
        // This test demonstrates the behavior when NO_ESCAPE_HTML would be set
        
        String input = "<script>alert('test')</script>";
        
        // Test with escaping enabled (default behavior)
        String result = HtmlEscapeUtils.escapeHtmlTags(input);
        
        // Should be escaped normally when env var is not set or not "true"
        Assertions.assertEquals("\\<script\\>alert('test')\\</script\\>", result);
        
        // Note: Cannot easily test environment variable modification in unit tests
        // without additional test setup or mocking framework
    }

    @Test
    void testEscapeHtmlInObject() {
        // Test object with String fields
        TestObject obj = new TestObject();
        obj.name = "Test <script>";
        obj.description = "<b>Bold</b> text";
        obj.number = 123;
        
        HtmlEscapeUtils.escapeHtmlInObject(obj);
        
        Assertions.assertEquals("Test \\<script\\>", obj.name);
        Assertions.assertEquals("\\<b\\>Bold\\</b\\> text", obj.description);
        Assertions.assertEquals(123, obj.number);
    }

    @Test
    void testEscapeHtmlInObjectWithStringList() {
        // Test object with List<String> field
        TestObjectWithStringList obj = new TestObjectWithStringList();
        obj.name = "Test <object>";
        obj.tags = new ArrayList<>();
        obj.tags.add("tag1 <html>");
        obj.tags.add("<script>alert('xss')</script>");
        obj.tags.add("normal tag");
        
        HtmlEscapeUtils.escapeHtmlInObject(obj);
        
        // Check that String field is escaped
        Assertions.assertEquals("Test \\<object\\>", obj.name);
        
        // Check that all strings in list are escaped
        Assertions.assertEquals("tag1 \\<html\\>", obj.tags.get(0));
        Assertions.assertEquals("\\<script\\>alert('xss')\\</script\\>", obj.tags.get(1));
        Assertions.assertEquals("normal tag", obj.tags.get(2));
    }

    @Test
    void testEscapeHtmlInObjectWithObjectList() {
        // Test object with List<Object> field
        TestObjectWithObjectList parent = new TestObjectWithObjectList();
        parent.name = "Parent <object>";
        parent.children = new ArrayList<>();
        
        TestObject child1 = new TestObject();
        child1.name = "Child1 <div>";
        child1.description = "<span>description</span>";
        parent.children.add(child1);
        
        TestObject child2 = new TestObject();
        child2.name = "Child2 <p>";
        child2.description = "<b>another</b> description";
        parent.children.add(child2);
        
        HtmlEscapeUtils.escapeHtmlInObject(parent);
        
        // Check that parent String field is escaped
        Assertions.assertEquals("Parent \\<object\\>", parent.name);
        
        // Check that all objects in list are escaped
        Assertions.assertEquals("Child1 \\<div\\>", parent.children.get(0).name);
        Assertions.assertEquals("\\<span\\>description\\</span\\>", parent.children.get(0).description);
        
        Assertions.assertEquals("Child2 \\<p\\>", parent.children.get(1).name);
        Assertions.assertEquals("\\<b\\>another\\</b\\> description", parent.children.get(1).description);
    }

    @Test
    void testEscapeHtmlInObjectWithEmptyList() {
        // Test object with empty list
        TestObjectWithStringList obj = new TestObjectWithStringList();
        obj.name = "Test <object>";
        obj.tags = new ArrayList<>(); // empty list
        
        HtmlEscapeUtils.escapeHtmlInObject(obj);
        
        Assertions.assertEquals("Test \\<object\\>", obj.name);
        Assertions.assertTrue(obj.tags.isEmpty());
    }

    @Test
    void testEscapeHtmlInObjectWithNullList() {
        // Test object with null list
        TestObjectWithStringList obj = new TestObjectWithStringList();
        obj.name = "Test <object>";
        obj.tags = null;
        
        HtmlEscapeUtils.escapeHtmlInObject(obj);
        
        Assertions.assertEquals("Test \\<object\\>", obj.name);
        Assertions.assertNull(obj.tags);
    }

    @Test
    void testEscapeHtmlInNestedObjects() {
        // Test recursive processing of nested objects
        NestedTestObject parent = new NestedTestObject();
        parent.name = "Parent <div>";
        parent.child = new TestObject();
        parent.child.name = "Child <script>";
        parent.child.description = "<b>nested</b> content";
        parent.child.number = 42;
        
        HtmlEscapeUtils.escapeHtmlInObject(parent);
        
        // Check parent fields are escaped
        Assertions.assertEquals("Parent \\<div\\>", parent.name);
        
        // Check nested object fields are escaped
        Assertions.assertEquals("Child \\<script\\>", parent.child.name);
        Assertions.assertEquals("\\<b\\>nested\\</b\\> content", parent.child.description);
        Assertions.assertEquals(42, parent.child.number);
    }

    @Test
    void testEscapeHtmlInDeeplyNestedObjects() {
        // Test deeply nested objects
        DeeplyNestedTestObject root = new DeeplyNestedTestObject();
        root.title = "Root <html>";
        root.level1 = new NestedTestObject();
        root.level1.name = "Level1 <div>";
        root.level1.child = new TestObject();
        root.level1.child.name = "Level2 <span>";
        root.level1.child.description = "<p>deep content</p>";
        
        HtmlEscapeUtils.escapeHtmlInObject(root);
        
        // Check all levels are escaped
        Assertions.assertEquals("Root \\<html\\>", root.title);
        Assertions.assertEquals("Level1 \\<div\\>", root.level1.name);
        Assertions.assertEquals("Level2 \\<span\\>", root.level1.child.name);
        Assertions.assertEquals("\\<p\\>deep content\\</p\\>", root.level1.child.description);
    }

    @Test
    void testEscapeHtmlWithSimpleTypes() {
        // Test that simple types are not processed recursively
        ObjectWithSimpleTypes obj = new ObjectWithSimpleTypes();
        obj.name = "Test <object>";
        obj.number = 123;
        obj.isActive = true;
        obj.date = new java.util.Date();
        obj.uuid = java.util.UUID.randomUUID();
        obj.bigDecimal = new java.math.BigDecimal("123.45");
        obj.enumValue = TestEnum.VALUE1;
        
        HtmlEscapeUtils.escapeHtmlInObject(obj);
        
        // Only String field should be escaped, others should remain unchanged
        Assertions.assertEquals("Test \\<object\\>", obj.name);
        Assertions.assertEquals(123, obj.number);
        Assertions.assertTrue(obj.isActive);
        Assertions.assertNotNull(obj.date);
        Assertions.assertNotNull(obj.uuid);
        Assertions.assertEquals(new java.math.BigDecimal("123.45"), obj.bigDecimal);
        Assertions.assertEquals(TestEnum.VALUE1, obj.enumValue);
    }

    // Test classes for reflection testing
    private static class TestObject {
        String name;
        String description;
        int number;
    }

    private static class TestObjectWithStringList {
        String name;
        List<String> tags;
    }

    private static class TestObjectWithObjectList {
        String name;
        List<TestObject> children;
    }

    private static class NestedTestObject {
        String name;
        TestObject child;
    }

    private static class DeeplyNestedTestObject {
        String title;
        NestedTestObject level1;
    }

    private static class ObjectWithSimpleTypes {
        String name;
        int number;
        boolean isActive;
        java.util.Date date;
        java.util.UUID uuid;
        java.math.BigDecimal bigDecimal;
        TestEnum enumValue;
    }

    private enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }
} 