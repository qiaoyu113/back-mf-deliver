package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeInfoVO;

import java.util.List;
import java.util.Map;

public interface DeliverServiceI {

    String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd);

    String toCheck(DeliverCheckCmd deliverCheckCmd);

    String toReplace(DeliverReplaceCmd deliverReplaceCmd);

    String toInsure(DeliverInsureCmd deliverInsureCmd);

    ServeInfoVO getDeliverLeaseTermAmountVOList(ServeQryCmd qry);

    Integer exportDeliverLeaseTermAmount(ServeQryCmd qry, TokenInfo tokenInfo);

    List<Map<String,Object>> exportDeliverLeaseTermAmountData(Map<String, Object> map);
}
