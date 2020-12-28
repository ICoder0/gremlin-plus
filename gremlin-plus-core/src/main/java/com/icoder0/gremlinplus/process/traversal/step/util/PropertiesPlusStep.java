package com.icoder0.gremlinplus.process.traversal.step.util;

import com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Configuring;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.FlatMapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Parameters;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.PropertyType;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.*;

/**
 * @author bofa1ex
 * @since 2020/12/23
 */
public class PropertiesPlusStep extends FlatMapStep<Element, Object> implements AutoCloseable, Configuring {

    protected Parameters parameters = new Parameters();
    protected final Map<String,Boolean> propertiesMap;
    protected final String[] propertyKeys;
    protected final PropertyType returnType;

    public PropertiesPlusStep(final Traversal.Admin traversal, final PropertyType propertyType, final Pair<String, Boolean> propertyPair) {
        super(traversal);
        this.returnType = propertyType;
        this.propertiesMap = Collections.singletonMap(propertyPair.getKey(), propertyPair.getValue());
        this.propertyKeys = new String[]{propertyPair.getKey()};
    }

    public PropertiesPlusStep(final Traversal.Admin traversal, final PropertyType returnType, final Map<String, Boolean> propertiesMap) {
        super(traversal);
        this.propertiesMap = propertiesMap;
        this.propertyKeys = propertiesMap.keySet().toArray(new String[]{});
        this.returnType = returnType;
    }

    @Override
    public Parameters getParameters() {
        return this.parameters;
    }

    @Override
    public void configure(final Object... keyValues) {
        this.parameters.set(null, keyValues);
    }

    @Override
    protected Iterator<Object> flatMap(final Traverser.Admin<Element> traverser) {
        final Element element = traverser.get();
        if (this.returnType.equals(PropertyType.VALUE)) {
            return propertiesMap.entrySet().parallelStream().map(entry -> {
                final Object value = element.value(entry.getKey());
                return entry.getValue() ? value : VertexDefinitionSupport.VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null);
            }).iterator();
        }
        return (Iterator) element.properties(propertyKeys);
    }

    public PropertyType getReturnType() {
        return this.returnType;
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, Arrays.asList(this.propertyKeys), this.returnType.name().toLowerCase());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() ^ this.returnType.hashCode();
        for (final String propertyKey : this.propertyKeys) {
            result ^= propertyKey.hashCode();
        }
        return result;
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.OBJECT);
    }

    @Override
    public void close() throws Exception {
        closeIterator();
    }
}
