package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.exception.CommonException;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServeRecoverDetailQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    public ServeRecoverDetailVO execute(ServeQryCmd cmd) {
        String serveNo = cmd.getServeNo();
        // 数据查询----start
        Result<ServeDTO> serveDtoResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if(!DeliverUtils.resultDataCheck(serveDtoResult)){
            // commonException应封到base中
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        ServeDTO serveDTO = serveDtoResult.getData();

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if(!DeliverUtils.resultDataCheck(deliverDTOResult)){
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单信息查询失败");
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();

        Result<RecoverVehicleDTO> recoverVehicleDTOResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(recoverVehicleDTOResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "收车单信息查询失败");
        }
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleDTOResult.getData();
        // 数据查询----end

        // 数据拼装----start
        ServeRecoverDetailVO serveRecoverDetailVO = new ServeRecoverDetailVO();
        serveRecoverDetailVO.setServeNo(serveNo);
        // serve的status属性为2或5，deliver的deliver_status属性为3，状态为待验车
        // 代验车需要补充订单/客户信息和车辆信息
        if ((ServeEnum.DELIVER.getCode().equals(serveDTO.getStatus()) || ServeEnum.REPAIR.getCode().equals(serveDTO.getStatus())) && DeliverEnum.IS_RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
        } else if (ServeEnum.RECOVER.getCode().equals(serveDTO.getStatus()) && DeliverEnum.RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
            if(JudgeEnum.YES.getCode().equals(deliverDTO.getIsCheck())){
                // serve的status属性为3，deliver的deliver_status属性为4，is_check属性为1，状态为待退保
                // 待退保需补充验车信息和收车单信息
                serveRecoverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
                serveRecoverDetailVO.setRecoverVehicleVO(getRecoverVehicleVO(recoverVehicleDTO));
            }
            if(JudgeEnum.YES.getCode().equals(deliverDTO.getIsInsurance())){
                // serve的status属性为3，deliver的deliver_status属性为4，is_insurance属性为1，状态为待处理违章
                // 待处理违章需补充保险信息
                serveRecoverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
            }
        } else if (ServeEnum.COMPLETED.getCode().equals(serveDTO.getStatus()) && DeliverEnum.RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            // serve的属性为4，deliver的deliver_status属性为4，is_check属性为1，is_insurance属性为1，is_deduction属性为1，状态为已完成
            // 可以只判断status、deliver_status，属性不用判断is_xxx属性
            // 已完成需补充违章信息
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
            serveRecoverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
            serveRecoverDetailVO.setRecoverVehicleVO(getRecoverVehicleVO(recoverVehicleDTO));
            serveRecoverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
            serveRecoverDetailVO.setViolationInfoVO(getViolationInfoVO(deliverDTO));
        } else {
            throw new CommonException(400005, "数据状态异常");
        }
        // 数据拼装----end

        return serveRecoverDetailVO;
    }

    public ViolationInfoVO getViolationInfoVO(DeliverDTO deliverDTO){
        ViolationInfoVO violationInfoVO = new ViolationInfoVO();
        BeanUtils.copyProperties(deliverDTO, violationInfoVO);
        return violationInfoVO;
    }

    public VehicleInsuranceVO getVehicleInsuranceVO(DeliverDTO deliverDTO){
        VehicleInsuranceVO vehicleInsuranceVO = new VehicleInsuranceVO();
        if(null != deliverDTO.getInsuranceEndTime()){
            // 正常退保 补全退保时间
            vehicleInsuranceVO.setIsInsurance(JudgeEnum.YES.getCode());
            vehicleInsuranceVO.setEndTime(deliverDTO.getInsuranceEndTime());
        }else{
            // 暂不退保 补全原因
            vehicleInsuranceVO.setIsInsurance(JudgeEnum.NO.getCode());
            vehicleInsuranceVO.setInsuranceRemark(deliverDTO.getInsuranceRemark());
        }
        return vehicleInsuranceVO;
    }

    public RecoverVehicleVO getRecoverVehicleVO(RecoverVehicleDTO recoverVehicleDTO){
        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicleVO);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(recoverVehicleDTO.getWareHouseId());
        if (wareHouseResult.getData() != null) {
            recoverVehicleVO.setWareHouseDisplay(wareHouseResult.getData().getName());
        }
        return recoverVehicleVO;
    }

    public VehicleValidationVO getVehicleValidationVO() {
        VehicleValidationVO vehicleValidationVO = new VehicleValidationVO();
        vehicleValidationVO.setCheckFlag(true);
        return vehicleValidationVO;
    }

    public VehicleVO getVehicleVO(ServeDTO serveDTO, DeliverDTO deliverDTO, RecoverVehicleDTO recoverVehicleDTO) {
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getCarModelId());

        Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverVehicleDTOResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "发车单信息查询失败");
        }
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleDTOResult.getData();

        VehicleVO vehicleVO = new VehicleVO();
        vehicleVO.setCarNum(deliverDTO.getCarNum());
        //vehicleVO.setBrandId();
        vehicleVO.setBrandModelDisplay(carModelResult.getData());
        vehicleVO.setVin(deliverDTO.getFrameNum());
        vehicleVO.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
        vehicleVO.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
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
        return orderVO;
    }

}
