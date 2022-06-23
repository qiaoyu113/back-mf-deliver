package com.mfexpress.rent.deliver.consumer.common;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RenewalCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.contract.ContractResultTopicDTO;
import com.mfexpress.component.enums.contract.ContractFailTypeEnum;
import com.mfexpress.component.enums.contract.ContractStatusEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.*;
import com.mfexpress.rent.deliver.dto.data.daily.DailyOperateCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverContractSigningCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ContractStatusChangeCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.serve.RenewalChargeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeDepositPayCmd;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd.DeliverVehicleProcessCmdExe;
import com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd.RecoverVehicleProcessCmdExe;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenContractTopic")
@Slf4j
public class ElecContractStatusMqCommand {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private DeliverVehicleProcessCmdExe deliverVehicleProcessCmdExe;

    @Resource
    private RecoverVehicleProcessCmdExe recoverVehicleProcessCmdExe;

    @Resource
    private ServeServiceI serveServiceI;
    @Resource
    private ContractAggregateRootApi orderContractAggregateRootApi;

    private MqTools mqTools;

    @Resource
    private BeanFactory beanFactory;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    private final String contractSignedRedisKey = "lock:mf-deliver:contractSigned:contractId:";

    @Resource
    private RedisLockRegistry redisLockRegistry;

    @MFMqCommonProcessMethod(tag = Constants.THIRD_PARTY_ELEC_CONTRACT_STATUS_TAG)
    public void execute(String body) {
        log.info("mq中的合同状态信息：{}", body);
        if (null == mqTools) {
            mqTools = beanFactory.getBean(MqTools.class);
        }
        ContractResultTopicDTO contractStatusInfo = JSONUtil.toBean(body, ContractResultTopicDTO.class);
        if (ContractStatusEnum.CREATING.getValue().equals(contractStatusInfo.getStatus())) {
            // 补全三方合同编号
            ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
            cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
            cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
            contractAggregateRootApi.completionContractForeignNo(cmd);
        } else if (ContractStatusEnum.SIGNING.getValue().equals(contractStatusInfo.getStatus())) {
            // 合同状态改为 已创建/签署中,并补全三方合同编号；交付单中的合同状态也需改为已创建/签署中
            contractSigning(contractStatusInfo);
        } else if (ContractStatusEnum.COMPLETE.getValue().equals(contractStatusInfo.getStatus())) {
            // 合同状态改为完成
            // 加锁避免重复回调
            Lock obtain = this.redisLockRegistry.obtain(StringUtils.join(contractSignedRedisKey, ":", contractStatusInfo.getThirdPartContractId()));
            obtain.lock();
            try {
                contractCompleted(contractStatusInfo);

            } finally {
                obtain.unlock();
            }
        } else if (ContractStatusEnum.EXPIRED.getValue().equals(contractStatusInfo.getStatus())) {
            // 合同状态改为失败，失败原因为过期；不需要更新交付单状态，因为失败原因为过期的话需要用户确认后才会去改变交付单的状态
            ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
            cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
            cmd.setFailureReason(ContractFailureReasonEnum.OVERDUE.getCode());
            contractAggregateRootApi.fail(cmd);
        } else if (ContractStatusEnum.FAIL.getValue().equals(contractStatusInfo.getStatus())) {
            // 只会在合同生成中才会有此命令，合同置为失效，交付单置为未签状态
            contractFail(contractStatusInfo);
        }
    }

    private void contractFail(ContractResultTopicDTO contractStatusInfo) {
        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
        cmd.setFailureReason(ContractFailureReasonEnum.CREATE_FAIL.getCode());
        if (ContractFailTypeEnum.AUTH_ERROR.getCode().equals(contractStatusInfo.getFailType())) {
            cmd.setFailureMsg("填写姓名与客户实名手机号不一致");
        }
        Result<Integer> failResult = contractAggregateRootApi.fail(cmd);
        ResultValidUtils.checkResultException(failResult);

        // 当合同在创建中失败时，交付单状态不改变，需用户确认后才改变
        //deliverAggregateRootApi.makeNoSignByDeliverNo(contractDTO.getDeliverNos(), contractDTO.getDeliverType());
    }

