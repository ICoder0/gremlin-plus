package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.toolkit.CglibSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.ExceptionUtils;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.stream.Stream;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.VERTEX_DEFINITION_MAP;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.VERTEX_UNSERIALIZED_MAP;

/**
 * @author bofa1ex
 * @since 2020/12/24
 */
public class GraphPlusTerminalTraversal<S, E, L> extends GraphPlusNormalTraversal<S, E, L> {

    public GraphPlusTerminalTraversal(final GraphPlusNormalTraversal<S, E, L> normalTraversal) {
        super((TraversalSource) normalTraversal);
    }

    public GraphPlusTerminalTraversal(final TraversalSource traversalSource) {
        super(traversalSource);
    }

    public GraphPlusTerminalTraversal(final TraversalSource traversalSource, Class<L> labelEntityClass) {
        super(traversalSource, labelEntityClass);
    }

    @Override
    public GraphTraversal.Admin<S, E> asAdmin() {
        return this;
    }

    @Override
    public GraphPlusTerminalTraversal<S, E, L> clone() {
        return (GraphPlusTerminalTraversal<S, E, L>) super.clone();
    }

    public Optional<L> tryToBean() {
        try {
            return Optional.ofNullable(toBean());
        } catch (ExceptionUtils.CheckedException e) {
            return Optional.empty();
        }
    }

    public Pair<L, Vertex> toPair() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }
        final Vertex vertex = ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).tryNext().orElseThrow(() -> ExceptionUtils.gpe(String.format("找不到对应{%s}#vertex记录", labelEntityClass.getName())));
        final L o = (L) CglibSupport.newInstance(labelEntityClass);
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
            if (vertexPropertyDefinition.isPrimaryKey()) {
                beanMap.put(o, entry.getKey(), vertex.id());
                continue;
            }
            Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                    .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                    .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
        }
        return Pair.of(o, vertex);
    }

    public L toBean() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }

        final Vertex vertex = ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).tryNext().orElseThrow(() -> ExceptionUtils.gpe(String.format("找不到对应{%s}#vertex记录", labelEntityClass.getName())));
        final L o = (L) CglibSupport.newInstance(labelEntityClass);
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
            if (vertexPropertyDefinition.isPrimaryKey()) {
                beanMap.put(o, entry.getKey(), vertex.id());
                continue;
            }
            Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                    .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                    .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
        }
        return o;
    }

    public List<Pair<L, Vertex>> toPairList() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final List<Pair<L, Vertex>> pairs = new ArrayList<>();
        for (Vertex vertex : ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).toList()) {
            final L o = (L) CglibSupport.newInstance(labelEntityClass);
            for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
                final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
                if (vertexPropertyDefinition.isPrimaryKey()) {
                    beanMap.put(o, entry.getKey(), vertex.id());
                    continue;
                }
                Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            pairs.add(Pair.of(o, vertex));
        }
        return pairs;
    }

    public List<L> toBeanList() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final List<L> beans = new ArrayList<>();
        for (Vertex vertex : ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).toList()) {
            final L o = (L) CglibSupport.newInstance(labelEntityClass);
            for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
                final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
                if (vertexPropertyDefinition.isPrimaryKey()) {
                    beanMap.put(o, entry.getKey(), vertex.id());
                    continue;
                }
                Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            beans.add(o);
        }
        return beans;
    }

    public Set<Pair<L, Vertex>> toPairSet() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final Set<Pair<L, Vertex>> pairs = new HashSet<>();
        for (Vertex vertex : ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).toList()) {
            final L o = (L) CglibSupport.newInstance(labelEntityClass);
            for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
                final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
                if (vertexPropertyDefinition.isPrimaryKey()) {
                    beanMap.put(o, entry.getKey(), vertex.id());
                    continue;
                }
                Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            pairs.add(Pair.of(o, vertex));
        }
        return pairs;
    }

    public Set<L> toBeanSet() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须有声明labelEntity的step");
        }
        final VertexDefinition vertexDefinition = VERTEX_DEFINITION_MAP.get(labelEntityClass);
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        final Set<L> beans = new HashSet<>();
        for (Vertex vertex : ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).toSet()) {
            final L o = (L) CglibSupport.newInstance(labelEntityClass);
            for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
                final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
                if (vertexPropertyDefinition.isPrimaryKey()) {
                    beanMap.put(o, entry.getKey(), vertex.id());
                    continue;
                }
                Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : VERTEX_UNSERIALIZED_MAP.getOrDefault(value, null))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            beans.add(o);
        }
        return beans;
    }

    public Stream<Pair<L, Vertex>> toPairStream() {
        return toPairSet().stream();
    }

    public Stream<L> toBeanStream() {
        return toBeanSet().stream();
    }
}
