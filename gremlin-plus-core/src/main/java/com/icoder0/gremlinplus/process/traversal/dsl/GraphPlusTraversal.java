package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.NotStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.TraversalFilterStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.PropertyType;

import java.util.Arrays;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveLabel;

/**
 * @author bofa1ex
 * @since 2020/12/5
 *
 * @param <S> start
 * @param <E> end
 * @param <L> Label Entity
 */
public class GraphPlusTraversal<S, E, L> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {

    public GraphPlusTraversal() {
        super();
    }

    public GraphPlusTraversal(final TraversalSource traversalSource) {
        super(traversalSource);
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
        final String key = SerializedFunction.method2Property(func);
        return (GraphPlusTraversal<S, E, L>) this.property(null, key, value, keyValues);
    }

    public <E2> GraphPlusTraversal<S, E2, L> value(final SerializedFunction<L, E2> func) {
        final String key = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, key);
        return (GraphPlusTraversal<S, E2, L>) this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.VALUE, key));
    }

    public <E2> GraphPlusTraversal<S, E2, L> values(final SerializedFunction<L, E2>... funcs) {
        final String[] propertyKeys = Arrays.stream(funcs).map(SerializedFunction::method2Property).toArray(String[]::new);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.values, (Object[]) propertyKeys);
        return (GraphPlusTraversal<S, E2, L>) this.asAdmin().addStep(new PropertiesStep<>(this.asAdmin(), PropertyType.VALUE, propertyKeys));
    }

    public <L2> GraphPlusTraversal<S, E, L2> hasLabel(final Class<L2> clazz){
        final String label = resolveLabel(clazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasLabel, label);
        return (GraphPlusTraversal<S, E, L2>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(org.apache.tinkerpop.gremlin.structure.T.label.getAccessor(), P.eq(label)));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final P<R> predicate) {
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, predicate);
        return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func) {
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey);
        return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(new TraversalFilterStep(this.asAdmin(), __.values(propertyKey)));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Object value) {
        final String propertyKey = SerializedFunction.method2Property(func);
        if (value instanceof P)
            return (GraphPlusTraversal<S, E, L>) this.has(propertyKey, (P<R>) value);
        else if (value instanceof Traversal)
            return (GraphPlusTraversal<S, E, L>) this.has(propertyKey, (Traversal<S,E>) value);
        else {
            this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, value);
            return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, P.eq(value)));
        }
    }

    public <R> GraphPlusTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final P<R> predicate) {
        final String label = resolveLabel(labelClazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, predicate);
        return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, predicate));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final Class<L> labelClazz, final SerializedFunction<L, R> func, final R value) {
        final String label = resolveLabel(labelClazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel"));
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, label, propertyKey, value);
        return (GraphPlusTraversal<S, E, L>) TraversalHelper.addHasContainer(this.asAdmin(), new HasContainer(propertyKey, value instanceof P ? (P<R>) value : P.eq(value)));
    }

    public <R> GraphPlusTraversal<S, E, L> has(final SerializedFunction<L, R> func, final Traversal<S, E> propertyTraversal) {
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.has, propertyKey, propertyTraversal);
        return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(
                new TraversalFilterStep<>(this.asAdmin(), propertyTraversal.asAdmin().addStep(0,
                        new PropertiesStep<R>(propertyTraversal.asAdmin(), PropertyType.VALUE, propertyKey))));
    }

    public <R> GraphPlusTraversal<S, E, L> hasNot(final SerializedFunction<L, R> func) {
        final String propertyKey = SerializedFunction.method2Property(func);
        this.asAdmin().getBytecode().addStep(GraphTraversal.Symbols.hasNot, propertyKey);
        return (GraphPlusTraversal<S, E, L>) this.asAdmin().addStep(new NotStep<>(this.asAdmin(), __.values(propertyKey)));
    }
}
