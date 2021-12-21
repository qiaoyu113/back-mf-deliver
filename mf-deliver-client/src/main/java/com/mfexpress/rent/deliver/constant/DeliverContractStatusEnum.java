package com.mfexpress.rent.deliver.constant;

public enum DeliverContractStatusEnum {

    NOSIGN(0, "未签署"),
    SIGNING(1, "签署中"),
    COMPLETED(2, "已完成");

    private int code;
    private String name;

    DeliverContractStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

}
