package com.icoder0.gremlinplus.process.extension;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;

import java.util.function.Supplier;


/**
 * @author bofa1ex
 * @since 2020/12/29
 */
public interface VertexDefinitionCache {
    VertexDefinition putIfAbsent(Class<?> key, Supplier<VertexDefinition> value);

    VertexDefinition get(Class<?> key);

    VertexDefinition getOrDefault(Class<?> key, VertexDefinition value);
}
