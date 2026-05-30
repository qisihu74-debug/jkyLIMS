package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TestProductItemTreeVo {
    //检验项目ID
    @TableId(type = IdType.AUTO)
    private Integer checkItemId;
    //父级ID
    private Integer checkItemPid;
    //检测项编号
    private String checkItemCode;
    //产品ID
    private Integer productId;
    //报告模板id
    private String reportModelId;
    //检验项目名称
    private String checkItemName;
    //检测项在报告中的位置坐标
    private String coordinate;
    //检测项图标
    private String icon;
    //附加费用
    private String additionalFees;
    //检测项单位（1点，2样）
    private String unit;
    //取样数量
    private Integer sampleCount;
    //实验地点
    private String place;
    //实验检测时长
    private String duration;
    //取样频率
    private String frequency;
    //技术难度
    private String difficulty;
    //备注
    private String remark;
    //检测价格
    private String checkPrice;
    //是否有效
    private String isAvailable;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //物流费用
    private String logisticsCosts;
    //应用印章
    private String methodSignet;
    List<TestProductItemTreeVo> children;
}
