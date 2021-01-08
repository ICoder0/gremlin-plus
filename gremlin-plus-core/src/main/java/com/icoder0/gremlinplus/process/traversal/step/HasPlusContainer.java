package com.icoder0.gremlinplus.process.traversal.step;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Property;


import static com.icoder0.gremlinplus.process.traversal.toolkit.UnSerializedPropertySupport.getUnSerializedPropertyCache;

/**
 * @author bofa1ex
 * @since 2020/12/24
 */
public class HasPlusContainer extends HasContainer {

    protected final boolean serialized;
    protected final P predicate;

    public HasPlusContainer(final String key, final boolean serialized, final P<?> predicate) {
        super(key, serialized ? predicate : com.icoder0.gremlinplus.process.traversal.dsl.P.empty());
        this.predicate = predicate;
        this.serialized = serialized;
    }

    @Override
    protected boolean testValue(Property property) {
        return this.predicate.test(
                serialized ? property.value() : getUnSerializedPropertyCache().get(property.value())
        );
    }
}
