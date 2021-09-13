package com.mfexpress.rent.deliver.constant;

import com.alibaba.fastjson.JSONObject;

public enum ValidStatusEnum {
    VALID(1, "有效"),
    INVALID(2, "无效"),
    INIT(0, "无");

    private Integer code;
    private String name;

    ValidStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static ValidStatusEnum getValidStatusEnum(Integer code) {
        for (ValidStatusEnum validStatusEnum : ValidStatusEnum.values()) {
            if (validStatusEnum.getCode().equals(code)) {
                return validStatusEnum;
            }
        }
        return null;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(code.toString(),name);
        return jsonObject.toJSONString();
    }
}
