package com.icoder0.gremlinplus.process.extension;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author bofa1ex
 * @since 2020/12/29
 */
public class DefaultVertexDefinitionCache implements VertexDefinitionCache{

    private final Map<Class<?>, WeakReference<VertexDefinition>> vertexDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public VertexDefinition putIfAbsent(Class<?> key, Supplier<VertexDefinition> supplier) {
        requireNonNull(key);
        return vertexDefinitionMap.compute(key, (ignored, old) -> {
            if (old == null || old.get() == null){
                return new WeakReference<>(supplier.get());
            }
            return old;
        }).get();
    }

    @Override
    public VertexDefinition get(Class<?> key) {
        requireNonNull(key);
        return vertexDefinitionMap.get(key).get();
    }

    @Override
    public VertexDefinition getOrDefault(Class<?> key, VertexDefinition value) {
        requireNonNull(key);
        return vertexDefinitionMap.getOrDefault(key, new WeakReference<>(value)).get();
    }

    public static class Holder {
        private static final DefaultVertexDefinitionCache instance = new DefaultVertexDefinitionCache();
    }

    public static DefaultVertexDefinitionCache getInstance() {
        return DefaultVertexDefinitionCache.Holder.instance;
    }
}
