package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Component
public class RecoverToCheckExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    public String execute(RecoverVechicleCmd recoverVechicleCmd) {
        //完善收车单信息
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        BeanUtils.copyProperties(recoverVechicleCmd, recoverVehicleDTO);

        Result<String> recoverResult = recoverVehicleAggregateRootApi.toCheck(recoverVehicleDTO);
        if (recoverResult.getCode() != 0) {
            return recoverResult.getMsg();
        }
        //更新交付单状态未 已验车
        Result<String> deliverResult = deliverAggregateRootApi.toCheck(recoverVechicleCmd.getServeNo());
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setCarServiceId(recoverVechicleCmd.getCarServiceId());
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverVechicleCmd.getServeNo()));
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);
        return deliverResult.getData();
    }
}

