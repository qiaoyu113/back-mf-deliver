package com.mfexpress.rent.deliver.entity;

import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;
import com.mfexpress.rent.deliver.entity.api.DailyEntityApi;
import com.mfexpress.rent.deliver.gateway.DailyGateway;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Table;

@Table(name = "daily")
@Data
@Component
public class Daily implements DailyEntityApi {

    @Resource
    private DailyGateway dailyGateway;


    @Override
    public void operateMaintain(DailyMaintainDTO dailyMaintainDTO) {
        //发起维修
        if (dailyMaintainDTO.getMaintainFlag()) {
            dailyGateway.updateDailyRepairFlagByServeNoAndGteRentDate(dailyMaintainDTO.getServeNo(), dailyMaintainDTO.getMaintainDate(), 1);
        } else {
            //结束维修
            dailyGateway.updateDailyRepairFlagByServeNoAndGteRentDate(dailyMaintainDTO.getServeNo(), dailyMaintainDTO.getMaintainDate(), 0);
        }
    }

    private String serveNo;
    private Integer vehicleId;

    private Integer leaseModelId;
    private Integer customerId;

    private Integer orgId;
    private String rentDate;

    private Integer chargeFlag;

    private Integer delFlag;

    private String carNum;
    private Integer replaceFlag;
    private Integer repairFlag;
}
