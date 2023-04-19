package com.lims.manage.erp.util;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/10/28 17:23
 * @Copyright © 河南交科院
 */
public class Const {
    public static final String text = "text/plain";
    public static final String css = "text/css";
    public static final String html = "text/html";
    public static final String js = "application/x-javascript";
    public static final String json = "application/json";
    public static final String image = "image/png jpg gif";
    public static final String contentType = "application/octet-stream";
    /**
     * 登出日志
     */
    public static final String LOGIN_LOG_OUT = "0";
    /**
     * 登陆日志
     */
    public static final String LOGIN_LOG = "1";
    /**
     * 系统管理日志
     */
    public static final String SYS_MANAGER_LOG = "2";
    /**
     * 实验检测日志
     */
    public static final String CHECK_LOG = "3";
    /**
     * 资产设备管理日志
     */
    public static final String DEVICE_LOG = "4";
    /**
     * 创建用户日志
     */
    public static final String CREATE_USER = "5";
    /**
     * 启用/停用用户日志
     */
    public static final String CHANGE_STATE = "6";
    /**
     * 重置密码
     */
    public static final String RESET_PASSWORD = "7";
    /**
     * 修改密码
     */
    public static final String UPDATE_PASSWORD = "8";
    /**
     * 修改用户信息
     */
    public static final String UPDATE_USERINFO = "9";
    /**
     * 委托发布
     */
    public static final String ENTRUST_PUBLISH = "10";
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "111111";
    public static final Integer PAGE_NUM = 1;
    public static final Integer PAGE_SIZE = 10;
    /**
     * 人员管理日志
     */
    public static final String PERSON_LOG = "11";
    /**
     * 部门管理日志
     */
    public static final String DEPT_LOG = "12";
    /**
     * 角色名 (approver) 审批人 基于数据库 sys_role 定义
     */
    public static final String approverStr = "approver";
    /**
     * 角色id =2L 审批人 基于数据库 sys_role 定义
     */
    public static final Long approverLongUserId = 2L;
    /**
     * 角色名 (signer) 签发人 基于数据库 sys_role 定义
     */
    public static final String signerStr = "signer";
    /**
     * 角色id = 3L 签发人 基于数据库 sys_role 定义
     */
    public static final Long signerLongUserId = 3L;
    /**
     * 产品管理日志
     */
    public static final String PRODUCT_MANAGEMENT_LOG = "13";
    /**
     * 知识管理日志
     */
    public static final String KNOWLEDGE_MANAGEMENT_LOG = "14";
    /**
     * 团队管理日志
     */
    public static final String TEAM_MANAGEMENT_LOG = "15";
    /**
     * 检测管理日志
     */
    public static final String DETECTION_MANAGEMENT_LOG = "16";
    /**
     * 仪器管理日志
     */
    public static final String INSTRUMENT_MANAGEMENT_LOG = "17";
    /**
     * 合作关系管理日志
     */
    public static final String PARTNERSHIP_MANAGEMENT_LOG = "19";
    /**
     * 委托创建
     */
    public static final String ENTRUST_FOUND = "20";
    /**
     * 委托附件处理
     */
    public static final String ENTRUST_file = "21";
    /**
     * 任务流转单
     */
    public static final String TASK_FLOW = "22";
    /**
     * 单位信息
     */
    public static final String Applicant_Info = "23";
    /**
     * 任务领取
     */
    public static final String TASK_GET = "24";
    /**
     * 试验检测
     */
    public static final String TASK_TEST = "25";
    /**
     * 报告，原始记录
     */
    public static final String REPORT_ORIGINAL = "26";
    /**
     * 常规图片后缀名大全。
     */
   public static final  String[] nameSuffixS ={"tiff","pjp","jfif","bmp","webp","pjpeg","avif","tif","jpg","svgz","png","xbm","dib","jxl","jpeg","gif","svg","ico"};
    /**
     * 设计全局枚举 菜单名 与对应的表格相一致。
     */
    public static final String[] taskKanbans = {"任务发布","任务领取","试验检测","报告合成","报告审批","报告签发","电子印章","报告邮寄"};
//   public static final String sampleStr = "查询/打印";
   public static final String entrustStr = "任务发布";
   public static final String taskStr = "任务领取";
   public static final String testStr = "试验检测";
   public static final String reportStr = "报告合成";
   public static final String approvalStr = "报告审批";
   public static final String verifyStr = "报告签发";
   public static final String sealStr = "电子印章";
   public static final String toBeAStr = "报告邮寄";
}
