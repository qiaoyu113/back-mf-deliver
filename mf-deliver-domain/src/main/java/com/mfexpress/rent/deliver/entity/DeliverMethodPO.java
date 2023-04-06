package com.mfexpress.rent.deliver.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "deliver_method")
public class DeliverMethodPO {

    @Id
    private Integer id;

    private Integer deliverType;

    private String deliverNo;

    private String deliverRecoverVehicleNo;

    private Integer deliverMethod;

    private String handoverImgUrls;

    private Date createTime;

    private Integer creatorId;

    private Date updateTime;

    private Integer updaterId;

}
