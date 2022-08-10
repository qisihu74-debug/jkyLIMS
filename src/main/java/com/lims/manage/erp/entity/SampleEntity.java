package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lims.manage.erp.vo.JudgmentBasisVo;
import com.lims.manage.erp.vo.LabelValueVo;
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
     * 附件url原始名称
     */
    private String fileUrlStr;
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
     * 产品可选的全部判定依据
     */
    private List<LabelValueVo> allStandardFileList;
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
     * 样品备注
     */
    private String sampleRemark;
    /**
     * 单位比
     */
    private String unitRatio;
    /**
     * 每立方米用量
     */
    private String cubicMeterConsumption;
    /**
     * 原材的父ID
     */
    private Integer pid;

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
     * 旧样品id
     */
    @TableField(exist = false)
    private Integer oldSampleid ;
    /**
     * 委托单id
     */
    @TableField(exist = false)
    private Long entrustId;

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
        this.sampleRemark = addParamVo.getSampleRemark();
//        this.state = state;
//        this.checkDate = checkDate;
//        this.remark = remark;
//        this.standardFileIds = standardFileIds;
    }

    public SampleEntity() {
        super();
    }

    public SampleEntity(TestSampleEntity entity) {
        this.id = entity.getId();
        this.productId = entity.getProductId();
        this.companyId = entity.getCompanyId();
        this.sampleName = entity.getSampleName();
        this.sampleCode = entity.getSampleCode();
        this.specs = entity.getSpecs();
        this.batchNumber = entity.getBatchNumber();
        this.manufacturer = entity.getManufacturer();
        this.sampleOrigin = entity.getSampleOrigin();
        this.outward = entity.getOutward();
        this.picture = entity.getPicture();
//        this.fileUrl = entity.getfil;
        this.fileUrlStr = entity.getFileUrlStr();
        this.savePlace = entity.getSavePlace();
        this.admin = entity.getAdmin();
        this.sampleGroups = entity.getSampleGroups();
        this.quantityPerGroup = entity.getQuantityPerGroup();
        this.inspector = entity.getInspector();
        this.receivedDate = entity.getReceivedDate();
        this.sampleRequirement = entity.getSampleRequirement();
        this.generation = entity.getGeneration();
        this.state = entity.getState();
        this.checkDate = entity.getCheckDate();
        this.remark = entity.getRemark();
        this.outwardDescribe = entity.getOutwardDescribe();
        this.sampleQuantity = entity.getSampleQuantity();
        this.aliasName = entity.getAliasName();
        this.sampleType = entity.getSampleType();
        this.sampleRemark = entity.getSampleRemark();
        this.unitRatio = entity.getUnitRatio();
        this.cubicMeterConsumption = entity.getCubicMeterConsumption();
        this.pid = entity.getPid();
        this.isUse = entity.getIsUse();
        this.aliasName = entity.getAliasName();
    }
}