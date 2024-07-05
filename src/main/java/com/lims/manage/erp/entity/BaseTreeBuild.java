package com.lims.manage.erp.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-05 15:27
 * @Copyright © 河南交科院
 */
public class BaseTreeBuild {
    /**
     * 将flat list转换为tree结构
     * @param flatAduditBaseDatas 扁平化列表
     * @return 树形结构的根节点列表
     */
    public static List<AduditBaseData> buildTree(List<AduditBaseData> flatAduditBaseDatas) {
        Map<Integer, AduditBaseData> AduditBaseDataMap = new HashMap<>();
        List<AduditBaseData> rootAduditBaseDatas = new ArrayList<>();
        // 先将所有节点放入map中，以便快速查找
        for (AduditBaseData AduditBaseData : flatAduditBaseDatas) {
            AduditBaseDataMap.put(AduditBaseData.getId(), AduditBaseData);
        }
        // 遍历所有节点，构建父子关系
        for (AduditBaseData AduditBaseData : flatAduditBaseDatas) {
            Integer pid = AduditBaseData.getPid();
            if (pid == null || !AduditBaseDataMap.containsKey(pid)) {
                // 如果pid为null或找不到父节点，则认为是根节点
                rootAduditBaseDatas.add(AduditBaseData);
            } else {
                AduditBaseData parentAduditBaseData = AduditBaseDataMap.get(pid);
                parentAduditBaseData.getChildren().add(AduditBaseData);
            }
        }
        return rootAduditBaseDatas;
    }
}
