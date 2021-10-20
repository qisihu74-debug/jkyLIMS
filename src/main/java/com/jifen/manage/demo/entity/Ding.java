package com.jifen.manage.demo.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.entity
 * @desc
 * @date 2021/8/25 16:36
 * @Copyright © 河南交科院
 */
@Data
public class Ding {
    private String userName;
    private List<String> list;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
