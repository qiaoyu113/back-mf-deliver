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


    public String execute(RecoverDeductionCmd recoverDeductionCmd) {
        DeliverDTO deliverDTO = new DeliverDTO();

        BeanUtils.copyProperties(recoverDeductionCmd, deliverDTO);
        Result<String> result = deliverAggregateRootApi.toDeduction(deliverDTO);

        if (result.getCode() != 0) {
            return result.getMsg();

        }
        //返回已完成服务单编号 更新服务单已完成状态
        if (result.getData().equals(deliverDTO.getServeNo())) {
            return serveAggregateRootApi.completed(recoverDeductionCmd.getServeNo()).getData();
        }
        return "";
    }
}
