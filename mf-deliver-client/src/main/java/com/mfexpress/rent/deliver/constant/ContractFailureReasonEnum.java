package com.mfexpress.rent.deliver.constant;

public enum ContractFailureReasonEnum {

    CANCEL(1, "普通取消"),
    OVERDUE(2, "过期"),
    OTHER(3, "其他/服务错误"),
    ABNORMAL_RECOVER_CANCEL(4, "异常收车导致的取消");

    private int code;
    private String name;

    ContractFailureReasonEnum(int code, String name) {
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
