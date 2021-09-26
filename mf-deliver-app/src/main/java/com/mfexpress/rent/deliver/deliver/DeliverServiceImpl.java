package com.mfexpress.rent.deliver.deliver;

import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.deliver.executor.DeliverToCheckExe;
import com.mfexpress.rent.deliver.deliver.executor.DeliverToInsureExe;
import com.mfexpress.rent.deliver.deliver.executor.DeliverToPreselectedExe;
import com.mfexpress.rent.deliver.deliver.executor.DeliverToReplaceExe;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeliverServiceImpl implements DeliverServiceI {

    @Resource
    private DeliverToCheckExe deliverToCheckExe;
    @Resource
    private DeliverToPreselectedExe deliverToPreselectedExe;
    @Resource
    private DeliverToInsureExe deliverToInsureExe;
    @Resource
    private DeliverToReplaceExe deliverToReplaceExe;


    @Override
    public String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd) {

        return deliverToPreselectedExe.execute(deliverPreselectedCmd);
    }

    @Override
    public String toCheck(DeliverCheckCmd deliverCheckCmd) {

        return deliverToCheckExe.execute(deliverCheckCmd);
    }

    @Override
    public String toReplace(DeliverReplaceCmd deliverReplaceCmd) {

        return deliverToReplaceExe.execute(deliverReplaceCmd);
    }

    @Override
    public String toInsure(DeliverInsureCmd deliverInsureCmd) {

        return deliverToInsureExe.execute(deliverInsureCmd);
    }
}
