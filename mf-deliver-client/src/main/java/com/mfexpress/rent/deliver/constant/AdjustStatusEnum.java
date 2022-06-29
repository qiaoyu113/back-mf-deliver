package com.mfexpress.rent.deliver.constant;

public enum AdjustStatusEnum {

    NOT_ADJUST(1, "待调整", "保存"),
    STRING_BILLING(2, "已调整计费", "调整计费"),
    COMPLETED(3, "已完成", "完成"),
    ;

    private int index;
    private String title;

    private String operatorName;

    AdjustStatusEnum(int index, String title, String operatorName) {
        this.index = index;
        this.title = title;
        this.operatorName = operatorName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getTitle(int index) {

        for (AdjustStatusEnum obj : AdjustStatusEnum.values()) {
            if (obj.index == index) {
                return obj.title;
            }
        }

        return "";
    }
}
