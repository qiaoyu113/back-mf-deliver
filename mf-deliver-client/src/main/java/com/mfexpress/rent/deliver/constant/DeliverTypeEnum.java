package com.mfexpress.rent.deliver.constant;

public enum DeliverTypeEnum {

    DELIVER(1, "发车"),
    RECOVER(2, "收车");

    private int code;
    private String name;

    DeliverTypeEnum(int code, String name) {
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
