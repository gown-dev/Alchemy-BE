package alchemy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Logged {

    String value() default "";
    boolean logEntry() default true;
    boolean logExit() default true;
    boolean logSuccess() default true;
    boolean logFailure() default true;

}