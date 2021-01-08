package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.extension.DefaultKeyGenerator;
import com.icoder0.gremlinplus.process.extension.KeyGenerator;

import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/12/23
 */
public class KeyGeneratorSupport {

    private static Class<? extends KeyGenerator> keyGeneratorPlugin;

    public static class KeyGeneratorHolder {
        public static KeyGenerator keyGenerator;

        static {
            keyGenerator = Optional.ofNullable(keyGeneratorPlugin).map(CglibSupport::newInstance)
                    .map(o -> (KeyGenerator) o)
                    .orElseGet(DefaultKeyGenerator::getInstance);
        }
    }

    public static void init(Class<? extends KeyGenerator> plugin) {
        keyGeneratorPlugin = plugin;
    }

    public static <T extends KeyGenerator> void init(T t) {
        KeyGeneratorHolder.keyGenerator = t;
    }

    public static Object generate() {
        return KeyGeneratorHolder.keyGenerator.key();
    }
}
