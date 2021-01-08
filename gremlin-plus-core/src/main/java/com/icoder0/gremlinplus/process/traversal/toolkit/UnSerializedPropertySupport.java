package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.extension.DefaultUnSerializedPropertyCache;
import com.icoder0.gremlinplus.process.extension.UnSerializedPropertyCache;

import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/29
 */
public class UnSerializedPropertySupport {

    private static Class<? extends UnSerializedPropertyCache> unSerializedPropertyCachePlugin;

    public static UnSerializedPropertyCache getUnSerializedPropertyCache(){
        return UnSerializedPropertyCacheHolder.unSerializedPropertyCache;
    }

    public static class UnSerializedPropertyCacheHolder{
        private static UnSerializedPropertyCache unSerializedPropertyCache;

        static{
            unSerializedPropertyCache = Optional.ofNullable(unSerializedPropertyCachePlugin).map(CglibSupport::newInstance)
                    .map(o -> (UnSerializedPropertyCache) o)
                    .orElseGet(DefaultUnSerializedPropertyCache::getInstance);
        }
    }

    public static void init(Class<? extends UnSerializedPropertyCache> plugin){
        unSerializedPropertyCachePlugin = plugin;
    }

    public static <T extends UnSerializedPropertyCache> void init(T t){
        UnSerializedPropertyCacheHolder.unSerializedPropertyCache = t;
    }
}
