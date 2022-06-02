package com.mfexpress.rent.deliver.constant;

public enum ServeAdjustChargeRentTypeEnum {

    NORMAL(1, "正常租赁"),
    ON_PROBATION(2, "试用"),
    SHOW(3, "展示"),
    DISCOUNT(4, "优惠"),
    ;

    private int code;

    private String title;

    ServeAdjustChargeRentTypeEnum(int code, String title) {
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

        for (ServeAdjustChargeRentTypeEnum obj : ServeAdjustChargeRentTypeEnum.values()) {
            if (code == obj.getCode()) {
                return obj.getTitle();
            }
        }

        return "";
    }
}
