package com.mfexpress.rent.deliver.sync;


import com.alibaba.fastjson.JSON;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.command.BaseCommand;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleMqDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DeliverVehicleMqCommand extends BaseCommand {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;


    @Override
    public void execute(String body) {

        DeliverVehicleMqDTO deliverVehicleMqDTO = JSON.parseObject(body, DeliverVehicleMqDTO.class);

        //取消预选
        if (deliverVehicleMqDTO.getSelectStatus() != null && deliverVehicleMqDTO.getSelectStatus().equals(JudgeEnum.YES.getCode())) {
            Result<String> deliverResult = deliverAggregateRootApi.cancelSelected(deliverVehicleMqDTO.getCarId());
            if (deliverResult.getCode() != 0) {
                Result<String> serveResult = serveAggregateRootApi.cancelSelected(deliverResult.getData());
            }
        }
        if (deliverVehicleMqDTO.getInsuranceStatus() != null) {
            Result<String> syncInsureResult = deliverAggregateRootApi.syncInsureStatus(deliverVehicleMqDTO.getCarId(), deliverVehicleMqDTO.getInsuranceStatus());
        }

        if (deliverVehicleMqDTO.getMileage() != null) {
            Result<String> syncMileageResult = deliverAggregateRootApi.syncVehicleMileage(deliverVehicleMqDTO.getCarId(), deliverVehicleMqDTO.getMileage());
        }

    }
}
