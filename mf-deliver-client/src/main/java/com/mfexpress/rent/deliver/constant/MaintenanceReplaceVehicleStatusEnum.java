package com.mfexpress.rent.deliver.constant;

import java.util.Arrays;

/**
 * <p></p>
 * <description><description>
 *
 * @author wanghailong
 * 2022/9/22
 */
public enum MaintenanceReplaceVehicleStatusEnum {

    TACK_EFFECT(1, "生效中"),
    NOT_EFFECT(2, "已取消")
    ;

    private int code;

    private String title;

    MaintenanceReplaceVehicleStatusEnum(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static String getTitle(int code) {

        return Arrays.asList(MaintenanceReplaceVehicleStatusEnum.values())
                .stream().filter(obj -> code == obj.getCode())
                .findFirst().map(MaintenanceReplaceVehicleStatusEnum::getTitle).orElse("");
    }
}
