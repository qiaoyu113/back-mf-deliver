package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import cn.hutool.core.date.DateUtil;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.dto.contract.ContractDocumentDTO;
import com.mfexpress.component.dto.contract.ContractDocumentInfoDTO;
import com.mfexpress.component.enums.contract.ContractModeEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.contract.MFContractTools;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.DeliverTypeEnum;
import com.mfexpress.rent.deliver.domainapi.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverContractGeneratingCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverContractSigningCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ContractStatusChangeCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractFrontCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.utils.CommonUtil;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleValidationAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehiclevalidation.VehicleValidationFullInfoDTO;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class CreateRecoverContractCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private VehicleValidationAggregateRootApi vehicleValidationAggregateRootApi;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private MFContractTools contractTools;

    @Resource
    private ElecHandoverContractAggregateRootApi elecHandoverContractAggregateRootApi;

    @Resource
    private RecoverVehicleProcessCmdExe recoverVehicleProcessCmdExe;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    /**
     * 收车签署开关
     */
    @Value(value = "${contract.recover.flag}")
    private String contractRecoverFlag;

    private Map<String, String> leaseModeMap;

    private Map<String, String> vehicleColorMap;

    public Map<Integer, String> vehicleTypeMap;

    public String execute(CreateRecoverContractFrontCmd cmd, TokenInfo tokenInfo) {

        //收车日期校验
        checkDate(cmd);

        // 初始化字典数据
        initDictData();

        RecoverInfo recoverInfo = cmd.getRecoverInfo();
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(recoverInfo.getServeNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != serveDTOResult.getCode() || null == serveDTOResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单查询失败");
        }
        ServeDTO serveDTO = serveDTOResult.getData();

        // 收车日期不能在替换车发车日期之前
        ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, serveDTO.getServeNo());
        if (Optional.ofNullable(replaceVehicleDTO).map(ReplaceVehicleDTO::getServeNo).isPresent()) {
            // 查找替换车发车信息
            Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(replaceVehicleDTO.getServeNo());
            if (!Optional.ofNullable(deliverDTOResult).map(Result::getData).isPresent()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到交付单");
            }
            Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTOResult.getData().getDeliverNo());
            if (!Optional.ofNullable(deliverVehicleDTOResult).map(Result::getData).isPresent()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到发车单");
            }
            if (recoverInfo.getRecoverVehicleTime().before(deliverVehicleDTOResult.getData().getDeliverVehicleTime())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期小于替换车发车日期");
            }
        }


        ReviewOrderQry qry = new ReviewOrderQry();
        qry.setId(serveDTO.getOrderId().toString());
        Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderInfo(qry);
        OrderDTO orderDTO = ResultDataUtils.getInstance(orderDTOResult).getDataOrException();
        if (null == orderDTO) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "订单查询失败");
        }

        // 交付单状态检查，收车电子合同签署状态应为0未签署，才能进行接下来的操作，如果检查
        recoverCheck(serveDTO.getServeNo());

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(recoverInfo.getServeNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != deliverDTOResult.getCode() || null == deliverDTOResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单查询失败");
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();
        recoverInfo.setDeliverNo(deliverDTO.getDeliverNo());
        recoverInfo.setCarId(deliverDTO.getCarId());

        Result<List<VehicleValidationFullInfoDTO>> fullInfoDTOSResult = vehicleValidationAggregateRootApi.getVehicleValidationFullInfoDTOSByIds(Collections.singletonList(deliverDTO.getCarId()));
        if (ResultErrorEnum.SUCCESSED.getCode() != fullInfoDTOSResult.getCode() || null == fullInfoDTOSResult.getData() || fullInfoDTOSResult.getData().isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "验车信息查询失败");
        }
        VehicleValidationFullInfoDTO validationFullInfoDTO = fullInfoDTOSResult.getData().get(0);

        // 收车日期不能早于发车日期不在这里判断，应在收车电子合同创建时判断

        // 数据收集操作应该前置，避免收集失败后回退本地合同状态、本地交接单状态、交付单状态，顺带拼装一个domain层接收的cmd
        List<ContractDocumentInfoDTO> docInfos = collectDataToCreateDocs(cmd, serveDTO, deliverDTO, validationFullInfoDTO);

        // 先本地创建合同和交接单
        ContractIdWithDocIds contractIdWithDocIds = createContractWithDocInLocal(cmd, tokenInfo, serveDTO.getOrgId());

        if ("1".equals(contractRecoverFlag)) {
            // 访问契约锁域创建合同
            Result<Boolean> createElecContractResult = createElecContract(cmd, contractIdWithDocIds, docInfos, orderDTO);
            if (ResultErrorEnum.SUCCESSED.getCode() != createElecContractResult.getCode() || null == createElecContractResult.getData()) {
                log.error("创建合同时调用契约锁域失败，返回msg：{}", createElecContractResult.getMsg());
                // 调用契约锁失败还得将本地创建的合同置为无效
                makeContractInvalid(contractIdWithDocIds.getContractId());
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "操作失败");
            }
        }

        // 什么时候改变交付单的状态，调用完契约锁域后，免得失败后还得改回来
        try {
            makeDeliverContractGenerating(Collections.singletonList(recoverInfo.getServeNo()), DeliverTypeEnum.RECOVER.getCode());

        } catch (Exception e) {
            // 操作交付单失败，合同应置为无效
            CancelContractCmd cancelContractCmd = new CancelContractCmd();
            cancelContractCmd.setContractId(contractIdWithDocIds.getContractId());
            cancelContractCmd.setFailureReason(ContractFailureReasonEnum.OTHER.getCode());
            contractAggregateRootApi.cancelContract(cancelContractCmd);
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建电子交接单失败");
        }

        // 增加电子交接单开关
        log.info("contractRecoverFlag---->{}", contractRecoverFlag);
        if ("0".equals(contractRecoverFlag)) {

            Result<ElecContractDTO> contractDTOResult = elecHandoverContractAggregateRootApi.getContractDTOByDeliverNoAndDeliverType(deliverDTO.getDeliverNo(), DeliverTypeEnum.RECOVER.getCode());
            ElecContractDTO elecContractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();

            ContractStatusChangeCmd contractStatusChangeCmd = new ContractStatusChangeCmd();
            contractStatusChangeCmd.setContractId(elecContractDTO.getContractId());
            contractStatusChangeCmd.setContractForeignNo(elecContractDTO.getContractShowNo());

            contractAggregateRootApi.autoCompleted(contractStatusChangeCmd);

            // 修改交付单的收车签署状态
            DeliverContractSigningCmd signingCmd = new DeliverContractSigningCmd();
            signingCmd.setDeliverNos(Collections.singletonList(deliverDTO.getDeliverNo()));
            signingCmd.setDeliverType(DeliverTypeEnum.RECOVER.getCode());
            deliverAggregateRootApi.contractSigning(signingCmd);

            recoverVehicleProcessCmdExe.execute(recoverVehicleProcessCmdExe.turnToCmd(elecContractDTO, deliverDTO, serveDTO));
        }
        return contractIdWithDocIds.getContractId().toString();
    }

    private void recoverCheck(String serveNo) {
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();

        if (DeliverContractStatusEnum.NOSIGN.getCode() != deliverDTO.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "选中车辆存在电子交接单，请回列表页查看");
        }
    }

    private Result<Boolean> createElecContract(CreateRecoverContractFrontCmd cmd, ContractIdWithDocIds contractIdWithDocIds, List<ContractDocumentInfoDTO> docInfos, OrderDTO orderDTO) {
        // 再调用契约锁域创建合同
        docInfos.forEach(docInfo -> {
            docInfo.setType(DeliverTypeEnum.RECOVER.getCode());
            docInfo.setElecDocId(contractIdWithDocIds.getDeliverNoWithDocId().get(docInfo.getDeliverNo()));
        });
        ContractDocumentDTO contractDocumentDTO = new ContractDocumentDTO();
        contractDocumentDTO.setElecContractId(contractIdWithDocIds.getContractId().toString());
        contractDocumentDTO.setUserName(cmd.getRecoverInfo().getContactsName());
        contractDocumentDTO.setPhone(cmd.getRecoverInfo().getContactsPhone());
        contractDocumentDTO.setDocumentInfoDTOList(docInfos);
        contractDocumentDTO.setType(ContractModeEnum.DELIVER.getName());
        contractDocumentDTO.setOrderContractId(orderDTO.getOaContractCode());

        return contractTools.create(contractDocumentDTO);
    }

    private List<ContractDocumentInfoDTO> collectDataToCreateDocs(CreateRecoverContractFrontCmd cmd, ServeDTO serveDTO, DeliverDTO deliverDTO, VehicleValidationFullInfoDTO validationFullInfoDTO) {
        // 数据收集
        // 客户名称
        Result<Customer> customerResult = customerAggregateRootApi.getCustomerById(serveDTO.getCustomerId());
        if (ResultErrorEnum.SUCCESSED.getCode() != customerResult.getCode() || null == customerResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "客户查询失败");
        }
        Customer customer = customerResult.getData();

        // 契约搜对象拼装
        ContractDocumentInfoDTO docInfo = new ContractDocumentInfoDTO();
        // 发车单信息补充
        BeanUtils.copyProperties(cmd.getRecoverInfo(), docInfo);
        docInfo.setCarAndPersonPhoto(cmd.getRecoverInfo().getImgUrl());

        // 车辆信息、租赁方式等信息补充
        docInfo.setServeNo(deliverDTO.getServeNo());
        docInfo.setDeliverNo(deliverDTO.getDeliverNo());
        docInfo.setCustomerName(customer.getName());
        docInfo.setLeaseModelType(leaseModeMap.get(serveDTO.getLeaseModelId().toString()));

        ReviewOrderQry qry = new ReviewOrderQry();
        qry.setId(serveDTO.getOrderId().toString());
        Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderInfo(qry);
        OrderDTO orderDTO = ResultDataUtils.getInstance(orderDTOResult).getDataOrException();
        docInfo.setOrderContactsPhone(orderDTO.getConsigneeMobile());

        Result<VehicleInfoDto> vehicleInfoDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverDTO.getCarId());
        if (ResultErrorEnum.SUCCESSED.getCode() != vehicleInfoDtoResult.getCode() || null == vehicleInfoDtoResult.getData()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "车辆信息查询失败");
        }
        VehicleInfoDto vehicleInfoDto = vehicleInfoDtoResult.getData();
        docInfo.setCarModel(vehicleTypeMap.get(vehicleInfoDto.getTypeId()));

        docInfo.setColor(vehicleColorMap.get(vehicleInfoDto.getColor().toString()));
        docInfo.setDeliverVehicleTime(FormatUtil.ymdFormatDateToString(cmd.getRecoverInfo().getRecoverVehicleTime()));
        docInfo.setPlateNumber(vehicleInfoDto.getPlateNumber());
        docInfo.setVin(vehicleInfoDto.getVin());

        cmd.getRecoverInfo().setCarNum(vehicleInfoDto.getPlateNumber());
        cmd.setOrderId(orderDTO.getOrderId());

        // 验车信息补充
        // 问题和图片需要验证一下copy过去了没有
        BeanUtils.copyProperties(validationFullInfoDTO, docInfo);
        docInfo.setMileage(validationFullInfoDTO.getMileage().toString());
        return Collections.singletonList(docInfo);
    }

    private ContractIdWithDocIds createContractWithDocInLocal(CreateRecoverContractFrontCmd cmd, TokenInfo tokenInfo, Integer orgId) {
        // 组合成原对象继续往下走 需要吗？主要是domain层得改
        Date recoverVehicleTime = cmd.getRecoverInfo().getRecoverVehicleTime();
        Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(cmd.getRecoverInfo().getDeliverNo());
        DeliverVehicleDTO deliverVehicleDTO = ResultDataUtils.getInstance(deliverVehicleDTOResult).getDataOrException();
        Date deliverVehicleTime = deliverVehicleDTO.getDeliverVehicleTime();
        if (recoverVehicleTime.before(deliverVehicleTime)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期不能早于发车日期");
        }

        // 查询完成的维修单
        Result<MaintenanceDTO> maintainResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(cmd.getRecoverInfo().getServeNo());
        if (ResultValidUtils.checkResult(maintainResult)) {
            MaintenanceDTO maintenanceDTO = maintainResult.getData();
            //格式化为yyyy-MM-dd
            String confirmDate = DateUtil.formatDate(maintenanceDTO.getConfirmDate());
            if (recoverVehicleTime.before(DateUtil.parseDate(confirmDate))) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "收车日期请晚于维修交车日期");
            }
        }
