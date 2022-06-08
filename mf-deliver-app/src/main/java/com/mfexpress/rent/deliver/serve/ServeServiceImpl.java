package com.mfexpress.rent.deliver.serve;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewableServeQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverDetailVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeFastPreselectedListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryListCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeRecoverDetailVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeToRenewalVO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import com.mfexpress.rent.deliver.serve.executor.ExportServeLeaseTermAmountCmdExe;
import com.mfexpress.rent.deliver.serve.executor.ExportServeLeaseTermAmountDataQryExe;
import com.mfexpress.rent.deliver.serve.executor.ReactivateServeCmdExe;
import com.mfexpress.rent.deliver.serve.executor.RenewableServeListQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeAddCmdExe;
import com.mfexpress.rent.deliver.serve.executor.ServeCheckQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeCompletedQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeDeliverDetailQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeDeliverQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeDeliverTaskListQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeFastPreselectedQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeInsureQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeLeaseTermAmountQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeListAllQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServePreselectedQryExe;
import com.mfexpress.rent.deliver.serve.executor.ServeRecoverDetailQryByDeliverExe;
import com.mfexpress.rent.deliver.serve.executor.ServeRecoverDetailQryExe;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeAdjustCheckCmdExe;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeAdjustCmdExe;
import com.mfexpress.rent.deliver.serve.executor.cmd.ServeDepositPayCmdExe;
import org.springframework.stereotype.Service;

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
    public void serveDepositPay(ServeDepositPayCmd cmd) {

        serveDepositPayCmdExe.execute(cmd);
    }
}
