package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionCmd;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverDeductionExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;


    public String toDeduction(RecoverDeductionCmd recoverDeductionCmd) {
        DeliverDTO deliverDTO = new DeliverDTO();

        BeanUtils.copyProperties(recoverDeductionCmd, deliverDTO);
        Result<String> result = deliverAggregateRootApi.toDeduction(deliverDTO);
        if (result.getCode()==0){
             serveAggregateRootApi.completed(recoverDeductionCmd.getServeNo());
        }
        return "";
    }
}
