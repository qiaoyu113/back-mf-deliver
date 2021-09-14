package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServeAddCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;


    public String addServe(ServeAddCmd serveAddCmd) {
        ServeAddDTO serveAddDTO = new ServeAddDTO();
        serveAddDTO.setOrderId(serveAddCmd.getOrderId());
        serveAddDTO.setVehicleDTOList(serveAddCmd.getServeVehicleCmdList());

        Result<String> result = serveAggregateRootApi.addServe(serveAddDTO);
        return result.getData();


    }
}
