package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecoverApplyExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;
    @Resource
    private EsSyncHandlerI syncServiceI;


    public String execute(RecoverApplyListCmd recoverApplyListCmd) {
        List<RecoverApplyCmd> recoverApplyCmdList = recoverApplyListCmd.getRecoverApplyCmdList().stream().distinct().collect(Collectors.toList());
        List<String> serveNoList = new LinkedList<>();
        List<RecoverVehicleDTO> recoverVehicleDTOList = new LinkedList<>();
        for (RecoverApplyCmd recoverApplyCmd : recoverApplyCmdList) {
            serveNoList.add(recoverApplyCmd.getServeNo());
            RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
            Result<RecoverVehicleDTO> recoverVehicleResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(recoverApplyCmd.getDeliverNo());
            if (recoverVehicleResult.getData() != null) {
                continue;
            }
            recoverVehicleDTO.setServeNo(recoverApplyCmd.getServeNo());
            recoverVehicleDTO.setDeliverNo(recoverApplyCmd.getDeliverNo());
            recoverVehicleDTO.setCarId(recoverApplyCmd.getCarId());
            recoverVehicleDTO.setExpectRecoverTime(recoverApplyListCmd.getExpectRecoverTime());
            recoverVehicleDTO.setStatus(ValidStatusEnum.VALID.getCode());
            recoverVehicleDTOList.add(recoverVehicleDTO);
        }
        // 交付单状态更新收车中
        Result<String> deliverResult = deliverAggregateRootApi.applyRecover(serveNoList);
        if (deliverResult.getCode() != 0) {
            return deliverResult.getMsg();
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(serveNoList);
        deliverCarServiceDTO.setCarServiceId(recoverApplyListCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);
        //生成收车单
        Result<String> recoverResult = recoverVehicleAggregateRootApi.addRecoverVehicle(recoverVehicleDTOList);

        HashMap<String, String> map = new HashMap<>();
        for (String serveNo : serveNoList) {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        }
        return recoverResult.getData();

    }
}
