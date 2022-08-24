package ru.testit.annotations;

import ru.testit.models.LinkType;
import ru.testit.services.Adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated This annotation is no longer acceptable to compute time between versions.
 * <p> Use {@link WorkItemIds} instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Deprecated
public @interface WorkItemId {
    String value();
}


