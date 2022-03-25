package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @deprecated 此类不再作为实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "deliver_vehicle")
public class DeliverVehicle {

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