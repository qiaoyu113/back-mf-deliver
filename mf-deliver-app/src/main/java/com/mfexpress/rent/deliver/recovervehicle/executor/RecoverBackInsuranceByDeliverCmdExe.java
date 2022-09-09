package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateSurrenderApplyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.RecoverBatchSurrenderApplyDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.vo.SurrenderApplyInfoVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.vo.SurrenderApplyVO;
import com.mfexpress.rent.deliver.util.ExternalRequestUtil;
import com.mfexpress.rent.deliver.utils.CommonUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecoverBackInsuranceByDeliverCmdExe {

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ExternalRequestUtil externalRequestUtil;

    private Map<String, String> insuranceCompanyDictMap;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    public SurrenderApplyVO execute(RecoverBackInsureByDeliverCmd cmd, TokenInfo tokenInfo) {
        initDictMap();
        cmd.setOperatorId(tokenInfo.getId());
        if (JudgeEnum.YES.getCode().equals(cmd.getIsInsurance())) {
            List<String> deliverNoList = cmd.getDeliverNoList();
            Result<List<DeliverDTO>> deliverDTOListResult = deliverAggregateRootApi.getDeliverDTOListByDeliverNoList(deliverNoList);
            List<DeliverDTO> deliverDTOList = ResultDataUtils.getInstance(deliverDTOListResult).getDataOrException();
            if (null == deliverDTOList || deliverDTOList.isEmpty() || deliverDTOList.size() != deliverNoList.size()) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
            }

            // 校验退保时间是否在商业险保险有效期内
            SurrenderApplyVO surrenderApplyVO = checkOperationLegitimacy(cmd);
            if (JudgeEnum.YES.getCode().equals(surrenderApplyVO.getTipFlag())) {
                return surrenderApplyVO;
            }

            // 发起退保申请
            Result<List<RecoverBatchSurrenderApplyDTO>> batchSurrenderApplyDTOResult = createSurrenderApply(cmd, deliverDTOList);
            List<RecoverBatchSurrenderApplyDTO> batchSurrenderApplyDTOS = batchSurrenderApplyDTOResult.getData();

            // 执行退保操作，改变交付单状态及保存申请编号
            backInsure(cmd, deliverDTOList, batchSurrenderApplyDTOS);

            return createResult(batchSurrenderApplyDTOResult);
        }

        Result<Integer> deliverResult = deliverAggregateRootApi.toBackInsureByDeliver(cmd);
        if (deliverResult.getCode() != 0) {
            throw new CommonException(deliverResult.getCode(), deliverResult.getMsg());
        }
        SurrenderApplyVO surrenderApplyVO = new SurrenderApplyVO();
        surrenderApplyVO.setTipFlag(JudgeEnum.NO.getCode());
        return surrenderApplyVO;
    }

    private void initDictMap() {
        if (null == insuranceCompanyDictMap) {
            insuranceCompanyDictMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "insurance_company");
        }
    }

    private SurrenderApplyVO createResult(Result<List<RecoverBatchSurrenderApplyDTO>> batchSurrenderApplyDTOResult) {
        List<RecoverBatchSurrenderApplyDTO> batchSurrenderApplyDTOS = batchSurrenderApplyDTOResult.getData();
        SurrenderApplyVO surrenderApplyVO = new SurrenderApplyVO();
        surrenderApplyVO.setTipFlag(JudgeEnum.NO.getCode());

        List<SurrenderApplyInfoVO> applyInfoVOS = new ArrayList<>();
        surrenderApplyVO.setSurrenderApplyInfoVOS(applyInfoVOS);
        Set<String> batchApplyCodeSet = new HashSet<>();
        for (RecoverBatchSurrenderApplyDTO applyDTO : batchSurrenderApplyDTOS) {
            if (!StringUtils.isEmpty(applyDTO.getBatchCode())) {
                if (batchApplyCodeSet.add(applyDTO.getBatchCode())) {
                    SurrenderApplyInfoVO surrenderApplyInfoVO = new SurrenderApplyInfoVO();
                    surrenderApplyInfoVO.setSurrenderApplyCode(applyDTO.getBatchCode());
                    /*if (!StringUtils.isEmpty(applyDTO.getInsuranceCompany())) {
                        surrenderApplyInfoVO.setInsuranceCompany(applyDTO.getInsuranceCompany());
                    } else {
                        if (null != applyDTO.getInsuranceCompanyId() && null != insuranceCompanyDictMap) {
                            surrenderApplyInfoVO.setInsuranceCompany(insuranceCompanyDictMap.get(applyDTO.getInsuranceCompanyId().toString()));
                        }
                    }*/
                    applyInfoVOS.add(surrenderApplyInfoVO);
                }
            } else {
                SurrenderApplyInfoVO surrenderApplyInfoVO = new SurrenderApplyInfoVO();
                surrenderApplyInfoVO.setSurrenderApplyCode(applyDTO.getApplyCode());
                if (!StringUtils.isEmpty(applyDTO.getInsuranceCompany())) {
                    surrenderApplyInfoVO.setInsuranceCompany(applyDTO.getInsuranceCompany());
                } else {
                    if (null != applyDTO.getInsuranceCompanyId() && null != insuranceCompanyDictMap) {
                        surrenderApplyInfoVO.setInsuranceCompany(insuranceCompanyDictMap.get(applyDTO.getInsuranceCompanyId().toString()));
                    }
                }
                applyInfoVOS.add(surrenderApplyInfoVO);
            }
        }

        return surrenderApplyVO;
    }

    private void backInsure(RecoverBackInsureByDeliverCmd cmd, List<DeliverDTO> deliverDTOList, List<RecoverBatchSurrenderApplyDTO> batchSurrenderApplyDTOS) {
        // 改变交付单状态为已退保，保存其退保申请数据
        Map<Integer, RecoverBatchSurrenderApplyDTO> surrenderApplyDTOMap = new HashMap<>();
        for (RecoverBatchSurrenderApplyDTO batchSurrenderApplyDTO : batchSurrenderApplyDTOS) {
            surrenderApplyDTOMap.put(batchSurrenderApplyDTO.getVehicleId(), batchSurrenderApplyDTO);
        }

        deliverDTOList.forEach(deliverDTO -> {
            DeliverDTO deliverDTOToUpdate = new DeliverDTO();
            deliverDTOToUpdate.setDeliverNo(deliverDTO.getDeliverNo());
            RecoverBatchSurrenderApplyDTO recoverSurrenderApplyDTO = surrenderApplyDTOMap.get(deliverDTO.getCarId());
            if (null != recoverSurrenderApplyDTO) {
                deliverDTO.setSurrenderApplyId(recoverSurrenderApplyDTO.getApplyId());
                deliverDTO.setSurrenderApplyCode(recoverSurrenderApplyDTO.getApplyCode());
                deliverDTO.setSurrenderApplyTime(recoverSurrenderApplyDTO.getApplyTime());
            }
        });
        cmd.setDeliverDTOList(deliverDTOList);
        Result<Integer> editResult = deliverAggregateRootApi.backInsure(cmd);
        ResultDataUtils.getInstance(editResult).getDataOrException();
    }

    private Result<List<RecoverBatchSurrenderApplyDTO>> createSurrenderApply(RecoverBackInsureByDeliverCmd cmd, List<DeliverDTO> deliverDTOList) {
        CreateSurrenderApplyCmd createSurrenderApplyCmd = new CreateSurrenderApplyCmd();
        createSurrenderApplyCmd.setSurrenderDate(cmd.getInsuranceTime());
        createSurrenderApplyCmd.setApplyUserId(cmd.getOperatorId());
        List<CreateSurrenderApplyCmd.SurrenderInfoDTO> surrenderInfoDTOS = new ArrayList<>();
        createSurrenderApplyCmd.setCreateH5SurrenderApplyCmdList(surrenderInfoDTOS);
        createSurrenderApplyCmd.setAcceptReason("收车任务触发退保");
        Date applyTime = new Date();
        createSurrenderApplyCmd.setApplyTime(applyTime);

        deliverDTOList.forEach(deliverDTO -> {
            CreateSurrenderApplyCmd.SurrenderInfoDTO surrenderInfoDTO = new CreateSurrenderApplyCmd.SurrenderInfoDTO();
            surrenderInfoDTO.setVehicleId(deliverDTO.getCarId());
            surrenderInfoDTO.setPlateNo(deliverDTO.getCarNum());
            surrenderInfoDTO.setApplyReason("收车任务触发退保");
            surrenderInfoDTOS.add(surrenderInfoDTO);
        });

        // 发送请求
        Result<List<RecoverBatchSurrenderApplyDTO>> result = externalRequestUtil.sendSurrenderApply(createSurrenderApplyCmd);
        List<RecoverBatchSurrenderApplyDTO> surrenderApplyDTOS = ResultDataUtils.getInstance(result).getDataOrException();
        if (null == surrenderApplyDTOS || surrenderApplyDTOS.isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "退保失败");
        }
        surrenderApplyDTOS.forEach(surrenderApplyDTO -> surrenderApplyDTO.setApplyTime(applyTime));

        return result;
    }

    private SurrenderApplyVO checkOperationLegitimacy(RecoverBackInsureByDeliverCmd cmd) {
        Result<List<VehicleDto>> vehicleDTOSResult = vehicleAggregateRootApi.getVehicleDTOByIds(cmd.getCarIdList());
        List<VehicleDto> vehicleDTOS = ResultDataUtils.getInstance(vehicleDTOSResult).getDataOrException();
        if (null == vehicleDTOS || vehicleDTOS.isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆查询失败");
        }
        Map<Integer, VehicleDto> vehicleDTOMap = vehicleDTOS.stream().collect(Collectors.toMap(VehicleDto::getId, Function.identity(), (v1, v2) -> v1));

        SurrenderApplyVO surrenderApplyVO = new SurrenderApplyVO();
        for (VehicleDto vehicleDTO : vehicleDTOS) {
            if (ValidSelectStatusEnum.CHECKED.getCode().equals(vehicleDTO.getSelectStatus())) {
                surrenderApplyVO.setTipFlag(JudgeEnum.YES.getCode());
                surrenderApplyVO.setTipMsg("您选择的车辆".concat(vehicleDTO.getPlateNumber()).concat("已被预选，暂不支持退保操作！"));
            }
            if (ValidSelectStatusEnum.LEASE.getCode().equals(vehicleDTO.getSelectStatus())) {
                surrenderApplyVO.setTipFlag(JudgeEnum.YES.getCode());
                surrenderApplyVO.setTipMsg("您选择的车辆".concat(vehicleDTO.getPlateNumber()).concat("已被租赁，暂不支持退保操作！"));
            }
        }

        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(cmd.getCarIdList());
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
        if (null == vehicleInsuranceDTOS || vehicleInsuranceDTOS.isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆保险信息查询失败");
        }


        for (VehicleInsuranceDTO vehicleInsuranceDTO : vehicleInsuranceDTOS) {
            VehicleDto vehicleDTO = vehicleDTOMap.get(vehicleInsuranceDTO.getVehicleId());
            if (null == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(vehicleDTO.getPlateNumber()).concat("的商业险状态异常"));
            }
            if (null == vehicleInsuranceDTO.getCommercialInsuranceStartDate() || null == vehicleInsuranceDTO.getCommercialInsuranceEndDate()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(vehicleDTO.getPlateNumber()).concat("的商业险起保日期或终保日期缺失"));
            }
            if (ValidStatusEnum.INVALID.getCode().equals(vehicleInsuranceDTO.getCommercialInsuranceStatus())) {
                surrenderApplyVO.setTipFlag(JudgeEnum.YES.getCode());
                surrenderApplyVO.setTipMsg("您选择的车辆".concat(vehicleDTO.getPlateNumber()).concat("的商业险已在失效状态，不能发起退保申请"));
            }
            if (cmd.getInsuranceTime().before(vehicleInsuranceDTO.getCommercialInsuranceStartDate()) || cmd.getInsuranceTime().after(vehicleInsuranceDTO.getCommercialInsuranceEndDate())) {
                surrenderApplyVO.setTipFlag(JudgeEnum.YES.getCode());
                surrenderApplyVO.setTipMsg("你选择的车辆".concat(vehicleDTO.getPlateNumber()).concat("的退保时间不在车辆保险有限期内，请重新选择"));
            }
        }

        return surrenderApplyVO;
    }

}
