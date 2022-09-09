package com.mfexpress.rent.deliver.consumer.common;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.InsuranceInfoDTO;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.DeliverStatusEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureCompleteCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenEventTopic")
@Slf4j
public class VehicleInsuranceTakeEffectMqCommand {

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @MFMqCommonProcessMethod(tag = Constants.VEHICLE_INSURANCE_TAKE_EFFECT_EVENT_TAG)
    public void execute(String vehicleId) {
        log.info("收到车辆保险生效消息，车辆id：{}", vehicleId);
        List<Integer> vehicleIds = Collections.singletonList(Integer.valueOf(vehicleId));
        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(vehicleIds);
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
        if (vehicleInsuranceDTOS.isEmpty() || null == vehicleInsuranceDTOS.get(0)) {
            log.error("收到车辆保险生效消息，查询车辆失败，车辆id：{}", vehicleInsuranceDTOS.get(0).getVehicleId());
            return;
        }
        VehicleInsuranceDTO vehicleInsuranceDTO = vehicleInsuranceDTOS.get(0);

        Result<List<DeliverDTO>> deliverDTOSResult = deliverAggregateRootApi.getDeliverDTOSByCarIdList(vehicleIds);
        List<DeliverDTO> deliverDTOList = ResultDataUtils.getInstance(deliverDTOSResult).getDataOrException();
        if (null == deliverDTOList || deliverDTOList.isEmpty()) {
            log.error("收到车辆保险生效消息，查询交付单失败，车辆id：{}", vehicleInsuranceDTO.getVehicleId());
            return;
        }

        DeliverDTO deliverDTO = null;
        // 找出交付单
        for (DeliverDTO dto : deliverDTOList) {
            if (DeliverEnum.IS_DELIVER.getCode().equals(dto.getDeliverStatus())) {
                deliverDTO = dto;
                break;
            }
        }
        if (null == deliverDTO) {
            log.error("收到车辆保险生效消息，查询交付单失败，车辆id：{}", vehicleInsuranceDTO.getVehicleId());
            return;
        }

        if (!DeliverEnum.IS_DELIVER.getCode().equals(deliverDTO.getStatus()) || !JudgeEnum.NO.getCode().equals(deliverDTO.getIsInsurance())) {
            log.error("收到车辆保险生效消息，对应的交付单不在发车中状态或已经操作投保，交付单号：{}", deliverDTO.getDeliverNo());
            return;
        }
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(Collections.singletonList(serveDTO.getContractCommodityId()));
        List<CommodityDTO> commodityDTOList = ResultDataUtils.getInstance(commodityListResult).getDataOrException();
        CommodityDTO commodityDTO = commodityDTOList.get(0);
        if (null == commodityDTO || null == commodityDTO.getInsuranceInfo()) {
            log.error("收到车辆保险生效消息，查询商品失败，商品id：{}", serveDTO.getContractCommodityId());
            return;
        }
        InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();

        // 比较商品的保险信息和车辆的保险信息
        Integer leaseModelId = serveDTO.getLeaseModelId();
        if (null == insuranceInfo.getThirdPartyLiabilityCoverage() && null == insuranceInfo.getInCarPersonnelLiabilityCoverage() && leaseModelId != 3) {
            // 未选择商业险，应由客户投保
            log.error("收到车辆保险生效消息，对应的商品未投保商业险，不做处理，商品id：{}", serveDTO.getContractCommodityId());
            return;
        }

        if (leaseModelId == 3) {
            // 如果租赁方式是展示，无需要求商业险有效，但交强险必有效
            if (vehicleInsuranceDTO.getCompulsoryInsuranceStatus() == PolicyStatusEnum.EFFECT.getCode() || vehicleInsuranceDTO.getCompulsoryInsuranceStatus() == PolicyStatusEnum.ABOUT_EXPIRED.getCode()) {
                String compulsoryInsurancePolicyNo = vehicleInsuranceDTO.getCompulsoryInsuranceNo();
                InsureCompleteCmd insureCompleteCmd = new InsureCompleteCmd();
                insureCompleteCmd.setDeliverNo(deliverDTO.getDeliverNo());
                if (null != vehicleInsuranceDTO.getCompulsoryInsuranceId()) {
                    insureCompleteCmd.setCompulsoryInsurancePolicyId(vehicleInsuranceDTO.getCompulsoryInsuranceId().toString());
                }
                insureCompleteCmd.setCompulsoryInsurancePolicyNo(compulsoryInsurancePolicyNo);
                deliverAggregateRootApi.insureComplete(insureCompleteCmd);
            } else {
                log.error("收到车辆保险生效消息，车辆无需投保商业险，但车辆的交强险状态无效，车辆id：{}，交付单号：{}，商品id：{}", vehicleInsuranceDTO.getVehicleId(), deliverDTO.getDeliverNo(), serveDTO.getContractCommodityId());
            }
            return;
        }

        // 商品选择了商业险的情况，要求车辆的交强险和商业险都需有效
        if ((vehicleInsuranceDTO.getCompulsoryInsuranceStatus() == PolicyStatusEnum.EFFECT.getCode() || vehicleInsuranceDTO.getCompulsoryInsuranceStatus() == PolicyStatusEnum.ABOUT_EXPIRED.getCode())
                && (vehicleInsuranceDTO.getCommercialInsuranceStatus() == PolicyStatusEnum.EFFECT.getCode() || vehicleInsuranceDTO.getCommercialInsuranceStatus() == PolicyStatusEnum.ABOUT_EXPIRED.getCode())) {
            InsureCompleteCmd insureCompleteCmd = new InsureCompleteCmd();
            insureCompleteCmd.setDeliverNo(deliverDTO.getDeliverNo());
            if (null != vehicleInsuranceDTO.getCompulsoryInsuranceId()) {
                insureCompleteCmd.setCompulsoryInsurancePolicyId(vehicleInsuranceDTO.getCompulsoryInsuranceId().toString());
            }
            insureCompleteCmd.setCompulsoryInsurancePolicyNo(vehicleInsuranceDTO.getCompulsoryInsuranceNo());
            if (null != vehicleInsuranceDTO.getCommercialInsuranceId()) {
                insureCompleteCmd.setCommercialInsurancePolicyId(vehicleInsuranceDTO.getCommercialInsuranceId().toString());
            }
            insureCompleteCmd.setCommercialInsurancePolicyNo(vehicleInsuranceDTO.getCommercialInsuranceNo());
            deliverAggregateRootApi.insureComplete(insureCompleteCmd);
        } else {
            log.error("收到车辆保险生效消息，车辆需投保商业和交强险，但车辆的交强险或商业险状态为无效，车辆id：{}，交付单号：{}，商品id：{}", vehicleInsuranceDTO.getVehicleId(), deliverDTO.getDeliverNo(), serveDTO.getContractCommodityId());
        }
    }

}
