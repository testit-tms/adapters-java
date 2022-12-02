package ru.testit.listener;

import ru.testit.models.TestResult;

public interface AdapterListener extends DefaultListener {
    default void beforeTestStop(final TestResult result) {
        //do nothing
    }
}
