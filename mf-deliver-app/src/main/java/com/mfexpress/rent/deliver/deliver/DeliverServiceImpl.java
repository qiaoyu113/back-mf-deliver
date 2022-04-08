package com.mfexpress.rent.deliver.deliver;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.deliver.executor.*;
import com.mfexpress.rent.deliver.dto.data.deliver.*;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @Resource
    private DeliverEachLeaseTermAmountQryExe deliverEachLeaseTermAmountQryExe;

    @Resource
    private ExportDeliverEachLeaseTermAmountCmdExe exportDeliverEachLeaseTermAmountCmdExe;

    @Resource
    private ExportDeliverEachLeaseTermAmountDataQryExe exportDeliverEachLeaseTermAmountDataQryExe;

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

    @Override
    public PagePagination<DeliverEachLeaseTermAmountVO> getDeliverLeaseTermAmountVOList(ServeQryCmd qry) {
        return deliverEachLeaseTermAmountQryExe.execute(qry);
    }

    @Override
    public Integer exportDeliverLeaseTermAmount(ServeQryCmd qry, TokenInfo tokenInfo) {
        return exportDeliverEachLeaseTermAmountCmdExe.execute(qry, tokenInfo);
    }

    @Override
    public List<Map<String, Object>> exportDeliverLeaseTermAmountData(Map<String, Object> map) {
        return exportDeliverEachLeaseTermAmountDataQryExe.execute(map);
    }
}
