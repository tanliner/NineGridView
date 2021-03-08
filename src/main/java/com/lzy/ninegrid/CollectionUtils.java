package com.lzy.ninegrid;

import java.util.Collection;

/**
 * @author tanlin
 * @version 1.0
 * @desc
 * @since 2021/3/4 15:16
 */
public class CollectionUtils {
    public static <T> int size(T... t) {
        return t == null ? 0 : t.length;
    }

    public static boolean isBlank(Collection<?> list) {
        return !isNotBlank(list);
    }

    public static boolean isNotBlank(Collection<?> list) {
        return list != null && list.size() > 0;
    }
}
