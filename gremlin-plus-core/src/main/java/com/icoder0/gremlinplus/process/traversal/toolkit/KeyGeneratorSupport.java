package com.icoder0.gremlinplus.process.traversal.toolkit;

import com.icoder0.gremlinplus.process.extension.DefaultKeyGenerator;
import com.icoder0.gremlinplus.process.extension.KeyGenerator;

/**
 * @author bofa1ex
 * @since 2020/12/23
 */
public class KeyGeneratorSupport {
    protected static KeyGenerator keyGenerator = DefaultKeyGenerator.getInstance();

    public static Object generate(){
        if (keyGenerator == null){
            throw new IllegalArgumentException("keyGenerator不可为空");
        }
        return keyGenerator.key();
    }
}
