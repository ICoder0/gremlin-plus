package com.icoder0.gremlinplus.process.traversal.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * @author bofa1ex
 * @since 2020/12/14
 */
public class P<V> extends org.apache.tinkerpop.gremlin.process.traversal.P<V> {

    public P(BiPredicate<V, V> biPredicate, V value) {
        super(biPredicate, value);
    }

    /**
     * Determines if a value is within the specified list of values.
     *
     * @since 3.0.0-incubating
     */
    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> flatWithin(final Collection<V> value) {
        return new P(Flat.within, value);
    }

    /**
     * Determines if a value is not within the specified list of values.
     *
     * @since 3.0.0-incubating
     */
    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> flatWithin(final V... values) {
        return flatWithin(Arrays.asList(values));
    }
}
