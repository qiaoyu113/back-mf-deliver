package com.mfexpress.rent.deliver.constant;

public enum ServeEnum {
    NOT_PRESELECTED(0, "未预选", "待预选"),
    PRESELECTED(1, "已预选", "已预选"),
    DELIVER(2, "已发车", "租赁中"),
    RECOVER(3, "已收车", "已收车"),
    COMPLETED(4, "已完成", "已完成"),
    REPAIR(5, "维修中", "维修中"),
    CANCEL(6, "已作废", "已作废");


    private Integer code;

    private String status;

    private String alias;

    private ServeEnum(Integer code, String status, String alias) {
        this.code = code;
        this.status = status;
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static ServeEnum getServeEnum(Integer code) {
        for (ServeEnum serveEnum : ServeEnum.values()) {
            if (serveEnum.getCode().equals(code)) {
                return serveEnum;
            }
        }
        return null;
    }
}
