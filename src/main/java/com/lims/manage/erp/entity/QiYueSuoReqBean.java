package com.lims.manage.erp.entity;
import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * 契约锁创建合同bean
 */
@Data
public class QiYueSuoReqBean {
    /**
     * 委托单id
     */
    private Long entrustId;
    /**
     * 合同名称
     */
    private String subject;
    /**
     * 印章类型PHYSICS("物理签章"),ELECTRONIC("电子签章"),不传默认查询电子章
     */
    private String category;
    /**
     *(契约锁提供)用印流程ID-> 2934717410113839636
     */
    private String categoryId;
    /**
     *是否发起合同，默认true。（true：立即发起；false：保存为草稿）
     */
    private Boolean send;
    /**
     * 合同文档ID的集合（一个文档只能属于一个合同）接口qiYueSuoHnadler.creatFile()返回
     */
    private List<String> documents;
    /**
     * 合同创建人姓名
     */
    private String creatorName;
    /**
     * 合同创建人手机号码
     */
    private String creatorContact;
    /**
     * 发起方名称
     */
    private String tenantName;
    /**
     * 签署方，为空时在合同签署完成后需要调用接口“封存合同”主动结束合同
     */
    private List<Signatories> signatories;

    @Data
    public static class Signatories {
        /**
         * 签约主体类型：COMPANY（外部企业），PERSONAL（个人）
         */
        private String tenantType;
        /**
         * 签约主体名称
         */
        private String tenantName;
        /**
         *接收人姓名
         */
        private String receiverName;
        /**
         * 接收人联系方式
         */
        private String contact;
        /**
         * 签署顺序（从1开始)；如果想按顺序签署，
         * 则分别设置签署方的serialNo为1,2,3；如果想无序签署，
         * 则设置签署方的serialNo为1,1,1；设置签署方顺序为1,2,2时，
         * 表示第一个先签署，后两个同时签署。
         */
        private String serialNo;
        /**
         * 签署动作,用印流程非预设且签署方为发起方时，使用用户传入的签署动作，其余情况使用用印流程的配置
         */
        private List<Actions> actions;

        @Data
        public static class Actions {
            /**
             * 签署动作类型：CORPORATE（企业签章），PERSONAL（个人签字），LP（法定代表人签字），AUDIT（审批）
             */
            private String type;
            /**
             * 签署动作名称
             */
            private String name;
            /**
             * 签署顺序（从1开始)；如果想按顺序签署，
             * 则分别设置签署动作的serialNo为1,2,3；
             * 如果想无序签署，则设置签署动作的serialNo为1,1,1；
             * 设置签署动作顺序为1,2,2时，表示第一个先签署，后两个同时签署
             */
            private String serialNo;
            /**
             * 指定印章，格式：[123123123213,123213213213]
             */
            private List<String> sealIds;
            /**
             * 签章所属组织ID，发起方生效，默认为发起方关联的组织
             */
            private Long sealOwner;
            /**
             *操作人信息
             */
            private String[] actionOperators;
        }
    }
}