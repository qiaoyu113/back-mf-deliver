package com.mfexpress.rent.deliver.constant;

public enum ElecHandoverContractStatus {

    GENERATING(0, "生成中"),
    SIGNING(1, "已生成/签署中"),
    COMPLETED(2, "已完成"),
    FAIL(3, "失败");

    private int code;
    private String name;

    ElecHandoverContractStatus(int code, String name) {
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
