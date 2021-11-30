package com.lims.manage.erp.vo;

public class CheckItemDetailVo {
    private Integer productId;
    private Integer itemPid;
    private Integer checkItemId;
    private String checkItemName;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getItemPid() {
        return itemPid;
    }

    public void setItemPid(Integer itemPid) {
        this.itemPid = itemPid;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public CheckItemDetailVo(Integer productId, Integer itemPid, Integer checkItemId, String checkItemName) {
        this.productId = productId;
        this.itemPid = itemPid;
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
    }

    public CheckItemDetailVo() {
    }

    @Override
    public String toString() {
        return "CheckItemDetailVo{" +
                "productId=" + productId +
                ", itemPid=" + itemPid +
                ", checkItemId=" + checkItemId +
                ", checkItemName='" + checkItemName + '\'' +
                '}';
    }
}
