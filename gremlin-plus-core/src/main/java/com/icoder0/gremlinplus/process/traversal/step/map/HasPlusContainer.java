package com.icoder0.gremlinplus.process.traversal.step.map;

import com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Property;

/**
 * @author bofa1ex
 * @since 2020/12/24
 */
public class HasPlusContainer extends HasContainer {

    protected final boolean serialized;
    protected final String key;
    protected final P predicate;

    public HasPlusContainer(final String key, final boolean serialized, final P<?> predicate) {
        super(key, predicate);
        this.key = key;
        this.predicate = predicate;
        this.serialized = serialized;
    }

    @Override
    protected boolean testValue(Property property) {
        return this.predicate.test(
                serialized ?
                property.value() : VertexDefinitionSupport.VERTEX_UNSERIALIZED_MAP.getOrDefault(property.value(), null)
        );
    }
}
