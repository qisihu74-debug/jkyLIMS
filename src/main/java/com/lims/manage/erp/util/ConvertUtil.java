package com.lims.manage.erp.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2022-07-18 16:42
 * @Copyright © 河南交科院
 */
public class ConvertUtil {
    /**
     *
     * @param originalList          原始的list数据
     * @param idFieldName           唯一标识的字段名称（主键）
     * @param pidFieldName          父节点标识的字段名称
     * @param childrenFieldName     子节点列表标识的字段名称
     * @return java.util.List<T>    树结构列表
     */
    public static <T> List<T> list2TreeList(List<T> originalList,
                                            String idFieldName,
                                            String pidFieldName,
                                            String childrenFieldName) {

        // 获取根节点，就是找出父节点为空的对象
        List<T> rootNodeList = new ArrayList<>();
        for (T t : originalList) {
            String parentId = "";
            try {
                parentId = BeanUtils.getProperty(t, pidFieldName);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            if (Integer.parseInt(parentId) == 0) {
                rootNodeList.add(0, t);
            }
        }
        // 将根节点从原始list移除，减少下次处理数据
        originalList.removeAll(rootNodeList);

        // 递归封装树
        try {
            packTree(rootNodeList, originalList, idFieldName, pidFieldName, childrenFieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootNodeList;
    }


    /**
     * 封装树（向下递归）
     *
     * @param parentNodeList        要封装为树的父节点对象集合
     * @param originalList          原始list数据
     * @param idFieldName           作为唯一标示的字段名称
     * @param pidFieldName          父节点标识字段名
     * @param childrenFieldName     子节点（列表）标识字段名
     */
    private static <T> void packTree(List<T> parentNodeList,
                                     List<T> originalList,
                                     String idFieldName,
                                     String pidFieldName,
                                     String childrenFieldName) throws Exception {
        for (T parentNode : parentNodeList) {
            // 找到当前父节点的子节点列表
            List<T> children = packChildren(parentNode, originalList, idFieldName, pidFieldName, childrenFieldName);
            if (children.isEmpty()) {
                continue;
            }

            // 将当前父节点的子节点从原始list移除，减少下次处理数据
            originalList.removeAll(children);

            // 开始下次递归
            packTree(children, originalList, idFieldName, pidFieldName, childrenFieldName);
        }
    }

    /**
     * 封装子对象
     *
     * @param parentNode        父节点对象
     * @param originalList      原始list数据
     * @param keyName           作为唯一标示的字段名称
     * @param pidFieldName      父节点标识字段名
     * @param childrenFieldName 子节点（列表）标识字段名
     */
    private static <T> List<T> packChildren(T parentNode,
                                            List<T> originalList,
                                            String keyName,
                                            String pidFieldName,
                                            String childrenFieldName) throws Exception {
        // 找到当前父节点下的子节点列表
        List<T> childNodeList = new ArrayList<>();
        String parentId = BeanUtils.getProperty(parentNode, keyName);
        for (T t : originalList) {
            String childNodeParentId = BeanUtils.getProperty(t, pidFieldName);
            if (parentId.equals(childNodeParentId)) {
                childNodeList.add(t);
            }
        }

        // 将当前父节点下的子节点列表写入到当前父节点下（给子节点列表字段赋值）
        if (!childNodeList.isEmpty()) {
            FieldUtils.writeDeclaredField(parentNode, childrenFieldName, childNodeList, true);
        }

        return childNodeList;
    }
}
