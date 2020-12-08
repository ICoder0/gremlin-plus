package com.icoder0.gremlinplus.process.traversal.function;

/**
 * @author bofa1ex
 * @since 2020/12/5
 */
public class CheckedException extends RuntimeException{

    public CheckedException() {
    }

    public CheckedException(Throwable cause) {
        super(cause);
    }

    public CheckedException(String message) {
        super(message);
    }

    public CheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
