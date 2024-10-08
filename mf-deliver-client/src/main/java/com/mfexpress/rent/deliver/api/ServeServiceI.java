package com.mfexpress.rent.deliver.api;


import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewableServeQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverDetailVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeFastPreselectedListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryListCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeRecoverDetailVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeToRenewalVO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustVO;

import java.util.List;
import java.util.Map;

public interface ServeServiceI {

    ServeListVO getServeListVoByOrderNoAll(ServeQryListCmd serveQryListCmd);

    ServePreselectedListVO getServeListVoPreselected(ServeQryListCmd serveQryListCmd);

    ServeListVO getServeListVoInsure(ServeQryListCmd serveQryListCmd);

    ServeListVO getServeListVoCheck(ServeQryListCmd serveQryListCmd);

    ServeListVO getServeListVoDeliver(ServeQryListCmd serveQryListCmd);

    ServeListVO getServeListVoCompleted(ServeQryListCmd serveQryListCmd);

    ServeFastPreselectedListVO getServeFastPreselectedVO(ServeQryListCmd serveQryListCmd);

    ServeDeliverTaskListVO getServeDeliverTaskListVO(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd, TokenInfo tokenInfo);


    String addServe(ServeAddCmd serveAddCmd);

    ServeDeliverDetailVO getServeDeliverDetail(ServeQryCmd cmd);

    ServeRecoverDetailVO getServeRecoverDetail(ServeQryCmd cmd);

    List<ServeToRenewalVO> getRenewableServeList(RenewableServeQry qry, TokenInfo tokenInfo);

    PagePagination<ServeAllLeaseTermAmountVO> getServeLeaseTermAmountVOList(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo);

    Integer exportServeLeaseTermAmount(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo);

    Integer reactivate(ReactivateServeCmd cmd, TokenInfo tokenInfo);

    ServeRecoverDetailVO getServeRecoverDetailByDeliver(ServeQryByDeliverCmd cmd);

    List<Map<String, Object>> exportServeLeaseTermAmountData(Map<String, Object> map);

    ServeAdjustVO serveAdjustCheck(ServeAdjustCheckCmd cmd, TokenInfo tokenInfo);

    void serveAdjust(ServeAdjustCmd cmd, TokenInfo tokenInfo);

    /**
     * 服务单押金支付
     * @param cmd
     */
    void serveDepositPay(ServeDepositPayCmd cmd);
}
