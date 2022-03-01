package com.mfexpress.rent.deliver.constant;

public enum ServeRenewalTypeEnum {

    NOT( 0, "未续约"),
    ACTIVE(1, "主动续约"),
    PASSIVE(2, "被动/自动续约");

    private final Integer code;
    private final String value;

    ServeRenewalTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return this.code;
    }

}
