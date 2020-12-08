package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import net.sf.cglib.beans.BeanMap;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.traversal.strategy.decoration.RemoteStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.InjectStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.IoStep;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.Map;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.*;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class GraphPlusTraversalSource implements TraversalSource {

    protected transient RemoteConnection connection;
    protected final Graph graph;
    protected TraversalStrategies strategies;
    protected Bytecode bytecode = new Bytecode();

    public GraphPlusTraversalSource(Graph graph) {
        this(graph, TraversalStrategies.GlobalCache.getStrategies(graph.getClass()));
    }

    public GraphPlusTraversalSource(Graph graph, TraversalStrategies strategies) {
        this.graph = graph;
        this.strategies = strategies;
    }

    public GraphPlusTraversalSource(RemoteConnection connection) {
        this(EmptyGraph.instance(), TraversalStrategies.GlobalCache.getStrategies(EmptyGraph.class).clone());
        this.connection = connection;
        this.strategies.addStrategies(new RemoteStrategy(connection));
    }

    // ************************************************************************
    //                                Feature
    // ************************************************************************
    public <T> Vertex addV(T entity) {
        final Class<?> vertexClazz = entity.getClass();
        final GraphPlusTraversalSource clone = this.clone();
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.computeIfAbsent(vertexClazz, ignored -> VertexDefinition.builder()
                .withLabel(resolveLabel(vertexClazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel")))
                .withVertexPropertyDefinitionMap(resolveProperties(vertexClazz))
                .withBeanMap(BeanMap.create(entity))
                .build()
        );
        final String label = vertexDefinition.getLabel();
        final Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap = vertexDefinition.getVertexPropertyDefinitionMap();

        clone.bytecode.addStep(GraphTraversal.Symbols.addV, label);
        final GraphPlusTraversal<Vertex, Vertex, T> first = new GraphPlusTraversal<>(clone);

        final Vertex vertex = first.addStep(new AddVertexStartStep(first, label)).next();

        final BeanMap beanMap = vertexDefinition.getBeanMap();
        for (Object key : beanMap.keySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = vertexPropertyDefinitionMap.get((String) key);
            // 如果是主键id, 跳过property赋值.
            if (vertexPropertyDefinition.isPrimaryKey()) {
                continue;
            }
            final String propertyName = vertexPropertyDefinition.getPropertyName();
            final Object value = beanMap.get(entity, key);
            vertex.property(propertyName, value);
        }
        return vertex;
    }

    /**
     * 效果如同addV(String label)
     */
    public <T> GraphPlusTraversal<Vertex, Vertex, T> addV(Class<T> clazz) {
        final GraphPlusTraversalSource clone = this.clone();
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.computeIfAbsent(clazz, ignored -> VertexDefinition.builder()
                .withLabel(resolveLabel(clazz).orElseThrow(() -> new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel")))
                .withVertexPropertyDefinitionMap(resolveProperties(clazz))
                .withBeanMap(BeanMap.create(clazz))
                .build()
        );
        final String label = vertexDefinition.getLabel();
        clone.bytecode.addStep(GraphTraversal.Symbols.addV, label);
        final GraphPlusTraversal<Vertex, Vertex, T> traversal = new GraphPlusTraversal<>(clone);
        return (GraphPlusTraversal<Vertex, Vertex, T>) traversal.addStep(new AddVertexStartStep(traversal, label));
    }

    /**
     * 效果如同addE(String label)
     */
    public <T> GraphPlusTraversal<Edge, Edge, T> addE(Class<T> clazz) {
        final GraphPlusTraversalSource clone = this.clone();
        final String label = resolveLabel(clazz).orElseThrow(() -> new IllegalArgumentException("Edge实体类必须声明标签注解GraphLabel"));
        clone.bytecode.addStep(GraphTraversal.Symbols.addE, label);
        final GraphPlusTraversal<Edge, Edge, T> traversal = new GraphPlusTraversal<>(clone);
        return (GraphPlusTraversal<Edge, Edge, T>) traversal.addStep(new AddEdgeStartStep(traversal, label));
    }


    /**
     * Spawns a {@link GraphTraversal} starting it with arbitrary values.
     */
    public <S> GraphPlusTraversal<S, S, ?> inject(S... starts) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.inject, starts);
        final GraphPlusTraversal<S, S, ?> traversal = new GraphPlusTraversal<>(clone);
        return (GraphPlusTraversal<S, S, ?>) traversal.addStep(new InjectStep<S>(traversal, starts));
    }

    /**
     * Spawns a {@link GraphTraversal} starting with all vertices or some subset of vertices as specified by their
     * unique identifier.
     */
    public GraphPlusTraversal<Vertex, Vertex, ?> V(final Object... vertexIds) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.V, vertexIds);
        final GraphPlusTraversal<Vertex, Vertex, ?> traversal = new GraphPlusTraversal<>(clone);
        return (GraphPlusTraversal<Vertex, Vertex, ?>) traversal.addStep(new GraphStep<>(traversal, Vertex.class, true, vertexIds));
    }

    /**
     * Spawns a {@link GraphTraversal} starting with all edges or some subset of edges as specified by their unique
     * identifier.
     */
    public GraphPlusTraversal<Edge, Edge, ?> E(final Object... edgesIds) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.E, edgesIds);
        final GraphPlusTraversal<Edge, Edge, ?> traversal = new GraphPlusTraversal<>(clone);
        return (GraphPlusTraversal<Edge, Edge, ?>) traversal.addStep(new GraphStep<>(traversal, Edge.class, true, edgesIds));
    }

    /**
     * Performs a read or write based operation on the {@link Graph} backing this {@code GraphTraversalSource}. This
     * step can be accompanied by the {@link GraphTraversal#with(String, Object)} modulator for further configuration
     * and must be accompanied by a {@link GraphTraversal#read()} or {@link GraphTraversal#write()} modulator step
     * which will terminate the traversal.
     *
     * @param file the name of file for which the read or write will apply - note that the context of how this
     *             parameter is used is wholly dependent on the implementation
     * @return the traversal with the {@link IoStep} added
     * @see <a href="http://tinkerpop.apache.org/docs/${project.version}/reference/#io-step" target="_blank">Reference Documentation - IO Step</a>
     * @see <a href="http://tinkerpop.apache.org/docs/${project.version}/reference/#read-step" target="_blank">Reference Documentation - Read Step</a>
     * @see <a href="http://tinkerpop.apache.org/docs/${project.version}/reference/#write-step" target="_blank">Reference Documentation - Write Step</a>
     * @since 3.4.0
     */
    public <S> GraphTraversal<S, S> io(final String file) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.io, file);
        final GraphPlusTraversal<S, S, ?> traversal = new GraphPlusTraversal<>(clone);
        return traversal.addStep(new IoStep<S>(traversal, file));
    }

    @Override
    public TraversalStrategies getStrategies() {
        return this.strategies;
    }

    @Override
    public Graph getGraph() {
        return this.graph;
    }

    @Override
    public Bytecode getBytecode() {
        return this.bytecode;
    }


    /**
     * {@inheritDoc}
     *
     * @see <a href="https://issues.apache.org/jira/browse/TINKERPOP-2078">TINKERPOP-2078</a>
     * @deprecated As of release 3.3.5, replaced by {@link AnonymousTraversalSource#withRemote(RemoteConnection)}.
     */
    @Override
    @Deprecated
    public TraversalSource withRemote(RemoteConnection connection) {
        // check if someone called withRemote() more than once, so just release resources on the initial
        // connection as you can't have more than one. maybe better to toss IllegalStateException??
        if (this.connection != null)
            throw new IllegalStateException(String.format("TraversalSource already configured with a RemoteConnection [%s]", connection));
        final GraphPlusTraversalSource clone = this.clone();
        clone.connection = connection;
        clone.getStrategies().addStrategies(new RemoteStrategy(connection));
        return clone;
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public GraphPlusTraversalSource clone() {
        try {
            final GraphPlusTraversalSource clone = (GraphPlusTraversalSource) super.clone();
            clone.strategies = this.strategies.clone();
            clone.bytecode = this.bytecode.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Proxies calls through to the underlying {@link Graph#tx()}.
     */
    public Transaction tx() {
        return this.graph.tx();
    }

    /**
     * If there is an underlying {@link RemoteConnection} it will be closed by this method.
     */
    @Override
    public void close() throws Exception {
        if (connection != null) connection.close();
    }

    @Override
    public String toString() {
        return StringFactory.traversalSourceString(this);
    }

}
