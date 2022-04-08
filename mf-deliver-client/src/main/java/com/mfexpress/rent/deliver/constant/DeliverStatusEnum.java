package com.mfexpress.rent.deliver.constant;

public enum DeliverStatusEnum {

    VALID(1, "有效"),
    INVALID(2, "无效"),
    HISTORICAL(3, "历史数据");

    private int code;
    private String name;

    DeliverStatusEnum(int code, String name) {
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
