package com.icoder0.gremlinplus.process.traversal.toolkit;

import java.util.Locale;

/**
 * @author bofa1ex
 * @since 2020/12/6
 */
public class PropertyNamerSupport {

    public static String resolvePropertyName(String original){
        if (original.startsWith("is")) {
            original = original.substring(2);
        } else {
            if (!original.startsWith("get") && !original.startsWith("set")) {
                throw new RuntimeException("Error parsing property name '" + original + "'.  Didn't start with 'is', 'get' or 'set'.");
            }
            original = original.substring(3);
        }
        if (original.length() == 1 || original.length() > 1 && !Character.isUpperCase(original.charAt(1))) {
            original = original.substring(0, 1).toLowerCase(Locale.ENGLISH) + original.substring(1);
        }
        return original;
    }
}
