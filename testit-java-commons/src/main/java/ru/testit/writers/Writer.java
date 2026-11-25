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

    /** Called once when the last main container of this process is finalized. */
    default void onAllMainContainersFinished() {
    }

    /**
     * SyncStorage {@code completed} failed / unavailable: finalize deferred InProgress cuts
     * via sendTestResults (same path as Work X; never PUT outcome).
     */
    default void finalizeDeferredSyncCuts() {
    }
}
