package com.Jeka8833.GenomeTests.console.console;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Param {
    String description() default "";

    String defaultValue() default "";

    String[] key();

    boolean need() default false;
}
