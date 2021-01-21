package com.icoder0.gremlinplus.process.traversal.dsl;

import java.util.function.BiPredicate;

/**
 * @author bofa1ex
 * @since 2021/1/19
 */
public enum TextPlus implements BiPredicate<Object, String> {
    regex() {
        @Override
        public boolean test(Object src, String pattern) {
            return src.toString().matches(pattern);
        }
    },
    notBlank() {
        @Override
        public boolean test(Object src, String ignored) {
            return src != null && !src.toString().isEmpty();
        }
    },
    blank() {
        @Override
        public boolean test(Object src, String ignored) {
            return src != null && src.toString().isEmpty();
        }
    }
}
