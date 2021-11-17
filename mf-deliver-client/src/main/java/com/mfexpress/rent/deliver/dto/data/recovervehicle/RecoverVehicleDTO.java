package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import lombok.Data;

import java.util.Date;

@Data
public class RecoverVehicleDTO {
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

    private Integer status;

    private Date expectRecoverTime;
    private Integer cancelRemarkId;

    private String cancelRemark;
    private Integer wareHouseId;
    private Integer carId;

    private Double damageFee;

    private Double parkFee;

}
