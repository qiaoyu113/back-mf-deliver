package com.mfexpress.rent.deliver.constant;

public enum DeliverMethodEnum {

    ONLINE_CONTACT(1,"线上契约锁"),
    OFFLINE_NORMAL(2,"线下正常"),
    OFFLINE_ABNORMAL(3,"线下异常");

    private Integer code;
    private String name;

    DeliverMethodEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

}
