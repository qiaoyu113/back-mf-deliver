package com.mfexpress.rent.deliver.deliver.executor;

import cn.hutool.core.bean.BeanUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.InsuranceApplyTypeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.ApplyByIdsQryCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsuranceApplyRentVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InsureApplyQryExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    /*@Resource
    private ExternalRequestUtil externalRequestUtil;*/

    @Resource
    private BackMarketInsuranceCmdExe backMarketInsuranceCmdExe;

    public InsureApplyVO execute(InsureApplyQry qry) {
        qry.setType(InsuranceApplyTypeEnum.INSURE.getCode());
        Result<InsuranceApplyDTO> applyDTOResult = deliverAggregateRootApi.getInsuranceApply(qry);
        InsuranceApplyDTO insuranceApplyDTO = ResultDataUtils.getInstance(applyDTOResult).getDataOrException();
        if (null == insuranceApplyDTO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "投保申请查询失败");
        }
        InsureApplyVO insureApplyVO = BeanUtil.toBean(insuranceApplyDTO, InsureApplyVO.class);
        insureApplyVO.setCompulsoryInsuranceApplyCode(insuranceApplyDTO.getCompulsoryApplyCode());
        insureApplyVO.setCommercialInsuranceApplyCode(insuranceApplyDTO.getCommercialApplyCode());
        insureApplyVO.setApplyDate(insuranceApplyDTO.getApplyTime());

        // 根据申请id查询其状态
        ApplyByIdsQryCmd applyByIdsQryCmd = new ApplyByIdsQryCmd();
        List<String> applyIds = new ArrayList<>();
        if (!StringUtils.isEmpty(insuranceApplyDTO.getCompulsoryApplyId())) {
            applyIds.add(insuranceApplyDTO.getCompulsoryApplyId());
        }
        if (!StringUtils.isEmpty(insuranceApplyDTO.getCommercialApplyId())) {
            applyIds.add(insuranceApplyDTO.getCommercialApplyId());
        }
        applyByIdsQryCmd.setApplyIds(applyIds);
        Result<List<InsuranceApplyRentVO>> insuranceApplyInfoResult = backMarketInsuranceCmdExe.getInsuranceApplyInfo(applyByIdsQryCmd);
        List<InsuranceApplyRentVO> insureApplyRentVOS = ResultDataUtils.getInstance(insuranceApplyInfoResult).getDataOrException();
        if (null == insureApplyRentVOS || insureApplyRentVOS.isEmpty() || insureApplyRentVOS.size() != applyIds.size()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "投保申请查询失败");
        }

        Map<String, InsuranceApplyRentVO> insuranceApplyRentVOMap = insureApplyRentVOS.stream().collect(Collectors.toMap(InsuranceApplyRentVO::getApplyId, Function.identity(), (v1, v2) -> v1));
        InsuranceApplyRentVO compulsoryInsuranceApplyRentVO = insuranceApplyRentVOMap.get(insureApplyVO.getCompulsoryApplyId());
        if (null != compulsoryInsuranceApplyRentVO) {
            insureApplyVO.setCompulsoryInsuranceApplyStatus(compulsoryInsuranceApplyRentVO.getApplyStatus());
            insureApplyVO.setCompulsoryInsuranceApplyStatusDisplay(compulsoryInsuranceApplyRentVO.getApplyStatusName());
        }

        InsuranceApplyRentVO commercialInsuranceApplyRentVO = insuranceApplyRentVOMap.get(insureApplyVO.getCommercialApplyId());
        if (null != commercialInsuranceApplyRentVO) {
            insureApplyVO.setCommercialInsuranceApplyStatus(commercialInsuranceApplyRentVO.getApplyStatus());
            insureApplyVO.setCommercialInsuranceApplyStatusDisplay(commercialInsuranceApplyRentVO.getApplyStatusName());
        }

        return insureApplyVO;
    }

}
