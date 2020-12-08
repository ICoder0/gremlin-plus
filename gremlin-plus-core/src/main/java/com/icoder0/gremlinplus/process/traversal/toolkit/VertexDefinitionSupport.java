package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.annotation.GraphLabel;
import com.icoder0.gremlinplus.annotation.VertexId;
import com.icoder0.gremlinplus.annotation.VertexProperty;
import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class VertexDefinitionSupport {

    public static final Map<Class<?>, VertexDefinition> VERTEX_DEFINITION_MAP = new ConcurrentHashMap<>();

    public static Optional<String> resolveLabel(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(GraphLabel.class))
                .map(GraphLabel::value);
    }

    public static Map<String, VertexPropertyDefinition> resolveProperties(Class<?> clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        Field primaryKeyField = null;
        final Map<String, VertexPropertyDefinition> vertexPropertyDefinitionMap = new HashMap<>();
        for (Field field : fields) {
            if (field.getAnnotation(VertexId.class) != null) {
                if (primaryKeyField != null) {
                    throw new IllegalArgumentException("VertexId不可以声明多个字段");
                }
                primaryKeyField = field;
                vertexPropertyDefinitionMap.put(primaryKeyField.getName(), VertexPropertyDefinition.builder()
                        .withPrimaryKey(true)
                        .build());
                continue;
            }
            final VertexPropertyDefinition vertexPropertyDefinition = Optional.ofNullable(field.getAnnotation(VertexProperty.class)).map(vertexProperty -> VertexPropertyDefinition.builder()
                    .withPropertyName(vertexProperty.value())
                    .withSerializable(vertexProperty.serializable())
                    .build()
            ).orElse(VertexPropertyDefinition.builder()
                    .withPropertyName(field.getName())
                    .withSerializable(true)
                    .build()
            );
            vertexPropertyDefinitionMap.put(field.getName(), vertexPropertyDefinition);
        }
        if (primaryKeyField == null){
            throw new IllegalArgumentException("VertexId必须声明字段");
        }
        return vertexPropertyDefinitionMap;
    }
}
