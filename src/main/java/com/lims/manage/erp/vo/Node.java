package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.vo
 * @desc
 * @date 2023-09-08 14:46
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_sample_area")
public class Node {
    int id;
    int pid;
    String name;
    @TableField(exist = false)
    List<Node> children;

    public Node(int id, int pid, String name) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.children = new ArrayList<>();
    }
}
