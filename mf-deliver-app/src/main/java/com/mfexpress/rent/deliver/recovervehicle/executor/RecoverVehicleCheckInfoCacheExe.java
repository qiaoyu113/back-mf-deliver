package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.alibaba.fastjson.JSONObject;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.exception.CommonException;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverVehicleCheckInfoCacheExe {

    @Resource
    private RedisTools redisTools;

    public String execute(RecoverVechicleCmd cmd) {
        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        BeanUtils.copyProperties(cmd, recoverVehicleVO);

        String serveNo = cmd.getServeNo();
        Integer operatorId = cmd.getCarServiceId();
        String key = DeliverUtils.concatCacheKey(Constants.RECOVER_VEHICLE_CHECK_INFO_CACHE_KEY, serveNo, String.valueOf(operatorId));
        boolean result = redisTools.set(key, JSONObject.toJSONString(recoverVehicleVO));
        if(!result){
            throw new CommonException(400003, "操作失败");
        }
        return ResultStatusEnum.SUCCESSED.getName();
    }
}
