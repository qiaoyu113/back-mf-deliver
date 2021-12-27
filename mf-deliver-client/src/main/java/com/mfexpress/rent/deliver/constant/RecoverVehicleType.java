package com.mfexpress.rent.deliver.constant;

public enum RecoverVehicleType {

    NORMAL(0, "正常收车"),
    ABNORMAL(1, "异常收车");

    private int code;
    private String name;

    RecoverVehicleType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static String getEnumValue(int code) {
        for (RecoverVehicleType typeEnum : RecoverVehicleType.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum.getName();
            }
        }
        return "";
    }

}
