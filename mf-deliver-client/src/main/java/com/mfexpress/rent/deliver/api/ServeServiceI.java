package com.mfexpress.rent.deliver.api;


import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.*;

import java.util.List;

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
}
