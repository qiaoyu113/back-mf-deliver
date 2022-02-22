package com.mfexpress.rent.deliver.constant;

public enum ContractFailureReasonEnum {

    CANCEL(1, "普通取消"),
    OVERDUE(2, "过期"),
    OTHER(3, "其他/服务错误"),
    ABNORMAL_RECOVER_CANCEL(4, "异常收车导致的取消"),
    CREATE_TIMEOUT(5, "前端主动调用的创建超时导致的取消"),
    CREATE_FAIL(6, "契约锁端返回的合同创建失败，只会出现在合同创建中的下一步");

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
