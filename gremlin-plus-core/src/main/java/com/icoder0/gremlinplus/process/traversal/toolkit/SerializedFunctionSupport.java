package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.function.ThrowingSupplier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.Optional;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.VERTEX_DEFINITION_MAP;

/**
 * @author bofa1ex
 * @since 2020/12/15
 */
public class SerializedFunctionSupport {

    /**
     * 根据lambda getter/setter表达式获取对应类的字段
     * 再根据对应类的字段获取其在实体类中声明的属性名.
     * 如果该字段声明不可持久化, 会由{@link KeyGeneratorSupport#generate()}生成唯一id作为持久化内容.
     * 并将该字段值存入进程内存.
     *
     * @return tuple with property name and key(which field is not support serialize).
     */
    public static Pair<String, Object> method2PropertyKeyPair(SerializedLambda lambda) {
        final Class<?> implClass = ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
        final String fieldName = PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
        return Optional.ofNullable(VERTEX_DEFINITION_MAP.get(implClass))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                .map(vertexPropertyDefinition -> vertexPropertyDefinition.isSerializable() ?
                        ImmutablePair.of(vertexPropertyDefinition.getPropertyName(), null) :
                        ImmutablePair.of(vertexPropertyDefinition.getPropertyName(), KeyGeneratorSupport.generate()))
                .orElse(ImmutablePair.nullPair());
    }

    /**
     * 根据lambda getter/setter表达式获取对应类的字段
     * 再根据对应类的字段获取其在实体类中声明的属性名.
     * 如果该字段声明不可持久化, 会返回布尔类型作为tuple的value值.
     *
     * @return tuple with property name and state(which field is not support serialize).
     */
    public static Pair<String, Boolean> method2PropertyBoolPair(SerializedLambda lambda) {
        final Class<?> implClass = ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
        final String fieldName = PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
        return Optional.ofNullable(VERTEX_DEFINITION_MAP.get(implClass))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                .map(vertexPropertyDefinition -> ImmutablePair.of(vertexPropertyDefinition.getPropertyName(), vertexPropertyDefinition.isSerializable()))
                .orElse(ImmutablePair.nullPair());
    }


    public static String method2Property(SerializedLambda lambda) {
        final Class<?> implClass = ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
        final String fieldName = PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
        return Optional.ofNullable(VERTEX_DEFINITION_MAP.get(implClass))
                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
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
}
