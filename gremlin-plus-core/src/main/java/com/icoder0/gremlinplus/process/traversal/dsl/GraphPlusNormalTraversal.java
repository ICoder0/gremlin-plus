package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.step.map.HasPlusContainer;
import com.icoder0.gremlinplus.process.traversal.step.util.PropertiesPlusStep;
import com.icoder0.gremlinplus.process.traversal.toolkit.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.FromToModulating;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.VERTEX_UNSERIALIZED_MAP;

/**
 * @param <S> start
 * @param <E> end
 * @param <L> Label Entity
 * @author GraphPlusTerminalTraversal
 * @since 2020/12/5
 */
public class GraphPlusNormalTraversal<S, E, L> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {

    protected Class<?> labelEntityClass;

    protected GraphPlusNormalTraversal(final TraversalSource traversalSource) {
        this(traversalSource, null);
    }

    protected GraphPlusNormalTraversal(final TraversalSource traversalSource, Class<L> labelEntityClass) {
        super(traversalSource);
        this.labelEntityClass = labelEntityClass;
    }

    @Override
    public GraphTraversal.Admin<S, E> asAdmin() {
        return this;
    }

    @Override
    public GraphPlusNormalTraversal<S, E, L> clone() {
        return (GraphPlusNormalTraversal<S, E, L>) super.clone();
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> property(final SerializedFunction<L, R> func, final R value, final Object... keyValues) {
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapKeyPair(func)).map(pair -> {
            final Object generateKey = pair.getRight();
            // 如果generateKey不为空, 说明该字段不支持序列化.
            if (pair.getRight() != null) {
                VERTEX_UNSERIALIZED_MAP.put(generateKey, value);
                return this.property(null, pair.getKey(), generateKey, keyValues);
            }
            return this.property(null, pair.getKey(), value, keyValues);
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> property(final SerializedFunction<L, R> func, final Traversal<S, Vertex> vertexTraversal) {
        final Object vertexId = vertexTraversal.next().id();
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapKeyPair(func)).map(pair -> {
            final Object generateKey = pair.getRight();
            // 如果generateKey不为空, 说明该字段不支持序列化.
            if (pair.getRight() != null) {
                VERTEX_UNSERIALIZED_MAP.put(generateKey, vertexId);
                return this.property(null, pair.getKey(), generateKey, null);
            }
            return this.property(null, pair.getKey(), vertexId, null);
        }).orElse(this);
    }

    public <E2> GraphPlusNormalTraversal<S, E2, L> value(final SerializedFunction<L, E2> func) {
        return Optional.ofNullable(SerializedFunction.unwrapBoolPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, pair.getKey());
            return (GraphPlusNormalTraversal<S, E2, L>) this.asAdmin().addStep(new PropertiesPlusStep(this.asAdmin(), PropertyType.VALUE, pair));
        }).orElse((GraphPlusNormalTraversal<S, E2, L>) this);
    }

