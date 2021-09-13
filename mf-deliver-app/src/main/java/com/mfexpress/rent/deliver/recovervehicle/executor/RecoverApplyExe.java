package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class RecoverApplyExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;


    public String applyRecover(RecoverApplyListCmd recoverApplyListCmd) {
        List<RecoverApplyCmd> recoverApplyCmdList = recoverApplyListCmd.getRecoverApplyCmdList();

        List<String> serveNoList = new LinkedList<>();

        List<RecoverVehicleDTO> recoverVehicleDTOList = new LinkedList<>();
        for (RecoverApplyCmd recoverApplyCmd : recoverApplyCmdList) {

            serveNoList.add(recoverApplyCmd.getServeNo());

            RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
            recoverVehicleDTO.setServeNo(recoverApplyCmd.getServeNo());
            recoverVehicleDTO.setDeliverNo(recoverApplyCmd.getDeliverNo());

            recoverVehicleDTO.setExpectRecoverTime(recoverApplyListCmd.getExpectRecoverTime());
            recoverVehicleDTO.setStatus(ValidStatusEnum.VALID.getCode());
            recoverVehicleDTOList.add(recoverVehicleDTO);
        }
        //todo 交付单状态更新收车中
        deliverAggregateRootApi.applyRecover(serveNoList);

        //todo 生成收车单
        recoverVehicleAggregateRootApi.addRecoverVehicle(recoverVehicleDTOList);

        return "";

    }
}
