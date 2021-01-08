package com.icoder0.gremlinplus.process.traversal.toolkit;

/**
 * @author bofa1ex
 * @since 2020/12/15
 */
public class ExceptionUtils {

    public static CheckedException gpe(Throwable e){
        return new CheckedException(e);
    }

    public static CheckedException gpe(String message) throws CheckedException{
        return new CheckedException(message);
    }

    public static CheckedException gpe(String message ,Throwable e) throws CheckedException{
        return new CheckedException(message, e);
    }

    public static class CheckedException extends RuntimeException{

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
}
