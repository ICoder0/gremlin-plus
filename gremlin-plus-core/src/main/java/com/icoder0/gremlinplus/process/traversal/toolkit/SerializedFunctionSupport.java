package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.definition.VertexDefinition;
import com.icoder0.gremlinplus.process.traversal.definition.VertexPropertyDefinition;
import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import com.icoder0.gremlinplus.process.traversal.function.ThrowingSupplier;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.getVertexDefinitionCache;
import static com.icoder0.gremlinplus.process.traversal.toolkit.VertexDefinitionSupport.resolveProperties;


/**
 * @author bofa1ex
 * @since 2020/12/15
 */
public class SerializedFunctionSupport {

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
        return Optional.of(VertexDefinitionSupport.getVertexDefinitionCache().putIfAbsent(implClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(implClass))
                .withVertexPropertyDefinitionMap(VertexDefinitionSupport.resolveProperties(implClass))
                .withBeanMap(BeanMap.create(CglibSupport.newInstance(implClass)))
                .build()))

                .map(VertexDefinition::getVertexPropertyDefinitionMap)
                .map(vertexPropertyDefinitionMap -> vertexPropertyDefinitionMap.get(fieldName))
                .map(vertexPropertyDefinition -> ImmutablePair.of(vertexPropertyDefinition.getPropertyName(), vertexPropertyDefinition.isSerializable()))
                .orElse(ImmutablePair.nullPair());
    }


    public static String method2Property(SerializedLambda lambda) {
        final Class<?> implClass = ThrowingSupplier.unchecked(() -> LambdaSupport.getImplClass(lambda)).get();
        final String fieldName = PropertyNamerSupport.resolvePropertyName(lambda.getImplMethodName());
        final BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBeanClass(implClass);
        return Optional.ofNullable(getVertexDefinitionCache().putIfAbsent(implClass, () -> VertexDefinition.builder()
                .withLabel(AnnotationSupport.resolveVertexLabel(implClass))
                .withVertexPropertyDefinitionMap(resolveProperties(implClass))
                .withBeanMap(generator.create())
                .build()))

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
