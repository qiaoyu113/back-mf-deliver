package com.mfexpress.rent.deliver.serve.executor.cmd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.billing.customer.api.aggregate.AdvincePaymentAggregateRootApi;
import com.mfexpress.billing.customer.data.dto.advince.OrderPayAdvincepaymentCmd;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockConfirmDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import org.springframework.stereotype.Component;

@Component
public class ServeDepositPayCmdExe {

    @Resource
    AdvincePaymentAggregateRootApi advincePaymentAggregateRootApi;

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    public void execute(ServeDTO serveDTO, Integer operatorId) {

        OrderPayAdvincepaymentCmd cmd = new OrderPayAdvincepaymentCmd();
        cmd.setOrderId(serveDTO.getOrderId());
        cmd.setCustomerId(serveDTO.getCustomerId());
        cmd.setAdvinceAmount(BigDecimal.ZERO);
        cmd.setAllAmount(serveDTO.getDeposit());
        cmd.setDepositAmount(serveDTO.getDeposit());
        cmd.setUserId(operatorId);

        advincePaymentAggregateRootApi.orderPayFromBalance(cmd);

        // 锁定押金
        List<CustomerDepositLockConfirmDTO> confirmDTOList = new ArrayList<>();
        CustomerDepositLockConfirmDTO dto = new CustomerDepositLockConfirmDTO();
        dto.setServeNo(serveDTO.getServeNo());
        dto.setLockAmount(cmd.getDepositAmount());
        dto.setCreatorId(cmd.getUserId());
        confirmDTOList.add(dto);

        serveAggregateRootApi.lockDeposit(confirmDTOList);
    }
}
