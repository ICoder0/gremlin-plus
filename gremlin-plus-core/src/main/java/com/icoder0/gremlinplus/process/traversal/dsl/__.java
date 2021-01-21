package com.icoder0.gremlinplus.process.traversal.dsl;


import com.icoder0.gremlinplus.process.traversal.function.SerializedFunction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author bofa1ex
 * @since 2020/12/30
 */
public class __ {

    private static <S, E, L> GraphPlusTerminalTraversal<S, E, L> start() {
        return new GraphPlusTerminalTraversal<>();
    }

    public static <S, E, L> GraphPlusTerminalTraversal<Vertex, Vertex, L> addV(final Class<L> clazz) {
        return __.<S, E, L>start().addV(clazz);
    }

    public static <S, E, L> GraphPlusTerminalTraversal<S, E, L> hasLabel(final Class<L> clazz) {
        return __.<S, E, L>start().hasLabel(clazz);
    }

    public static <S, E, L, R> GraphPlusTerminalTraversal<S, E, L> property(final SerializedFunction<L, R> func, final R value, final Object... keyValues) {
        return __.<S, E, L>start().property(func, value, keyValues);
    }

    public static <S, E, L, R> GraphPlusNormalTraversal<S, R, L> value(final SerializedFunction<L, R> func) {
        return __.<S, E, L>start().value(func);
    }
}
