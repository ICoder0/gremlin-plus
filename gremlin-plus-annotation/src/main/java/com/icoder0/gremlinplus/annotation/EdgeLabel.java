package com.icoder0.gremlinplus.annotation;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/12/14
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EdgeLabel {
    String value() default "";
}
