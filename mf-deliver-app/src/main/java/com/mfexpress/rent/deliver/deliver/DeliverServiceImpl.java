package com.mfexpress.rent.deliver.deliver;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.deliver.executor.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverInsureByCustomerCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.deliver.executor.InsureByCompanyCmdExe;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeInfoVO;
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

    @Resource
    private InsureByCompanyCmdExe insureByCompanyCmdExe;

    @Resource
    private InsureApplyQryExe insureApplyQryExe;

    @Resource
    private InsureByCustomerCmdExe insureByCustomerCmdExe;

    @Resource
    private PreselectedVehicleCmdExe preselectedVehicleCmdExe;

    @Resource
    private ReplaceVehicleCheckCmdExe replaceVehicleCheckCmdExe;

    @Resource
    private CancelPreSelectedCmdExe cancelPreSelectedCmdExe;

    @Override
    public String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd) {

        return deliverToPreselectedExe.execute(deliverPreselectedCmd);
    }

    @Override
    public String toCheck(DeliverCheckCmd deliverCheckCmd) {

        return deliverToCheckExe.execute(deliverCheckCmd);
    }

    @Override
    public TipVO toReplace(DeliverReplaceCmd deliverReplaceCmd) {

        return deliverToReplaceExe.execute(deliverReplaceCmd);
    }

    @Override
    public String toInsure(DeliverInsureCmd deliverInsureCmd) {

        return deliverToInsureExe.execute(deliverInsureCmd);
    }

    @Override
    public ServeInfoVO getDeliverLeaseTermAmountVOList(ServeQryCmd qry) {
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

    @Override
    public InsureApplyVO insureByCompany(DeliverInsureCmd cmd, TokenInfo tokenInfo) {
        return insureByCompanyCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public InsureApplyVO getInsureInfo(InsureApplyQry qry) {
        return insureApplyQryExe.execute(qry);
    }

    @Override
    public Integer insureByCustomer(DeliverInsureByCustomerCmd cmd, TokenInfo tokenInfo) {
        return insureByCustomerCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public TipVO preselectedVehicle(DeliverPreselectedCmd cmd, TokenInfo tokenInfo) {
        return preselectedVehicleCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public TipVO replaceVehicleShowTip(DeliverReplaceVehicleCheckCmd cmd) {
        return replaceVehicleCheckCmdExe.execute(cmd);
    }

    @Override
    public TipVO cancelPreSelected(CancelPreSelectedCmd cmd, TokenInfo tokenInfo) {
        return cancelPreSelectedCmdExe.execute(cmd, tokenInfo);
    }

}
