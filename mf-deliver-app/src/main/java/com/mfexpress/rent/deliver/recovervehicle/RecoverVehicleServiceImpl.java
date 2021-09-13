package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.recovervehicle.executor.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverVehicleServiceImpl implements RecoverVehicleServiceI {

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
    public List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd) {
        return recoverVehicleQryExe.getRecoverVehicleListVO(recoverApplyQryCmd);
    }

    @Override
    public String applyRecover(RecoverApplyListCmd recoverApplyListCmd) {
        return recoverApplyExe.applyRecover(recoverApplyListCmd);
    }

    @Override
    public String cancelRecover(RecoverCancelCmd recoverCancelCmd) {
        return recoverCancelExe.cancelRecover(recoverCancelCmd);
    }

    @Override
    public String toCheck(RecoverVechicleCmd recoverVechicleCmd) {
        return recoverToCheckExe.toCheck(recoverVechicleCmd);
    }

    @Override
    public String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd) {
        return recoverBackInsureExe.toBackInsure(recoverBackInsureCmd);
    }


    @Override
    public RecoverTaskListVO getRecoverApplyListAll(RecoverQryListCmd recoverQryListCmd) {
        return recoverVehicleQryExe.getRecoverApplyListAll(recoverQryListCmd);
    }

    @Override
    public RecoverTaskListVO getStayRecoverApplyList(RecoverQryListCmd recoverQryListCmd) {

        return recoverVehicleQryExe.getStayRecoverApplyList(recoverQryListCmd);
    }

    @Override
    public RecoverTaskListVO getCompletedRecoverApplyList(RecoverQryListCmd recoverQryListCmd) {

        return recoverVehicleQryExe.getCompletedRecoverApplyList(recoverQryListCmd);
    }

    @Override
    public RecoverTaskListVO getRecoverTaskListVoInsure(RecoverQryListCmd recoverQryListCmd) {
        return recoverVehicleQryExe.getRecoverTaskListVoInsure(recoverQryListCmd);
    }

    @Override
    public RecoverTaskListVO getRecoverTaskListVoDeduction(RecoverQryListCmd recoverQryListCmd) {

        return recoverVehicleQryExe.getRecoverTaskListVoDeduction(recoverQryListCmd);
    }

    @Override
    public RecoverTaskListVO getRecoverTaskListVoCompleted(RecoverQryListCmd recoverQryListCmd) {

        return recoverVehicleQryExe.getRecoverTaskListVoCompleted(recoverQryListCmd);
    }

    @Override
    public String toDeduction(RecoverDeductionCmd recoverDeductionCmd) {

        return recoverDeductionExe.toDeduction(recoverDeductionCmd);
    }
}
