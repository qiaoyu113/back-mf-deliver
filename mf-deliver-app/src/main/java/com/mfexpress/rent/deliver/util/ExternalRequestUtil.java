package com.mfexpress.rent.deliver.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mfexpress.component.dto.proxy.OutsideRequestDTO;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.*;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.RecoverBatchSurrenderApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsuranceApplyRentVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.PolicyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.RentInsureApplyResultVO;
import com.mfexpress.rent.deliver.externalApi.BackMarketApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ExternalRequestUtil {

    @Resource
    private BackMarketApi backMarketApi;

    @Value("${externalService.backMarket.url}")
    private String backMarketUrl;

    public Result<List<RecoverBatchSurrenderApplyDTO>> sendSurrenderApply(CreateSurrenderApplyCmd cmd) {
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/insurance/apply/createSurrenderApply");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(cmd));
        log.info("请求后市场服务，发送退保请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送退保请求参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            List<RecoverBatchSurrenderApplyDTO> batchSurrenderApplyDTOS = JSONUtil.toList((JSONArray) result.getData(), RecoverBatchSurrenderApplyDTO.class);
            result.setData(batchSurrenderApplyDTOS);
        } else {
            result.setData(null);
        }
        return result;
    }

    public Result<RentInsureApplyResultVO> createInsureApply(CreateInsureApplyCmd cmd) {
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/insurance/apply/insure/rent");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(cmd));
        log.info("请求后市场服务，发送投保请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送投保请求参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            RentInsureApplyResultVO deliverInsureApplyDTO = JSONUtil.toBean((JSONObject)result.getData(), RentInsureApplyResultVO.class);
            result.setData(deliverInsureApplyDTO);
        } else {
            result.setData(null);
        }
        return result;
    }

    public Result<String> createInsurancePolicy(CreateInsurancePolicyCmd cmd) {
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/policy/commercial/create/rent");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(cmd));
        log.info("请求后市场服务，发送创建保单请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送创建保单请求参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            Long policyId = (Long)result.getData();
            result.setData(policyId.toString());
        } else {
            result.setData(null);
        }
        return result;
    }

    public Result<List<InsuranceApplyRentVO>> getInsuranceApplyInfo(ApplyByIdsQryCmd cmd) {
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/insurance/apply/insurance/code/rent");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(cmd));
        log.info("请求后市场服务，发送查询申请信息请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送查询申请信息参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            List<InsuranceApplyRentVO> insuranceApplyRentVOS = JSONUtil.toList((JSONArray) result.getData(), InsuranceApplyRentVO.class);
            result.setData(insuranceApplyRentVOS);
        } else {
            result.setData(null);
        }
        return result;
    }

    public Result<PolicyVO> getCompulsoryPolicy(String policyId) {
        PolicyDetailQryCmd policyDetailQryCmd = new PolicyDetailQryCmd();
        policyDetailQryCmd.setPolicyId(policyId);
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/policy/compulsory/rent/policyId");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(policyDetailQryCmd));
        log.info("请求后市场服务，发送查询交强险保单信息请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送查询交强险保单信息参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            PolicyVO policyVO = JSONUtil.toBean((JSONObject) result.getData(), PolicyVO.class);
            result.setData(policyVO);
        } else {
            result.setData(null);
        }
        return result;
    }

    public Result<PolicyVO> getCommercialPolicy(String policyId) {
        PolicyDetailQryCmd policyDetailQryCmd = new PolicyDetailQryCmd();
        policyDetailQryCmd.setPolicyId(policyId);
        OutsideRequestDTO outsideRequestDTO = new OutsideRequestDTO();
        outsideRequestDTO.setUri("api/insurance/client/policy/commercial/rent/policyId");
        outsideRequestDTO.setProjectGateway(backMarketUrl);
        outsideRequestDTO.setParams(BeanUtil.beanToMap(policyDetailQryCmd));
        log.info("请求后市场服务，发送查询商业险保单信息请求参数：{}", outsideRequestDTO);
        String resultStr = backMarketApi.postRequest(outsideRequestDTO);
        log.info("请求后市场服务，发送查询商业险保单信息参数返回结果：{}",resultStr);
        Result result = JSONUtil.toBean(resultStr, Result.class);
        if(null != result.getData() && !(result.getData() instanceof JSONNull)){
            PolicyVO policyVO = JSONUtil.toBean((JSONObject) result.getData(), PolicyVO.class);
            result.setData(policyVO);
        } else {
            result.setData(null);
        }
        return result;
    }

}
