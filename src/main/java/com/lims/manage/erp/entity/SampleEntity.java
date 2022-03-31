package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lims.manage.erp.vo.JudgmentBasisVo;
import com.lims.manage.erp.vo.SampleAddDetailVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class SampleEntity {
    /**
     * 主键id
     */
    private Integer id;
    /**
     *
     */
    private Integer productId;
    /**
     * 委托单位
     */
    private Integer companyId;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 规格/等级
     */
    private String specs;
    /**
     * 批号、编号
     */
    private String batchNumber;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 样品产地
     */
    private String sampleOrigin;
    /**
     * 外观
     */
    private String outward;
    /**照片文件
     *
     */
    //private MultipartFile pictureFile;
    /**
     * 样品照片
     */
    private String picture;
    /**
     * 文件对象
     */
    //private MultipartFile file;
    /**
     * 附件url
     */
    private String fileUrl;
    /**
     * 保存地点
     */
    private String savePlace;
    /**
     * 管理员
     */
    private String admin;
    /**
     * 样品组数
     */
    private Integer sampleGroups;
    /**
     * 每组样品数
     */
    private Integer quantityPerGroup;
    /**
     * 验收员
     */
    private String inspector;
    /**
     * 接收日期
     */
    private String receivedDate;
    /**
     * 样品要求
     */
    private String sampleRequirement;
    /**
     * 代表批量
     */
    private String generation;
    /**
     * 样品状态
     */
    private String state;
    /**
     * 检验时间
     */
    private Date checkDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 判定依据
     */
    private List<Integer> standardFileIds;
    /**
     * 判定依据 JSON 格式
     */
    private List<JudgmentBasisVo> standardFileIdStr;
    /**
     * 样品依据 ID 和名称
     */
    /**
     * 样品检测项
     */
    private List<SampleItemEntity> sampleCheckItem;
//    /**
//     * 样品与检测项
//     */
//    private List<CheckItemInfoVo> sampleCheckItem;
    /**
     * 外观描述
     */
    private String outwardDescribe;
    /**
     * 外观描述
     */
    private String insertFlag;
    /**
     * 样品数量
     */
    private String sampleQuantity;
    /**
     * 别名
     */
    private String aliasName;
    /**
     * 样品类型：做原材检测还是配合比检测
     */
    private String sampleType;
    /**
     * 0为未使用，1为已使用
     */
    private Integer isUse;
    @TableField(exist = false)
    private Integer pageNum;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private String beginDate;
    @TableField(exist = false)
    private String endDate;

    /**
     * 样品下检测项、检测依据、总价
     */
    private List<JudgmentBasisVo> judgmentBasisVos;

    /**
     * 样品下检测项、检测依据、总价 String 表示
     */
   private  List<JudgmentBasisVo> judgmentBasisVoStr;


    public SampleEntity(SampleAddParamVo addParamVo, SampleAddDetailVo detailVo, String sampleName, String sampleCode, String pictureUrl, String insertFlag) {
        this.productId = addParamVo.getSampleName();
        this.companyId = addParamVo.getCompanyId();
        this.sampleName = sampleName;
        this.sampleCode = sampleCode;
        this.specs = addParamVo.getSpecs();
        this.batchNumber = detailVo == null ? null : detailVo.getBatchNumber();
        this.manufacturer = addParamVo.getManufacturer();
        this.sampleOrigin = addParamVo.getSampleOrigin();
        this.outward = addParamVo.getOutward();
//        this.pictureFile = pictureFile;
        this.picture = pictureUrl;
//        this.file = file;
//        this.fileUrl = fileUrl;
//        this.savePlace = savePlace;
//        this.admin = admin;
        this.sampleGroups = addParamVo.getSampleGroups();
        this.quantityPerGroup = addParamVo.getQuantityPerGroup();
        this.inspector = addParamVo.getInspector();
        this.receivedDate = addParamVo.getReceivedDate();
        this.sampleRequirement = addParamVo.getSampleRequirement();
        this.generation = addParamVo.getGeneration();
        this.outwardDescribe = addParamVo.getOutwardDescribe();
        this.sampleQuantity=addParamVo.getSampleQuantity();
        this.insertFlag = insertFlag;
        this.isUse = 0;
        this.aliasName = addParamVo.getAliasName();
        this.sampleType = addParamVo.getSampleType();
//        this.state = state;
//        this.checkDate = checkDate;
//        this.remark = remark;
//        this.standardFileIds = standardFileIds;
    }

    public SampleEntity() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName == null ? null : sampleName.trim();
    }

    public String getSampleCode() {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode) {
        this.sampleCode = sampleCode == null ? null : sampleCode.trim();
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer == null ? null : manufacturer.trim();
    }

    public String getSampleOrigin() {
        return sampleOrigin;
    }

    public void setSampleOrigin(String sampleOrigin) {
        this.sampleOrigin = sampleOrigin == null ? null : sampleOrigin.trim();
    }

    public String getOutward() {
        return outward;
    }

    public void setOutward(String outward) {
        this.outward = outward == null ? null : outward.trim();
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture == null ? null : picture.trim();
    }

    public String getsavePlace() {
        return savePlace;
    }

    public void setsavePlace(String savePlace) {
        this.savePlace = savePlace == null ? null : savePlace.trim();
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin == null ? null : admin.trim();
    }

    public Integer getSampleGroups() {
        return sampleGroups;
    }

    public void setSampleGroups(Integer sampleGroups) {
        this.sampleGroups = sampleGroups;
    }

    public Integer getQuantityPerGroup() {
        return quantityPerGroup;
    }

    public void setQuantityPerGroup(Integer quantityPerGroup) {
        this.quantityPerGroup = quantityPerGroup;
    }

    public String getInspector() {
        return inspector;
    }

    public void setInspector(String inspector) {
        this.inspector = inspector == null ? null : inspector.trim();
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate == null ? null : receivedDate.trim();
    }

    public String getSampleRequirement() {
        return sampleRequirement;
    }

    public void setSampleRequirement(String sampleRequirement) {
        this.sampleRequirement = sampleRequirement == null ? null : sampleRequirement.trim();
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation == null ? null : generation.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getOutwardDescribe() {
        return outwardDescribe;
    }

    public void setOutwardDescribe(String outwardDescribe) {
        this.outwardDescribe = outwardDescribe;
    }

    public String getInsertFlag() {
        return insertFlag;
    }

    public void setInsertFlag(String insertFlag) {
        this.insertFlag = insertFlag;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}