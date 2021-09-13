package com.mfexpress.rent.deliver.constant;


public enum JudgeEnum {

    YES(1,"是"),
    NO(0,"否");


    private Integer code;
    private String name;

    JudgeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public static JudgeEnum getJudgeEnum(Integer code) {
        for (JudgeEnum judgeEnum : JudgeEnum.values()) {
            if (judgeEnum.getCode().equals(code)) {
                return judgeEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
