package com.lims.manage.erp.util;

import com.lims.manage.erp.vo.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2023-09-08 14:45
 * @Copyright © 河南交科院
 */
public class TreeBuilder {
    public static List<Node> buildTree(List<Node> nodes) {
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getPid() == 0) { // 如果节点是根节点
                result.add(node);
            } else { // 如果节点有父节点
                Node parent = findParent(nodes, node.getPid()); // 查找父节点
                if (parent != null) {
                    parent.getChildren().add(node); // 将当前节点添加到父节点的子节点列表中
                }
            }
        }
        return result;
    }

    private static Node findParent(List<Node> nodes, int pid) {
        for (Node node : nodes) {
            if (node.getId() == pid) {
                return node;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        List<Node> dataList = new ArrayList<>();
        dataList.add(new Node(1, 0, "水泥"));
        dataList.add(new Node(2, 1, "1-1"));
        dataList.add(new Node(3, 2, "1-1-1"));
        dataList.add(new Node(4, 1, "1-2"));
        dataList.add(new Node(5, 2, "1-1-2"));
        dataList.add(new Node(6, 0, "化学"));
        dataList.add(new Node(7, 4, "1-2-1"));
        dataList.add(new Node(8, 6, "2-1"));

        List<Node> tree = buildTree(dataList);
        System.out.println(tree);
    }
}
