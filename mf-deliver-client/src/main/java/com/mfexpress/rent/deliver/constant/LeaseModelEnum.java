package com.mfexpress.rent.deliver.constant;

public enum LeaseModelEnum {

    NORMAL(1, "正常租赁"),
    TRIAL(2, "试用"),
    SHOW(3, "展示"),
    DISCOUNT(4, "优惠");

    private int code;
    private String name;

    LeaseModelEnum(int code, String name) {
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
