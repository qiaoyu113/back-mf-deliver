package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverInsureByCustomerCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverInsureByCustomerCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeInfoVO;

import java.util.List;
import java.util.Map;

public interface DeliverServiceI {

    String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd);

    String toCheck(DeliverCheckCmd deliverCheckCmd);

    TipVO toReplace(DeliverReplaceCmd deliverReplaceCmd);

    String toInsure(DeliverInsureCmd deliverInsureCmd);

    ServeInfoVO getDeliverLeaseTermAmountVOList(ServeQryCmd qry);

    Integer exportDeliverLeaseTermAmount(ServeQryCmd qry, TokenInfo tokenInfo);

    List<Map<String,Object>> exportDeliverLeaseTermAmountData(Map<String, Object> map);

    InsureApplyVO insureByCompany(DeliverInsureCmd cmd, TokenInfo tokenInfo);

    InsureApplyVO getInsureInfo(InsureApplyQry qry);

    Integer insureByCustomer(DeliverInsureByCustomerCmd cmd, TokenInfo tokenInfo);

    TipVO preselectedVehicle(DeliverPreselectedCmd cmd, TokenInfo tokenInfo);

    TipVO replaceVehicleShowTip(DeliverReplaceVehicleCheckCmd cmd);

    TipVO cancelPreSelected(CancelPreSelectedCmd cmd, TokenInfo tokenInfo);
}
