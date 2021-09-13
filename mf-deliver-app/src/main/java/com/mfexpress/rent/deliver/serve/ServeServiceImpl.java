package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.serve.executor.ServeAddCmdExe;
import com.mfexpress.rent.deliver.serve.executor.ServeQryCmdExe;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ServeServiceImpl implements ServeServiceI {

    @Resource
    private ServeQryCmdExe serveQryCmdExe;
    @Resource
    private ServeAddCmdExe serveAddCmdExe;


    @Override
    public ServeListVO getServeListVoByOrderNoAll(ServeQryListCmd serveQryListCmd) {

        return serveQryCmdExe.getServeListVoByOrderNoAll(serveQryListCmd);
    }

    @Override
    public ServePreselectedListVO getServeListVoPreselected(ServeQryListCmd serveQryListCmd) {


        return serveQryCmdExe.getServeListVoPreselected(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoInsure(ServeQryListCmd serveQryListCmd) {
        return serveQryCmdExe.getServeListVoInsure(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoCheck(ServeQryListCmd serveQryListCmd) {
        return serveQryCmdExe.getServeListVoCheck(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoDeliver(ServeQryListCmd serveQryListCmd) {
        return serveQryCmdExe.getServeListVoDeliver(serveQryListCmd);
    }

    @Override
    public ServeListVO getServeListVoCompleted(ServeQryListCmd serveQryListCmd) {
        return serveQryCmdExe.getServeListVoCompleted(serveQryListCmd);
    }


    @Override
    public ServeFastPreselectedListVO getServeFastPreselectedVO(ServeQryListCmd serveQryListCmd) {
        return serveQryCmdExe.getServeFastPreselectedVO(serveQryListCmd);
    }

    @Override
    public ServeDeliverTaskListVO getServeDeliverTaskListVO(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd) {


        return serveQryCmdExe.getServeDeliverTaskListVO(serveDeliverTaskQryCmd);
    }



    @Override
    public String addServe(ServeAddCmd serveAddCmd) {
        return serveAddCmdExe.addServe(serveAddCmd);
    }
}
