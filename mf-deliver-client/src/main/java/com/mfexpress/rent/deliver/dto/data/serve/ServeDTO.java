package com.mfexpress.rent.deliver.dto.data.serve;

import lombok.Data;

import java.util.Date;

@Data
public class ServeDTO {
    private Integer id;

    private Integer orderId;

    private String serveNo;

    private Integer carModelId;

    private Integer leaseModelId;

    private Integer brandId;

    private Integer status;

    private Integer carServiceId;

    private Integer saleId;

    private String remark;

    private Integer createId;

    private Integer updateId;

    private Date createTime;

    private Date updateTime;
}
