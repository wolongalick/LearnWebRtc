package com.alick.learnwebrtc.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 数据工具类
 * Created by cxw on 2017/3/1.
 */
public class FilterUtils {
    public interface FilterCallback<E> {
        /**
         * 是否需要移除
         *
         * @param model
         * @return
         */
        boolean isNeedRemove(E model);
    }

    /**
     * 过滤集合中的数据
     *
     * @param list           待处理的数据集合
     * @param filterCallback
     * @param <E>
     * @return 已删除的个数
     */
    public static <E> int filterList(List<E> list, FilterCallback<E> filterCallback) {
        if (list == null) {
            return 0;
        }
        int         deleteCount = 0;
        Iterator<E> iterator    = list.iterator();

        E e;
        while (iterator.hasNext()) {
            e = iterator.next();
            if (filterCallback.isNeedRemove(e)) {
                iterator.remove();
                deleteCount++;
            }
        }

        return deleteCount;
    }

    /**
     * 判断集合数据是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }


}
