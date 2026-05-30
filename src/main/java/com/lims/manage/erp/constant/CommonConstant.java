package com.lims.manage.erp.constant;

/**
 * 系统常量
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface CommonConstant {

	/** 删除标志:已删除 */
	Integer DEL_FLAG_1 = 1;

	/** 删除标志:未删除 */
	Integer DEL_FLAG_0 = 0;

    //================规则信息========================

    /**
     *签到规则id
     */
    String INTEGRAL_RULE_SIGN_IN="1";

    /**
     *上传学习资料id
     */
    String INTEGRAL_RULE_UPLOAD_LEARN_DATA="2";

    /**
     *上传学习视频id
     */
    String INTEGRAL_RULE_UPLOAD_LEARN_VIDEO="3";

    /**
     *阅读学习资料id
     */
    String INTEGRAL_RULE_READ_LEARN_DATA="4";

    /**
     *观看学习视频id
     */
    String INTEGRAL_RULE_READ_LEARN_VIDEO="5";

    /**
     * 点赞规则id
     */
    String INTEGRAL_RULE_LIKE_NUM="9";

    /**
     * 评论点赞数量
     */
    Integer INTEGRAL_RULE_COMMENT_LIKE_NUM=10;

    /**
     * 完成培训开始计划
     */
    String INTEGRAL_RULE_PLAN_COMPLETE="6";
    //===================缓存key======================
    /**
     * 用户签到缓存key
     */
    String CACHE_INTEGRAL_RULE_SIGN_IN="cache:integralRule:signIn:";

    /**
     * 学习资料上传缓存key
     */
    String CACHE_INTEGRAL_RULE_UPLOAD_LEARN_DATA="cache:integralRule:learnData:upload:";

    /**
     * 学习视频上传缓存key
     */
    String CACHE_INTEGRAL_RULE_UPLOAD_LEARN_VIDEO="cache:integralRule:learnVideo:upload:";

    /**
     * 学习资料阅读缓存key
     */
    String CACHE_INTEGRAL_RULE_READ_LEARN_DATA="cache:integralRule:learnData:read:";

    /**
     * 学习视频阅读缓存key
     */
    String CACHE_INTEGRAL_RULE_READ_LEARN_VIDEO="cache:integralRule:learnVideo:read:";
    //===================常用常量=======================
    /**
     * 资料类型-学习资料
     */
    String DATA_TYPE_LEARN_DATA="3";

    /**
     * 资料类型-学习视频
     */
    String DATA_TYPE_LEARN_VIDEO="4";

    /**
     * 审核状态-审核通过
     */
    Integer AUDIT_STATUS_1=1;

    /**
     * 计划参与状态-已报名
     */
    String PLAN_PARTAKE_STATUS_ENROLL="7";

    /**
     * 计划参与状态-已完成
     */
    String PLAN_PARTAKE_STATUS_COMPLETE="8";

    /**
     * 计划参与状态-未完成
     */
    String PLAN_PARTAKE_STATUS_INCOMPLETE="9";

    /**
     * 计划参与状态-未报名但完成
     */
    String PLAN_PARTAKE_STATUS_NO_ENROLL_COMPLETE="22";

    /**
     * 用户操作事件类型-资料
     */
    String USER_OPERATION_EVENT_TYPE_DATA="13";

    /**
     * 用户操作事件类型-问答
     */
    String USER_OPERATION_EVENT_TYPE_PROBLEM="14";

    /**
     * 用户操作事件类型-评论
     */
    String USER_OPERATION_EVENT_TYPE_COMMENT="15";

    /**
     * 用户操作事件类型-计划类型
     */
    String USER_OPERATION_EVENT_TYPE_PLAN="23";

    /**
     * 用户操作类型-浏览
     */
    String USER_OPERATION_TYPE_VIEW="16";

    /**
     * 用户操作类型-点赞
     */
    String USER_OPERATION_TYPE_LIKE="17";

    /**
     * 用户操作类型-点踩
     */
    String USER_OPERATION_TYPE_TAP="18";

    /**
     * 用户操作类型-收藏
     */
    String USER_OPERATION_TYPE_COLLECT="19";

    /**
     * 用户操作类型-完成
     */
    String USER_OPERATION_TYPE_COMPLETE="20";

    /**
     * 用户角色-具有新建培训计划的角色
     */
    String USER_ROLE_PLAN="1676864215192100";

    /**
     * 用户角色-具有问题归类的角色
     */
    String USER_ROLE_PROBLEM_CLASSIFICATION="1676864215192100";

    /**
     * 问答类型-普通提问
     */
    String PROBLEM_TYPE_COMMON="10";

    /**
     * 知识标签类型-用户自定义标签
     */
    Integer DATA_LABEL_TYPE_1=1;
}