//        DateTime endDate = DateUtil.endOfMonth(new Date());
//        DateTime startDate = DateUtil.beginOfMonth(new Date());
        //增加收车日期限制
//        if (!endDate.isAfter(recoverVehicleTime) || recoverVehicleTime.before(startDate)) {
//            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "收车日期请选择在当月内");
//        }

        CreateRecoverContractCmd createRecoverContractCmd = new CreateRecoverContractCmd();

        DeliverImgInfo deliverImgInfo = new DeliverImgInfo();
        RecoverInfo recoverInfo = cmd.getRecoverInfo();
        deliverImgInfo.setServeNo(recoverInfo.getServeNo());
        deliverImgInfo.setDeliverNo(recoverInfo.getDeliverNo());
        deliverImgInfo.setImgUrl(recoverInfo.getImgUrl());
        deliverImgInfo.setCarId(recoverInfo.getCarId());
        deliverImgInfo.setCarNum(recoverInfo.getCarNum());

        createRecoverContractCmd.setDeliverImgInfos(Collections.singletonList(deliverImgInfo));
        createRecoverContractCmd.setOperatorId(tokenInfo.getId());
        createRecoverContractCmd.setDeliverType(DeliverTypeEnum.RECOVER.getCode());
        createRecoverContractCmd.setOrgId(orgId);
        createRecoverContractCmd.setRecoverInfo(recoverInfo);
        createRecoverContractCmd.setOrderId(cmd.getOrderId());

        Result<ContractIdWithDocIds> createContractResult = contractAggregateRootApi.createRecoverContract(createRecoverContractCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != createContractResult.getCode() || null == createContractResult.getData()) {
            // 前端创建时没有电子合同的概念
            log.error("创建合同失败，返回msg：{}", createContractResult.getMsg());
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建电子交接单失败");
        }
        return createContractResult.getData();
    }

    private void makeDeliverContractGenerating(List<String> serveNos, int type) {
        DeliverContractGeneratingCmd cmd = new DeliverContractGeneratingCmd();
        cmd.setServeNos(serveNos);
        cmd.setDeliverType(type);
        deliverAggregateRootApi.contractGenerating(cmd);
    }

    private void makeContractInvalid(Long contractId) {
        CancelContractCmd cmd = new CancelContractCmd();
        cmd.setContractId(contractId);
        cmd.setFailureReason(ContractFailureReasonEnum.OTHER.getCode());
        contractAggregateRootApi.cancelContract(cmd);
    }

    private void initDictData() {
        if (null == leaseModeMap) {
            leaseModeMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "lease_mode");
        }
        if (null == vehicleColorMap) {
            vehicleColorMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "vehicle_color");
        }
        if (null == vehicleTypeMap) {
            Result<Map<Integer, String>> vehicleTypeResult = vehicleAggregateRootApi.getAllVehicleBrandTypeList();
            if (ResultErrorEnum.SUCCESSED.getCode() == vehicleTypeResult.getCode() && null != vehicleTypeResult.getData()) {
                vehicleTypeMap = vehicleTypeResult.getData();
            }
        }
    }


    private void checkDate(CreateRecoverContractFrontCmd cmd) {

        Date recoverVehicleTime = cmd.getRecoverInfo().getRecoverVehicleTime();
        Date tSubNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), -DeliverProjectProperties.RECOVER_TIME_RANGE.getPre());
        Date tAddNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), DeliverProjectProperties.RECOVER_TIME_RANGE.getSuf());
        if (recoverVehicleTime.before(tSubNDate) ||
                recoverVehicleTime.after(tAddNDate)) {
            throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期超出可选范围");
        }

        // 查询完成的维修单
        Result<MaintenanceDTO> maintenanceByServeNo = maintenanceAggregateRootApi.getMaintenanceByServeNo(cmd.getRecoverInfo().getServeNo());
        if (ResultValidUtils.checkResult(maintenanceByServeNo)) {
            if (cmd.getRecoverInfo().getRecoverVehicleTime().before(maintenanceByServeNo.getData().getConfirmDate())) {
                log.info("收车日期超出可选范围  参数:{}", cmd);
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期不可小于维修交车日期");
            }
        }

        Result<DeliverVehicleDTO> deliverVehicleDto = deliverVehicleAggregateRootApi.getDeliverVehicleDto(cmd.getRecoverInfo().getDeliverNo());
        if (ResultValidUtils.checkResult(deliverVehicleDto)) {
            if (cmd.getRecoverInfo().getRecoverVehicleTime().before(deliverVehicleDto.getData().getDeliverVehicleTime())) {
                log.info("收车日期超出可选范围  参数:{}", cmd);
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期不可小于发车日期");
            }
        }

    }
}
