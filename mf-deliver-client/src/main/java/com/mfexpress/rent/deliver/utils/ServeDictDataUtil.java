package com.mfexpress.rent.deliver.utils;

import java.util.Map;

import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import org.springframework.beans.factory.BeanFactory;

public class ServeDictDataUtil {

    public static Map<String, String> leaseModeMap;

    public static Map<String, String> vehicleColorMap;

    public static Map<Integer, String> vehicleTypeMap;

    public static void initDictData(BeanFactory beanFactory) {

        DictAggregateRootApi dictAggregateRootApi = beanFactory.getBean(DictAggregateRootApi.class);

        if (null == leaseModeMap) {
            leaseModeMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "lease_mode");
        }
        if (null == vehicleColorMap) {
            vehicleColorMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "vehicle_color");
        }
        if (null == vehicleTypeMap) {
            VehicleAggregateRootApi vehicleAggregateRootApi = beanFactory.getBean(VehicleAggregateRootApi.class);
            Result<Map<Integer, String>> vehicleTypeResult = vehicleAggregateRootApi.getAllVehicleBrandTypeList();
            if (ResultErrorEnum.SUCCESSED.getCode() == vehicleTypeResult.getCode() && null != vehicleTypeResult.getData()) {
                vehicleTypeMap = vehicleTypeResult.getData();
            }
        }
    }
}
