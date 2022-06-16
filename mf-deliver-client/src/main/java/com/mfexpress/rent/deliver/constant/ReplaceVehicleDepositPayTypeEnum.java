package com.mfexpress.rent.deliver.constant;

public enum ReplaceVehicleDepositPayTypeEnum {

    ACCOUNT_DEPOSIT_UNLOCK_PAY(1, "未锁定押金账本余额"),
    SOURCE_DEPOSIT_PAY(2, "使用车辆押金进行支付");

    private int code;

    private String title;

    ReplaceVehicleDepositPayTypeEnum(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public static String getTitle(int code) {

        for (ReplaceVehicleDepositPayTypeEnum obj : ReplaceVehicleDepositPayTypeEnum.values()) {
            if (code == obj.getCode()) {
                return obj.getTitle();
            }
        }

        return "";
    }
}
