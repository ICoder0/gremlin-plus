package com.icoder0.gremlinplus.process.traversal.dsl;

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

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


    private static boolean itemIsCollection(Object value){
        return value instanceof Collection;
    }

    private static boolean bothAreCollection(Object first, Object second) {
        return first instanceof Collection && second instanceof Collection;
    }
}
