package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.step.HasPlusContainer;
import com.icoder0.gremlinplus.process.traversal.step.PropertiesPlusStep;
import com.icoder0.gremlinplus.process.traversal.toolkit.*;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.FromToModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.NotStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.RangeGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.TraversalFilterStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;

import java.util.*;
import java.util.stream.Collectors;

import static com.icoder0.gremlinplus.process.traversal.toolkit.UnSerializedPropertySupport.getUnSerializedPropertyCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.getVertexDefinitionCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveProperties;


/**
 * @param <S> start
 * @param <E> end
 * @param <L> Label Entity
 * @author GraphPlusTerminalTraversal
 * @since 2020/12/5
 */
public class GraphPlusNormalTraversal<S, E, L> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {

    protected Class<?> labelEntityClass;

    public GraphPlusNormalTraversal() {
    }

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

    /**
     * tryNext操作并没有释放Traversal对象中的steps资源.
     * orientdb#OResultSet需要及时释放, 否则会出现资源泄漏以及警告日志.
     */
    public Optional<E> tryNextWithIterate() {
        try {
            return this.hasNext() ? Optional.of(this.next()) : Optional.empty();
        } finally {
            CloseableIterator.closeIterator(this);
        }
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> property(final SerializedFunction<L, R> func, final R value, final Object... keyValues) {
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            if (!pair.getValue()) {
                final Object generateKey = ((GraphPlusNormalTraversal<S, Vertex, L>) this.clone()).tryNext()
                        .map(vertex -> vertex.property(SerializedFunctionSupport.method2Property(LambdaSupport.resolve(func))).orElse(null))
                        .orElse(KeyGeneratorSupport.generate());
                getUnSerializedPropertyCache().put(generateKey, value);
                return this.property(null, pair.getKey(), generateKey, keyValues);
            }
            return this.property(null, pair.getKey(), value, keyValues);
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> property(final SerializedFunction<L, R> func, final Traversal<S, E> vertexTraversal) {
        final E value = Optional.ofNullable(vertexTraversal).map(Traversal::next).orElse(null);
        CloseableIterator.closeIterator(vertexTraversal);
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            if (!pair.getValue()) {
                final Object generateKey = ((GraphPlusNormalTraversal<S, Vertex, L>) this.clone()).tryNext()
                        .map(vertex -> vertex.property(SerializedFunctionSupport.method2Property(LambdaSupport.resolve(func))).orElse(null))
                        .orElse(KeyGeneratorSupport.generate());
                getUnSerializedPropertyCache().put(generateKey, value);
                return this.property(null, pair.getKey(), generateKey, new Object[]{});
            }
            return this.property(null, pair.getKey(), value, new Object[]{});
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> properties(final SerializedFunction<L, R>... funcs) {
        final String[] propertyKeys = Arrays.stream(funcs).map(SerializedFunction::unwrapPair).map(Pair::getLeft).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.properties, (Object[]) propertyKeys);
        return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.PROPERTY, propertyKeys));
    }

