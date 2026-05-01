package ru.testit.writers;

import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;

public interface Writer {

    /** @return {@code false} if the result could not be written to Test IT */
    boolean writeTestRealtime(TestResult testResult);

    void writeTest(TestResult testResult);

    void writeClass(ClassContainer container);

    void writeTests(MainContainer container);

    String writeAttachment(String path);
}
