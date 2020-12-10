package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.toolkit.CglibSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.LambdaSupport;
import net.sf.cglib.beans.BeanMap;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.NotStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.TraversalFilterStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.PropertyType;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.VERTEX_DEFINITION_MAP;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveLabel;

/**
 * @param <S> start
 * @param <E> end
 * @param <L> Label Entity
 * @author bofa1ex
 * @since 2020/12/5
 */
public class GraphPlusTraversal<S, E, L> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {

    private Class<?> labelEntityClass;

    private final boolean supportSerializable;

    public GraphPlusTraversal(final TraversalSource traversalSource, boolean supportSerializable) {
        this(traversalSource, supportSerializable, null);
    }

    public GraphPlusTraversal(final TraversalSource traversalSource, boolean supportSerializable, Class<L> labelEntityClass) {
        super(traversalSource);
        this.supportSerializable = supportSerializable;
        this.labelEntityClass = labelEntityClass;
    }

    @Override
    public GraphTraversal.Admin<S, E> asAdmin() {
        return this;
    }

    @Override
    public GraphTraversal<S, E> iterate() {
        return GraphTraversal.Admin.super.iterate();
    }

    @Override
    public GraphPlusTraversal<S, E, L> clone() {
        return (GraphPlusTraversal<S, E, L>) super.clone();
    }

    public <R> GraphPlusTraversal<S, E, L> property(final SerializedFunction<L, R> func, final R value, final Object... keyValues) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(key ->
                (GraphPlusTraversal<S, E, L>) this.property(null, key, value, keyValues)
        ).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> property(final SerializedFunction<L, R> func, final Traversal<S, Vertex> vertexTraversal) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        final Object vertexId = vertexTraversal.next().id();
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(key ->
                (GraphPlusTraversal<S, E, L>) this.property(null, key, vertexId, null)
        ).orElse(this);
    }

    public <E2> GraphPlusTraversal<S, E2, L> value(final SerializedFunction<L, E2> func) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(key -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, key);
            return (GraphPlusTraversal<S, E2, L>) this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.VALUE, key));
        }).orElse((GraphPlusTraversal<S, E2, L>) this);
    }

    public GraphPlusTraversal<S, Object, L> values(final SerializedFunction<L, Object>... funcs) {
        final String[] propertyKeys = Arrays.stream(funcs).map(LambdaSupport::resolve)
                .map(lambda -> SerializedFunction.method2Property(lambda, supportSerializable))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, (Object[]) propertyKeys);
        return (GraphPlusTraversal<S, Object, L>) this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.VALUE, propertyKeys));
    }

    public <L2> GraphPlusTraversal<S, E, L2> hasLabel(final Class<L2> clazz) {
        this.labelEntityClass = clazz;
        final String label = resolveLabel(clazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, label);
        return (GraphPlusTraversal<S, E, L2>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(org.apache.tinkerpop.gremlin.structure.T.label.getAccessor(), P.eq(label)));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final P<R> predicate) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, predicate);
            return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
        }).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey);
            return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), __.values(propertyKey)));
        }).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Object value) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        final String propertyKey = SerializedFunction.method2Property(lambda, supportSerializable);
        if (propertyKey != null) {
            if (value instanceof P)
                return (GraphPlusTraversal<S, E, L>) this.has(propertyKey, (P<R>) value);
            else if (value instanceof Traversal)
                return (GraphPlusTraversal<S, E, L>) this.has(propertyKey, (Traversal<S, E>) value);
            else {
                this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, value);
                return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, P.eq(value)));
            }
        }
        return this;
    }

    public <R> GraphPlusTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final P<R> predicate) {
        final String label = resolveLabel(labelClazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, predicate);
            return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
        }).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final R value) {
        final String label = resolveLabel(labelClazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, value);
            return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, value instanceof P ? (P<R>) value : P.eq(value)));
        }).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Traversal<S, E> propertyTraversal) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, propertyTraversal);
            return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(
                    new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                            new PropertiesStep<R>(propertyTraversal.asAdmin(), PropertyType.VALUE, propertyKey)))
            );
        }).orElse(this);
    }

    public <R> GraphPlusTraversal<S, E, L> hasNot(final SerializedFunction<L, R> func) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return Optional.ofNullable(SerializedFunction.method2Property(lambda, supportSerializable)).map(propertyKey -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, propertyKey);
            return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(propertyKey)));
        }).orElse(this);
    }

    public GraphPlusTraversal<S, Vertex, L> out(final Class<?> edgeClass) {
        final String edgeLabel = resolveLabel(edgeClass).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.out, edgeLabel);
        return (GraphPlusTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.OUT, edgeLabel));
    }

    public GraphPlusTraversal<S, Vertex, L> in(final Class<?> edgeClass) {
        final String edgeLabel = resolveLabel(edgeClass).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.in, edgeLabel);
        return (GraphPlusTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.IN, edgeLabel));
    }

    public GraphPlusTraversal<S, Vertex, L> both(final Class<?> edgeClass) {
        final String edgeLabel = resolveLabel(edgeClass).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.both, edgeLabel);
        return (GraphPlusTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.BOTH, edgeLabel));
    }

    public L toBean() {
        final L o = (L) CglibSupport.newInstance(labelEntityClass);
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final Vertex vertex = ((GraphPlusTraversal<S, Vertex, L>) this.asAdmin()).next();
        for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
            if (vertexPropertyDefinition.isPrimaryKey()) {
                beanMap.put(o, entry.getKey(), vertex.id());
                continue;
            }
            beanMap.put(o, entry.getKey(), vertex.property(entry.getValue().getPropertyName()).orElse(null));
        }
        return o;
    }
}
