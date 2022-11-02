package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.billing.customer.api.aggregate.BookAggregateRootApi;
import com.mfexpress.billing.customer.constant.AccountBookTypeEnum;
import com.mfexpress.billing.customer.constant.BusinessTypeEnum;
import com.mfexpress.billing.customer.data.dto.book.BookMoveBalanceDTO;
import com.mfexpress.billing.customer.data.dto.book.CustomerBookDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.TerminationServiceCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author hzq
 * @Package com.mfexpress.rent.deliver.serve.executor
 * @date 2022/10/8 13:24
 * @Copyright ©
 */
@Component
@Slf4j
public class TerminationServeExecute {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private BookAggregateRootApi bookAggregateRootApi;

    public Boolean execute(TerminationServiceCmd terminationServiceCmd, TokenInfo tokenInfo){

        Result<ServeDTO> serveDtoResult = serveAggregateRootApi.getServeDtoByServeNo(terminationServiceCmd.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDtoResult).getDataOrException();

        Result<List<CustomerBookDTO>> bookListResult = bookAggregateRootApi.getBookListByCustomerId(serveDTO.getCustomerId());
        ResultValidUtils.checkResultException(bookListResult);
        CustomerBookDTO bookDTO = bookListResult.getData().get(0);
        if (Objects.isNull(bookDTO.getAccountId()) || bookDTO.getAccountId() == 0) {
            log.error("客户账户异常 客户Id:{}", serveDTO.getCustomerId());
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "客户账户异常");
        }

        Map<Integer, CustomerBookDTO> customerBookDTOMap = bookListResult.getData().stream().collect(Collectors.toMap(CustomerBookDTO::getBookType, a -> a));

        Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderDTOByOrderId(serveDTO.getOrderId().toString());
        OrderDTO orderDTO = ResultDataUtils.getInstance(orderDTOResult).getDataOrException();

        CustomerBookDTO customerBookDTO = customerBookDTOMap.get(AccountBookTypeEnum.LOCK_ADVANCE.getCode());
        if (Objects.isNull(customerBookDTO)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到锁定预付款账本");
        }
        if (customerBookDTO.getBalance().compareTo(new BigDecimal(orderDTO.getDownPayment())) < 0) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "锁定预付款账本余额不足");
        }


        //解锁服务单押金
        Result<Boolean> unLockDepositResult = serveAggregateRootApi.unLockDeposit(Arrays.asList(terminationServiceCmd.getServeNo()), tokenInfo.getId(), true);
        ResultDataUtils.getInstance(unLockDepositResult).getDataOrException();

        // 原车押金转移记录
        BookMoveBalanceDTO unLockDeposit = BookMoveBalanceDTO.builder()
                .accountId(bookListResult.getData().get(0).getAccountId())
                .userId(tokenInfo.getId())
                .targetType(AccountBookTypeEnum.DEPOSIT_BALANCE.getCode())
                .amount(serveDTO.getPaidInDeposit())
                .sourceType(AccountBookTypeEnum.DEPOSIT.getCode()).build();

        Result<Boolean> lockDepositResult = bookAggregateRootApi.unLockDeposit(unLockDeposit);
        ResultValidUtils.checkResultException(lockDepositResult);
        if (!lockDepositResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "解除押金操作异常");
        }

        //更改服务单状态
        ServeDTO newServeDto = new ServeDTO();
        newServeDto.setServeNo(terminationServiceCmd.getServeNo());
        Result<Boolean> terminationServeResult = serveAggregateRootApi.terminationServe(newServeDto);
        ResultDataUtils.getInstance(terminationServeResult).getDataOrException();

        //转移预付款金额
        //预付款->租金
        BookMoveBalanceDTO bookMoveBalanceDTO = BookMoveBalanceDTO.builder()
                .accountId(bookListResult.getData().get(0).getAccountId())
                .amount(new BigDecimal(orderDTO.getDownPayment()))
                .oaNo("")
                .sourceType(AccountBookTypeEnum.LOCK_ADVANCE.getCode())
                .targetAccountId(bookListResult.getData().get(0).getAccountId())
                .targetType(AccountBookTypeEnum.RENT_BALANCE.getCode())
                .advancePayment(true)
                .operType(BusinessTypeEnum.TERMINATION_OF_SERVICE.getCode())
                .userId(tokenInfo.getId()).build();
        Result<Long> moveBalanceResult = bookAggregateRootApi.moveBalance(bookMoveBalanceDTO);
        ResultDataUtils.getInstance(moveBalanceResult).getDataOrException();

        return Boolean.TRUE;

    }

}
