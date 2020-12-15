package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.function.ThrowingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/15
 */
public class SerializedFunctionSupport {

    static Logger LOG = LoggerFactory.getLogger(SerializedFunction.class);

    public static String method2Property(SerializedLambda lambda) {
        return method2Property(lambda, false);
    }

    public static String method2Property(SerializedLambda lambda, boolean supportSerializable) {
        final Class<?> implClass = getFuncImplClass(lambda);
        final String fieldName = method2Field(lambda);

        return Optional.ofNullable(VertexDefinitionSupport.VERTEX_DEFINITION_MAP.get(implClass))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                .filter(vertexPropertyDefinition -> {
                    if (vertexPropertyDefinition.isPrimaryKey()) {
                        throw new IllegalArgumentException("property不支持vertexId查询");
                    }
                    if (supportSerializable && !vertexPropertyDefinition.isSerializable()) {
                        LOG.warn("{}#{} 字段不支持持久化", implClass.getSimpleName(), fieldName);
                        return false;
                    }
                    return true;
                })
                .map(VertexPropertyDefinition::getPropertyName)
                .orElse(null);
    }

    @SafeVarargs
    public static <T> String[] method2Properties(SerializedFunction<T, ?>... funcs) {
        return Arrays.stream(funcs)
                .map(LambdaSupport::resolve)
                .map(SerializedFunctionSupport::method2Property)
                .toArray(String[]::new);
    }

    static String method2Field(SerializedLambda lambda) {
        return PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
    }

    static Class<?> getFuncImplClass(SerializedLambda lambda) {
        return ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
    }
}
