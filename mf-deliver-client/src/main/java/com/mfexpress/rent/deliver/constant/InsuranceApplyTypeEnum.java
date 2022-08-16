package com.mfexpress.rent.deliver.constant;

public enum InsuranceApplyTypeEnum {

    INSURE(1,"投保"),
    SURRENDER(2,"退保");

    private Integer code;
    private String name;

    InsuranceApplyTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static InsuranceApplyTypeEnum getStatusEnum(Integer code) {
        for (InsuranceApplyTypeEnum status : InsuranceApplyTypeEnum.values()) {
            if (status.getCode().equals(code)){
                return status;
            }
        }
        return null;
    }

}
