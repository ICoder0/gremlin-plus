package com.icoder0.gremlinplus.process.traversal.dsl;


import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * @author bofa1ex
 * @since 2020/12/14
 */
public enum Flat implements BiPredicate<Collection<Object>, Collection<Object>> {
    within(){
        @Override
        public boolean test(Collection first, Collection second) {
            for (Object item : second) {
                if (first.contains(item)) {
                    return true;
                }
            }
            return false;
        }
    };
}
