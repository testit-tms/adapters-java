package ru.testit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.testit.annotations.ExternalId;
import ru.testit.models.TestResult;

import java.lang.reflect.Method;

class ExternalIdAngleBracketsTest {

    @Test
    void extractExternalID_doesNotEscapeAngleBrackets() throws Exception {
        class TestClass {
            @ExternalId("<test>")
            void testMethod() {
                // empty
            }
        }

        Method m = TestClass.class.getDeclaredMethod("testMethod");
        Assertions.assertEquals("<test>", Utils.extractExternalID(m, null));
    }

    @Test
    void testResultSetter_doesNotEscapeAngleBrackets() {
        TestResult tr = new TestResult().setExternalId("<test>");
        Assertions.assertEquals("<test>", tr.getExternalId());
    }
}

