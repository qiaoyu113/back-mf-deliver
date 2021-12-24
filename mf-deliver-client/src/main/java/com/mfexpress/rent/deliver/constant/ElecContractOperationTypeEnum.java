package com.mfexpress.rent.deliver.constant;

public enum ElecContractOperationTypeEnum {

    CREATED(0, "交接单生成成功"),
    SEND_SMS(1, "短信发送成功"),
    RESEND_SMS(2, "短信重新发送成功"),
    AUTHENTICATED(3, "客户已认证"),
    OVERDUE(4, "交接单已过期"),
    SIGNED(5, "交接单签署成功"),
    DELIVERED(6, "发车成功"),
    RECOVERED(7, "收车成功"),
    CREATING(8, "交接单生成中"),
    FAIL(9, "交接单生成失败");

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
