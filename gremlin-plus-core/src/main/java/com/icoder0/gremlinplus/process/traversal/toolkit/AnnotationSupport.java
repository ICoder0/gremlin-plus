package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.annotation.*;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/15
 */
public class AnnotationSupport {

    public static boolean checkVertexId(Field field) {
        return field.getAnnotation(VertexId.class) != null;
    }

    public static VertexPropertyDefinition resolveVertexProperty(Field field) {
        return Optional.ofNullable(field.getAnnotation(VertexProperty.class)).map(vertexProperty -> VertexPropertyDefinition.builder()
                .withPropertyName(vertexProperty.value())
                .withSerializable(vertexProperty.serializable())
                .build())
                .orElse(VertexPropertyDefinition.builder()
                        .withPropertyName(field.getName())
                        .withSerializable(true)
                        .build()
                );
    }

    public static String resolveLabel(Class<?> clazz) {
        final VertexLabel vertexLabel = clazz.getAnnotation(VertexLabel.class);
        final EdgeLabel edgeLabel = clazz.getAnnotation(EdgeLabel.class);
        final GraphLabel graphLabel = clazz.getAnnotation(GraphLabel.class);

        if (vertexLabel != null) {
            return vertexLabel.value();
        }
        if (edgeLabel != null) {
            return edgeLabel.value();
        }
        if (graphLabel != null) {
            return graphLabel.value();
        }
        throw ExceptionUtils.gpe(new IllegalArgumentException("Vertex/Edge实体类必须声明标签注解GraphLabel/VertexLabel/EdgeLabel"));
    }

    public static String resolveVertexLabel(Class<?> clazz) {
        final VertexLabel vertexLabel = clazz.getAnnotation(VertexLabel.class);
        final GraphLabel graphLabel = clazz.getAnnotation(GraphLabel.class);

        if (vertexLabel != null) {
            return vertexLabel.value();
        }
        if (graphLabel != null) {
            return graphLabel.value();
        }
        throw ExceptionUtils.gpe(new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel或VertexLabel"));
    }

    public static String resolveEdgeLabel(Class<?> clazz) {
        final EdgeLabel edgeLabel = clazz.getAnnotation(EdgeLabel.class);
        final GraphLabel graphLabel = clazz.getAnnotation(GraphLabel.class);

        if (edgeLabel != null) {
            return edgeLabel.value();
        }
        if (graphLabel != null) {
            return graphLabel.value();
        }
        throw ExceptionUtils.gpe(new IllegalArgumentException("Vertex实体类必须声明标签注解GraphLabel或EdgeLabel"));
    }
}
