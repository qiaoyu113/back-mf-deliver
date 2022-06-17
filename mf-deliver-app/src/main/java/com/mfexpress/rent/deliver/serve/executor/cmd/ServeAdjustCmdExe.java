package com.mfexpress.rent.deliver.serve.executor.cmd;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ServeAdjustCmdExe {

    @Resource
    private ServeAdjustCheckCmdExe serveAdjustCheckCmdExe;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private ServeDepositPayCmdExe serveDepositPayCmdExe;

    public void execute(ServeAdjustCmd cmd, TokenInfo tokenInfo) {

        // 替换车服务单调整检查 包含权限校验 所以这里不需要
        ServeAdjustCheckCmd checkCmd = new ServeAdjustCheckCmd();
        checkCmd.setServeNo(cmd.getServeNo());
        ServeAdjustRecordVo vo = serveAdjustCheckCmdExe.execute(checkCmd, tokenInfo);

        if (ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode() == cmd.getDepositPayType()
                && vo.getUnlockDepositAmount().compareTo(vo.getChargeDepositAmount().subtract(vo.getPaidInDepositAmount())) == -1) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "未锁定押金账本金额小于补缴押金差额，无法进行服务单调整");
        }

        initCmd(cmd, vo);
        cmd.setOperatorId(tokenInfo.getId());
        log.info("cmd---->{}", cmd);

        if (ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode() == cmd.getDepositPayType()) {
            // 账本扣除 原车扣除在违章处理完成后处理
            ServeDepositPayCmd serveDepositPayCmd = new ServeDepositPayCmd();
            serveDepositPayCmd.setDepositPayType(ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode());
            serveDepositPayCmd.setServeNo(cmd.getServeNo());
            serveDepositPayCmd.setPayAbleDepositAmount(vo.getChargeDepositAmount());
            serveDepositPayCmd.setPaidInDepositAmount(vo.getPaidInDepositAmount());
            serveDepositPayCmd.setOrderId(cmd.getOrderId());
            serveDepositPayCmd.setCustomerId(cmd.getCustomerId());
            serveDepositPayCmd.setOperatorId(cmd.getOperatorId());

            serveDepositPayCmdExe.execute(serveDepositPayCmd);
        }

        // 服务单调整业务逻辑
        ResultValidUtils.checkResultException(serveAggregateRootApi.serveAdjustment(cmd));
    }

    void initCmd(ServeAdjustCmd cmd, ServeAdjustRecordVo vo) {
        cmd.setChargeLeaseModelId(vo.getChargeLeaseModelId());
        cmd.setChargeRentAmount(vo.getChargeRentAmount());
        cmd.setChargeRentRatio(vo.getChargeRentRatio());
        cmd.setChargeDepositAmount(vo.getChargeDepositAmount());
        cmd.setExpectRecoverTime(vo.getExpectRecoverTime());
        cmd.setOrderId(vo.getOrderId());
        cmd.setCustomerId(vo.getCustomerId());
    }
}
