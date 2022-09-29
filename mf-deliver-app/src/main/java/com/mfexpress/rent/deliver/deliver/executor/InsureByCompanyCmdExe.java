package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.InsuranceInfoDTO;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateInsureApplyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.ApplyMobileCreateDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.DeliverBatchInsureApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.DeliverInsureApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.RentInsureApplyResultVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.util.ExternalRequestUtil;
import com.mfexpress.rent.deliver.utils.CommonUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InsureByCompanyCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    private Map<String, String> thirdInsuranceAmountDictMap;

    private Map<String, String> seatInsuredAmountDictMap;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    @Resource
    private ExternalRequestUtil externalRequestUtil;

    public InsureApplyVO execute(DeliverInsureCmd cmd, TokenInfo tokenInfo) {
        initDictMap();
        List<String> serveNoList = cmd.getServeNoList();
        Result<List<ServeDTO>> serveDTOListResult = serveAggregateRootApi.getServeDTOByServeNoList(serveNoList);
        List<ServeDTO> serveDTOList = ResultDataUtils.getInstance(serveDTOListResult).getDataOrException();
        if (null == serveDTOList || serveDTOList.isEmpty()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单查询失败");
        }
        Result<List<DeliverDTO>> deliverDTOListResult = deliverAggregateRootApi.getDeliverDTOListByServeNoList(serveNoList);
        List<DeliverDTO> deliverDTOList = ResultDataUtils.getInstance(deliverDTOListResult).getDataOrException();
        if (null == deliverDTOList || deliverDTOList.isEmpty() || deliverDTOList.size() != serveNoList.size()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }

        // 判断服务单是否支持投保申请操作
        Map<Integer, CommodityDTO> commodityDTOMap = checkOperationLegitimacy(serveDTOList, deliverDTOList);

        // 发起投保申请
        DeliverBatchInsureApplyDTO insureApplyDTO = createInsureApply(cmd, serveDTOList, deliverDTOList, commodityDTOMap, tokenInfo);

        // 改变交付单状态及保存申请编号
        changeDeliver(cmd, tokenInfo, deliverDTOList, insureApplyDTO);

        // 拼装返回结果
        return createResult(insureApplyDTO);
    }

    private void initDictMap() {
        if (null == thirdInsuranceAmountDictMap) {
            thirdInsuranceAmountDictMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "third_liability_insurance");
        }
        if (null == seatInsuredAmountDictMap) {
            seatInsuredAmountDictMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "seat_insurance");
        }
    }

    private InsureApplyVO createResult(DeliverBatchInsureApplyDTO insureApplyDTO) {
        InsureApplyVO insureApplyVO = new InsureApplyVO();
        List<DeliverInsureApplyDTO> deliverInsureApplyDTOS = insureApplyDTO.getDeliverInsureApplyDTOS();
        String compulsoryBatchAcceptCode = insureApplyDTO.getCompulsoryBatchAcceptCode();
        if (!StringUtils.isEmpty(compulsoryBatchAcceptCode)) {
            insureApplyVO.setCompulsoryInsuranceApplyCode(compulsoryBatchAcceptCode);
        } else {
            for (DeliverInsureApplyDTO deliverInsureApplyDTO : deliverInsureApplyDTOS) {
                if (!StringUtils.isEmpty(deliverInsureApplyDTO.getCompulsoryApplyCode())) {
                    insureApplyVO.setCompulsoryInsuranceApplyCode(deliverInsureApplyDTO.getCompulsoryApplyCode());
                    break;
                }
            }
        }
        String commercialBatchAcceptCode = insureApplyDTO.getCommercialBatchAcceptCode();
        if (!StringUtils.isEmpty(commercialBatchAcceptCode)) {
            insureApplyVO.setCommercialInsuranceApplyCode(commercialBatchAcceptCode);
        } else {
            for (DeliverInsureApplyDTO deliverInsureApplyDTO : deliverInsureApplyDTOS) {
                if (!StringUtils.isEmpty(deliverInsureApplyDTO.getCommercialApplyCode())) {
                    insureApplyVO.setCommercialInsuranceApplyCode(deliverInsureApplyDTO.getCommercialApplyCode());
                    break;
                }
            }
        }

        insureApplyVO.setTipFlag(JudgeEnum.NO.getCode());

        return insureApplyVO;
    }

    private void changeDeliver(DeliverInsureCmd cmd, TokenInfo tokenInfo, List<DeliverDTO> deliverDTOList, DeliverBatchInsureApplyDTO insureApplyDTO) {
        List<DeliverInsureApplyDTO> deliverInsureApplyDTOS = insureApplyDTO.getDeliverInsureApplyDTOS();
        Map<Integer, DeliverInsureApplyDTO> vehicleInsureApplyDTOMap = deliverInsureApplyDTOS.stream().collect(Collectors.toMap(DeliverInsureApplyDTO::getVehicleId, Function.identity(), (v1, v2) -> v1));
        for (DeliverDTO deliverDTO : deliverDTOList) {
            DeliverInsureApplyDTO deliverInsureApplyDTO = vehicleInsureApplyDTOMap.get(deliverDTO.getCarId());
            if (null != deliverInsureApplyDTO) {
                deliverInsureApplyDTO.setDeliverNo(deliverDTO.getDeliverNo());
            }
        }

        cmd.setDeliverBatchInsureApplyDTO(insureApplyDTO);
        cmd.setOperatorId(tokenInfo.getId());

        // 查询车辆的保险状态，如果在有效状态，取其保单号
        /*VehicleDto vehicleDto = new VehicleDto();
        for (DeliverInsureApplyDTO deliverInsureApplyDTO : deliverInsureApplyDTOS) {
            if (JudgeEnum.YES.getCode().equals(vehicleDto.getCompulsoryInsuranceStatus())) {
                deliverInsureApplyDTO.setCompulsoryInsurancePolicyNo(vehicleDto.getCompulsoryInsuranceNo());
            }
            if (JudgeEnum.YES.getCode().equals(vehicleDto.getCommercialInsuranceStatus())) {
                deliverInsureApplyDTO.setCommercialInsurancePolicyNo(vehicleDto.getCommercialInsuranceNo());
            }
        }*/

        // 投保申请操作成功，修改交付单的投保状态并补充申请编号
        Result<Integer> insureResult = deliverAggregateRootApi.insureByCompany(cmd);
        ResultDataUtils.getInstance(insureResult).getDataOrException();
    }

    private DeliverBatchInsureApplyDTO createInsureApply(DeliverInsureCmd cmd, List<ServeDTO> serveDTOList, List<DeliverDTO> deliverDTOList, Map<Integer, CommodityDTO> commodityDTOMap, TokenInfo tokenInfo) {
        // 拼接投保请求参数
        Date applyTime = new Date();
        CreateInsureApplyCmd createInsureApplyCmd = new CreateInsureApplyCmd();
        createInsureApplyCmd.setStartInsureDate(cmd.getStartInsureDate());
        createInsureApplyCmd.setEndInsureDate(cmd.getEndInsureDate());
        createInsureApplyCmd.setOperatorUserId(tokenInfo.getId());
        createInsureApplyCmd.setRemarks(serveDTOList.get(0).getOaContractCode());
        createInsureApplyCmd.setOperatorTime(applyTime);

        Map<String, DeliverDTO> deliverDTOMap = deliverDTOList.stream().collect(Collectors.toMap(DeliverDTO::getServeNo, Function.identity(), (v1, v2) -> v1));
        List<CreateInsureApplyCmd.InsureInfoDTO> insuranceInfoDTOS = serveDTOList.stream().map(serveDTO -> {
            CreateInsureApplyCmd.InsureInfoDTO insureInfo = new CreateInsureApplyCmd.InsureInfoDTO();
            DeliverDTO deliverDTO = deliverDTOMap.get(serveDTO.getServeNo());
            insureInfo.setVehicleId(deliverDTO.getCarId());
            insureInfo.setPlate(deliverDTO.getCarNum());
            insureInfo.setApplyReason(serveDTO.getOaContractCode().concat(",").concat(serveDTO.getServeNo()));
            CommodityDTO commodityDTO = commodityDTOMap.get(serveDTO.getContractCommodityId());
            InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
            if(null != insuranceInfo.getInCarPersonnelLiabilityCoverage() && 0 != insuranceInfo.getInCarPersonnelLiabilityCoverage()){
                insureInfo.setSeatInsuredAmount(seatInsuredAmountDictMap.get(insuranceInfo.getInCarPersonnelLiabilityCoverage().toString()).replace("（万）", ""));
            }
            if(null != insuranceInfo.getThirdPartyLiabilityCoverage() && 0 != insuranceInfo.getThirdPartyLiabilityCoverage()){
                insureInfo.setThirdInsuredAmount(seatInsuredAmountDictMap.get(insuranceInfo.getThirdPartyLiabilityCoverage().toString()).replace("（万）", ""));
            }
            insureInfo.setDamageFlag(JudgeEnum.YES.getCode());
            return insureInfo;
        }).collect(Collectors.toList());
        createInsureApplyCmd.setInsuranceApplyList(insuranceInfoDTOS);

        // 发送请求
        Result<RentInsureApplyResultVO> result = externalRequestUtil.createInsureApply(createInsureApplyCmd);
        RentInsureApplyResultVO rentInsureApplyResultVO = ResultDataUtils.getInstance(result).getDataOrException();
        if (null == rentInsureApplyResultVO || (null == rentInsureApplyResultVO.getCommercialApplyList() && null == rentInsureApplyResultVO.getCompulsoryApplyList()) ||
                (rentInsureApplyResultVO.getCommercialApplyList().isEmpty() && rentInsureApplyResultVO.getCompulsoryApplyList().isEmpty())) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), result.getMsg());
        }

        Map<Integer, DeliverInsureApplyDTO> deliverInsureApplyDTOMap = new HashMap<>();
        List<ApplyMobileCreateDTO> compulsoryApplyList = rentInsureApplyResultVO.getCompulsoryApplyList();
        List<ApplyMobileCreateDTO> commercialApplyList = rentInsureApplyResultVO.getCommercialApplyList();

        for (ApplyMobileCreateDTO applyMobileCreateDTO : compulsoryApplyList) {
            int vehicleId = applyMobileCreateDTO.getVehicleId().intValue();
            DeliverInsureApplyDTO deliverInsureApplyDTO = deliverInsureApplyDTOMap.get(vehicleId);
            if (null == deliverInsureApplyDTO) {
                deliverInsureApplyDTO = new DeliverInsureApplyDTO();
                deliverInsureApplyDTO.setVehicleId(vehicleId);
                deliverInsureApplyDTO.setCompulsoryApplyId(applyMobileCreateDTO.getApplyId().toString());
                deliverInsureApplyDTO.setCompulsoryApplyCode(applyMobileCreateDTO.getApplyCode());
                deliverInsureApplyDTO.setApplyTime(applyTime);
                deliverInsureApplyDTOMap.put(vehicleId, deliverInsureApplyDTO);
            } else {
                deliverInsureApplyDTO.setVehicleId(vehicleId);
                deliverInsureApplyDTO.setCompulsoryApplyId(applyMobileCreateDTO.getApplyId().toString());
                deliverInsureApplyDTO.setCompulsoryApplyCode(applyMobileCreateDTO.getApplyCode());
            }
        }

        for (ApplyMobileCreateDTO applyMobileCreateDTO : commercialApplyList) {
            int vehicleId = applyMobileCreateDTO.getVehicleId().intValue();
            DeliverInsureApplyDTO deliverInsureApplyDTO = deliverInsureApplyDTOMap.get(vehicleId);
            if (null == deliverInsureApplyDTO) {
                deliverInsureApplyDTO = new DeliverInsureApplyDTO();
                deliverInsureApplyDTO.setVehicleId(vehicleId);
                deliverInsureApplyDTO.setCommercialApplyId(applyMobileCreateDTO.getApplyId().toString());
                deliverInsureApplyDTO.setCommercialApplyCode(applyMobileCreateDTO.getApplyCode());
                deliverInsureApplyDTO.setApplyTime(applyTime);
                deliverInsureApplyDTOMap.put(vehicleId, deliverInsureApplyDTO);
            } else {
                deliverInsureApplyDTO.setVehicleId(vehicleId);
                deliverInsureApplyDTO.setCommercialApplyId(applyMobileCreateDTO.getApplyId().toString());
                deliverInsureApplyDTO.setCommercialApplyCode(applyMobileCreateDTO.getApplyCode());
            }
        }

        DeliverBatchInsureApplyDTO insureApplyDTO = new DeliverBatchInsureApplyDTO();
        insureApplyDTO.setCompulsoryBatchAcceptCode(rentInsureApplyResultVO.getCompulsoryBatchCode());
        insureApplyDTO.setCommercialBatchAcceptCode(rentInsureApplyResultVO.getCommercialBatchCode());
        insureApplyDTO.setDeliverInsureApplyDTOS(new ArrayList<>(deliverInsureApplyDTOMap.values()));

        return insureApplyDTO;
    }

    private Map<Integer, CommodityDTO> checkOperationLegitimacy(List<ServeDTO> serveDTOList, List<DeliverDTO> deliverDTOList) {
        List<Integer> commodityIds = serveDTOList.stream().map(ServeDTO::getContractCommodityId).collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIds);
        List<CommodityDTO> commodityDTOList = ResultDataUtils.getInstance(commodityListResult).getDataOrException();
        Map<Integer, CommodityDTO> commodityDTOMap = commodityDTOList.stream().collect(Collectors.toMap(CommodityDTO::getId, Function.identity(), (v1, v2) -> v1));
        for (ServeDTO serveDTO : serveDTOList) {
            CommodityDTO commodityDTO = commodityDTOMap.get(serveDTO.getContractCommodityId());
            if (null == commodityDTO) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("的商品信息查询失败"));
            }
            InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
            if (null == insuranceInfo) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("的商品保险信息查询失败"));
            }
            if (null == insuranceInfo.getThirdPartyLiabilityCoverage() && null == insuranceInfo.getInCarPersonnelLiabilityCoverage()) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("对应的商品在合同中约定不包含商业险，不能进行投保申请操作"));
            }
        }

        for (DeliverDTO deliverDTO : deliverDTOList) {
            if (!JudgeEnum.NO.getCode().equals(deliverDTO.getIsInsurance())) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单".concat(deliverDTO.getDeliverNo()).concat("已进行投保操作，不可重复投保"));
            }
        }

        return commodityDTOMap;
    }
}
