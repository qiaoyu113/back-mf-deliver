package com.mfexpress.rent.deliver.constant;

public enum DeliverSortEnum {

    ZERO(0, "默认的，无含义"),
    ONE(1, "服务单维度-发车-待发车"),
    TWO(2, "服务单维度-发车-签署中"),
    THREE(3, "服务单维度-发车-待预选"),
    FOUR(4, "服务单维度-发车-待验车"),
    FIVE(5, "服务单维度-发车-待投保"),
    SIX(6, "服务单维度-发车-已完成"),
    SEVEN(7, "预留"),
    EIGHT(8, "预留"),
    NINE(9, "预留"),
    TEN(10, "服务单维度-收车-待验车"),
    ELEVEN(11, "服务单维度-收车-待收车"),
    TWELVE(12, "服务单维度-收车-签署中"),
    THIRTEEN(13, "服务单维度-收车-待退保"),
    FOURTEEN(14, "服务单维度-收车-待处理违章"),
    FIFTEEN(15, "服务单维度-收车-交付单已完成"),
    SIXTEEN(16, "服务单维度-服务单已完成"),
    TWENTY(20, "预留"),
    TWENTY_ONE(21, "预留"),
    TWENTY_TWO(22, "预留"),
    TWENTY_THREE(23, "交付单维度-收车-待收车"),
    TWENTY_FOUR(24, "交付单维度-收车-待退保"),
    TWENTY_FIVE(25, "交付单维度-收车-待处理事项"),
    TWENTY_SIX(26, "交付单维度-已完成");

    private Integer sort;

    private String mean;

    DeliverSortEnum(Integer sort, String mean) {
        this.sort = sort;
        this.mean = mean;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