    private void contractSigning(ContractResultTopicDTO contractStatusInfo) {
        Long contractId = Long.valueOf(contractStatusInfo.getLocalContractId());
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(contractId);
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        if (null == contractDTO) {
            log.error("收车电子合同创建完成时，根据本地合同id查询合同失败，本地合同id：{}", contractId);
            return;
        }

        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        cmd.setContractId(contractId);
        Result<Integer> contractSigningResult = contractAggregateRootApi.signing(cmd);
        ResultValidUtils.checkResultException(contractSigningResult);

        DeliverContractSigningCmd signingCmd = new DeliverContractSigningCmd();
        signingCmd.setDeliverNos(JSONUtil.toList(contractDTO.getDeliverNos(), String.class));
        signingCmd.setDeliverType(contractDTO.getDeliverType());
        deliverAggregateRootApi.contractSigning(signingCmd);
    }

    // 合同状态为已完成后触发的后续操作
    private void contractCompleted(ContractResultTopicDTO contractStatusInfo) {
        // 数据准备
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByForeignNo(contractStatusInfo.getThirdPartContractId());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        String deliverNo = JSONUtil.toList(contractDTO.getDeliverNos(), String.class).get(0);

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(deliverNo);
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
        cmd.setDocPdfUrlMap(contractStatusInfo.getDocUrlMapping());
        Result<Integer> completedResult = contractAggregateRootApi.completed(cmd);
        ResultValidUtils.checkResultException(completedResult);

//        List<String> serveNoList;
        if (DeliverTypeEnum.DELIVER.getCode() == contractDTO.getDeliverType()) {
            // 发车处理
            deliverVehicleProcessCmdExe.execute(deliverVehicleProcessCmdExe.turnToCmd(deliverDTO, contractDTO));
//            serveNoList = deliverVehicleProcess(serveDTO, contractDTO);
        } else {
            // 收车处理
            recoverVehicleProcessCmdExe.execute(recoverVehicleProcessCmdExe.turnToCmd(contractDTO, deliverDTO, serveDTO));
//            serveNoList = recoverVehicleProcess(serveDTO, deliverDTO, contractStatusInfo, contractDTO);
        }

        //同步
//        Map<String, String> map = new HashMap<>();
//        serveNoList.forEach(serveNo -> {
//            map.put("serve_no", serveNo);
//            serveSyncServiceI.execOne(map);
//        });
    }

    @Deprecated
    private List<String> recoverVehicleProcess(ServeDTO serveDTO, DeliverDTO deliverDTO, ContractResultTopicDTO contractStatusInfo, ElecContractDTO contractDTO) {
        List<String> serveNoList = new LinkedList<>();
        // 交付单、服务单修改
        Result<Integer> recoveredResult = recoverVehicleAggregateRootApi.recovered(deliverDTO.getDeliverNo(), contractStatusInfo.getThirdPartContractId());
        ResultValidUtils.checkResultException(recoveredResult);

        //更新车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(deliverDTO.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(contractDTO.getRecoverWareHouseId());
        vehicleSaveCmd.setCustomerId(0);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
        if (wareHouseResult.getData() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != changeVehicleStatusResult.getCode()) {
            log.error("收车电子合同签署完成时，更改车辆状态失败，serveNo：{}，车辆id：{}", serveDTO.getServeNo(), deliverDTO.getCarId());
        }
        Result<CommodityDTO> commodityResult = orderContractAggregateRootApi.getCommodityById(serveDTO.getContractCommodityId());
        if (!Objects.nonNull(commodityResult.getData())) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到商品信息");
        }
        // 发送收车信息到mq，由合同域判断服务单所属的合同是否到已履约完成状态
        // 租赁服务单1.1迭代，改为当服务单状态到已完成时，再向合同域发送此消息
        /*if (JudgeEnum.YES.getCode().equals(serveDTO.getReplaceFlag())) {
            ServeDTO serveDTOToNoticeContract = new ServeDTO();
            serveDTOToNoticeContract.setServeNo(serveDTO.getServeNo());
            serveDTOToNoticeContract.setOaContractCode(serveDTO.getOaContractCode());
            serveDTOToNoticeContract.setGoodsId(serveDTO.getGoodsId());
            serveDTOToNoticeContract.setCarServiceId(contractDTO.getCreatorId());
            serveDTOToNoticeContract.setRenewalType(serveDTO.getRenewalType());
            log.info("正常收车时，交付域向合同域发送的收车单信息：{}", serveDTOToNoticeContract);
            mqTools.send(event, "recover_serve_to_contract", null, JSON.toJSONString(serveDTOToNoticeContract));
        }*/

