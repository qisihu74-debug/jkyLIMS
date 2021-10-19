package com.stu.manage.demo.entity;

public class StepsInfoVo {
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

    @Override
    public String toString() {
        return "StepsInfoVo{" +
                "name='" + name + '\'' +
                ", steps=" + steps +
                ", userId='" + userId + '\'' +
                '}';
    }
}
