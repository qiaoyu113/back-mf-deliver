package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverQryContext {


    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        RecoverQryServiceI bean = (RecoverQryServiceI) applicationContext.getBean(RecoverEnum.getServiceName(recoverQryListCmd.getTag()) + "QryExe");
        RecoverTaskListVO recoverTaskListVO = bean.execute(recoverQryListCmd, tokenInfo);
       List<RecoverVehicleVO> recoverVehicleVOList = recoverTaskListVO.getRecoverVehicleVOList();
        for (RecoverVehicleVO v : recoverVehicleVOList) {
            Result<MaintenanceDTO> maintainResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(v.getServeNo());
            if (maintainResult.getData()!=null){
                v.setConfirmDate(maintainResult.getData().getConfirmDate());
            }
        }
        return recoverTaskListVO;

    }

}
