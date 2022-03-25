package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.deliver.*;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;

import java.util.List;
import java.util.Map;

public interface DeliverServiceI {

    String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd);

    String toCheck(DeliverCheckCmd deliverCheckCmd);

    String toReplace(DeliverReplaceCmd deliverReplaceCmd);

    String toInsure(DeliverInsureCmd deliverInsureCmd);

    PagePagination<DeliverEachLeaseTermAmountVO> getDeliverLeaseTermAmountVOList(ServeQryCmd qry);

    Integer exportDeliverLeaseTermAmount(ServeQryCmd qry, TokenInfo tokenInfo);

    List<Map<String,Object>> exportDeliverLeaseTermAmountData(Map<String, Object> map);
}
