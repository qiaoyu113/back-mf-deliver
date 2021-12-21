package com.mfexpress.rent.deliver.constant;

public enum ElecContractOperationTypeEnum {

    CREATED(0, "交接单生成成功"),
    SEND_SMS(1, "短信发送成功"),
    AUTHENTICATED(2, "客户已认证"),
    SIGNED(3, "交接单签署成功"),
    DELIVERED(4, "发车成功"),
    RECOVERED(5, "收车成功");

    private final int code;
    private final String name;

    ElecContractOperationTypeEnum(int code, String name) {
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
