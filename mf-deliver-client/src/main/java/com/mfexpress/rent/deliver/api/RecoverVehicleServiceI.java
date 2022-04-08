package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;

import java.util.List;

public interface RecoverVehicleServiceI {

    List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo);

    String applyRecover(RecoverApplyListCmd recoverApplyListCmd);

    String cancelRecover(RecoverCancelCmd recoverCancelCmd);

    String whetherToCheck(RecoverVechicleCmd recoverVechicleCmd);

    String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd);

    String toDeduction(RecoverDeductionCmd recoverDeductionCmd);

    RecoverTaskListVO getRecoverListVO(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo);

    String cacheCheckInfo(RecoverVechicleCmd recoverVechicleCmd);

    RecoverVehicleVO getCachedCheckInfo(RecoverVechicleCmd recoverVechicleCmd);

    RecoverDetailVO getRecoverDetail(RecoverDetailQryCmd cmd);

    Integer abnormalRecover(RecoverAbnormalCmd cmd, TokenInfo tokenInfo);

    RecoverAbnormalVO getRecoverAbnormalInfo(RecoverAbnormalQry cmd);

    Integer toBackInsureByDeliver(RecoverBackInsureByDeliverCmd cmd, TokenInfo tokenInfo);

    Integer cancelRecoverByDeliver(RecoverCancelByDeliverCmd cmd, TokenInfo tokenInfo);

    Integer toDeductionByDeliver(RecoverDeductionByDeliverCmd cmd, TokenInfo tokenInfo);
}
