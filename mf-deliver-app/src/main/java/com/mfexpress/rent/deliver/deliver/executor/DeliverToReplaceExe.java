package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DeliverToReplaceExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;


    public String toReplace(DeliverReplaceCmd deliverReplaceCmd) {
        DeliverVehicleSelectCmd deliverVehicleSelectCmd = deliverReplaceCmd.getDeliverVehicleSelectCmd();


        serveAggregateRootApi.toReplace(deliverReplaceCmd.getServeNo());

        Result<DeliverDTO> deliverDtoResult = deliverAggregateRootApi.getDeliverByServeNo(deliverReplaceCmd.getServeNo());
        if (deliverDtoResult.getData() != null) {
            DeliverDTO deliverDTO = deliverDtoResult.getData();
            //todo 原车辆更新预选状态为未预选
            deliverDTO.getCarId();
        }

        //todo 查询车辆投保状态
        //更换车辆信息 原交付单失效
        DeliverDTO deliverDTO = new DeliverDTO();
        deliverDTO.setServeNo(deliverReplaceCmd.getServeNo());
        deliverDTO.setCarNum(deliverVehicleSelectCmd.getCarNum());
        deliverDTO.setCarId(deliverVehicleSelectCmd.getCarId());
        deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
        deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
        Result<String> result = deliverAggregateRootApi.toReplace(deliverDTO);
        return result.getData();

    }


}
