package com.lims.manage.erp.entity;

import com.lims.manage.erp.vo.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-05 15:27
 * @Copyright © 河南交科院
 */
public class BaseTreeBuild {
    public static List<AduditBaseData> buildTree(List<AduditBaseData> nodes) {
        List<AduditBaseData> result = new ArrayList<>();
        for (AduditBaseData node : nodes) {
            if (node.getPid() == 0) { // 如果节点是根节点
                result.add(node);
            } else { // 如果节点有父节点
                AduditBaseData parent = findParent(nodes, node.getPid()); // 查找父节点
                if (parent != null) {
                    parent.getChildren().add(node); // 将当前节点添加到父节点的子节点列表中
                }
            }
        }
        return result;
    }

    private static AduditBaseData findParent(List<AduditBaseData> nodes, int pid) {
        for (AduditBaseData node : nodes) {
            if (node.getId() == pid) {
                return node;
            }
        }
        return null;
    }
}
