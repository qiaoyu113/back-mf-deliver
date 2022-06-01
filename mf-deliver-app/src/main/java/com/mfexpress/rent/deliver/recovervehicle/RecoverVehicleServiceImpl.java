package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.recovervehicle.executor.*;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.api.RentalCustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecoverVehicleServiceImpl implements RecoverVehicleServiceI {

    @Resource
    private RecoverQryContext recoverQryContext;
    @Resource
    private RecoverVehicleQryExe recoverVehicleQryExe;
    @Resource
    private RecoverApplyExe recoverApplyExe;
    @Resource
    private RecoverCancelExe recoverCancelExe;
    @Resource
    private RecoverToCheckExe recoverToCheckExe;
    @Resource
    private RecoverBackInsureExe recoverBackInsureExe;
    @Resource
    private RecoverDeductionExe recoverDeductionExe;

    @Resource
    private RentalCustomerAggregateRootApi rentalCustomerAggregateRootApi;
    @Resource
    private RecoverVehicleCheckInfoCacheExe checkInfoCacheExe;

    @Resource
    private RecoverVehicleCheckInfoQryExe checkInfoQryExe;

    @Resource
    private RecoverVehicleDetailQryExe recoverVehicleDetailQryExe;

    @Resource
    private RecoverAbnormalCmdExe recoverAbnormalCmdExe;

    @Resource
    private RecoverAbnormalQryExe recoverAbnormalQryExe;

    @Resource
    private RecoverBackInsureByDeliverExe recoverBackInsureByDeliverExe;

    @Resource
    private RecoverCancelByDeliverExe recoverCancelByDeliverExe;

    @Resource
    private RecoverDeductionByDeliverExe recoverDeductionByDeliverExe;

    @Resource
    private CustomerAggregateRootApi  customerAggregateRootApi;
    @Override
    public List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo) {
        return recoverVehicleQryExe.execute(recoverApplyQryCmd, tokenInfo);
    }

    @Override
    public String applyRecover(RecoverApplyListCmd recoverApplyListCmd) {
        return recoverApplyExe.execute(recoverApplyListCmd);
    }

    @Override
    public String cancelRecover(RecoverCancelCmd recoverCancelCmd) {
        return recoverCancelExe.execute(recoverCancelCmd);
    }

    @Override
    public String whetherToCheck(RecoverVechicleCmd recoverVechicleCmd) {
        return recoverToCheckExe.execute(recoverVechicleCmd);
    }

    @Override
    public String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd) {
        return recoverBackInsureExe.execute(recoverBackInsureCmd);
    }


    @Override
    public RecoverTaskListVO getRecoverListVO(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {

        return recoverQryContext.execute(recoverQryListCmd, tokenInfo);

    }

    @Override
    public String toDeduction(RecoverDeductionCmd recoverDeductionCmd) {
        return recoverDeductionExe.execute(recoverDeductionCmd);
    }

    @Override
    public String cacheCheckInfo(RecoverVechicleCmd cmd) {
        return checkInfoCacheExe.execute(cmd);
    }

    @Override
    public RecoverVehicleVO getCachedCheckInfo(RecoverVechicleCmd cmd) {
        return checkInfoQryExe.execute(cmd);
    }

    @Override
    public RecoverDetailVO getRecoverDetail(RecoverDetailQryCmd cmd) {
        return recoverVehicleDetailQryExe.execute(cmd);
    }

    @Override
    public Integer abnormalRecover(RecoverAbnormalCmd cmd, TokenInfo tokenInfo) {
        return recoverAbnormalCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public RecoverAbnormalVO getRecoverAbnormalInfo(RecoverAbnormalQry cmd) {
        return recoverAbnormalQryExe.execute(cmd);
    }

    @Override
    public Integer toBackInsureByDeliver(RecoverBackInsureByDeliverCmd cmd, TokenInfo tokenInfo) {
        return recoverBackInsureByDeliverExe.execute(cmd, tokenInfo);
    }

    @Override
    public Integer cancelRecoverByDeliver(RecoverCancelByDeliverCmd cmd, TokenInfo tokenInfo) {
        return recoverCancelByDeliverExe.execute(cmd, tokenInfo);
    }

    @Override
    public Integer toDeductionByDeliver(RecoverDeductionByDeliverCmd cmd, TokenInfo tokenInfo) {
        return recoverDeductionByDeliverExe.execute(cmd, tokenInfo);
    }


    @Override
    public LinkmanVo getRecoverVehicleDtoByDeliverNo(Integer customerId) {
        Result<List<LinkmanVo>> customerId1 = customerAggregateRootApi.getLinkMansByCusomerId(customerId);
        List<LinkmanVo> objects = new ArrayList<>();
        List<LinkmanVo> data = customerId1.getData();
        if (data != null && data.size()>0) {
            for (LinkmanVo v : data) {
                if (v.getType() == 2) {
                    objects.add(v);
                }
            }
            return objects.get(0);
        }
       return new LinkmanVo();
    }

}
