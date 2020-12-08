package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.commons.lang3.ClassUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.icoder0.gremlinplus.process.traversal.function.ThrowingSupplier.unchecked;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class LambdaSupport {

    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<Class<?>, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析 lambda 表达式.
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     * @see #resolve(SerializedFunction)
     */
    public static <T> SerializedLambda resolve(SerializedFunction<T, ?> func) {
        final Class<?> functionClazz = func.getClass();
        return Optional.ofNullable(FUNC_CACHE.get(functionClazz))
                .map(WeakReference::get)
                .orElseGet(unchecked(() -> {
                    final Method writeReplaceMethod = functionClazz.getDeclaredMethod("writeReplace");
                    writeReplaceMethod.setAccessible(true);
                    final SerializedLambda lambda = (SerializedLambda) writeReplaceMethod.invoke(func);
                    FUNC_CACHE.put(functionClazz, new WeakReference<>(lambda));
                    return lambda;
                }));
    }


    /**
     * 正常化类名称，将类名称中的 / 替换为 .
     *
     * @param name 名称
     * @return 正常的类名
     */
    private static String normalName(String name) {
        return name.replace('/', '.');
    }


    /**
     * 获取实现的 class
     *
     * @return 实现类
     */
    public static Class<?> getImplClass(SerializedLambda lambda) throws ClassNotFoundException {
        return Class.forName(getImplClassName(lambda));
    }

    /**
     * 获取 class 的名称
     *
     * @return 类名
     */
    public static String getImplClassName(SerializedLambda lambda) {
        return normalName(lambda.getImplClass());
    }

}
