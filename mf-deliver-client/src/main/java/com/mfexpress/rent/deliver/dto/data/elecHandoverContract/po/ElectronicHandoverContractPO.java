package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "electronic_handover_contract")
public class ElectronicHandoverContractPO {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    private Integer id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "deliver_nos")
    private String deliverNos;

    @Column(name = "deliver_type")
    private Integer deliverType;

    @Column(name = "contract_foreign_no")
    private String contractForeignNo;

    @Column(name = "contacts_name")
    private String contactsName;

    @Column(name = "contacts_phone")
    private String contactsPhone;

    @Column(name = "contacts_card")
    private String contactsCard;

    @Column(name = "deliver_vehicle_time")
    private String deliverVehicleTime;

    @Column(name = "recover_vehicle_time")
    private String recoverVehicleTime;

    @Column(name = "plate_number_with_imgs")
    private String plateNumberWithImgs;

    @Column(name = "recover_damage_fee")
    private Double recoverDamageFee;

    @Column(name = "recover_park_fee")
    private Double recoverParkFee;

    @Column(name = "recover_ware_house_id")
    private Integer recoverWareHouseId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "failure_reason")
    private Integer failureReason;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "city_id")
    private Integer cityId;

    @Column(name = "is_show")
    private Integer isShow;

    @Column(name = "send_sms_count")
    private Integer sendSmsCount;

    @Column(name = "send_sms_date")
    private String sendSmsDate;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "updater_id")
    private Integer updaterId;

}
