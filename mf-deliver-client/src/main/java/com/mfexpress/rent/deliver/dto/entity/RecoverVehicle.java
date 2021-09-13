package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recover_vehicle")
@Builder
public class RecoverVehicle {
    @Id
    private Integer id;

    private String deliverNo;

    private String recoverVehicleNo;

    private String serveNo;

    private String contactsName;

    private String contactsPhone;

    private String contactsCard;

    private Date recoverVehicleTime;

    private String imgUrl;

    private Date createTime;

    private Integer saleId;

    private Integer carServiceId;

    private Date updateTime;

    private Integer createId;

    private Integer updateId;

    private Date expectRecoverTime;

    private Integer status;

    private Integer cancelRemarkId;

    private String cancelRemark;

    private Integer wareHouseId;

    private Integer carId;
}