    public <E2> GraphPlusNormalTraversal<S, E2, L> value(final SerializedFunction<L, E2> func) {
        return Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, pair.getKey());
            return (GraphPlusNormalTraversal<S, E2, L>) this.asAdmin().addStep(new PropertiesPlusStep(this.asAdmin(), PropertyType.VALUE, pair));
        }).orElse((GraphPlusNormalTraversal<S, E2, L>) this);
    }

    public GraphPlusNormalTraversal<S, Object, L> values(final SerializedFunction<L, Object>... funcs) {
        final Map<String, Boolean> propertiesMap = Arrays.stream(funcs)
                .map(SerializedFunction::unwrapPair)
                .filter(pair -> pair.equals(ImmutablePair.nullPair()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, propertiesMap.keySet());
        return (GraphPlusNormalTraversal<S, Object, L>) this.asAdmin().addStep(new PropertiesPlusStep(this.asAdmin(), PropertyType.VALUE, propertiesMap));
    }

    public <L2> GraphPlusTerminalTraversal<S, E, L2> hasLabel(final Class<L2> clazz) {
        this.labelEntityClass = clazz;
        final String label = Optional.ofNullable(AnnotationSupport.resolveLabel(clazz)).orElseThrow(() -> new IllegalArgumentException(String.format("实体类:[%s]label不可为空", clazz.getName())));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, label);
        return (GraphPlusTerminalTraversal<S, E, L2>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(org.apache.tinkerpop.gremlin.structure.T.label.getAccessor(), P.eq(label)));
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final org.apache.tinkerpop.gremlin.process.traversal.P<R> predicate) {
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey(), predicate);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), predicate));
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func) {
        return Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey());
            return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new TraversalFilterStep<>(this.asAdmin(), __.values(pair.getKey())));
        }).orElse((GraphPlusTerminalTraversal<S, E, L>) this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Object value) {
        final Pair<String, Boolean> pair = SerializedFunction.unwrapPair(func);
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

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final P<R> predicate) {
        final String label = AnnotationSupport.resolveLabel(labelClazz);
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, pair.getKey(), predicate);
            return TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), predicate));
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final R value) {
        final String label = AnnotationSupport.resolveLabel(labelClazz);
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, pair.getKey(), value);
            return (GraphPlusNormalTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasPlusContainer(pair.getKey(), pair.getValue(), value instanceof P ? (P<R>) value : P.eq(value)));
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Traversal<S, E> propertyTraversal) {
        return (GraphPlusTerminalTraversal<S, E, L>) Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, pair.getKey(), propertyTraversal);
            return (GraphPlusNormalTraversal<S, E, L>) this.asAdmin().addStep(
                    new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                            new PropertiesStep<R>(propertyTraversal.asAdmin(), PropertyType.VALUE, pair.getKey())))
            );
        }).orElse(this);
    }

    public <R> GraphPlusTerminalTraversal<S, E, L> hasNot(final SerializedFunction<L, R> func) {
        return Optional.ofNullable(SerializedFunction.unwrapPair(func)).map(pair -> {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, pair.getKey());
            return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(pair.getKey())));
        }).orElse((GraphPlusTerminalTraversal<S, E, L>) this);
    }

    public GraphPlusTerminalTraversal<S, Vertex, L> out(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.out, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.OUT, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, Vertex, L> in(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.in, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.IN, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, Vertex, L> both(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.both, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Vertex, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Vertex.class, Direction.BOTH, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, Edge, L> outE(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.outE, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Edge, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.OUT, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, Edge, L> inE(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.inE, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Edge, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.IN, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, Edge, L> bothE(final Class<?>... edgeClasses) {
        final String[] edgeLabels = Arrays.stream(edgeClasses).map(AnnotationSupport::resolveEdgeLabel).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.bothE, (Object[]) edgeLabels);
        return (GraphPlusTerminalTraversal<S, Edge, L>) this.asAdmin().addStep(new VertexStep<>(this.asAdmin(), Edge.class, Direction.BOTH, edgeLabels));
    }

    public GraphPlusTerminalTraversal<S, E, L> from(final Vertex fromVertex) {
        if (fromVertex == null) {
            TraversalHelper.removeAllSteps(this);
            return (GraphPlusTerminalTraversal<S, E, L>) this;
        }
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.from, fromVertex);
        ((FromToModulating) this.asAdmin().getEndStep()).addFrom(__.constant(fromVertex).asAdmin());
        return (GraphPlusTerminalTraversal<S, E, L>) this;
    }

    public GraphPlusTerminalTraversal<S, E, L> to(final Vertex toVertex) {
        if (toVertex == null) {
            TraversalHelper.removeAllSteps(this);
            return (GraphPlusTerminalTraversal<S, E, L>) this;
        }
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.to, toVertex);
        ((FromToModulating) this.asAdmin().getEndStep()).addTo(__.constant(toVertex).asAdmin());
        return (GraphPlusTerminalTraversal<S, E, L>) this;
    }

    public GraphPlusTerminalTraversal<S, Vertex, L> V(final Object... vertexIdsOrElements) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.V, vertexIdsOrElements);
        return (GraphPlusTerminalTraversal<S, Vertex, L>) this.asAdmin().addStep(new GraphStep<>(this.asAdmin(), Vertex.class, false, vertexIdsOrElements));
    }

    public GraphPlusTerminalTraversal<S, E, L> limit(final long limit) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.limit, limit);
        return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), 0, limit));
    }

    public GraphPlusTerminalTraversal<S, E, L> skip(final long skip) {
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.skip, skip);
        return (GraphPlusTerminalTraversal<S, E, L>) this.asAdmin().addStep(new RangeGlobalStep<>(this.asAdmin(), skip, -1));
    }

    public GraphPlusTerminalTraversal<Vertex, Vertex, L> addV(final Class<L> clazz) {
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(clazz);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(clazz, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(clazz))
                .withVertexPropertyDefinitionMap(resolveProperties(clazz))
                .withBeanMap(generator.create())
                .build()
        );
        final String label = vertexDefinition.getLabel();
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.addV, label);
        return (GraphPlusTerminalTraversal<Vertex, Vertex, L>) this.asAdmin().addStep(new AddVertexStep<>(this.asAdmin(), label));
    }
}
