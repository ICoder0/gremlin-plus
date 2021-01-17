package com.icoder0.gremlinplus.process.traversal.dsl;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.toolkit.*;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static com.icoder0.gremlinplus.process.traversal.toolkit.UnSerializedPropertySupport.getUnSerializedPropertyCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.getVertexDefinitionCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveProperties;


/**
 * @author bofa1ex
 * @since 2020/12/24
 */
public class GraphPlusTerminalTraversal<S, E, L> extends GraphPlusNormalTraversal<S, E, L> {

    private static final Logger log = LoggerFactory.getLogger(GraphPlusTerminalTraversal.class);

    public GraphPlusTerminalTraversal() {
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
        } catch (ExceptionUtils.CheckedException ignored){
        } catch(Exception e) {
            log.error("toBean#invoke异常", e);
        }
        return Optional.empty();
    }


    public Optional<L> tryToBean(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return tryToBean();
    }

    public Pair<L, Vertex> toPair() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }
        final Vertex vertex = ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).tryNext().orElseThrow(() -> ExceptionUtils.gpe(String.format("找不到对应{%s}#vertex记录", labelEntityClass.getName())));
        final L o = (L) CglibSupport.newInstance(labelEntityClass);
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
            if (vertexPropertyDefinition.isPrimaryKey()) {
                beanMap.put(o, entry.getKey(), vertex.id());
                continue;
            }
            Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                    .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                    .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return Pair.of(o, vertex);
    }

    public Pair<L, Vertex> toPair(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toPair();
    }

    public L toBean() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }

        final Vertex vertex = ((GraphPlusNormalTraversal<S, Vertex, L>) this.asAdmin()).tryNext().orElseThrow(() -> ExceptionUtils.gpe(String.format("找不到对应{%s}#vertex记录", labelEntityClass.getName())));
        final L o = (L) CglibSupport.newInstance(labelEntityClass);
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
        final BeanMap beanMap = vertexDefinition.getBeanMap();
        for (Map.Entry<String, VertexPropertyDefinition> entry : vertexDefinition.getVertexPropertyDefinitionMap().entrySet()) {
            final VertexPropertyDefinition vertexPropertyDefinition = entry.getValue();
            if (vertexPropertyDefinition.isPrimaryKey()) {
                beanMap.put(o, entry.getKey(), vertex.id());
                continue;
            }
            Optional.ofNullable(vertex.property(vertexPropertyDefinition.getPropertyName()).orElse(null))
                    .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                    .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return o;
    }


    public L toBean(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toBean();
    }

    public List<Pair<L, Vertex>> toPairList() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
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
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            pairs.add(Pair.of(o, vertex));
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return pairs;
    }


    public List<Pair<L, Vertex>> toPairList(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toPairList();
    }


    public List<L> toBeanList() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
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
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            beans.add(o);
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return beans;
    }


    public List<L> toBeanList(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toBeanList();
    }

    public Set<Pair<L, Vertex>> toPairSet() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
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
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            pairs.add(Pair.of(o, vertex));
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return pairs;
    }


    public Set<Pair<L, Vertex>> toPairSet(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toPairSet();
    }

    public Set<L> toBeanSet() {
        if (Objects.isNull(labelEntityClass)) {
            throw ExceptionUtils.gpe("必须指定单个label");
        }
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(labelEntityClass);
        final VertexDefinition vertexDefinition = getVertexDefinitionCache().putIfAbsent(labelEntityClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(labelEntityClass))
                .withVertexPropertyDefinitionMap(resolveProperties(labelEntityClass))
                .withBeanMap(generator.create())
                .build()
        );
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
                        .map(value -> vertexPropertyDefinition.isSerializable() ? value : getUnSerializedPropertyCache().get(value))
                        .ifPresent(value -> beanMap.put(o, entry.getKey(), value));
            }
            beans.add(o);
        }
        // 切记释放资源.
        CloseableIterator.closeIterator(this);
        return beans;
    }


    public Set<L> toBeanSet(Class<L> clazz) {
        this.labelEntityClass = clazz;
        return toBeanSet();
    }


    public Stream<Pair<L, Vertex>> toPairStream() {
        return toPairSet().stream();
    }

    public Stream<L> toBeanStream() {
        return toBeanSet().stream();
    }

    public Stream<Pair<L, Vertex>> toPairStream(Class<L> clazz) {
        return toPairSet(clazz).stream();
    }

    public Stream<L> toBeanStream(Class<L> clazz) {
        return toBeanSet(clazz).stream();
    }
}
