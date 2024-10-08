package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @deprecated 此类不再用于实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "recover_vehicle")
@Builder
public class RecoverVehicle {

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

    private Double damageFee;

    private Double parkFee;

}