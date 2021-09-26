package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverQryContext {


    @Resource
    private ApplicationContext applicationContext;

    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        RecoverQryServiceI bean = (RecoverQryServiceI) applicationContext.getBean(RecoverEnum.getServiceName(recoverQryListCmd.getTag()) + "QryExe");
        return bean.execute(recoverQryListCmd, tokenInfo);

    }

}
