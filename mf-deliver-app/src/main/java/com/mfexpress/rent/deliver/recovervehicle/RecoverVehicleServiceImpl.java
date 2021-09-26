package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.recovervehicle.executor.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverVehicleServiceImpl implements RecoverVehicleServiceI {

    @Resource
    private RecoverQryContext recoverQryContext;
    @Resource
    private RecoverVehicleQryExe recoverVehicleQryExe;
    @Resource
    private RecoverApplyExe recoverApplyExe;
    @Resource
    private RecoverCancelExe recoverCancelExe;
    @Resource
    private RecoverToCheckExe recoverToCheckExe;
    @Resource
    private RecoverBackInsureExe recoverBackInsureExe;
    @Resource
    private RecoverDeductionExe recoverDeductionExe;


    @Override
    public List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo) {
        return recoverVehicleQryExe.execute(recoverApplyQryCmd,tokenInfo);
    }

    @Override
    public String applyRecover(RecoverApplyListCmd recoverApplyListCmd) {
        return recoverApplyExe.execute(recoverApplyListCmd);
    }

    @Override
    public String cancelRecover(RecoverCancelCmd recoverCancelCmd) {
        return recoverCancelExe.execute(recoverCancelCmd);
    }

    @Override
    public String toCheck(RecoverVechicleCmd recoverVechicleCmd) {
        return recoverToCheckExe.execute(recoverVechicleCmd);
    }

    @Override
    public String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd) {
        return recoverBackInsureExe.execute(recoverBackInsureCmd);
    }


    @Override
    public RecoverTaskListVO getRecoverListVO(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {

        return recoverQryContext.execute(recoverQryListCmd, tokenInfo);

    }

    @Override
    public String toDeduction(RecoverDeductionCmd recoverDeductionCmd) {

        return recoverDeductionExe.execute(recoverDeductionCmd);
    }
}
