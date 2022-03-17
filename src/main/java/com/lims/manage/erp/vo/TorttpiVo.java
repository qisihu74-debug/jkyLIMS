package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class TorttpiVo {
    //主键id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //模板编号
    private String code;
    //模板名称
    private String name;
    //检测项id
    private Integer checkItemId;
    //原始记录
    private String fileUrl;
    //是否有效1有效，0无效
    private String isAvailable;

    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //备注
    private String remark;

    private  String checkitemname;


}
