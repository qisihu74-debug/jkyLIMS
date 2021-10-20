package com.jifen.manage.demo.entity;

public class StepsInfoVo {
    private String date;
    private String name;
    private long steps;
    private String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "StepsInfoVo{" +
                "date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", steps=" + steps +
                ", userId='" + userId + '\'' +
                '}';
    }
}
