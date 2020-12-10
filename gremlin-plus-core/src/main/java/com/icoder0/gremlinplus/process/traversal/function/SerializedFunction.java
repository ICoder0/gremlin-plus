package com.icoder0.gremlinplus.process.traversal.function;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.toolkit.LambdaSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.PropertyNamerSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    Logger LOG = LoggerFactory.getLogger(SerializedFunction.class);

    static String method2Property(SerializedLambda lambda) {
        return method2Property(lambda, false);
    }

    static String method2Property(SerializedLambda lambda, boolean supportSerializable) {
        final Class<?> implClass = getFuncImplClass(lambda);
        final String fieldName = method2Field(lambda);

        return Optional.ofNullable(VertexDefinitionSupport.VERTEX_DEFINITION_MAP.get(implClass))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                .filter(vertexPropertyDefinition -> {
                    if (vertexPropertyDefinition.isPrimaryKey()) {
                        throw new IllegalArgumentException("property不支持vertexId查询");
                    }
                    if (supportSerializable && !vertexPropertyDefinition.isSerializable()){
                        LOG.warn("{}#{} 字段不支持持久化", implClass.getSimpleName(), fieldName);
                        return false;
                    }
                    return true;
                })
                .map(VertexPropertyDefinition::getPropertyName)
                .orElse(null);
    }

    @SafeVarargs
    static <T> String[] method2Properties(SerializedFunction<T, ?>... funcs) {
        return Arrays.stream(funcs)
                .map(LambdaSupport::resolve)
                .map(SerializedFunction::method2Property)
                .toArray(String[]::new);
    }

    static String method2Field(SerializedLambda lambda) {
        return PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
    }

    static Class<?> getFuncImplClass(SerializedLambda lambda){
        return ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
    }
}
