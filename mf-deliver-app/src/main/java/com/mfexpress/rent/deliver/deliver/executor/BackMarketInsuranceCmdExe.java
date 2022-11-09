package com.mfexpress.rent.deliver.deliver.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.hx.backmarket.insurance.constant.policy.PolicyStatusEnum;
import com.hx.backmarket.insurance.domainapi.apply.InsuranceApplyAggregateRootApi;
import com.hx.backmarket.insurance.domainapi.apply.SurrenderInsuranceApplyAggregateRootApi;
import com.hx.backmarket.insurance.domainapi.policy.CommercialInsurancePolicyAggregateRootApi;
import com.hx.backmarket.insurance.domainapi.policy.CompulsoryInsurancePolicyAggregateRootApi;
import com.hx.backmarket.insurance.dto.apply.data.base.cmd.InsureApplyVehicleCmd;
import com.hx.backmarket.insurance.dto.apply.data.base.cmd.RentInsureApplyCmd;
import com.hx.backmarket.insurance.dto.apply.data.base.dto.InsuranceApplyDTO;
import com.hx.backmarket.insurance.dto.apply.data.base.dto.InsureApplyResultDTO;
import com.hx.backmarket.insurance.dto.apply.data.surrender.cmd.CreateBatchH5SurrenderApplyCmd;
import com.hx.backmarket.insurance.dto.apply.data.surrender.cmd.CreateH5SurrenderApplyCmd;
import com.hx.backmarket.insurance.dto.apply.data.surrender.dto.SurrenderInsuranceApplyDTO;
import com.hx.backmarket.insurance.dto.apply.data.surrender.vo.H5SurrenderApplyVO;
import com.hx.backmarket.insurance.dto.policy.data.base.qry.PolicyDetailQryCmd;
import com.hx.backmarket.insurance.dto.policy.data.commercial.cmd.CommercialPolicyCreateCmd;
import com.hx.backmarket.insurance.dto.policy.data.commercial.dto.CommercialInsurancePolicyDTO;
import com.hx.backmarket.insurance.dto.policy.data.commercial.vo.CommercialPolicyRentVO;
import com.hx.backmarket.insurance.dto.policy.data.compulsory.dto.CompulsoryInsurancePolicyDTO;
import com.hx.backmarket.insurance.dto.policy.data.compulsory.vo.CompulsoryPolicyRentVO;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysCompanyDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.BusinessException;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.ApplyByIdsQryCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateInsurancePolicyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateInsureApplyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateSurrenderApplyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.RecoverBatchSurrenderApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsuranceApplyRentVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.PolicyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.RentInsureApplyResultVO;
import com.mfexpress.rent.deliver.utils.CommonUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceSaveCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BackMarketInsuranceCmdExe {

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private SurrenderInsuranceApplyAggregateRootApi surrenderInsuranceApplyAggregateRootApi;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private InsuranceApplyAggregateRootApi insuranceApplyAggregateRootApi;

    @Resource
    private CommercialInsurancePolicyAggregateRootApi commercialInsurancePolicyAggregateRootApi;

    @Resource
    private CompulsoryInsurancePolicyAggregateRootApi compulsoryInsurancePolicyAggregateRootApi;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    public static Map<String, String> insuranceCompanyMap = new HashMap<>();

    public Result<List<RecoverBatchSurrenderApplyDTO>> sendSurrenderApply(CreateSurrenderApplyCmd createSurrenderApplyCmd) {
        CreateBatchH5SurrenderApplyCmd cmd = JSONUtil.toBean(JSONUtil.toJsonStr(createSurrenderApplyCmd), CreateBatchH5SurrenderApplyCmd.class);
        cmd.setOperatorUserId(createSurrenderApplyCmd.getApplyUserId());
        log.info("H5创建退保申请 参数:{}", cmd);
        List<Integer> vehicleIds = cmd.getCreateH5SurrenderApplyCmdList().stream().map(CreateH5SurrenderApplyCmd::getVehicleId).distinct().collect(Collectors.toList());
        Result<List<VehicleInfoDto>> vehicleInfoSResult = vehicleAggregateRootApi.getVehicleInfoListByIdList(vehicleIds);
        List<VehicleInfoDto> vehicleInfoDTOS = ResultDataUtils.getInstance(vehicleInfoSResult).getDataOrException();
        Map<Integer, VehicleInfoDto> vehicleInfoDTOMap = vehicleInfoDTOS.stream().collect(Collectors.toMap(VehicleInfoDto::getId, Function.identity(), (v1, v2) -> v1));
        Result<List<VehicleInsuranceDTO>> insuranceInfoResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(vehicleIds);
        List<VehicleInsuranceDTO> insuranceDTOS = ResultDataUtils.getInstance(insuranceInfoResult).getDataOrException();
        Map<Integer, VehicleInsuranceDTO> insuranceDTOMap = insuranceDTOS.stream().collect(Collectors.toMap(VehicleInsuranceDTO::getVehicleId, a -> a));
        cmd.getCreateH5SurrenderApplyCmdList().forEach(h -> {
            if (Objects.nonNull(insuranceDTOMap.get(h.getVehicleId()))) {
                VehicleInsuranceDTO vehicleInsuranceDTO = insuranceDTOMap.get(h.getVehicleId());
                h.setEndInsureDate(vehicleInsuranceDTO.getCommercialInsuranceEndDate());
                VehicleInfoDto vehicleInfoDTO = vehicleInfoDTOMap.get(h.getVehicleId());
                if (null != vehicleInfoDTO) {
                    h.setOrgId(vehicleInfoDTO.getOrgId());
                    h.setBuType(vehicleInfoDTO.getBuType());
                    h.setCityId(vehicleInfoDTO.getCityId());
                }
            } else {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "未查询到车辆保险信息");
            }
        });
        Result<List<SurrenderInsuranceApplyDTO>> h5SurrenderApply = surrenderInsuranceApplyAggregateRootApi.createH5SurrenderApply(cmd);
        List<SurrenderInsuranceApplyDTO> surrenderInsuranceApplyDTOS = ResultDataUtils.getInstance(h5SurrenderApply).getDataOrException();

        surrenderInsuranceApplyDTOS.forEach(i -> {
            VehicleInsuranceSaveCmd vehicleInsuranceSaveCmd = new VehicleInsuranceSaveCmd();
            vehicleInsuranceSaveCmd.setVehicleId(i.getVehicleId().intValue());
            vehicleInsuranceSaveCmd.setCommercialInsuranceId(i.getPolicyId());
            vehicleInsuranceSaveCmd.setCommercialInsuranceNo(i.getPolicyNo());
            vehicleInsuranceSaveCmd.setCommercialInsuranceEndDate(i.getEndInsureDate());
            vehicleInsuranceSaveCmd.setCommercialInsuranceStartDate(i.getStartInsureDate());
            vehicleInsuranceSaveCmd.setCommercialInsuranceStatus(PolicyStatusEnum.IN_EFFECT.getIndex());
            vehicleInsuranceSaveCmd.setCommercialSurrenderStatus(2);
            log.info("H5创建退保 同步车辆 同步参数:{}", vehicleInsuranceSaveCmd);
//            ResultDataUtils.getInstance(vehicleAggregateRootApi.saveInsurance(vehicleInsuranceSaveCmd)).getDataOrException();
        });


        List<H5SurrenderApplyVO> h5SurrenderApplyVOS = BeanUtil.copyToList(surrenderInsuranceApplyDTOS, H5SurrenderApplyVO.class, CopyOptions.create().ignoreError());

        List<RecoverBatchSurrenderApplyDTO> batchSurrenderApplyDTOS = BeanUtil.copyToList(h5SurrenderApplyVOS, RecoverBatchSurrenderApplyDTO.class, CopyOptions.create().ignoreError());

        return Result.getInstance(batchSurrenderApplyDTOS).success();
    }

    public Result<RentInsureApplyResultVO> createInsureApply(CreateInsureApplyCmd createInsureApplyCmd) {
        RentInsureApplyCmd cmd = BeanUtil.toBean(createInsureApplyCmd, RentInsureApplyCmd.class);
        cmd.setInsuranceApplyList(BeanUtil.copyToList(createInsureApplyCmd.getInsuranceApplyList(), InsureApplyVehicleCmd.class, CopyOptions.create().ignoreError()));
        // 查询车辆信息 设置投保、被保公司
        Optional.ofNullable(cmd).map(RentInsureApplyCmd::getInsuranceApplyList).ifPresent(insuranceApplyList -> {

            insuranceApplyList.forEach(insuranceApply -> {

                VehicleInfoDto vehicleDTO = ResultDataUtils.getInstance(vehicleAggregateRootApi.getVehicleInfoVOById(insuranceApply.getVehicleId().intValue())).getDataOrNull();
                if (vehicleDTO == null) {
                    throw new BusinessException(ResultErrorEnum.NOT_FOUND.getCode(), "车辆(id=" + insuranceApply.getVehicleId() + ")不存在");
                }
                insuranceApply.setOrgId(vehicleDTO.getOrgId());
                insuranceApply.setCityId(vehicleDTO.getCityId());
                insuranceApply.setBuType(vehicleDTO.getBuType());
                SysCompanyDTO companyDTO = new SysCompanyDTO();
                companyDTO.setCompanyType(vehicleDTO.getTenantId());
                companyDTO.setOfficeId(vehicleDTO.getOrgId());
                List<SysCompanyDTO> companyDTOList = ResultDataUtils.getInstance(officeAggregateRootApi.getCompanyList(companyDTO)).getDataOrNull();
                Optional.ofNullable(companyDTOList).filter(companyList -> CollectionUtil.isNotEmpty(companyList)).ifPresent(companyList -> {
                    insuranceApply.setInsureCompanyId(companyList.get(0).getId());
                    insuranceApply.setInsuredCompanyId(companyList.get(0).getId());
                });
            });
        });

        Result<InsureApplyResultDTO> resultDTOResult = insuranceApplyAggregateRootApi.insureApply(cmd);
        InsureApplyResultDTO dto = ResultDataUtils.getInstance(resultDTOResult).getDataOrException();

        RentInsureApplyResultVO vo = new RentInsureApplyResultVO();
        BeanUtil.copyProperties(dto, vo, true);

        RentInsureApplyResultVO rentInsureApplyResultVO = BeanUtil.toBeanIgnoreError(vo, RentInsureApplyResultVO.class);
        return Result.getInstance(rentInsureApplyResultVO).success();
    }

    public Result<String> createInsurancePolicy(CreateInsurancePolicyCmd createInsurancePolicyCmd) {
        CommercialPolicyCreateCmd cmd = JSONUtil.toBean(JSONUtil.toJsonStr(createInsurancePolicyCmd), CommercialPolicyCreateCmd.class);
        Result<VehicleInfoDto> vehicleInfoDTOResult = vehicleAggregateRootApi.getVehicleInfoVOById(cmd.getVehicleId().intValue());
        VehicleInfoDto vehicleInfoDTO = ResultDataUtils.getInstance(vehicleInfoDTOResult).getDataOrException();
        if (null == vehicleInfoDTO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆数据查询失败");
        }
        cmd.setOrgId(vehicleInfoDTO.getOrgId());
        cmd.setCityId(vehicleInfoDTO.getCityId());
        cmd.setBuType(vehicleInfoDTO.getBuType());
        Result<CommercialInsurancePolicyDTO> result = commercialInsurancePolicyAggregateRootApi.create(cmd);

        CommercialInsurancePolicyDTO dto = ResultDataUtils.getInstance(result).getDataOrException();

        Optional.ofNullable(dto).filter(policy -> policy.getPolicyId() != null).ifPresent(policy -> {
            notice(policy.getVehicleId(), policy, null);
        });

        Long countResult = Optional.ofNullable(dto).filter(policy -> policy.getPolicyId() != null).map(CommercialInsurancePolicyDTO::getPolicyId).orElse(-1L);
        return Result.getInstance(countResult.toString()).success();
    }

    public Result<List<InsuranceApplyRentVO>> getInsuranceApplyInfo(ApplyByIdsQryCmd applyByIdsQryCmd) {
        com.hx.backmarket.insurance.dto.apply.data.base.qry.ApplyByIdsQryCmd qryCmd = new com.hx.backmarket.insurance.dto.apply.data.base.qry.ApplyByIdsQryCmd();
        ArrayList<Long> applyIds = new ArrayList<>();
        qryCmd.setApplyIds(applyIds);
        applyByIdsQryCmd.getApplyIds().forEach(applyId -> {
            applyIds.add(Long.valueOf(applyId));
        });

        Result<List<InsuranceApplyDTO>> result = insuranceApplyAggregateRootApi.getByApplyIds(qryCmd);
        List<InsuranceApplyDTO> dtoList = ResultDataUtils.getInstance(result).getDataOrException();

        List<com.hx.backmarket.insurance.dto.apply.data.base.vo.InsuranceApplyRentVO> collect = dtoList.stream().map(dto -> {
            com.hx.backmarket.insurance.dto.apply.data.base.vo.InsuranceApplyRentVO vo = new com.hx.backmarket.insurance.dto.apply.data.base.vo.InsuranceApplyRentVO();
            vo.setApplyCode(dto.getApplyCode());
            vo.setVehicleId(String.valueOf(dto.getVehicleId()));
            vo.setApplyId(String.valueOf(dto.getApplyId()));
            vo.setApplyStatus(dto.getApplyStatus());
            vo.setPolicyId(dto.getPolicyId());
            vo.setPolicyNo(dto.getPolicyNo());
            return vo;
        }).collect(Collectors.toList());

        List<InsuranceApplyRentVO> applyRentVOS = new ArrayList<>();
        collect.forEach(vo -> {
            InsuranceApplyRentVO insuranceApplyRentVO = new InsuranceApplyRentVO();
            BeanUtil.copyProperties(vo, insuranceApplyRentVO, CopyOptions.create().ignoreError());
            insuranceApplyRentVO.setApplyStatusName(vo.getApplyStatusName());
            applyRentVOS.add(insuranceApplyRentVO);
        });

        return Result.getInstance(applyRentVOS).success();
    }

    public Result<PolicyVO> getCompulsoryPolicy(String policyId) {
        if (null == insuranceCompanyMap) {
            insuranceCompanyMap = CommonUtil.getDictDataDTOMapByDictType(dictAggregateRootApi, "insurance_company");
        }
        PolicyDetailQryCmd qryCmd = new PolicyDetailQryCmd();
        qryCmd.setPolicyId(Long.valueOf(policyId));
        CompulsoryPolicyRentVO vo = new CompulsoryPolicyRentVO();

        CompulsoryInsurancePolicyDTO dto = ResultDataUtils.getInstance(compulsoryInsurancePolicyAggregateRootApi.getPolicyInfo(qryCmd)).getDataOrNull();

        Optional.ofNullable(dto).ifPresent(policy -> {
            BeanUtil.copyProperties(policy, vo, true);
            vo.setPolicyId(String.valueOf(policy.getPolicyId()));
            vo.setInsuranceCompanyName(insuranceCompanyMap.get(policy.getInsuranceCompanyId().toString()));
        });

        PolicyVO policyVO = BeanUtil.toBean(vo, PolicyVO.class);
        return Result.getInstance(policyVO).success();
    }

    public Result<PolicyVO> getCommercialPolicy(String policyId) {
        PolicyDetailQryCmd qryCmd = new PolicyDetailQryCmd();
        qryCmd.setPolicyId(Long.valueOf(policyId));

        CommercialPolicyRentVO vo = new CommercialPolicyRentVO();
        Result<CommercialInsurancePolicyDTO> result = commercialInsurancePolicyAggregateRootApi.getPolicyInfo(qryCmd);
        CommercialInsurancePolicyDTO dto = ResultDataUtils.getInstance(result).getDataOrNull();

        Optional.ofNullable(dto).ifPresent(policy -> {
            BeanUtil.copyProperties(policy, vo, true);
            vo.setPolicyId(String.valueOf(policy.getPolicyId()));
            log.info("policy.getInsuranceCompanyId()---->{}", policy.getInsuranceCompanyId());
            vo.setInsuranceCompanyName(insuranceCompanyMap.get(policy.getInsuranceCompanyId().toString()));
        });

        PolicyVO policyVO = BeanUtil.toBean(vo, PolicyVO.class);
        return Result.getInstance(policyVO).success();
    }


    public void notice(Long vehicleId, CommercialInsurancePolicyDTO commercialPolicy, CompulsoryInsurancePolicyDTO compulsoryPolicy) {
        VehicleInsuranceSaveCmd vehicleInsuranceSaveCmd = new VehicleInsuranceSaveCmd();
        vehicleInsuranceSaveCmd.setVehicleId(vehicleId.intValue());
        // 商业险
        if (Optional.ofNullable(commercialPolicy).isPresent()) {
            vehicleInsuranceSaveCmd.setCommercialInsuranceId(commercialPolicy.getPolicyId());
            vehicleInsuranceSaveCmd.setCommercialInsuranceNo(commercialPolicy.getPolicyNo());
            vehicleInsuranceSaveCmd.setCommercialInsuranceStartDate(commercialPolicy.getStartInsureDate());
            vehicleInsuranceSaveCmd.setCommercialInsuranceEndDate(commercialPolicy.getEndInsureDate());
            vehicleInsuranceSaveCmd.setCommercialInsuranceStatus(commercialPolicy.getPolicyStatus());
            vehicleInsuranceSaveCmd.setCommercialSurrenderStatus(commercialPolicy.getSurrenderStatus());
        }
        // 交强险
        if (Optional.ofNullable(compulsoryPolicy).isPresent()) {
            vehicleInsuranceSaveCmd.setCompulsoryInsuranceId(compulsoryPolicy.getPolicyId());
            vehicleInsuranceSaveCmd.setCompulsoryInsuranceNo(compulsoryPolicy.getPolicyNo());
            vehicleInsuranceSaveCmd.setCompulsoryInsuranceStartDate(compulsoryPolicy.getStartInsureDate());
            vehicleInsuranceSaveCmd.setCompulsoryInsuranceEndDate(compulsoryPolicy.getEndInsureDate());
            vehicleInsuranceSaveCmd.setCompulsoryInsuranceStatus(compulsoryPolicy.getPolicyStatus());
        }


        VehicleAggregateRootApi vehicleAggregateRootApi = SpringUtil.getBean(VehicleAggregateRootApi.class);
        // todo re
//        vehicleAggregateRootApi.saveInsurance(vehicleInsuranceSaveCmd);
    }

}
