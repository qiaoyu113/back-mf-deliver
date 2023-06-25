package com.mfexpress.rent.deliver.deliver.executor;


import cn.hutool.core.collection.CollectionUtil;
import com.hx.backmarket.insurance.domainapi.policy.insurance.InsurancePolicyBaseAggregateRootApi;
import com.hx.backmarket.insurance.dto.insurance.policy.data.dto.InsurancePolicyDTO;
import com.hx.backmarket.insurance.dto.insurance.policy.data.qry.InsurancePolicyIdsQry;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeliverToPreselectedExe {
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;
    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;
    @Resource
    private InsurancePolicyBaseAggregateRootApi insurancePolicyBaseAggregateRootApi;
    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;


    public String execute(DeliverPreselectedCmd deliverPreselectedCmd) {
        List<DeliverDTO> deliverList = new LinkedList<>();
        //服务单编号
        List<String> serveNoList = deliverPreselectedCmd.getServeList();
        //车辆信息
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = deliverPreselectedCmd.getDeliverVehicleSelectCmdList();
        if (serveNoList.size() != deliverVehicleSelectCmdList.size()) {
            log.error("服务单选中数量与预选车辆数量不符");
            throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), ResultErrorEnum.VILAD_ERROR.getName());
        }

        //车辆id list
        List<Integer> carIdList = deliverVehicleSelectCmdList.stream().map(DeliverVehicleSelectCmd::getId).collect(Collectors.toList());
        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(carIdList);
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();

        // 车辆id, 保单状态 map
        Map<Integer, Integer> insuranceCompulsoryPolicyStatusMap = new HashMap<>();
        Map<Integer, Integer> insuranceCommercialPolicyStatusMap = new HashMap<>();

        // 车辆id, 车辆保险信息 map
        Map<Integer, VehicleInsuranceDTO> vehicleInsuranceDTOMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(vehicleInsuranceDTOS)) {
            vehicleInsuranceDTOMap = vehicleInsuranceDTOS.stream().collect(Collectors.toMap(VehicleInsuranceDTO::getVehicleId, Function.identity(), (v1, v2) -> v1));

            // 交强险/商业险 保单id
            Set<Long> insuranceCompulsoryIdSet = vehicleInsuranceDTOS.stream().map(VehicleInsuranceDTO::getCompulsoryInsuranceId).collect(Collectors.toSet());
            Set<Long> insuranceCommercialIdSet = vehicleInsuranceDTOS.stream().map(VehicleInsuranceDTO::getCommercialInsuranceId).collect(Collectors.toSet());

            // 交强险
            if (CollectionUtil.isNotEmpty(insuranceCompulsoryIdSet)) {
                Result<List<InsurancePolicyDTO>> insuranceCompulsoryPolicyResult = insurancePolicyBaseAggregateRootApi.list(InsurancePolicyIdsQry.builder().policyIds(new ArrayList<>(insuranceCompulsoryIdSet)).build());
                if (ResultDataUtils.checkResultData(insuranceCompulsoryPolicyResult) && CollectionUtil.isNotEmpty(insuranceCompulsoryPolicyResult.getData())) {
                    List<InsurancePolicyDTO> insuranceCompulsoryPolicyDTOList = insuranceCompulsoryPolicyResult.getData();
                    insuranceCompulsoryPolicyDTOList.forEach(policy -> {
                        if (CollectionUtil.isEmpty(insuranceCompulsoryPolicyStatusMap)) {
                            insuranceCompulsoryPolicyStatusMap.put(Integer.valueOf(policy.getVehicleId().toString()), policy.getPolicyStatus());
                        }
                        Integer policyStatus = insuranceCompulsoryPolicyStatusMap.get(Integer.valueOf(policy.getVehicleId().toString()));
                        if (Objects.nonNull(policyStatus) && policy.getPolicyStatus() > policyStatus) {
                            insuranceCompulsoryPolicyStatusMap.put(Integer.valueOf(policy.getVehicleId().toString()), policy.getPolicyStatus());
                        }
                    });
                }
            }

            // 商业险
            if (CollectionUtil.isNotEmpty(insuranceCommercialIdSet)) {
                Result<List<InsurancePolicyDTO>> insuranceCommercialPolicyResult = insurancePolicyBaseAggregateRootApi.list(InsurancePolicyIdsQry.builder().policyIds(new ArrayList<>(insuranceCommercialIdSet)).build());
                if (ResultDataUtils.checkResultData(insuranceCommercialPolicyResult) && CollectionUtil.isNotEmpty(insuranceCommercialPolicyResult.getData())) {
                    List<InsurancePolicyDTO> insuranceCommercialPolicyDTOList = insuranceCommercialPolicyResult.getData();
                    insuranceCommercialPolicyDTOList.forEach(policy -> {
                        if (CollectionUtil.isEmpty(insuranceCommercialPolicyStatusMap)) {
                            insuranceCommercialPolicyStatusMap.put(Integer.valueOf(policy.getVehicleId().toString()), policy.getPolicyStatus());
                        }
                        Integer policyStatus = insuranceCommercialPolicyStatusMap.get(Integer.valueOf(policy.getVehicleId().toString()));
                        if (Objects.nonNull(policyStatus) && policy.getPolicyStatus() > policyStatus) {
                            insuranceCommercialPolicyStatusMap.put(Integer.valueOf(policy.getVehicleId().toString()), policy.getPolicyStatus());
                        }
                    });
                }
            }
        }

        // 重新激活的服务单在进行预选操作时需要的校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(deliverPreselectedCmd.getServeList()).build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);
        List<String> preSelectedServeNoList = new ArrayList<>();
        List<Integer> preVehicleIdList = new ArrayList<>();
        for (int i = 0; i < serveNoList.size(); i++) {
            DeliverDTO deliverDTO = new DeliverDTO();
            //已经生成交付单 不能重复预选
            deliverDTO.setServeNo(serveNoList.get(i));
            Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(deliverDTO.getServeNo());
            if (deliverResult.getData() != null) {
                continue;
            }
            Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(serveNoList.get(i));
            ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
            if (null == serveDTO) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单查询失败");
            }

            DeliverVehicleSelectCmd deliverVehicleSelectCmd = deliverVehicleSelectCmdList.get(i);

            // 保险相关信息
            if (CollectionUtil.isNotEmpty(vehicleInsuranceDTOMap) && Objects.nonNull(vehicleInsuranceDTOMap.get(deliverVehicleSelectCmd.getId()))) {
                VehicleInsuranceDTO vehicleInsuranceDTO = vehicleInsuranceDTOMap.get(deliverVehicleSelectCmd.getId());

                if (((PolicyStatusEnum.EFFECT.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || PolicyStatusEnum.ABOUT_EXPIRED.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) &&
                        (PolicyStatusEnum.EFFECT.getCode() == vehicleInsuranceDTO.getCommercialInsuranceStatus() || PolicyStatusEnum.ABOUT_EXPIRED.getCode() == vehicleInsuranceDTO.getCommercialInsuranceStatus())) ||
                        (PolicyStatusEnum.EFFECT.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || PolicyStatusEnum.ABOUT_EXPIRED.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) &&
                                LeaseModelEnum.SHOW.getCode() == serveDTO.getLeaseModelId()) {
                    // 车辆保险状态都是有效，设isInsurance为true
                    // 如果服务单租赁方式为展示，需要额外的判断逻辑，如果车辆的交强险有效，设isInsurance为true
                    deliverDTO.setIsInsurance(JudgeEnum.YES.getCode());
                    Result<VehicleInsuranceDto> vehicleInsuranceDtoResult = vehicleInsuranceAggregateRootApi.getVehicleInsuranceById(deliverVehicleSelectCmd.getId());
                    if (ResultDataUtils.checkResultData(vehicleInsuranceDtoResult)) {
                        deliverDTO.setInsuranceStartTime(DeliverUtils.getYYYYMMDDByString(vehicleInsuranceDtoResult.getData().getStartTime()));
                    }
                }
            }

            Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverVehicleSelectCmd.getId());
            if (vehicleResult.getCode() != 0 || vehicleResult.getData() == null) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleResult.getMsg());
            }
            VehicleInfoDto vehicleInfoDto = vehicleResult.getData();

            // 交强险退保状态判断
            if (CollectionUtil.isNotEmpty(insuranceCompulsoryPolicyStatusMap)) {
                Integer policyStatus = insuranceCompulsoryPolicyStatusMap.get(vehicleInfoDto.getVehicleId());
                if (Objects.nonNull(policyStatus) && Objects.equals(policyStatus, com.hx.backmarket.insurance.constant.policy.PolicyStatusEnum.SURRENDERING.getIndex())) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在退保事项，无法进行预选");
                }
            }
            // 商业险退保状态判断
            if (CollectionUtil.isNotEmpty(insuranceCommercialPolicyStatusMap)) {
                Integer policyStatus = insuranceCommercialPolicyStatusMap.get(vehicleInfoDto.getVehicleId());
                if (Objects.nonNull(policyStatus) && Objects.equals(policyStatus, com.hx.backmarket.insurance.constant.policy.PolicyStatusEnum.SURRENDERING.getIndex())) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在退保事项，无法进行预选");
                }
            }

            deliverDTO.setCarId(deliverVehicleSelectCmd.getId());
            deliverDTO.setCarNum(deliverVehicleSelectCmd.getPlateNumber());
            deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
            deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
            deliverDTO.setFrameNum(deliverVehicleSelectCmd.getVin());
            deliverDTO.setMileage(deliverVehicleSelectCmd.getMileage());
            deliverDTO.setVehicleAge(deliverVehicleSelectCmd.getVehicleAge());
            deliverDTO.setCustomerId(deliverPreselectedCmd.getCustomerId());
            deliverDTO.setCarServiceId(deliverPreselectedCmd.getCarServiceId());
            deliverDTO.setVehicleBusinessMode(vehicleInfoDto.getVehicleBusinessMode());
            deliverDTO.setCompulsoryPolicyId(deliverVehicleSelectCmd.getCompulsoryPolicyId());
            deliverDTO.setCommercialPolicyId(deliverVehicleSelectCmd.getCommercialPolicyId());
            deliverList.add(deliverDTO);
            //成功预选的服务单与车辆编号
            preSelectedServeNoList.add(serveNoList.get(i));
            preVehicleIdList.add(deliverVehicleSelectCmd.getId());
        }


        if (CollectionUtil.isNotEmpty(preVehicleIdList)) {
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(carIdList);
            vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.CHECKED.getCode());
            Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
            if (vehicleResult.getCode() != 0) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleResult.getMsg());
            }
        }

        if (CollectionUtil.isNotEmpty(preSelectedServeNoList)) {
            Result<String> serveResult = serveAggregateRootApi.toPreselected(serveNoList);
            if (serveResult.getCode() != 0) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), serveResult.getMsg());
            }
        }
        if (CollectionUtil.isEmpty(deliverList)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "所选服务单已经被他人预选请刷新页面");
        }
        Result<String> deliverResult = deliverAggregateRootApi.addDeliver(deliverList);
        if (deliverResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), deliverResult.getMsg());
        }
        //强同步es
        HashMap<String, String> map = new HashMap<>();
        for (String serveNo : serveNoList) {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        }

        return deliverResult.getData();
    }

}
