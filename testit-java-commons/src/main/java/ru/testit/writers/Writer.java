package ru.testit.writers;

import ru.testit.models.ClassContainer;
import ru.testit.models.MainContainer;
import ru.testit.models.TestResult;

public interface Writer {
    void startLaunch();

    void finishLaunch();

    void writeTest(TestResult testResult);

    void writeClass(ClassContainer container);

    void writeTests(MainContainer container);
}
