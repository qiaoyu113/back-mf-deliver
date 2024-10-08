package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverCancelCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class RecoverCancelExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;


    public String execute(RecoverCancelCmd recoverCancelCmd) {

        //交付单回退到已发车状态
        Result<String> deliverResult = deliverAggregateRootApi.cancelRecover(recoverCancelCmd.getServeNo());
        if (deliverResult.getCode() != 0) {
            return deliverResult.getMsg();
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverCancelCmd.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(recoverCancelCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);
        //收车单设为失效 填写取消收车原因
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        BeanUtils.copyProperties(recoverCancelCmd, recoverVehicleDTO);
        Result<String> recoverResult = recoverVehicleAggregateRootApi.cancelRecover(recoverVehicleDTO);

        Map<String, String> map = new HashMap<>();
        map.put("serve_no", recoverCancelCmd.getServeNo());
        syncServiceI.execOne(map);
        return "";
    }
}
