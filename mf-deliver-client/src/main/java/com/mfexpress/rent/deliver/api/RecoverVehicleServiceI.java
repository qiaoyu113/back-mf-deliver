package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.vo.SurrenderApplyVO;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RecoverVehicleServiceI {

    List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo);

    String applyRecover(RecoverApplyListCmd recoverApplyListCmd);

    String cancelRecover(RecoverCancelCmd recoverCancelCmd);

    Boolean whetherToCheck(RecoverVechicleCmd recoverVechicleCmd);

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


    LinkmanVo getRecoverVehicleDtoByDeliverNo(Integer customerId);

    SurrenderApplyVO backInsureByDeliver(RecoverBackInsureByDeliverCmd cmd, TokenInfo tokenInfo);
}
