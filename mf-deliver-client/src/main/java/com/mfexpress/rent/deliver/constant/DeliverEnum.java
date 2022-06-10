package com.mfexpress.rent.deliver.constant;

public enum DeliverEnum {
    IS_DELIVER(1, "发车中"),
    DELIVER(2, "已发车"),
    IS_RECOVER(3, "收车中"),
    RECOVER(4, "已收车"),
    COMPLETED(5, "已完成"),
    CANCEL(6, "作废");


    private Integer code;
    private String status;

    DeliverEnum(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public static DeliverEnum getDeliveryEnum(Integer code, String status) {
        for (DeliverEnum deliveryEnum : DeliverEnum.values()) {
            if (deliveryEnum.getCode().equals(code)) {
                return deliveryEnum;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
