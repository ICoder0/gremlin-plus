package com.icoder0.gremlinplus.process.traversal.toolkit;

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
            throw ExceptionUtils.gpe(String.format("{%s}反射调用失败", clazz.getName()));
        } catch (IllegalArgumentException e){
            throw ExceptionUtils.gpe(String.format("检查{%s}构造函数是否提供空构造入口", clazz.getName()));
        }
    }
}
