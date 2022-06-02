package com.mfexpress.rent.deliver.serve;

import com.mfexpress.billing.customer.data.dto.advince.OrderPayAdvincepaymentCmd;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositSourceToReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import com.mfexpress.rent.deliver.serve.executor.*;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeAdjustCheckCmdExe;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeAdjustCmdExe;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeDepositPayCmdExe;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServeServiceImpl implements ServeServiceI {

    @Resource
    private ServeAddCmdExe serveAddCmdExe;
    @Resource
    private ServeDeliverTaskListQryExe serveDeliverTaskListQryExe;
    @Resource
    private ServeFastPreselectedQryExe serveFastPreselectedQryExe;
    @Resource
    private ServePreselectedQryExe servePreselectedQryExe;
    @Resource
    private ServeCheckQryExe serveCheckQryExe;
    @Resource
    private ServeInsureQryExe serveInsureQryExe;
    @Resource
    private ServeDeliverQryExe serveDeliverQryExe;
    @Resource
    private ServeCompletedQryExe serveCompletedQryExe;
    @Resource
    private ServeListAllQryExe serveListAllQryExe;

    @Resource
    private ServeDeliverDetailQryExe serveDeliverDetailQryExe;

    @Resource
    private ServeRecoverDetailQryExe serveRecoverDetailQryExe;

    @Resource
    private RenewableServeListQryExe renewableServeListQryExe;

    @Resource
    private ReactivateServeCmdExe reactivateServeCmdExe;

    @Resource
    private ServeRecoverDetailQryByDeliverExe serveRecoverDetailQryByDeliverExe;

    @Resource
    private ServeLeaseTermAmountQryExe serveLeaseTermAmountQryExe;

    @Resource
    private ExportServeLeaseTermAmountCmdExe exportServeLeaseTermAmountCmdExe;

    @Resource
    private ExportServeLeaseTermAmountDataQryExe exportServeLeaseTermAmountDataQryExe;


    @Resource
    private ServeAdjustCheckCmdExe serveAdjustCheckCmdExe;

    @Resource
    private ServeAdjustCmdExe serveAdjustCmdExe;

    @Resource
    private ServeDepositPayCmdExe serveDepositPayCmdExe;

    @Override
    public ServeListVO getServeListVoByOrderNoAll(ServeQryListCmd serveQryListCmd) {

        return serveListAllQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServePreselectedListVO getServeListVoPreselected(ServeQryListCmd serveQryListCmd) {
        return servePreselectedQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoInsure(ServeQryListCmd serveQryListCmd) {
        return serveInsureQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoCheck(ServeQryListCmd serveQryListCmd) {
        return serveCheckQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoDeliver(ServeQryListCmd serveQryListCmd) {
        return serveDeliverQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoCompleted(ServeQryListCmd serveQryListCmd) {
        return serveCompletedQryExe.execute(serveQryListCmd);
    }


    @Override
    public ServeFastPreselectedListVO getServeFastPreselectedVO(ServeQryListCmd serveQryListCmd) {
        return serveFastPreselectedQryExe.execute(serveQryListCmd);
    }

    @Override
    public ServeDeliverTaskListVO getServeDeliverTaskListVO(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd , TokenInfo tokenInfo) {
        return serveDeliverTaskListQryExe.execute(serveDeliverTaskQryCmd,tokenInfo);
    }


    @Override
    public String addServe(ServeAddCmd serveAddCmd) {
        return serveAddCmdExe.execute(serveAddCmd);
    }

    @Override
    public ServeDeliverDetailVO getServeDeliverDetail(ServeQryCmd cmd) {
        return serveDeliverDetailQryExe.execute(cmd);
    }

    @Override
    public ServeRecoverDetailVO getServeRecoverDetail(ServeQryCmd cmd) {
        return serveRecoverDetailQryExe.execute(cmd);
    }

    @Override
    public List<ServeToRenewalVO> getRenewableServeList(RenewableServeQry qry, TokenInfo tokenInfo) {
        return renewableServeListQryExe.execute(qry, tokenInfo);
    }

    @Override
    public PagePagination<ServeAllLeaseTermAmountVO> getServeLeaseTermAmountVOList(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo) {
        return serveLeaseTermAmountQryExe.execute(qry, tokenInfo);
    }

    @Override
    public Integer exportServeLeaseTermAmount(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo) {
        return exportServeLeaseTermAmountCmdExe.execute(qry, tokenInfo);
    }

    @Override
    public Integer reactivate(ReactivateServeCmd cmd, TokenInfo tokenInfo) {
        return reactivateServeCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public ServeRecoverDetailVO getServeRecoverDetailByDeliver(ServeQryByDeliverCmd cmd) {
        return serveRecoverDetailQryByDeliverExe.execute(cmd);
    }

    @Override
    public List<Map<String, Object>> exportServeLeaseTermAmountData(Map<String, Object> map) {
        return exportServeLeaseTermAmountDataQryExe.execute(map);
    }

    @Override
    public ServeAdjustRecordVo serveAdjustCheck(ServeAdjustCheckCmd cmd, TokenInfo tokenInfo) {
        return serveAdjustCheckCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public void serveAdjust(ServeAdjustCmd cmd, TokenInfo tokenInfo) {

        serveAdjustCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public void serveDepositPay(ServeDTO serveDTO, Integer operatorId) {

        serveDepositPayCmdExe.execute(serveDTO, operatorId);
    }
}
