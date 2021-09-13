package com.mfexpress.rent.deliver.dto.data.delivervehicle;

import lombok.Data;

import java.util.Date;

@Data
public class DeliverVehicleDTO {
    private Integer id;

    private String deliverVehicleNo;

    private String deliverNo;

    private String serveNo;

    private String contactsName;

    private String contactsPhone;

    private String contactsCard;

    private Date deliverVehicleTime;

    private String imgUrl;

    private Date createTime;

    private Date updateTime;
}
