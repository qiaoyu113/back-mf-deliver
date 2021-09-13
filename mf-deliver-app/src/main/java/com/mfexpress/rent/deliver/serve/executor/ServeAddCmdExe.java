package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class ServeAddCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;


    public String addServe(ServeAddCmd serveAddCmd) {
        ServeAddDTO serveAddDTO = new ServeAddDTO();
        serveAddDTO.setOrderId(serveAddCmd.getOrderId());
        serveAddDTO.setVehicleDTOList(serveAddCmd.getServeVehicleCmdList());
        List<ServeDTO> serveDTOList = new LinkedList<>();
        List<ServeVehicleDTO> serveVehicleCmdList = serveAddCmd.getServeVehicleCmdList();
        for (ServeVehicleDTO serveVehicleDTO : serveVehicleCmdList) {
            Integer num = serveVehicleDTO.getNum();
            for (int i = 0; i < num; i++) {
                ServeDTO serveDTO = new ServeDTO();
                serveDTO.setOrderId(serveAddCmd.getOrderId());
                serveDTO.setCarModelId(serveVehicleDTO.getCarModelId());
                serveDTO.setLeaseModelId(serveVehicleDTO.getLeaseModelId());
                serveDTOList.add(serveDTO);
            }

        }
        Result<String> result = serveAggregateRootApi.addServe(serveAddDTO);
        return result.getData();


    }
}
