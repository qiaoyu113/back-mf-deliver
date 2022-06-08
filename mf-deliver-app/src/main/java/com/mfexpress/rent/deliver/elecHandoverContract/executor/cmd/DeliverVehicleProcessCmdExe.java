package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.cmd.DeliverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliverVehicleProcessCmdExe {

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    public void execute(DeliverVehicleProcessCmd cmd) {

        Result<List<String>> serveNoListResult = deliverVehicleAggregateRootApi.deliverVehicleProcess(cmd);

        List<String> serveNoList = ResultDataUtils.getInstance(serveNoListResult).getDataOrException();

        //同步
        Map<String, String> map = new HashMap<>();
        serveNoList.forEach(s -> {
            map.put("serve_no", s);
            serveSyncServiceI.execOne(map);
        });
    }

    public DeliverVehicleProcessCmd turnToCmd(DeliverDTO deliverDTO, ElecContractDTO elecContractDTO) {

        DeliverVehicleProcessCmd deliverVehicleProcessCmd = new DeliverVehicleProcessCmd();
        deliverVehicleProcessCmd.setCustomerId(deliverDTO.getCustomerId());
        elecContractDTO.setContractForeignNo(elecContractDTO.getContractShowNo());
        deliverVehicleProcessCmd.setContractDTO(elecContractDTO);

        return deliverVehicleProcessCmd;
    }
}
