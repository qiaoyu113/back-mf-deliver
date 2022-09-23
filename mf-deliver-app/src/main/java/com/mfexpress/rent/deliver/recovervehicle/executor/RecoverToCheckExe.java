package com.mfexpress.rent.deliver.recovervehicle.executor;

import javax.annotation.Resource;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.constant.ContractStatusEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecoverToCheckExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    /**
     * 1. 是否为维修中
     * 2. 维修性质 故障维修不允许收车
     * 3. 是否存在替换车
     * 4. 替换车服务单是否发车
     *      1. 未发车 替换车申请是否取消
     *      2. 已发车 是否存在服务单调整单
     */
    public String execute(RecoverVechicleCmd recoverVechicleCmd) {

        // 判断是否可以验车
        RecoverCheckJudgeCmd cmd = new RecoverCheckJudgeCmd();
        cmd.setServeNo(recoverVechicleCmd.getServeNo());
        ResultValidUtils.checkResultException(serveAggregateRootApi.recoverCheckJudge(cmd));


        Result<Integer> countResult = contractAggregateRootApi.getRenewalContractCountByStatusAndServeNo(ContractStatusEnum.CREATED.getCode(), recoverVechicleCmd.getServeNo());
        Integer count = ResultDataUtils.getInstance(countResult).getDataOrException();
        if (null == count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "判断服务单是否已被合同续约失败");
        }
        if (0 != count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "该服务单已被合同预续约，不支持收车操作");
        }

        return null;
    }

}

