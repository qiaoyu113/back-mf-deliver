package com.mfexpress.rent.deliver.serve.executor.cmd;

import com.mfexpress.billing.customer.api.aggregate.AccountAggregateRootApi;
import com.mfexpress.billing.customer.api.aggregate.AdvincePaymentAggregateRootApi;
import com.mfexpress.billing.customer.api.aggregate.BookAggregateRootApi;
import com.mfexpress.billing.customer.constant.AccountBookTypeEnum;
import com.mfexpress.billing.customer.constant.AccountOperTypeEnum;
import com.mfexpress.billing.customer.constant.AccountSourceTypeEnum;
import com.mfexpress.billing.customer.data.dto.advince.OrderPayPaymentDTO;
import com.mfexpress.billing.customer.data.dto.advince.PayInfoDTO;
import com.mfexpress.billing.customer.data.dto.book.BookMoveBalanceDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.DepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockConfirmDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServePaidInDepositUpdateCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        if (DepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getCode() == cmd.getDepositPayType()) {
            BookMoveBalanceDTO.BookMoveBalanceDTOBuilder bookMoveBalanceDTOBuilder = BookMoveBalanceDTO.builder().accountId(accountId)
                    .userId(cmd.getOperatorId());
            // 原车解锁押金
            List<String> serveNoList = new ArrayList<>();
            serveNoList.add(cmd.getSourceServeNo());

            // 在服务单解锁押金前查询
            ServeDTO sourceServe = ResultDataUtils.getInstance(serveAggregateRootApi.getServeDtoByServeNo(cmd.getSourceServeNo())).getDataOrException();

            Result<Boolean> unLockDepositResult = serveAggregateRootApi.unLockDeposit(serveNoList, cmd.getOperatorId(),false);
            ResultValidUtils.checkResultException(unLockDepositResult);
            if (!unLockDepositResult.getData()) {
                // 不记录log了 查看入参排错吧
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "解除押金操作异常");
            }

            // 原车押金转移记录
            bookMoveBalanceDTOBuilder.targetType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode())
                    .amount(sourceServe.getPaidInDeposit())
                    .sourceType(AccountBookTypeEnum.DEPOSIT.getCode()).build();

            Result<Boolean> lockDepositResult = bookAggregateRootApi.unLockDeposit(bookMoveBalanceDTOBuilder.build());
            ResultValidUtils.checkResultException(lockDepositResult);
            if (!lockDepositResult.getData()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "解除押金操作异常");
            }

            // 替换车押金锁定
            List<CustomerDepositLockConfirmDTO> confirmDTOList = new ArrayList<>();
            CustomerDepositLockConfirmDTO dto = new CustomerDepositLockConfirmDTO();
            dto.setServeNo(cmd.getServeNo());
            dto.setLockAmount(payAmount);
            dto.setCreatorId(cmd.getOperatorId());
            confirmDTOList.add(dto);
            // 锁定替换单押金
            Result<Boolean> deliverLockDepositResult = serveAggregateRootApi.lockDeposit(confirmDTOList);
            ResultValidUtils.checkResultException(deliverLockDepositResult);
            if (!deliverLockDepositResult.getData()) {
                // 不记录log了 查看入参排错吧
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "锁定押金操作异常");
            }

            // 替换车押金转移记录
            bookMoveBalanceDTOBuilder.targetType(AccountBookTypeEnum.DEPOSIT.getCode())
                    .amount(payAmount)
                    .sourceType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode()).build();

            lockDepositResult = bookAggregateRootApi.lockDeposit(bookMoveBalanceDTOBuilder.build());
            ResultValidUtils.checkResultException(lockDepositResult);
            if (!lockDepositResult.getData()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "锁定押金操作异常");
            }
        } else if (DepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode() == cmd.getDepositPayType()) {
            // 使用账本支付时 进行押金支付
            // 押金支付
            OrderPayPaymentDTO orderPayPaymentDTO = new OrderPayPaymentDTO();
            orderPayPaymentDTO.setOrderId(cmd.getOrderId());
            orderPayPaymentDTO.setCustomerId(cmd.getCustomerId());
            PayInfoDTO payInfoDTO = new PayInfoDTO();
            payInfoDTO.setAmount(payAmount);
            payInfoDTO.setBookType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode());
            payInfoDTO.setPayType("deposit");
            orderPayPaymentDTO.setPayTypeId(AccountOperTypeEnum.PAY_DEPOSIT.getCode());
            orderPayPaymentDTO.setPayInfoDTOList(Arrays.asList(payInfoDTO));
            orderPayPaymentDTO.setUserId(cmd.getUserId());
            advincePaymentAggregateRootApi.orderPay(orderPayPaymentDTO);

            // 修改替换车实缴金额

            ServePaidInDepositUpdateCmd servePaidInDepositUpdateCmd = ServePaidInDepositUpdateCmd.builder()
                    .serveNo(cmd.getServeNo()).chargeDepositAmount(payAmount).build();
            serveAggregateRootApi.updateServePaidInDeposit(servePaidInDepositUpdateCmd);
        }
    }
}
