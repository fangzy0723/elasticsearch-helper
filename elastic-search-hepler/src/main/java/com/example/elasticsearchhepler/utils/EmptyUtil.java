package com.example.elasticsearchhepler.utils;

import org.springframework.util.ObjectUtils;

public class EmptyUtil {

    public EmptyUtil() {
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else {
            return obj instanceof String ? ((String)obj).trim().equals("") : ObjectUtils.isEmpty(obj);
        }
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