        // 判断实际收车日期和预计收车日期的前后关系，如果实际收车日期在预计收车日期之前或当天，发送收车计费消息，反之，发送自动续约消息
        Date recoverVehicleTime = contractDTO.getRecoverVehicleTime();
        String expectRecoverDateChar = serveDTO.getExpectRecoverDate();
        DateTime expectRecoverDate = DateUtil.parseDate(expectRecoverDateChar);
        // 发送收车计费消息
        if (expectRecoverDate.isAfterOrEquals(recoverVehicleTime)) {
            //收车计费
            RecoverVehicleCmd recoverVehicleCmd = new RecoverVehicleCmd();
            recoverVehicleCmd.setServeNo(serveDTO.getServeNo());
            recoverVehicleCmd.setVehicleId(deliverDTO.getCarId());
            recoverVehicleCmd.setDeliverNo(deliverDTO.getDeliverNo());
            recoverVehicleCmd.setCustomerId(serveDTO.getCustomerId());
            recoverVehicleCmd.setCreateId(contractDTO.getCreatorId());
            recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(contractDTO.getRecoverVehicleTime()));
            recoverVehicleCmd.setRentRatio(commodityResult.getData().getRentRatio());
            log.info("正常收车时，交付域向计费域发送的收车单信息：{}", recoverVehicleCmd);
            mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));

            // 服务单维修中
            if (ServeEnum.REPAIR.getCode().equals(serveDTO.getStatus())) {
                // 查找维修单
                MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceByServeNo(maintenanceAggregateRootApi, serveDTO.getServeNo());
                // 维修性质为故障维修
                if (MaintenanceTypeEnum.FAULT.getCode().intValue() == maintenanceDTO.getType()) {
                    // 查询替换车服务单
                    ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, serveDTO.getServeNo());
                    if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {
                        Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceVehicleDTO.getServeNo());
                        ServeDTO replaceServe = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();
                        // 替换单已发车且变更为正常服务单
                        if (Optional.ofNullable(replaceServe)
                                .filter(o -> ServeEnum.DELIVER.getCode().equals(o.getStatus())
                                        && JudgeEnum.NO.getCode().equals(o.getReplaceFlag())
                                        && LeaseModelEnum.NORMAL.getCode() == o.getLeaseModelId()).isPresent()) {
                            // 原车维修单变为库存中维修
                            maintenanceAggregateRootApi.updateMaintenanceDetailByServeNo(serveDTO.getServeNo());
                            // 替换车押金支付
                            // 查询替换单支付方式
                            ServeAdjustQry qry = new ServeAdjustQry();
                            qry.setServeNo(replaceServe.getServeNo());
                            Result<ServeAdjustDTO> serveAdjustDTOResult = serveAggregateRootApi.getServeAdjust(qry);

                            ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(serveAdjustDTOResult).getDataOrException();

                            if (DepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode() == serveAdjustDTO.getDepositPayType()) {
                                // 账本扣除
                                ServeDepositPayCmd serveDepositPayCmd = new ServeDepositPayCmd();
                                serveDepositPayCmd.setPayAbleDepositAmount(replaceServe.getDeposit());
                                serveDepositPayCmd.setPaidInDepositAmount(replaceServe.getPaidInDeposit());
                                serveDepositPayCmd.setServeNo(replaceServe.getServeNo());
                                serveDepositPayCmd.setOrderId(replaceServe.getOrderId());
                                serveDepositPayCmd.setCustomerId(replaceServe.getCustomerId());
                                serveDepositPayCmd.setOperatorId(contractDTO.getCreatorId());
                                serveDepositPayCmd.setDepositPayType(DepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode());

                                serveServiceI.serveDepositPay(serveDepositPayCmd);
                            }

                            // 替换车开始计费
                            Result<DeliverDTO> replaceDeliverResult = deliverAggregateRootApi.getDeliverByServeNo(replaceServe.getServeNo());
                            DeliverDTO replaceDeliver = ResultDataUtils.getInstance(replaceDeliverResult).getDataOrException();
                            RenewalCmd renewalCmd = new RenewalCmd();
                            renewalCmd.setServeNo(replaceServe.getServeNo());
                            renewalCmd.setDeliverNo(replaceDeliver.getDeliverNo());
                            renewalCmd.setVehicleId(replaceDeliver.getCarId());
                            renewalCmd.setCustomerId(replaceServe.getCustomerId());
                            renewalCmd.setRent(replaceServe.getRent());
                            renewalCmd.setRentRatio(replaceServe.getRentRatio().doubleValue());

//                        renewalCmd.setRenewalDate();
                            renewalCmd.setCreateId(contractDTO.getCreatorId());
                            renewalCmd.setRentEffectDate(FormatUtil.ymdFormatDateToString(new Date()));
                            renewalCmd.setEffectFlag(true);
                            mqTools.send(event, "price_change", null, JSON.toJSONString(renewalCmd));
                        }
                    }
                }
            }
        } else {
            // 发送自动续约消息
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(serveDTO.getServeNo());
            renewalChargeCmd.setCreateId(contractDTO.getCreatorId());
            renewalChargeCmd.setCustomerId(serveDTO.getCustomerId());
            renewalChargeCmd.setDeliverNo(deliverDTO.getDeliverNo());
            renewalChargeCmd.setVehicleId(deliverDTO.getCarId());
            renewalChargeCmd.setEffectFlag(false);
            renewalChargeCmd.setRentRatio(commodityResult.getData().getRentRatio());
            // 续约目标日期为实际收车日期
            renewalChargeCmd.setRenewalDate(DateUtil.formatDate(recoverVehicleTime));
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
        }

        serveNoList.add(serveDTO.getServeNo());
        //操作日报
        createDaily(serveNoList, contractDTO.getRecoverVehicleTime(), false);
        return serveNoList;
    }

    @Deprecated
    private List<String> deliverVehicleProcess(ServeDTO serveDTO, ElecContractDTO contractDTO) {
        // 数据收集
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        List<String> serveNoList = deliverImgInfos.stream().map(DeliverImgInfo::getServeNo).collect(Collectors.toList());
        Result<List<ServeDTO>> serveDTOListResult = serveAggregateRootApi.getServeDTOByServeNoList(serveNoList);
        if (serveDTOListResult.getCode() != 0 || null == serveDTOListResult.getData() || serveDTOListResult.getData().isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息不存在");
        }
        List<ServeDTO> serveDTOList = serveDTOListResult.getData();
        List<Integer> commodityIds = serveDTOList.stream().map(ServeDTO::getContractCommodityId).distinct().collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = orderContractAggregateRootApi.getCommodityListByIdList(commodityIds);
        if (commodityListResult.getCode() != 0 || null == commodityListResult.getData() || commodityListResult.getData().isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "商品信息不存在");
        }
        Map<Integer, CommodityDTO> commodityDTOMap = commodityListResult.getData().stream().collect(Collectors.toMap(CommodityDTO::getId, a -> a));
        Map<String, ServeDTO> serveDTOMap = serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (v1, v2) -> v1));
        //每个服务单对应的预计收车日期
        Map<String, String> expectRecoverDateMap = new HashMap<>(serveDTOList.size());
        for (String serveNo : serveNoList) {
            ServeDTO serve = serveDTOMap.get(serveNo);
            //替换车使用维修车的预计收车日期，重新激活的服务单不更新预计收车日期
            if (!JudgeEnum.YES.getCode().equals(serve.getReplaceFlag()) && !JudgeEnum.YES.getCode().equals(serve.getReactiveFlag())) {
                String expectRecoverDate = getExpectRecoverDate(contractDTO.getDeliverVehicleTime(), serve.getLeaseMonths(), serve.getLeaseDays());
                expectRecoverDateMap.put(serveNo, expectRecoverDate);
            }
        }
        contractDTO.setExpectRecoverDateMap(expectRecoverDateMap);
        //生成发车单 交付单状态更新已发车并初始化操作状态  服务单状态更新为已发车
        Result<Integer> result = deliverVehicleAggregateRootApi.deliverVehicles(contractDTO);
        ResultValidUtils.checkResultException(result);
        List<Integer> carIdList = new LinkedList<>();
        deliverImgInfos.forEach(deliverImgInfo -> {
            carIdList.add(deliverImgInfo.getCarId());
            //发车操作mq触发计费
            ServeDTO serve = serveDTOMap.get(deliverImgInfo.getServeNo());
            DeliverVehicleCmd rentChargeCmd = new DeliverVehicleCmd();
            rentChargeCmd.setServeNo(deliverImgInfo.getServeNo());
            rentChargeCmd.setDeliverNo(deliverImgInfo.getDeliverNo());
            rentChargeCmd.setRent(serve.getRent());
            String expectRecoverDate = expectRecoverDateMap.get(deliverImgInfo.getServeNo());
            if (Objects.isNull(expectRecoverDate)) {
                //替换车使用原车的预计收车日期作为计费截止日期，重新激活服务单使用原来的预计收车日期作为计费截止日期
                rentChargeCmd.setExpectRecoverDate(serve.getExpectRecoverDate());
            } else {
                rentChargeCmd.setExpectRecoverDate(expectRecoverDate);
            }
            rentChargeCmd.setDeliverFlag(true);
            rentChargeCmd.setCustomerId(serve.getCustomerId());
            rentChargeCmd.setCreateId(contractDTO.getCreatorId());
            rentChargeCmd.setVehicleId(deliverImgInfo.getCarId());
            rentChargeCmd.setDeliverDate(DateUtil.formatDate(contractDTO.getDeliverVehicleTime()));
            CommodityDTO commodityDTO = commodityDTOMap.get(serve.getContractCommodityId());
            if (Objects.nonNull(commodityDTO)) {
                rentChargeCmd.setRentRatio(commodityDTO.getRentRatio());
            }
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(rentChargeCmd));
        });

        // 修改对应的车辆状态为租赁状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(serveDTO.getCustomerId());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(serveDTO.getCustomerId());
        if (customerResult.getData() != null) {
            vehicleSaveCmd.setAddress(customerResult.getData().getName());
        }
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != vehicleResult.getCode()) {
            log.error("发车电子合同签署完成时，更改车辆状态失败。车辆id：{}", carIdList);
        }
        //生成日报
        createDaily(serveNoList, contractDTO.getDeliverVehicleTime(), true);
        return serveNoList;
    }

    private String getExpectRecoverDate(Date deliverVehicleDate, Integer offsetMonths, Integer offsetDays) {
        DateTime dateTime = DateUtil.endOfMonth(deliverVehicleDate);
        String deliverDate = DateUtil.formatDate(deliverVehicleDate);
        String endDate = DateUtil.formatDate(dateTime);
        if (deliverDate.equals(endDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.endOfMonth(dateTime));
            if (null != offsetMonths) {
                calendar.add(Calendar.MONTH, offsetMonths);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            if (null != offsetDays) {
                // offsetDays -= 1;
                if (offsetDays > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, offsetDays);
                }
            }
            return DateUtil.formatDate(calendar.getTime());
        } else {
            if (null != offsetMonths) {
                deliverVehicleDate = DateUtil.offsetMonth(deliverVehicleDate, offsetMonths);
            }
            if (null != offsetDays) {
                deliverVehicleDate = DateUtil.offsetDay(deliverVehicleDate, offsetDays);
            }
            return DateUtil.formatDate(deliverVehicleDate);
        }
    }


    private void createDaily(List<String> serveNoList, Date date, boolean deliverFlag) {
        Result<Map<String, Serve>> serveResult = serveAggregateRootApi.getServeMapByServeNoList(serveNoList);
        Map<String, Serve> serveMap = serveResult.getData();
        List<Serve> serveList = serveMap.values().stream().collect(Collectors.toList());
        Result<Map<String, Deliver>> deliverResult = deliverAggregateRootApi.getDeliverByServeNoList(serveNoList);
        Map<String, Deliver> deliverMap = deliverResult.getData();
        DailyOperateCmd dailyCreateCmd = DailyOperateCmd.builder().serveList(serveList).deliverMap(deliverMap).date(date).build();
        //发车标识
        if (deliverFlag) {
            //发车
            dailyAggregateRootApi.deliverDaily(dailyCreateCmd);
        } else {
            //收车
            dailyAggregateRootApi.recoverDaily(dailyCreateCmd);
        }
    }
}
