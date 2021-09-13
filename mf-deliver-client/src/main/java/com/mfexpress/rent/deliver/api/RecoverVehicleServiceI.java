package com.mfexpress.rent.deliver.api;

import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;

import java.util.List;

public interface RecoverVehicleServiceI {

    List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd);

    String applyRecover(RecoverApplyListCmd recoverApplyListCmd);

    String cancelRecover(RecoverCancelCmd recoverCancelCmd);

    String toCheck(RecoverVechicleCmd recoverVechicleCmd);

    String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd);

    String toDeduction(RecoverDeductionCmd recoverDeductionCmd);

    RecoverTaskListVO getRecoverApplyListAll(RecoverQryListCmd recoverQryListCmd);

    RecoverTaskListVO getStayRecoverApplyList(RecoverQryListCmd recoverQryListCmd);

    RecoverTaskListVO getCompletedRecoverApplyList(RecoverQryListCmd recoverQryListCmd);

    RecoverTaskListVO getRecoverTaskListVoInsure(RecoverQryListCmd recoverQryListCmd);

    RecoverTaskListVO getRecoverTaskListVoDeduction(RecoverQryListCmd recoverQryListCmd);

    RecoverTaskListVO getRecoverTaskListVoCompleted(RecoverQryListCmd recoverQryListCmd);


}
