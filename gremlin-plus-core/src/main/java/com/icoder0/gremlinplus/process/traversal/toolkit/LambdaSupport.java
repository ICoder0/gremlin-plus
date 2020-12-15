package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.invoke.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
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
     *
     * key: User::getName
     * value: serializedLambda(类名, 方法名, 字段名)
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
        Class<?> functionClazz = func.getClass();
        // 支持evaluate expression调试, 走proxy代理, 每次缓存key都不命中, 因此不考虑缓存.
        if (func instanceof Proxy) {
            try {
                final Field field = FieldUtils.getDeclaredField(functionClazz.getSuperclass(), "h", true);
                final Object resp0 = field.get(func);
                final Field field2 = FieldUtils.getDeclaredField(resp0.getClass(), "val$target", true);
                final Object resp1 = field2.get(resp0);
                final Field field3 = FieldUtils.getDeclaredField(resp1.getClass(), "member", true);
                final Object resp2 = field3.get(resp1);
                final Field field4 = FieldUtils.getDeclaredField(resp2.getClass(), "name", true);
                final String methodName = (String) field4.get(resp2);
                final Field field5 = FieldUtils.getDeclaredField(resp2.getClass(), "clazz", true);
                final Class<?> clazz = (Class<?>) field5.get(resp2);
                return new SerializedLambda(null, null, null,
                        null, 0, clazz.getName(), methodName,
                        null, null, new Object[0]
                );
            } catch (IllegalAccessException e) {
                throw ExceptionUtils.gpe(e);
            }
        }
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
