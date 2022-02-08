package com.lims.manage.erp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/2/7 16:39
 * 前端JSON 数据 与后台交互 接收类
 */
@Data
public class SysRoleFuncMenuVo {

    /**
     * 角色id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;
    /**
     * 菜单id
     */
    private List<Long> list;

}
