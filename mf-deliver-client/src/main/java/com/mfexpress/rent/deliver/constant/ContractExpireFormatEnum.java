package com.mfexpress.rent.deliver.constant;

import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.CheckForNull;
import java.util.HashMap;

/**
 * @author yj
 * @Depreacated 废弃, 使用系统配置{@link DeliverProjectProperties.ContractExpireNotify} DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY.getFormatRules()
 * @date 2022/11/3 14:38
 */
@Getter
@AllArgsConstructor
@Deprecated
public enum ContractExpireFormatEnum {

    LOOP_TEMPLATE("loopTemplate", "#LOOP#", DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY.getCommonNoticeTemplate().getLoopTemplate(),
            true, "", "",
            DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY.getCommonNoticeTemplate().getLoopTemplateSeparator(), false, 1),
    CUSTOMER_NAME("customerName", "#客户名称#", "\"customerName\"",
            false, "", "", "",
            true, 2),
    LICENSE_PLATE("licensePlateList", "#车牌号列表#", "\"licensePlateList\"",
            true, "\"item\"", "item", "、",
            true, 2),
    VEHICLE_SIZE("carNumber", "#车数量#", "carNumber",
            false, "", "", "",
            true, 2),

    ;

    /**
     * 将sourceFieldName属性替换 targetString 变为 format
     */
    public String sourceFieldName;
    /**
     * 替换的模板字符串
     */
    public String targetString;
    /**
     * 替换为的字符串
     */
    public String format;
    /**
     * 是否为列表类型
     */
    public boolean isList;
    /**
     * 列表类型属性  元素替换的目标字符串
     */
    public String itemFormat;
    /**
     * 元素名
     */
    public String itemName;
    /**
     * 元素分隔符
     */
    public String separator;
    /**
     * 是否需要替换
     */
    public boolean autoReplace;
    /**
     * 替换顺序  数字越小,优先级越大
     */
    public Integer replaceSort;

    private static final HashMap<String, ContractExpireFormatEnum> enumMap = new HashMap<>();

    static {
        for (ContractExpireFormatEnum value : values()) {
            enumMap.put(value.sourceFieldName, value);
        }
    }

    @CheckForNull
    public static ContractExpireFormatEnum getByFieldName(String sourceFieldName) {
        return enumMap.get(sourceFieldName);
    }


}
