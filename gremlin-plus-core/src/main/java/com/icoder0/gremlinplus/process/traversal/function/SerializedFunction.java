package com.icoder0.gremlinplus.process.traversal.function;


import com.icoder0.gremlinplus.process.traversal.toolkit.LambdaSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.SerializedFunctionSupport;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
@FunctionalInterface
public interface SerializedFunction<T, R> extends Function<T, R>, Serializable {

    static <T, R> String unwrap(SerializedFunction<T, R> func) {
        return unwrap(func, false);
    }

    static <T, R> String unwrap(SerializedFunction<T, R> func, boolean supportSerializable) {
        return SerializedFunctionSupport.method2Property(LambdaSupport.resolve(func), supportSerializable);
    }
}
