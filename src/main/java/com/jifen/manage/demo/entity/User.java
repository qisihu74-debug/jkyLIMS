package com.jifen.manage.demo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 登录
 *
 * @author gjl
 */


@Data
public class User {
    private Long id;
    /**
     * 用户名称
     */
    private String userName;
    private String passWord;
    private String mobile;
    /**
     * 商家位置（坐标）
     */
    private String address;
    /**
     * 自己的邀请码
     */
    private String userCode;
    /**
     * 别人的邀请码
     */
    private String userParentCode;
    /**
     * 用户类型
     * 1平台
     * 2商家
     * 3客户
     * 4推广大使
     */
    private String userType;
    /**
     * 商家经营范围
     */
    private String businessType;
    /**
     * 身份证号
     */
    private String identification;
    /**
     * 营业执照url
     */
    private String businessLicens;
    /**
     * 身份证正面url
     */
    private String identificationPositive;
    /**
     * 身份证反面url
     */
    private String identificationObverse;

    private String token;

    /**
     * 菜单
     */
    private List<FunctionEntity> list;
    /**
     * 营业执照文件
     */
    private MultipartFile businessFile;
    /**
     * 身份证正面文件
     */
    private MultipartFile IdPositiveFile;
    /**
     * 身份证反面文件
     */
    private MultipartFile IdObverseFile;
    /**
     * 用户审核0未审核，1通过，2拒绝
     */
    private int isValid;

}
