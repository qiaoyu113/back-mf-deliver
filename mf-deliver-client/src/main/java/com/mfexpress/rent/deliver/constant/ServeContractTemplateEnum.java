package com.mfexpress.rent.deliver.constant;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ServeContractTemplateEnum {

    RENT("", 1, "长租"),
    DCP("DCP产品", 2, "DCP"),
    SXZ("随新租", 3, "随新租"),
    DXZL("定向租赁", 4, "定向租赁");


    private String templateName;
    private Integer businessType;
    private String desc;

    final static Map<String, ServeContractTemplateEnum> map = new HashMap<>();

    static {
        for (ServeContractTemplateEnum serveContractTemplateEnum : ServeContractTemplateEnum.values()) {
            map.put(serveContractTemplateEnum.getTemplateName(), serveContractTemplateEnum);
        }
    }


    public static ServeContractTemplateEnum getServeContractTemplate(String templateName) {
        return map.get(templateName);
    }

    public static ServeContractTemplateEnum getByBusinessType(Integer businessType) {
        for (ServeContractTemplateEnum modeEnum : ServeContractTemplateEnum.values()) {
            if (modeEnum.getBusinessType().equals(businessType)){
                return modeEnum;
            }
        }
        return null;
    }

}
