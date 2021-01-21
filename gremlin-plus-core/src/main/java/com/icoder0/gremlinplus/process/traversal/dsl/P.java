package com.icoder0.gremlinplus.process.traversal.dsl;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * @author bofa1ex
 * @since 2020/12/14
 */
public class P<V> extends org.apache.tinkerpop.gremlin.process.traversal.P<V> {

    static final Logger log = LoggerFactory.getLogger(P.class);

    public P(BiPredicate<V, V> biPredicate, V value) {
        super(biPredicate, value);
    }

    /**
     * Determines a empty predicate when property is not support serializable.
     */
    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> empty() {
        return new P<>((ignore0,ignore1) -> true, null);
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
     *
     * @since 3.0.0-incubating
     */
    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> flatWithin(final V... values) {
        return flatWithin(Arrays.asList(values));
    }

    public static <V extends CharSequence> org.apache.tinkerpop.gremlin.process.traversal.P<V> regex(final V value){
        if (StringUtils.isBlank(value)){
            log.warn("正则表达式为空, P.regex => P.empty");
            return empty();
        }
        return new P(TextPlus.regex, value);
    }

    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> notnull(){
        return new P(Compare.neq, null);
    }

    public static <V> org.apache.tinkerpop.gremlin.process.traversal.P<V> isnull(){
        return new P(Compare.eq, null);
    }
}
