package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import cn.hutool.json.JSONUtil;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.dto.contract.ContractDocumentDTO;
import com.mfexpress.component.dto.contract.ContractDocumentInfoDTO;
import com.mfexpress.component.enums.contract.ContractModeEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.contract.MFContractTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.DeliverTypeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverContractGeneratingCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateDeliverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.utils.CommonUtil;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleValidationAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehiclevalidation.VehicleValidationFullInfoDTO;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CreateDeliverContractCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private VehicleValidationAggregateRootApi vehicleValidationAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    // 需抽离到公共类
    public Map<String, String> leaseModeMap;

    public Map<String, String> vehicleColorMap;

    public Map<Integer, String> vehicleTypeMap;

    @Resource
    private MFContractTools contractTools;

    @Resource
    private EsSyncHandlerI syncServiceI;

    public String execute(CreateDeliverContractCmd cmd, TokenInfo tokenInfo) {
        // 初始化字典数据
        initDictData();

        List<DeliverImgInfo> deliverImgInfos = cmd.getDeliverImgInfos();
        // 获取数据的orgId
        List<String> serveNos = deliverImgInfos.stream().map(DeliverImgInfo::getServeNo).collect(Collectors.toList());
        Result<Map<String, Serve>> serveMapResult = serveAggregateRootApi.getServeMapByServeNoList(serveNos);
        if(ResultErrorEnum.SUCCESSED.getCode() != serveMapResult.getCode() || null == serveMapResult.getData() || serveMapResult.getData().isEmpty()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单查询失败");
        }
        Map<String, Serve> serveMap = serveMapResult.getData();

        ReviewOrderQry qry = new ReviewOrderQry();
        Long orderId = serveMap.get(deliverImgInfos.get(0).getServeNo()).getOrderId();
        qry.setId(orderId.toString());
        Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderInfo(qry);
        OrderDTO orderDTO = ResultDataUtils.getInstance(orderDTOResult).getDataOrException();
        if (null == orderDTO) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "订单查询失败");
        }

        // 交付单状态检查，交车电子合同签署状态应为0未签署，才能进行接下来的操作，如果检查
        deliverCheck(serveNos);

        // 验车信息获取
        List<Integer> vehicleIds = deliverImgInfos.stream().map(DeliverImgInfo::getCarId).collect(Collectors.toList());
        Result<List<VehicleValidationFullInfoDTO>> fullInfoDTOSResult = vehicleValidationAggregateRootApi.getVehicleValidationFullInfoDTOSByIds(vehicleIds);
        if(ResultErrorEnum.SUCCESSED.getCode() != fullInfoDTOSResult.getCode() || null == fullInfoDTOSResult.getData() || fullInfoDTOSResult.getData().isEmpty()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "验车信息获取失败");
        }
        // 验车信息需要验证是否和车辆id匹配，看看有没有缺
        List<VehicleValidationFullInfoDTO> fullInfoDTOS = fullInfoDTOSResult.getData();
        Map<Integer, VehicleValidationFullInfoDTO> fullInfoDTOMap = new HashMap<>();
        fullInfoDTOS.forEach(fullInfoDTO -> {
            fullInfoDTOMap.put(fullInfoDTO.getId(), fullInfoDTO);
        });

        // 数据收集操作应该前置，避免收集失败后回退本地合同状态、本地交接单状态、交付单状态
        List<ContractDocumentInfoDTO> docInfos = collectDataToCreateDocs(cmd, serveMap, fullInfoDTOMap);

        // 先本地创建合同和交接单
        ContractIdWithDocIds contractIdWithDocIds = createContractWithDocInLocal(cmd, tokenInfo, serveMap.get(deliverImgInfos.get(0).getServeNo()).getOrgId(), serveMap.get(deliverImgInfos.get(0).getServeNo()).getOrderId());

        // 访问契约锁域创建合同
        Result<Boolean> createElecContractResult = createElecContract(cmd, contractIdWithDocIds, docInfos, orderDTO);
        if(ResultErrorEnum.SUCCESSED.getCode() != createElecContractResult.getCode() || null == createElecContractResult.getData()){
            log.error("创建合同时调用契约锁域失败，返回msg：{}", createElecContractResult.getMsg());
            // 调用契约锁失败还得将本地创建的合同置为无效
            makeContractInvalid(contractIdWithDocIds.getContractId());
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "操作失败");
        }

        // 什么时候改变交付单的状态，调用完契约锁域后，免得失败后还得改回来
        try{
            makeDeliverContractGenerating(serveNos, DeliverTypeEnum.DELIVER.getCode());
        }catch (Exception e){
            // 操作交付单失败，合同应置为无效
            CancelContractCmd cancelContractCmd = new CancelContractCmd();
            cancelContractCmd.setContractId(contractIdWithDocIds.getContractId());
            cancelContractCmd.setFailureReason(ContractFailureReasonEnum.OTHER.getCode());
            contractAggregateRootApi.cancelContract(cancelContractCmd);
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建电子交接单失败");
        }

        HashMap<String, String> map = new HashMap<>();
        for (String serveNo : serveNos) {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        }
        return contractIdWithDocIds.getContractId().toString();
    }

    // 调用契约锁域创建合同
    private Result<Boolean> createElecContract(CreateDeliverContractCmd cmd, ContractIdWithDocIds contractIdWithDocIds, List<ContractDocumentInfoDTO> docInfos, OrderDTO orderDTO) {
        docInfos.forEach(docInfo -> {
            docInfo.setType(DeliverTypeEnum.DELIVER.getCode());
            docInfo.setElecDocId(contractIdWithDocIds.getDeliverNoWithDocId().get(docInfo.getDeliverNo()));
        });

        ContractDocumentDTO contractQysDocumentDTO = new ContractDocumentDTO();
        contractQysDocumentDTO.setElecContractId(contractIdWithDocIds.getContractId().toString());
        contractQysDocumentDTO.setUserName(cmd.getDeliverInfo().getContactsName());
        contractQysDocumentDTO.setPhone(cmd.getDeliverInfo().getContactsPhone());
        contractQysDocumentDTO.setDocumentInfoDTOList(docInfos);
        contractQysDocumentDTO.setType(ContractModeEnum.DELIVER.getName());
        contractQysDocumentDTO.setOrderContractId(orderDTO.getContractCode());

        System.out.println(JSONUtil.toJsonStr(contractQysDocumentDTO));
        return contractTools.create(contractQysDocumentDTO);
    }

    private void deliverCheck(List<String> serveNos) {
        Result<Map<String, Deliver>> deliversResult = deliverAggregateRootApi.getDeliverByServeNoList(serveNos);
        Map<String, Deliver> data = ResultDataUtils.getInstance(deliversResult).getDataOrException();
        data.values().forEach(deliver -> {
            if (DeliverContractStatusEnum.NOSIGN.getCode() != deliver.getDeliverContractStatus()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
            }
        });
    }

    // 收集各种数据去创建电子交接单对象
    private List<ContractDocumentInfoDTO> collectDataToCreateDocs(CreateDeliverContractCmd cmd, Map<String, Serve> serveMap, Map<Integer, VehicleValidationFullInfoDTO> fullInfoDTOMap){
        Map<Integer, Customer> customerMap = getCustomerMap(serveMap);
        List<DeliverImgInfo> deliverImgInfos = cmd.getDeliverImgInfos();

        List<ContractDocumentInfoDTO> docInfos = deliverImgInfos.stream().map(deliverImgInfo -> {
            ContractDocumentInfoDTO docInfo = new ContractDocumentInfoDTO();
            // 发车单信息补充
            BeanUtils.copyProperties(deliverImgInfo, docInfo);
            BeanUtils.copyProperties(cmd.getDeliverInfo(), docInfo);
            docInfo.setCarAndPersonPhoto(deliverImgInfo.getImgUrl());

            // 车辆信息、租赁方式等信息补充
            docInfo.setServeNo(deliverImgInfo.getServeNo());
            docInfo.setDeliverNo(deliverImgInfo.getDeliverNo());
            docInfo.setCustomerName(customerMap.get(serveMap.get(deliverImgInfo.getServeNo()).getCustomerId()).getName());
            docInfo.setLeaseModelType(leaseModeMap.get(serveMap.get(deliverImgInfo.getServeNo()).getLeaseModelId().toString()));

            Result<VehicleInfoDto> vehicleInfoDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverImgInfo.getCarId());
            if(ResultErrorEnum.SUCCESSED.getCode() != vehicleInfoDtoResult.getCode() || null == vehicleInfoDtoResult.getData()){
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "车辆信息查询失败");
            }
            VehicleInfoDto vehicleInfoDto = vehicleInfoDtoResult.getData();
            docInfo.setCarModel(vehicleTypeMap.get(vehicleInfoDto.getTypeId()));
            docInfo.setColor(vehicleColorMap.get(vehicleInfoDto.getColor().toString()));
            docInfo.setDeliverVehicleTime(FormatUtil.ymdFormatDateToString(cmd.getDeliverInfo().getDeliverVehicleTime()));
            docInfo.setPlateNumber(vehicleInfoDto.getPlateNumber());
            docInfo.setVin(vehicleInfoDto.getVin());

            // 订单联系人补充
            ReviewOrderQry qry = new ReviewOrderQry();
            qry.setId(serveMap.get(deliverImgInfo.getServeNo()).getOrderId().toString());
            Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderInfo(qry);
            if(ResultErrorEnum.SUCCESSED.getCode() != orderDTOResult.getCode() || null == orderDTOResult.getData()){
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "订单信息查询失败");
            }
            docInfo.setOrderContactsPhone(orderDTOResult.getData().getConsigneeMobile());

            // 验车信息补充
            // 问题和图片需要验证一下copy过去了没有
            VehicleValidationFullInfoDTO fullInfoDTO = fullInfoDTOMap.get(deliverImgInfo.getCarId());
            BeanUtils.copyProperties(fullInfoDTO, docInfo);
            docInfo.setMileage(fullInfoDTO.getMileage().toString());
            return docInfo;
        }).collect(Collectors.toList());
        return docInfos;
    }

    public void initDictData() {
        if(null == leaseModeMap){
            leaseModeMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "lease_mode");
        }
        if(null == vehicleColorMap){
            vehicleColorMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "vehicle_color");
        }
        if(null == vehicleTypeMap){
            Result<Map<Integer, String>> vehicleTypeResult = vehicleAggregateRootApi.getAllVehicleBrandTypeList();
            if(ResultErrorEnum.SUCCESSED.getCode() == vehicleTypeResult.getCode() && null != vehicleTypeResult.getData()){
                vehicleTypeMap = vehicleTypeResult.getData();
            }
        }
    }

    private ContractIdWithDocIds createContractWithDocInLocal(CreateDeliverContractCmd cmd, TokenInfo tokenInfo, Integer orgId, Long orderId) {
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setDeliverType(DeliverTypeEnum.DELIVER.getCode());
        cmd.setOrgId(orgId);
        cmd.setOrderId(orderId);
        Result<ContractIdWithDocIds> createContractResult = contractAggregateRootApi.createDeliverContract(cmd);
        if(ResultErrorEnum.SUCCESSED.getCode() != createContractResult.getCode() || null == createContractResult.getData()){
            // 前端创建时没有电子合同的概念
            log.error("创建合同失败，返回msg：{}", createContractResult.getMsg());
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建电子交接单失败");
        }
        return createContractResult.getData();
    }

    private Map<Integer, Customer> getCustomerMap(Map<String, Serve> serveMap) {
        List<Integer> customerId = serveMap.values().stream().map(Serve::getCustomerId).collect(Collectors.toList());
        Result<List<Customer>> customersResult = customerAggregateRootApi.getCustomerByIdList(customerId);
        if(ResultErrorEnum.SUCCESSED.getCode() != customersResult.getCode() || null == customersResult.getData()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "客户查询失败");
        }
        List<Customer> customers = customersResult.getData();
        return customers.stream().collect(Collectors.toMap(Customer::getId, Function.identity(), (v1, v2) -> v1));
    }

    private void makeDeliverContractGenerating(List<String> serveNos, int type) {
        DeliverContractGeneratingCmd cmd = new DeliverContractGeneratingCmd();
        cmd.setServeNos(serveNos);
        cmd.setDeliverType(type);
        Result<Integer> result = deliverAggregateRootApi.contractGenerating(cmd);
        ResultValidUtils.checkResultException(result);
    }

    private void makeContractInvalid(Long contractId) {
        CancelContractCmd cmd = new CancelContractCmd();
        cmd.setContractId(contractId);
        cmd.setFailureReason(ContractFailureReasonEnum.OTHER.getCode());
        contractAggregateRootApi.cancelContract(cmd);
    }
}
