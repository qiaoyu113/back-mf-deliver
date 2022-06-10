package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecoverVehicleProcessCmdExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    public void execute(RecoverVehicleProcessCmd cmd) {

        Result<List<String>> serveNoListResult = recoverVehicleAggregateRootApi.recoverVehicleProcess(cmd);

        List<String> serveNoList = ResultDataUtils.getInstance(serveNoListResult).getDataOrException();

        //同步
        Map<String, String> map = new HashMap<>();
        serveNoList.forEach(serveNo -> {
            map.put("serve_no", serveNo);
            serveSyncServiceI.execOne(map);
        });
    }

    public RecoverVehicleProcessCmd turnToCmd(ElecContractDTO contractDTO, DeliverDTO deliverDTO, ServeDTO serveDTO) {

        RecoverVehicleProcessCmd cmd = new RecoverVehicleProcessCmd();
        cmd.setContractForeignNo(contractDTO.getContractShowNo());
        cmd.setRecoverVehicleTime(contractDTO.getRecoverVehicleTime());
        cmd.setCarId(deliverDTO.getCarId());
        cmd.setServeNo(serveDTO.getServeNo());
        cmd.setDeliverNo(deliverDTO.getDeliverNo());
        cmd.setCustomerId(serveDTO.getCustomerId());
        cmd.setExpectRecoverDate(serveDTO.getExpectRecoverDate());
        cmd.setRecoverWareHouseId(contractDTO.getRecoverWareHouseId());
        cmd.setContactId(contractDTO.getContractId());
        cmd.setServeStatus(serveDTO.getStatus());
        cmd.setOperatorId(contractDTO.getCreatorId());

        return cmd;
    }
}
