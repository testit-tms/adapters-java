package ru.testit.services;

import java.lang.annotation.Annotation;

public enum TestMethodType {
    //@formatter:off
    TEST,
    STEP,
    BEFORE_CLASS,
    BEFORE_GROUPS,
    BEFORE_METHOD,
    BEFORE_SUITE,
    BEFORE_TEST,
    AFTER_CLASS,
    AFTER_GROUPS,
    AFTER_METHOD,
    AFTER_SUITE,
    AFTER_TEST;
    //@formatter:on
}
