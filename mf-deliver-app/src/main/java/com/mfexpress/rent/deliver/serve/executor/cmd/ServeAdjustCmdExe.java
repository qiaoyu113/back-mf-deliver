package com.mfexpress.rent.deliver.serve.executor.cmd;

import javax.annotation.Resource;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import org.springframework.stereotype.Component;

@Component
public class ServeAdjustCmdExe {

    @Resource
    private ServeAdjustCheckCmdExe serveAdjustCheckCmdExe;
    @Resource
    private ServeEntityApi serveEntityApi;

    public void execute(ServeAdjustCmd cmd, TokenInfo tokenInfo) {

        // 替换车服务单调整检查 包含权限校验 所以这里不需要
        ServeAdjustCheckCmd checkCmd = new ServeAdjustCheckCmd();
        checkCmd.setServeNo(cmd.getServeNo());
        ServeAdjustRecordVo vo = serveAdjustCheckCmdExe.execute(checkCmd, tokenInfo);

        initCmd(cmd, vo);
        cmd.setOperatorId(tokenInfo.getId());
        // 服务单调整业务逻辑
        serveEntityApi.serveAdjustment(cmd);
    }

    void initCmd(ServeAdjustCmd cmd, ServeAdjustRecordVo vo) {
        cmd.setChargeRentType(vo.getChargeLeaseModelId());
        cmd.setChargeRentAmount(vo.getChargeRentAmount());
        cmd.setChargeDepositAmount(vo.getChargeDepositAmount());
        cmd.setExpectRecoverTime(vo.getExpectRecoverTime());
    }
}
