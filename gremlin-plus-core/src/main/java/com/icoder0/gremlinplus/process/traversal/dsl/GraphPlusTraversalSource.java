package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.toolkit.AnnotationSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.KeyGeneratorSupport;
import net.sf.cglib.beans.BeanMap;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.traversal.strategy.decoration.RemoteStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
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
import java.util.Optional;

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
    //                                New Feature
    // ************************************************************************
    public <T> Vertex addV(T entity) {
        final Class<?> vertexClazz = entity.getClass();
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.computeIfAbsent(vertexClazz, ignored -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(vertexClazz))
                .withVertexPropertyDefinitionMap(resolveProperties(vertexClazz))
                .withBeanMap(BeanMap.create(entity))
                .build()
        );
        final String label = vertexDefinition.getLabel();
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap = vertexDefinition.getVertexPropertyDefinitionMap();
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.addV, label);
        final GraphPlusNormalTraversal<Vertex, Vertex, T> first = new GraphPlusNormalTraversal<>(clone, (Class<T>) entity.getClass());

        final Vertex vertex = first.addStep(new AddVertexStartStep(first, label)).next();

        for (Object key : beanMap.keySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = vertexPropertyDefinitionMap.get((String) key);
            final String propertyName = vertexPropertyDefinition.getPropertyName();
            // 如果是主键id, 跳过property赋值.
            if (vertexPropertyDefinition.isPrimaryKey()) {
                // vertex#id赋值
                beanMap.put(entity, key, vertex.id());
                continue;
            }
            // 如果该字段不支持持久化.
            if (!vertexPropertyDefinition.isSerializable()) {
                Optional.ofNullable(beanMap.get(entity, key)).ifPresent(value -> {
                    final Object genKey = KeyGeneratorSupport.generate();
                    vertex.property(propertyName, genKey);
                    VERTEX_UNSERIALIZED_MAP.put(genKey, value);
                });
                continue;
            }

            Optional.ofNullable(beanMap.get(entity, key)).ifPresent(value -> vertex.property(propertyName, value));
        }
        return vertex;
    }

    /**
     * 效果如同addV(String label)
     */
    public <T> GraphPlusTerminalTraversal<Vertex, Vertex, T> addV(Class<T> clazz) {
        final GraphPlusTraversalSource clone = this.clone();
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(clazz);
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.computeIfAbsent(clazz, ignored -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(clazz))
                .withVertexPropertyDefinitionMap(resolveProperties(clazz))
                .withBeanMap(generator.create())
                .build()
        );
        final String label = vertexDefinition.getLabel();
        clone.bytecode.addStep(GraphTraversal.Symbols.addV, label);
        final GraphPlusTerminalTraversal<Vertex, Vertex, T> traversal = new GraphPlusTerminalTraversal<>(clone, clazz);
        return (GraphPlusTerminalTraversal<Vertex, Vertex, T>) traversal.addStep(new AddVertexStartStep(traversal, label));
    }

    /**
     * 效果如同addE(String label)
     */
    public <T> GraphPlusTerminalTraversal<Edge, Edge, T> addE(Class<T> clazz) {
        final GraphPlusTraversalSource clone = this.clone();
        final String label = AnnotationSupport.resolveEdgeLabel(clazz);
        clone.bytecode.addStep(GraphTraversal.Symbols.addE, label);
        final GraphPlusTerminalTraversal<Edge, Edge, T> traversal = new GraphPlusTerminalTraversal<>(clone, clazz);
        return (GraphPlusTerminalTraversal<Edge, Edge, T>) traversal.addStep(new AddEdgeStartStep(traversal, label));
    }

    /**
     * Spawns a {@link GraphTraversal} starting it with arbitrary values.
     */
    public <S> GraphPlusTerminalTraversal<S, S, ?> inject(S... starts) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.inject, starts);
        final GraphPlusTerminalTraversal<S, S, ?> traversal = new GraphPlusTerminalTraversal<>(clone);
        return (GraphPlusTerminalTraversal<S, S, ?>) traversal.addStep(new InjectStep<>(traversal, starts));
    }

    /**
     * Spawns a {@link GraphTraversal} starting with all vertices or some subset of vertices as specified by their
     * unique identifier.
     */
    public <T> GraphPlusTerminalTraversal<Vertex, Vertex, T> V(final Object... vertexIds) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.V, vertexIds);
        final GraphPlusTerminalTraversal<Vertex, Vertex, T> traversal = new GraphPlusTerminalTraversal<>(clone);
        return (GraphPlusTerminalTraversal<Vertex, Vertex, T>) traversal.addStep(new GraphStep<>(traversal, Vertex.class, true, vertexIds));
    }

    /**
     * Spawns a {@link GraphTraversal} starting with all edges or some subset of edges as specified by their unique
     * identifier.
     */
    public <T> GraphPlusTerminalTraversal<Edge, Edge, T> E(final Object... edgesIds) {
        final GraphPlusTraversalSource clone = this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.E, edgesIds);
        final GraphPlusTerminalTraversal<Edge, Edge, T> traversal = new GraphPlusTerminalTraversal<>(clone);
        return (GraphPlusTerminalTraversal<Edge, Edge, T>) traversal.addStep(new GraphStep<>(traversal, Edge.class, true, edgesIds));
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
        final GraphPlusTerminalTraversal<S, S, ?> traversal = new GraphPlusTerminalTraversal<>(clone);
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
