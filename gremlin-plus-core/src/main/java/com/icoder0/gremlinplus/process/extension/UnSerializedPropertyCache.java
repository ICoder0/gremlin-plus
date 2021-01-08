package com.icoder0.gremlinplus.process.extension;


/**
 * @author bofa1ex
 * @since 2020/12/29
 */
public interface UnSerializedPropertyCache {
    void put(Object key, Object value);

    Object get(Object generateKey);
}
