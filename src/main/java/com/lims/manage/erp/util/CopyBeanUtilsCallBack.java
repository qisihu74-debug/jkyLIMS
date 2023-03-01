package com.lims.manage.erp.util;

/**
 * 类型转化公共类回调类
 * @author : zhq
 * @date :   2023-01-03
 * @version : V1.0
 */
@FunctionalInterface
public interface CopyBeanUtilsCallBack<S, T> {

    /**
     * 回调方法
     * @param t 数据源类
     * @param s 目标类
     * */
    void callBack(S t, T s);
}
