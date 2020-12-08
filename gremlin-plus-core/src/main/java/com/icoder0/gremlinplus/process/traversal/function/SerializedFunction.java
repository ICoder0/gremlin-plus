package com.icoder0.gremlinplus.process.traversal.function;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.toolkit.LambdaSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.PropertyNamerSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
@FunctionalInterface
public interface SerializedFunction<T, R> extends Function<T, R>, Serializable {

    static <T> String method2Property(SerializedFunction<T, ?> func) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        final String fieldName = PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());

        return Optional.ofNullable(VertexDefinitionSupport.VERTEX_DEFINITION_MAP.get(ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get()))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                // 跳过主键
                .filter(vertexPropertyDefinition -> !vertexPropertyDefinition.isPrimaryKey())
                .map(VertexPropertyDefinition::getPropertyName)
                .orElseThrow(() -> new CheckedException("不会出现该异常情况"));
    }

    static <T> String[] method2Properties(SerializedFunction<T, ?>... funcs) {
        return Arrays.stream(funcs).map(SerializedFunction::method2Property).toArray(String[]::new);
    }

    static <T, R> String method2Field(SerializedFunction<T, R> func) {
        final SerializedLambda lambda = LambdaSupport.resolve(func);
        return PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
    }
}
