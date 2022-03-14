package com.mfexpress.rent.deliver.serve;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.serve.executor.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        return null;
    }

    @Override
    public Integer reactivate(ReactivateServeCmd cmd, TokenInfo tokenInfo) {
        return reactivateServeCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public ServeRecoverDetailVO getServeRecoverDetailByDeliver(ServeQryByDeliverCmd cmd) {
        return serveRecoverDetailQryByDeliverExe.execute(cmd);
    }

}
