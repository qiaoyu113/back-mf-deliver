package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.InsuranceApplyTypeEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.ApplyByIdsQryCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsuranceApplyRentVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.util.ExternalRequestUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReplaceVehicleCheckCmdExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ExternalRequestUtil externalRequestUtil;

    String tipMsg = "针对当前车辆已经发起了%s投保申请，目前处于投保中，无法终止，请与保险专员联系协商后继处理办法是否继续换车操作？";

    public TipVO execute(DeliverReplaceVehicleCheckCmd cmd) {
        Result<DeliverDTO> deliverDTOResult = null;
        if (!StringUtils.isEmpty(cmd.getServeNo())) {
            deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        } else {
            deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(cmd.getDeliverNo());
        }
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();
        if (null == deliverDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }

        TipVO tipVO = new TipVO();
        if (JudgeEnum.NO.getCode().equals(deliverDTO.getIsInsurance())) {
            InsureApplyQry insureApplyQry = new InsureApplyQry();
            insureApplyQry.setDeliverNo(deliverDTO.getDeliverNo());
            insureApplyQry.setType(InsuranceApplyTypeEnum.INSURE.getCode());
            Result<InsuranceApplyDTO> insuranceApplyDTOResult = deliverAggregateRootApi.getInsuranceApply(insureApplyQry);
            InsuranceApplyDTO insuranceApplyDTO = ResultDataUtils.getInstance(insuranceApplyDTOResult).getDataOrException();
            if (null != insuranceApplyDTO) {
                List<String> applyIdList = new ArrayList<>();
                if (!StringUtils.isEmpty(insuranceApplyDTO.getCompulsoryApplyId())) {
                    applyIdList.add(insuranceApplyDTO.getCompulsoryApplyId());
                }
                if (!StringUtils.isEmpty(insuranceApplyDTO.getCommercialApplyId())) {
                    applyIdList.add(insuranceApplyDTO.getCommercialApplyId());
                }
                if (!applyIdList.isEmpty()) {
                    // 根据申请id查询申请状态
                    ApplyByIdsQryCmd applyByIdsQryCmd = new ApplyByIdsQryCmd();
                    applyByIdsQryCmd.setApplyIds(applyIdList);
                    Result<List<InsuranceApplyRentVO>> insuranceApplyInfoSResult = externalRequestUtil.getInsuranceApplyInfo(applyByIdsQryCmd);
                    List<InsuranceApplyRentVO> applyRentVOS = ResultDataUtils.getInstance(insuranceApplyInfoSResult).getDataOrNull();
                    if (null != applyRentVOS && !applyRentVOS.isEmpty()) {
                        // 如果申请存在，并且申请状态为受理中，弹框提示
                        StringBuilder tip = new StringBuilder();
                        Map<String, InsuranceApplyRentVO> insuranceApplyRentVOMap = applyRentVOS.stream().collect(Collectors.toMap(InsuranceApplyRentVO::getApplyId, Function.identity(), (v1, v2) -> v1));
                        InsuranceApplyRentVO compulsoryInsuranceApplyRentVO = insuranceApplyRentVOMap.get(insuranceApplyDTO.getCompulsoryApplyId());
                        if (null != compulsoryInsuranceApplyRentVO) {
                            // 受理中 2
                            if (2 == compulsoryInsuranceApplyRentVO.getApplyStatus()) {
                                tip.append("交强险");
                            }
                        }
                        InsuranceApplyRentVO commercialInsuranceApplyRentVO = insuranceApplyRentVOMap.get(insuranceApplyDTO.getCommercialApplyId());
                        if (null != commercialInsuranceApplyRentVO) {
                            // 受理中 2
                            if (2 == commercialInsuranceApplyRentVO.getApplyStatus()) {
                                if (tip.length() == 0) {
                                    tip.append("商业险");
                                }else{
                                    tip.append("、商业险");
                                }
                            }
                        }

                        if (tip.length() != 0) {
                            tipVO.setTipFlag(JudgeEnum.YES.getCode());
                            tipVO.setTipMsg(String.format(tipMsg, tip));
                            return tipVO;
                        }
                    }
                }
            }
        }

        tipVO.setTipFlag(JudgeEnum.NO.getCode());
        return tipVO;
    }
}
