package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
@Data
public class TestInstrumentVo {
    //仪器id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //仪器大类id
    private Integer typeId;
    //仪器名称
    @TableField("`name`")
    private String name;
    //所在实验室
    private Integer laboratoryId;
    //生产编号
    @TableField("`code`")
    private String code;
    //旧编号
    private String oldCode;
    //生产厂家
    private String manufacturer;
    //规格型号
    private String model;
    //出厂编号
    private String serialNumber;
    //购置价格
    private String price;
    //仪器图片
    private String picture;
    //入编日期
    private Date incorporatedDate;
    //最新一次鉴定日期
    private Date appraisalDate;
    //有无串口0没有1有
    private String isPort;
    //精度等级
    @TableField("`level`")
    private String level;
    //有效期
    private Date validityDate;
    //服务内容
    private String content;
    //检测使用仪器一次的费用
    private String checkPrice;
    //规格类型，扩展不确定度、最大允差、准确度等级
    private String specs;
    //仪器量程
    @TableField("`range`")
    private String range;
    //合同url
    private String contractUrl;
    //发票url
    private String invoiceUrl;
    //备注
    private String remark;
    //设备管理员
    private String deviceAdmin;
    //小时单价
    private String pricePerHour;
    //溯源方式，送校、内部校验、送检、比对、标准物质、其它
    private String traceabilityMode;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //所在实验室名称
    private String laboratoryName;
}
