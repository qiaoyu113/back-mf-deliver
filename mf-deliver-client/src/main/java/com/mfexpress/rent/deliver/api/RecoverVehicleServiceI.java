package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;

import java.util.List;

public interface RecoverVehicleServiceI {

    List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo);

    String applyRecover(RecoverApplyListCmd recoverApplyListCmd);

    String cancelRecover(RecoverCancelCmd recoverCancelCmd);

    String toCheck(RecoverVechicleCmd recoverVechicleCmd);


    String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd);

    String toDeduction(RecoverDeductionCmd recoverDeductionCmd);

    RecoverTaskListVO getRecoverListVO(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo);


}
