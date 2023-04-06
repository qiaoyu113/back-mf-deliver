package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.alibaba.fastjson.JSONObject;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Component
public class RecoverVehicleCheckInfoQryExe {

    @Resource
    private RedisTools redisTools;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    public RecoverVehicleVO execute(RecoverVehicleCmd cmd) {

        String serveNo = cmd.getServeNo();
        Integer operatorId = cmd.getOperatorId();
        String key = DeliverUtils.concatCacheKey(Constants.RECOVER_VEHICLE_CHECK_INFO_CACHE_KEY, serveNo, String.valueOf(operatorId));
        String result = redisTools.get(key);

        RecoverVehicleVO recoverVehicleVO;
        if(StringUtils.isEmpty(result)){
            recoverVehicleVO = new RecoverVehicleVO();
        }else{
            recoverVehicleVO = JSONObject.parseObject(result, RecoverVehicleVO.class);
            if(null != recoverVehicleVO.getWareHouseId()){
                Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(recoverVehicleVO.getWareHouseId());
                if (wareHouseResult.getData() != null) {
                    recoverVehicleVO.setWareHouseDisplay(wareHouseResult.getData().getName());
                }
            }
        }

        // 应前端要求 WareHouseDisplay如果为null，设为空字符串
        if(null == recoverVehicleVO.getWareHouseDisplay()){
            recoverVehicleVO.setWareHouseDisplay("");
        }

        return recoverVehicleVO;
    }
}
