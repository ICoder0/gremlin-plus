package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.extension.*;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class VertexDefinitionSupport {

    private static Class<? extends VertexDefinitionCache> vertexDefinitionCachePlugin;

    public static VertexDefinitionCache getVertexDefinitionCache() {
        return VertexDefinitionCacheHolder.vertexDefinitionCache;
    }

    public static void init(Class<? extends VertexDefinitionCache> plugin) {
        vertexDefinitionCachePlugin = plugin;
    }

    public static <T extends VertexDefinitionCache> void init(T t) {
        VertexDefinitionCacheHolder.vertexDefinitionCache = t;
    }

    public static class VertexDefinitionCacheHolder {
        private static VertexDefinitionCache vertexDefinitionCache;

        static {
            vertexDefinitionCache = Optional.ofNullable(vertexDefinitionCachePlugin).map(CglibSupport::newInstance)
                    .map(o -> ((VertexDefinitionCache) o))
                    .orElseGet(DefaultVertexDefinitionCache::getInstance);
        }
    }

    public static Map<String, VertexPropertyDefinition> resolveProperties(Class<?> clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        Field primaryKeyField = null;
        final Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap = new HashMap<>();
        for (Field field : fields) {
            if (AnnotationSupport.checkVertexIdNotExist(field)) {
                vertexPropertyDefinitionMap.put(field.getName(), AnnotationSupport.resolveVertexProperty(field));
                continue;
            }
            if (primaryKeyField != null) {
                throw ExceptionUtils.gpe(new IllegalArgumentException(String.format("{%s} @VertexId不可以声明多个字段", clazz.getName())));
            }
            primaryKeyField = field;
            vertexPropertyDefinitionMap.put(primaryKeyField.getName(), VertexPropertyDefinition.builder()
                    .withPropertyName(field.getName())
                    .withPrimaryKey(true)
                    .withSerializable(true)
                    .build()
            );
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            if (superclass.getSimpleName().equalsIgnoreCase(Object.class.getSimpleName()) || ClassUtils.isPrimitiveOrWrapper(superclass)) {
                return vertexPropertyDefinitionMap;
            }
            vertexPropertyDefinitionMap.putAll(resolveProperties(superclass));
        }
        return vertexPropertyDefinitionMap;
    }
}
