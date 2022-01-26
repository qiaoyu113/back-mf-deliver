package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDetailQryCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDetailVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverVehicleDetailQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    public RecoverDetailVO execute(RecoverDetailQryCmd cmd) {
        String serveNo = cmd.getServeNo();

        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if (!DeliverUtils.resultDataCheck(serveResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息查询失败");
        }
        ServeDTO serveDTO = serveResult.getData();

        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if (!DeliverUtils.resultDataCheck(deliverResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交车单信息查询失败");
        }
        // 车牌号、车架号
        DeliverDTO deliverDTO = deliverResult.getData();

        Result<RecoverVehicleDTO> recoverVehicleResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "收车单信息查询失败");
        }
        // 收车日期、预计还车日期
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleResult.getData();

        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(serveDTO.getOrderId().toString());
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (!DeliverUtils.resultDataCheck(orderResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "订单信息查询失败");
        }
        // 合同编号
        OrderDTO orderDTO = orderResult.getData();

        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(orderDTO.getCustomerId());
        if (!DeliverUtils.resultDataCheck(customerResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "客户信息查询失败");
        }
        // 客户名称
        CustomerVO customerVO = customerResult.getData();

        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getCarModelId());
        // 车型
        carModelResult.getData();

        Result<DeliverVehicleDTO> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverVehicleResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "发车信息查询失败");
        }
        // 发车日期
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleResult.getData();

        // 数据拼装
        RecoverDetailVO recoverDetailVO = new RecoverDetailVO();
        recoverDetailVO.setCustomerName(customerVO.getName());
        recoverDetailVO.setContractNo(orderDTO.getOaContractCode());

        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        recoverVehicleVO.setCarNum(deliverDTO.getCarNum());
        recoverVehicleVO.setBrandModelDisplay(carModelResult.getData());
        recoverVehicleVO.setFrameNum(deliverDTO.getFrameNum());
        recoverVehicleVO.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
        recoverVehicleVO.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
        recoverVehicleVO.setRecoverVehicleTime(recoverVehicleDTO.getRecoverVehicleTime());
        recoverDetailVO.setRecoverVehicleVO(recoverVehicleVO);

        return recoverDetailVO;
    }

}
