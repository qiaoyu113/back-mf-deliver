package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.exception.CommonException;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ServeDeliverDetailQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    public ServeDeliverDetailVO execute(ServeQryCmd cmd) {
        String serveNo = cmd.getServeNo();
        Result<ServeDTO> serveDtoResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if(!DeliverUtils.resultDataCheck(serveDtoResult)){
            // commonException应封到base中
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        ServeDTO serveDTO = serveDtoResult.getData();

        ServeDeliverDetailVO serveDeliverDetailVO = new ServeDeliverDetailVO();
        serveDeliverDetailVO.setServeNo(serveNo);
        // 不管是什么状态，必补充订单信息
        serveDeliverDetailVO.setOrderVO(getOrderVO(serveDTO));

        if(!ServeEnum.NOT_PRESELECTED.getCode().equals(serveDTO.getStatus())){
            Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
            if(!DeliverUtils.resultDataCheck(deliverDTOResult)){
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单信息查询失败");
            }
            DeliverDTO deliverDTO = deliverDTOResult.getData();

            // serve的status属性为1，状态为已预选，服务单信息选择补充
            if (ServeEnum.PRESELECTED.getCode().equals(serveDTO.getStatus())){
                // serve的status属性为1，deliver的deliver_status属性必为1，必补充车辆信息
                serveDeliverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO));
                if (JudgeEnum.YES.getCode().equals(deliverDTO.getIsCheck())) {
                    // 验车标志位为true补充验车信息
                    serveDeliverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
                }
                if(JudgeEnum.YES.getCode().equals(deliverDTO.getIsInsurance())){
                    // 投保标志位为true补充投保信息
                    serveDeliverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
                }
            } else {
                // serve的status属性为2/3/4/5，服务单信息全部补充
                serveDeliverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO));
                serveDeliverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
                serveDeliverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
                serveDeliverDetailVO.setDeliverVehicleVO(getDeliverVehicleVO(deliverDTO));
            }
        }

        return serveDeliverDetailVO;
    }

    public DeliverVehicleVO getDeliverVehicleVO(DeliverDTO deliverDTO){
        Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverVehicleDTOResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "发车单信息查询失败");
        }
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleDTOResult.getData();
        DeliverVehicleVO deliverVehicleVO = new DeliverVehicleVO();
        BeanUtils.copyProperties(deliverVehicleDTO, deliverVehicleVO);
        return deliverVehicleVO;
    }

    public VehicleInsuranceVO getVehicleInsuranceVO(DeliverDTO deliverDTO){
        VehicleInsuranceVO vehicleInsuranceVO = new VehicleInsuranceVO();
        vehicleInsuranceVO.setStartTime(deliverDTO.getInsuranceStartTime());
        return vehicleInsuranceVO;
    }

    public VehicleValidationVO getVehicleValidationVO() {
        VehicleValidationVO vehicleValidationVO = new VehicleValidationVO();
        vehicleValidationVO.setCheckFlag(true);
        return vehicleValidationVO;
    }

    public VehicleVO getVehicleVO(ServeDTO serveDTO, DeliverDTO deliverDTO) {
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getCarModelId());
        VehicleVO vehicleVO = new VehicleVO();
        vehicleVO.setCarNum(deliverDTO.getCarNum());
        //vehicleVO.setBrandId();
        vehicleVO.setBrandModelDisplay(carModelResult.getData());
        vehicleVO.setVin(deliverDTO.getFrameNum());
        vehicleVO.setMileage(deliverDTO.getMileage());
        vehicleVO.setVehicleAge(deliverDTO.getVehicleAge());
        return vehicleVO;
    }

    public OrderVO getOrderVO(ServeDTO serveDTO) {
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(String.valueOf(serveDTO.getOrderId()));
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (!DeliverUtils.resultDataCheck(orderResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "订单信息查询失败");
        }
        OrderDTO orderDTO = orderResult.getData();
        OrderVO orderVO = new OrderVO();
        orderVO.setContractNo(orderDTO.getContractCode());
        orderVO.setDeliveryDate(orderDTO.getDeliveryDate());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(orderDTO.getCustomerId());
        if (!DeliverUtils.resultDataCheck(customerResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "客户信息查询失败");
        }
        orderVO.setCustomerName(customerResult.getData().getName());

        if (JudgeEnum.YES.getCode().equals(serveDTO.getReplaceFlag())) {
            orderVO.setPurposeDisplay("替换");
        } else {
            orderVO.setPurposeDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.DELIVER_LEASE_MODE), serveDTO.getLeaseModelId().toString()));
        }
        return orderVO;
    }

    private Map<String, DictDataDTO> getDictDataDtoMapByDictType(String dictType) {
        DictTypeDTO dictTypeDTO = new DictTypeDTO();
        dictTypeDTO.setDictType(dictType);
        Result<List<DictDataDTO>> dictDataResult = dictAggregateRootApi.getDictDataByType(dictTypeDTO);
        if (dictDataResult.getCode() == 0) {
            List<DictDataDTO> dictDataDTOList = dictDataResult.getData();
            if (dictDataDTOList == null || dictDataDTOList.isEmpty()) {
                return new HashMap<>(16);
            }
            Map<String, DictDataDTO> dictDataDTOMap = dictDataDTOList.stream().collect(Collectors.toMap(DictDataDTO::getDictValue, Function.identity(), (key1, key2) -> key1));
            return dictDataDTOMap;
        }
        return null;
    }

    private String getDictDataDtoLabelByValue(Map<String, DictDataDTO> dictDataDtoMap, String value) {
        if (dictDataDtoMap == null) {
            return "";
        }

        DictDataDTO dictDataDTO = dictDataDtoMap.get(value);
        if (dictDataDTO != null) {
            return dictDataDTO.getDictLabel();
        }
        return "";
    }

}
