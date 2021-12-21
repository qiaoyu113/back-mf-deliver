package com.mfexpress.rent.deliver.consumer.common;


import com.alibaba.fastjson.JSON;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleMqDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenEventTopic")
public class DeliverVehicleMqCommand {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    /*@Resource
    private RedisTools redisTools;*/

    @MFMqCommonProcessMethod(tag = Constants.DELIVER_VEHICLE_TAG)
    public void execute(String body) {

        List<DeliverVehicleMqDTO> deliverVehicleMqDTOList = JSON.parseArray(body, DeliverVehicleMqDTO.class);
        if (deliverVehicleMqDTOList == null || deliverVehicleMqDTOList.isEmpty()) {
            return;
        }
        //取消预选
        if (deliverVehicleMqDTOList.get(0).getSelectStatus() != null && deliverVehicleMqDTOList.get(0).getSelectStatus().equals(JudgeEnum.YES.getCode())) {
            Result<String> deliverResult = deliverAggregateRootApi.cancelSelected(deliverVehicleMqDTOList.get(0).getCarId());
            if (deliverResult.getCode() == 0) {
                Result<String> serveResult = serveAggregateRootApi.cancelSelected(deliverResult.getData());
            }
        }
        if (deliverVehicleMqDTOList.get(0).getInsuranceStatus() != null) {
            Result<String> syncInsureResult = deliverAggregateRootApi.syncInsureStatus(deliverVehicleMqDTOList);
        }

        if (deliverVehicleMqDTOList.get(0).getMileage() != null || deliverVehicleMqDTOList.get(0).getVehicleAge() != null || deliverVehicleMqDTOList.get(0).getCarNum() != null) {
            Result<String> syncMileageResult = deliverAggregateRootApi.syncVehicleAgeAndMileage(deliverVehicleMqDTOList);
        }


    }
}
