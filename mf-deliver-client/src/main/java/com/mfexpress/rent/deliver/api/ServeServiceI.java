package com.mfexpress.rent.deliver.api;


import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.serve.*;

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


}
