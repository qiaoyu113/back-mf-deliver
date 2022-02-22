package com.mfexpress.rent.deliver.constant;

public enum ElecContractCreateStatusEnum {

    CREATING(1, "创建中"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败");

    private int code;
    private String name;

    ElecContractCreateStatusEnum(int code, String name) {
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
