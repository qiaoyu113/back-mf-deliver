package com.mfexpress.rent.deliver.api;

import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;

public interface DeliverServiceI {

    String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd);

    String toCheck(DeliverCheckCmd deliverCheckCmd);

    String toReplace(DeliverReplaceCmd deliverReplaceCmd);

    String toInsure(DeliverInsureCmd deliverInsureCmd);
}
