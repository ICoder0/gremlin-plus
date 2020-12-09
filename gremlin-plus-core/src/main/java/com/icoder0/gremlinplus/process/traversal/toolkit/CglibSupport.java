package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.traversal.function.CheckedException;
import net.sf.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;

/**
 * @author bofa1ex
 * @since 2020/12/9
 */
public class CglibSupport {

    public static <T> T newInstance(Class<T> clazz){
        try {
            return (T) FastClass.create(clazz).newInstance();
        } catch (InvocationTargetException e) {
            throw new CheckedException(e);
        }
    }
}
