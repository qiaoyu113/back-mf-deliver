package com.mfexpress.rent.deliver.constant;

public enum DeliverSortEnum {

    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4);

    private Integer sort;

    DeliverSortEnum(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
