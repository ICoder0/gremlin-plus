package com.icoder0.gremlinplus.annotation;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/12/4
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphLabel {

    String value() default "";
}
