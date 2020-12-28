package com.icoder0.gremlinplus.process.extension;

import java.util.UUID;

/**
 * @author bofa1ex
 * @since 2020/12/20
 */
public class DefaultKeyGenerator implements KeyGenerator {

    public static class Holder {
        private static final DefaultKeyGenerator instance = new DefaultKeyGenerator();
    }

    public static KeyGenerator getInstance() {
        return Holder.instance;
    }

    @Override
    public Object key() {
        return UUID.randomUUID().toString();
    }
}
