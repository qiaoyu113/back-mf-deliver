package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deliver_vehicle")
public class DeliverVehicle {
    @Id
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