package com.icoder0.gremlinplus.process.traversal.function;


import com.icoder0.gremlinplus.process.traversal.toolkit.LambdaSupport;
import com.icoder0.gremlinplus.process.traversal.toolkit.SerializedFunctionSupport;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
@FunctionalInterface
public interface SerializedFunction<T, R> extends Function<T, R>, Serializable {

    static <T, R> Pair<String, Boolean> unwrapPair(SerializedFunction<T, R> func) {
        return SerializedFunctionSupport.method2PropertyBoolPair(LambdaSupport.resolve(func));
    }
}
