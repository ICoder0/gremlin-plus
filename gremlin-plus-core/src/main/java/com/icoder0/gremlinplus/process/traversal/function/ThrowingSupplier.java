package com.icoder0.gremlinplus.process.traversal.function;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public interface ThrowingSupplier<T,E extends Exception> {

    T get() throws E;

    static <T> Supplier<T> unchecked(ThrowingSupplier<? extends T, ?> supplier) {
        requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (final Exception e) {
                throw new CheckedException(e);
            }
        };
    }
}
