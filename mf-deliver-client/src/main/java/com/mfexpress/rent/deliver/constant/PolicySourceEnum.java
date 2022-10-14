package com.mfexpress.rent.deliver.constant;

public enum PolicySourceEnum {

    H5(1,"h5端"),
    BACK_MARKET(2,"后市场端"),
    EXISTS(3,"已存在"),
    CUSTOMER(4,"客户");

    private Integer code;
    private String name;

    PolicySourceEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static PolicySourceEnum getStatusEnum(Integer code) {
        for (PolicySourceEnum modeEnum : PolicySourceEnum.values()) {
            if (modeEnum.getCode().equals(code)){
                return modeEnum;
            }
        }
        return null;
    }
}
