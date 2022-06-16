package com.mfexpress.rent.deliver.serve.executor.cmd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.billing.customer.api.aggregate.AccountAggregateRootApi;
import com.mfexpress.billing.customer.api.aggregate.AdvincePaymentAggregateRootApi;
import com.mfexpress.billing.customer.api.aggregate.BookAggregateRootApi;
import com.mfexpress.billing.customer.constant.AccountBookTypeEnum;
import com.mfexpress.billing.customer.constant.AccountSourceTypeEnum;
import com.mfexpress.billing.customer.data.dto.advince.OrderPayAdvincepaymentCmd;
import com.mfexpress.billing.customer.data.dto.book.BookMoveBalanceDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockConfirmDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import org.springframework.stereotype.Component;

@Component
public class ServeDepositPayCmdExe {

    @Resource
    AdvincePaymentAggregateRootApi advincePaymentAggregateRootApi;

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    BookAggregateRootApi bookAggregateRootApi;

    @Resource
    AccountAggregateRootApi accountAggregateRootApi;

    public void execute(ServeDepositPayCmd cmd) {

        if (cmd.getPayAbleDepositAmount().compareTo(cmd.getPaidInDepositAmount()) <= 0) {
            // 应缴押金小于等于实缴押金 不做任何操作 直接返回
            return;
        }
        // 需缴押金金额
        BigDecimal payAmount = cmd.getPayAbleDepositAmount().subtract(cmd.getPaidInDepositAmount());

        Result<Integer> accountIdResult = accountAggregateRootApi.getAccountIdByCustomerId(cmd.getCustomerId(), AccountSourceTypeEnum.CUSTOMER.getCode());
        Integer accountId = ResultDataUtils.getInstance(accountIdResult).getDataOrException();

        BookMoveBalanceDTO.BookMoveBalanceDTOBuilder bookMoveBalanceDTOBuilder = BookMoveBalanceDTO.builder().accountId(accountId)
                .amount(payAmount)
                .userId(cmd.getOperatorId());

        if (ReplaceVehicleDepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getCode() == cmd.getDepositPayType()) {
            // 原车解锁押金
            List<String> serveNoList = new ArrayList<>();
            serveNoList.add(cmd.getSourceServeNo());

            Result<Boolean> unLockDepositResult = serveAggregateRootApi.unLockDeposit(serveNoList, cmd.getOperatorId());
            ResultValidUtils.checkResultException(unLockDepositResult);
            if (!unLockDepositResult.getData()) {
                // 不记录log了 查看入参排错吧
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "解除押金操作异常");
            }

            // 原车押金转移记录
            bookMoveBalanceDTOBuilder.targetType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode())
                    .sourceType(AccountBookTypeEnum.DEPOSIT.getCode()).build();

            Result<Boolean> lockDepositResult = bookAggregateRootApi.unLockDeposit(bookMoveBalanceDTOBuilder.build());
            ResultValidUtils.checkResultException(lockDepositResult);
            if (!lockDepositResult.getData()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "解除押金操作异常");
            }
        }

        List<CustomerDepositLockConfirmDTO> confirmDTOList = new ArrayList<>();
        CustomerDepositLockConfirmDTO dto = new CustomerDepositLockConfirmDTO();
        dto.setServeNo(cmd.getServeNo());
        dto.setLockAmount(payAmount);
        dto.setCreatorId(cmd.getOperatorId());
        confirmDTOList.add(dto);
        // 锁定押金替换单押金
        Result<Boolean> deliverLockDepositResult = serveAggregateRootApi.lockDeposit(confirmDTOList);
        ResultValidUtils.checkResultException(deliverLockDepositResult);
        if (!deliverLockDepositResult.getData()) {
            // 不记录log了 查看入参排错吧
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "锁定押金操作异常");
        }

        // 替换车押金转移记录
        bookMoveBalanceDTOBuilder.targetType(AccountBookTypeEnum.DEPOSIT.getCode())
                .sourceType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode()).build();

        Result<Boolean> lockDepositResult = bookAggregateRootApi.lockDeposit(bookMoveBalanceDTOBuilder.build());
        ResultValidUtils.checkResultException(lockDepositResult);
        if (!lockDepositResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "锁定押金操作异常");
        }

        // 押金支付
        OrderPayAdvincepaymentCmd orderPayAdvincepaymentCmd = new OrderPayAdvincepaymentCmd();
        orderPayAdvincepaymentCmd.setOrderId(cmd.getOrderId());
        orderPayAdvincepaymentCmd.setCustomerId(cmd.getCustomerId());
        orderPayAdvincepaymentCmd.setAdvinceAmount(BigDecimal.ZERO);
        orderPayAdvincepaymentCmd.setAllAmount(payAmount);
        orderPayAdvincepaymentCmd.setDepositAmount(payAmount);
        orderPayAdvincepaymentCmd.setUserId(cmd.getOperatorId());

        advincePaymentAggregateRootApi.orderPayFromBalance(orderPayAdvincepaymentCmd);
    }
}
