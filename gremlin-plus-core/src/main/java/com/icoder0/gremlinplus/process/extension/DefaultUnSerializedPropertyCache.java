package com.icoder0.gremlinplus.process.extension;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * @author bofa1ex
 * @since 2020/12/29
 */
public class DefaultUnSerializedPropertyCache implements UnSerializedPropertyCache {

    /**
     * key:自生成id, value:不支持序列化字段值
     */
    private final Map<Object, Object> unSerializedPropertyCache = new ConcurrentHashMap<>();

    @Override
    public void put(Object key, Object val) {
        requireNonNull(key);
        if (unSerializedPropertyCache.containsKey(key) && val == null){
            unSerializedPropertyCache.remove(key);
            return;
        }
        unSerializedPropertyCache.put(key, val);
    }

    @Override
    public Object get(Object key) {
        requireNonNull(key);
        return unSerializedPropertyCache.get(key);
    }

    public static class Holder {
        private static final DefaultUnSerializedPropertyCache instance = new DefaultUnSerializedPropertyCache();
    }

    public static DefaultUnSerializedPropertyCache getInstance() {
        return DefaultUnSerializedPropertyCache.Holder.instance;
    }
}
