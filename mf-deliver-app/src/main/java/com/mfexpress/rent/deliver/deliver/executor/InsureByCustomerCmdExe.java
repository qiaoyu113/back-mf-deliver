package com.mfexpress.rent.deliver.deliver.executor;

import cn.hutool.core.collection.CollectionUtil;
import com.hx.backmarket.insurance.constant.policy.PolicyStatusEnum;
import com.hx.backmarket.insurance.constant.policy.PolicyTypeEnum;
import com.hx.backmarket.insurance.domainapi.policy.insurance.InsurancePolicyBaseAggregateRootApi;
import com.hx.backmarket.insurance.dto.insurance.policy.data.dto.InsurancePolicyDTO;
import com.hx.backmarket.insurance.dto.insurance.policy.data.qry.VehiclePolicyQry;
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
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverInsureByCustomerCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CreateInsurancePolicyCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceSaveCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class InsureByCustomerCmdExe {

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private InsurancePolicyBaseAggregateRootApi insurancePolicyBaseAggregateRootApi;

    /*@Resource
    private ExternalRequestUtil externalRequestUtil;*/

    @Resource
    private BackMarketInsuranceCmdExe backMarketInsuranceCmdExe;

    public Integer execute(DeliverInsureByCustomerCmd cmd, TokenInfo tokenInfo) {
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
        if (null == serveDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单查询失败");
        }
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();
        if (null == deliverDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }

        // 校验操作合法性
        checkOperationLegitimacy(serveDTO, deliverDTO);

        // 访问后市场执行创建保单操作
        String policyId = createInsurancePolicy(cmd, deliverDTO, tokenInfo);

        // 判断交强险是否生效中
        // 交强险状态
        boolean compulsoryStatus = false;
        Integer carId = deliverDTO.getCarId();
        Result<List<InsurancePolicyDTO>> compulsoryListResult = insurancePolicyBaseAggregateRootApi.list(VehiclePolicyQry.builder().vehicleId(Long.valueOf(carId)).policyType(PolicyTypeEnum.COMPULSORY.getIndex()).build());
        if (!ResultDataUtils.checkResultData(compulsoryListResult)) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交强险信息查询失败");
        }
        List<InsurancePolicyDTO> compulsoryList = compulsoryListResult.getData();
        if (CollectionUtil.isNotEmpty(compulsoryList)) {
            for (InsurancePolicyDTO insurancePolicyDTO : compulsoryList) {
                if (Objects.equals(insurancePolicyDTO.getPolicyStatus(), PolicyStatusEnum.IN_EFFECT.getIndex())) {
                    compulsoryStatus = true;
                }
            }
        }
        cmd.setCompulsoryStatus(compulsoryStatus);

        // 补充商业险保单号，置交付单到下一状态
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setDeliverNo(deliverDTO.getDeliverNo());
        cmd.setCommercialPolicyId(policyId);

        Result<Integer> operateResult = deliverAggregateRootApi.insureByCustomer(cmd);
        ResultDataUtils.getInstance(operateResult).getDataOrException();

        //2022-11-15 调用车辆域保存保单
        VehicleInsuranceSaveCmd vehicleInsuranceSaveCmd = new VehicleInsuranceSaveCmd();
        vehicleInsuranceSaveCmd.setVehicleId(deliverDTO.getCarId());
        //商业险
        vehicleInsuranceSaveCmd.setCommercialInsuranceId(Long.parseLong(policyId));
        vehicleInsuranceSaveCmd.setCommercialInsuranceNo(cmd.getPolicyNo());
        vehicleInsuranceSaveCmd.setCommercialInsuranceEndDate(cmd.getEndInsureDate());
        vehicleInsuranceSaveCmd.setCommercialInsuranceStartDate(cmd.getStartInsureDate());
        //默认都是1
        vehicleInsuranceSaveCmd.setCommercialInsuranceStatus(1);
        vehicleAggregateRootApi.saveInsurance(vehicleInsuranceSaveCmd);

        return 0;
    }

    private String createInsurancePolicy(DeliverInsureByCustomerCmd cmd, DeliverDTO deliverDTO, TokenInfo tokenInfo) {
        CreateInsurancePolicyCmd createInsurancePolicyCmd = new CreateInsurancePolicyCmd();
        createInsurancePolicyCmd.setStartInsureDate(cmd.getStartInsureDate());
        createInsurancePolicyCmd.setEndInsureDate(cmd.getEndInsureDate());
        createInsurancePolicyCmd.setOperatorUserId(tokenInfo.getId());
        createInsurancePolicyCmd.setInsuranceCompanyId(cmd.getAcceptCompanyId());
        createInsurancePolicyCmd.setPolicyHolder(cmd.getInsureCompany());
        createInsurancePolicyCmd.setInsuredPerson(cmd.getInsureCompany());
        createInsurancePolicyCmd.setPolicyNo(cmd.getPolicyNo());
        createInsurancePolicyCmd.setVehicleId(deliverDTO.getCarId());
        createInsurancePolicyCmd.setPolicyVouchers(cmd.getFileUrls());
        createInsurancePolicyCmd.setRemarks("客户投保");

        // send request
        Result<String> createResult = backMarketInsuranceCmdExe.createInsurancePolicy(createInsurancePolicyCmd);
        String policyId = ResultDataUtils.getInstance(createResult).getDataOrException();
        return policyId;
    }

    private void checkOperationLegitimacy(ServeDTO serveDTO, DeliverDTO deliverDTO) {
        Integer contractCommodityId = serveDTO.getContractCommodityId();
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(Collections.singletonList(contractCommodityId));
        List<CommodityDTO> commodityDTOList = ResultDataUtils.getInstance(commodityListResult).getDataOrException();
        CommodityDTO commodityDTO = commodityDTOList.get(0);
        if (null == commodityDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("的商品信息查询失败"));
        }
        InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
        if (null == insuranceInfo) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("的商品保险信息查询失败"));
        }
        if (null != insuranceInfo.getThirdPartyLiabilityCoverage() || null != insuranceInfo.getInCarPersonnelLiabilityCoverage()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "服务单".concat(serveDTO.getServeNo()).concat("对应的商品在合同中约定包含商业险，不能进行录入保单信息操作"));
        }

        if (!JudgeEnum.NO.getCode().equals(deliverDTO.getIsInsurance())) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单".concat(deliverDTO.getDeliverNo()).concat("已进行投保操作，不可重复投保"));
        }
    }

}
