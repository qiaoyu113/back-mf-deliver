package com.mfexpress.rent.deliver.constant;

public enum InsuranceApplyStatusEnum {

    INIT(0,"未申请"),
    APPLYING(1,"申请中"),
    COMPLETED(2,"已完成"),
    REJECT(3,"驳回");

    private Integer code;
    private String name;

    InsuranceApplyStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static InsuranceApplyStatusEnum getStatusEnum(Integer code) {
        for (InsuranceApplyStatusEnum status : InsuranceApplyStatusEnum.values()) {
            if (status.getCode().equals(code)){
                return status;
            }
        }
        return null;
    }

}
