package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.toolkit.AnnotationSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.KeyGeneratorSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.UnSerializedPropertySupport;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;

import java.util.Map;
import java.util.Optional;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.getVertexDefinitionCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveProperties;


/**
 * @author bofa1ex
 * @since 2020/12/30
 */
public class GraphPlusQueryTraversal<S, E, L> extends GraphPlusTerminalTraversal<S, E, L> {

    public GraphPlusQueryTraversal(TraversalSource traversalSource) {
        super(traversalSource);
    }

    public GraphPlusQueryTraversal(TraversalSource traversalSource, Class<L> labelEntityClass) {
        super(traversalSource, labelEntityClass);
    }
    public Pair<L, Vertex> getIfAbsent(L l) {
        if (this.asAdmin().hasNext()) {
            return toPair();
        }
        this.locked = false;
        final Class<?> vertexClazz = l.getClass();
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(vertexClazz, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(vertexClazz))
                .withVertexPropertyDefinitionMap(resolveProperties(vertexClazz))
                .withBeanMap(BeanMap.create(l))
                .build()
        );
        final String label = vertexDefinition.getLabel();
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap = vertexDefinition.getVertexPropertyDefinitionMap();

        final Vertex vertex = this.addStep(new AddVertexStartStep(this, label)).next();
        for (Object key : beanMap.keySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = vertexPropertyDefinitionMap.get((String) key);
            final String propertyName = vertexPropertyDefinition.getPropertyName();
            // 如果是主键id, 跳过property赋值.
            if (vertexPropertyDefinition.isPrimaryKey()) {
                // vertex#id赋值
                beanMap.put(l, key, vertex.id());
                continue;
            }
            // 如果该字段不支持持久化.
            if (!vertexPropertyDefinition.isSerializable()) {
                Optional.ofNullable(beanMap.get(l, key)).ifPresent(value -> {
                    final Object genKey = KeyGeneratorSupport.generate();
                    vertex.property(propertyName, genKey);
                    UnSerializedPropertySupport.getUnSerializedPropertyCache().put(genKey, value);
                });
                continue;
            }

            Optional.ofNullable(beanMap.get(l, key)).ifPresent(value -> vertex.property(propertyName, value));
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return Pair.of(l, vertex);
    }


    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> property(SerializedFunction<L, R> func, R value, Object... keyValues) {
        return (GraphPlusQueryTraversal<S, E, L>) super.property(func, value, keyValues);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> property(SerializedFunction<L, R> func, Traversal<S, E> vertexTraversal) {
        return (GraphPlusQueryTraversal<S, E, L>) super.property(func, vertexTraversal);
    }

    @Override
    public <L2> GraphPlusQueryTraversal<S, E, L2> hasLabel(Class<L2> clazz) {
        return (GraphPlusQueryTraversal<S, E, L2>) super.hasLabel(clazz);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(SerializedFunction<L, R> func, P<R> predicate) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(func, predicate);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(SerializedFunction<L, R> func) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(func);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(SerializedFunction<L, R> func, Object value) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(func, value);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(Class<L> labelClazz, SerializedFunction<L, R> func, com.icoder0.gremlinplus.process.traversal.dsl.P<R> predicate) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(labelClazz, func, predicate);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(Class<L> labelClazz, SerializedFunction<L, R> func, R value) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(labelClazz, func, value);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> has(SerializedFunction<L, R> func, Traversal<S, E> propertyTraversal) {
        return (GraphPlusQueryTraversal<S, E, L>) super.has(func, propertyTraversal);
    }

    @Override
    public <R> GraphPlusQueryTraversal<S, E, L> hasNot(SerializedFunction<L, R> func) {
        return (GraphPlusQueryTraversal<S, E, L>) super.hasNot(func);
    }

    @Override
    public GraphPlusQueryTraversal<S, Vertex, L> out(Class<?>... edgeClass) {
        return (GraphPlusQueryTraversal<S, Vertex, L>) super.out(edgeClass);
    }

    @Override
    public GraphPlusQueryTraversal<S, Vertex, L> in(Class<?>... edgeClass) {
        return (GraphPlusQueryTraversal<S, Vertex, L>) super.in(edgeClass);
    }

    @Override
    public GraphPlusQueryTraversal<S, Vertex, L> both(Class<?>... edgeClass) {
        return (GraphPlusQueryTraversal<S, Vertex, L>) super.both(edgeClass);
    }

    @Override
    public GraphPlusQueryTraversal<S, E, L> from(Vertex fromVertex) {
        return (GraphPlusQueryTraversal<S, E, L>) super.from(fromVertex);
    }

    @Override
    public GraphPlusQueryTraversal<S, E, L> to(Vertex toVertex) {
        return (GraphPlusQueryTraversal<S, E, L>) super.to(toVertex);
    }

    @Override
    public GraphPlusQueryTraversal<S, Vertex, L> V(Object... vertexIdsOrElements) {
        return (GraphPlusQueryTraversal<S, Vertex, L>) super.V(vertexIdsOrElements);
    }

    @Override
    public GraphPlusQueryTraversal<S, Edge, L> outE(Class<?>... edgeClasses) {
        return (GraphPlusQueryTraversal<S, Edge, L>) super.outE(edgeClasses);
    }

    @Override
    public GraphPlusQueryTraversal<S, Edge, L> inE(Class<?>... edgeClasses) {
        return (GraphPlusQueryTraversal<S, Edge, L>) super.inE(edgeClasses);
    }

    @Override
    public GraphPlusQueryTraversal<S, Edge, L> bothE(Class<?>... edgeClasses) {
        return (GraphPlusQueryTraversal<S, Edge, L>) super.bothE(edgeClasses);
    }
}