    public GraphPlusNormalTraversal<S, Object, L> values(final SerializedFunction<L, Object>... funcs) {
        final Map<String, Boolean> propertiesMap = Arrays.stream(funcs)
                .map(SerializedFunction::unwrapBoolPair)
                .filter(pair -> pair.equals(ImmutablePair.nullPair()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, propertiesMap.keySet());
        return (GraphPlusNormalTraversal<S, Object, L>) this.asAdmin().addStep(new PropertiesPlusStep(this.asAdmin(), PropertyType.VALUE, propertiesMap));
    }

    public <L2> GraphPlusTerminalTraversal<S, E, L2> hasLabel(final Class<L2> clazz) {
        this.labelEntityClass = clazz;
        final String label = AnnotationSupport.resolveVertexLabel(clazz);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, label);
        return (GraphPlusTerminalTraversal<S, E, L2>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(org.apache.tinkerpop.gremlin.structure.T.label.getAccessor(), P.eq(label)));
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final P<R> predicate) {
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapBoolPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey(), predicate);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), predicate));
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func) {
        return Optional.ofNullable(SerializedFunction.unwrapKeyPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey());
            return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), __.values(pair.getKey())));
        }).orElse((GraphPlusTerminalTraversal<S, E, L>) this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Object value) {
        final Pair<String, Boolean> pair = SerializedFunction.unwrapBoolPair(func);
        if (pair != null) {
            if (value instanceof P)
                return (GraphPlusTerminalTraversal<S, E, L>) this.has(pair.getKey(), (P<R>) value);
            else if (value instanceof Traversal)
                return (GraphPlusTerminalTraversal<S, E, L>) this.has(pair.getKey(), (Traversal<S, E>) value);
            else {
                this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey(), value);
                return (GraphPlusTerminalTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), P.eq(value)));
            }
        }
        return (GraphPlusTerminalTraversal<S, E, L>) this;
    }

    public <R> GraphPlusNormalTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final P<R> predicate) {
        final String label = AnnotationSupport.resolveLabel(labelClazz);
        return Optional.ofNullable(SerializedFunction.unwrapBoolPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, pair.getKey(), predicate);
            return (GraphPlusNormalTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), predicate));
        }).orElse(this);
    }

    public <R> GraphPlusNormalTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final R value) {
        final String label = AnnotationSupport.resolveLabel(labelClazz);
        return Optional.ofNullable(SerializedFunction.unwrapBoolPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, pair.getKey(), value);
            return (GraphPlusNormalTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), value instanceof P ? (P<R>) value : P.eq(value)));
        }).orElse(this);
    }

    public <R> GraphPlusNormalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Traversal<S, E> propertyTraversal) {
        return Optional.ofNullable(SerializedFunction.unwrapKeyPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey(), propertyTraversal);
            return (GraphPlusNormalTraversal<S, E, L>) this.asAdmin().addStep(
                    new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                            new PropertiesStep<R>(propertyTraversal.asAdmin(), PropertyType.VALUE, pair.getKey())))
            );
        }).orElse(this);
    }

    public <R> GraphPlusNormalTraversal<S, E, L> hasNot(final SerializedFunction<L, R> func) {
        return Optional.ofNullable(SerializedFunction.unwrapKeyPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, pair.getKey());
            return (GraphPlusNormalTraversal<S, E, L>) this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(pair.getKey())));
        }).orElse(this);
    }

    public GraphPlusNormalTraversal<S, Vertex, L> out(final Class<?> edgeClass) {
        final String edgeLabel = AnnotationSupport.resolveEdgeLabel(edgeClass);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.out, edgeLabel);
        return (GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.OUT, edgeLabel));
    }

    public GraphPlusNormalTraversal<S, Vertex, L> in(final Class<?> edgeClass) {
        final String edgeLabel = AnnotationSupport.resolveEdgeLabel(edgeClass);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.in, edgeLabel);
        return (GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.IN, edgeLabel));
    }

    public GraphPlusNormalTraversal<S, Vertex, L> both(final Class<?> edgeClass) {
        final String edgeLabel = AnnotationSupport.resolveEdgeLabel(edgeClass);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.both, edgeLabel);
        return (GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.BOTH, edgeLabel));
    }

    public GraphPlusNormalTraversal<S, E, L> from(final Vertex fromVertex) {
        if (fromVertex == null) {
            TraversalHelper.removeAllSteps(this);
            return this;
        }
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.from, fromVertex);
        ((FromToModulating) this.asAdmin().getEndStep()).addFrom(__.constant(fromVertex).asAdmin());
        return this;
    }

    public GraphPlusNormalTraversal<S, E, L> to(final Vertex toVertex) {
        if (toVertex == null) {
            TraversalHelper.removeAllSteps(this);
            return this;
        }
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.to, toVertex);
        ((FromToModulating) this.asAdmin().getEndStep()).addTo(__.constant(toVertex).asAdmin());
        return this;
    }
}
