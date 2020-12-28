package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class VertexDefinitionSupport {

    public static final Map<Class<?>, VertexDefinition> VERTEX_DEFINITION_MAP = new ConcurrentHashMap<>();

    public static final Map<Object, Object> VERTEX_UNSERIALIZED_MAP = new ConcurrentHashMap<>();

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
        if (primaryKeyField == null) {
            throw ExceptionUtils.gpe(new IllegalArgumentException(String.format("{%s} VertexId必须声明字段", clazz.getName())));
        }
        return vertexPropertyDefinitionMap;
    }
}
