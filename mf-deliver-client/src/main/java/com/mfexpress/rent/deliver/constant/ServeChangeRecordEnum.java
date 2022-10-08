package com.mfexpress.rent.deliver.constant;

public enum ServeChangeRecordEnum {

    RENEWAL(1, "续约"),
    REACTIVE(2, "重新激活"),
    DEPOSIT_LOCK(3, "押金锁定"),
    DEPOSIT_UNLOCK(4, "押金解锁"),

    REPLACE_ADJUST(5, "替换车调整"),

    CANCEL(6, "服务单取消"),
    TERMINATION(7,"终止服务"),

    ;


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
