package com.mfexpress.rent.deliver.constant;

public enum ServeChangeRecordEnum {

    RENEWAL(1, "续约"),
    REACTIVE(2, "重新激活");

    private int code;
    private String name;

    ServeChangeRecordEnum(int code, String name) {
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
