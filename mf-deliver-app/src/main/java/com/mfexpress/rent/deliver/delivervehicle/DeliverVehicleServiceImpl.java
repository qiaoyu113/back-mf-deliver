package com.mfexpress.rent.deliver.delivervehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.constant.DeliverMethodEnum;
import com.mfexpress.rent.deliver.delivervehicle.executor.DeliverVehicleExe;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeliverVehicleServiceImpl implements DeliverVehicleServiceI {

    @Resource
    private DeliverVehicleExe deliverVehicleExe;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;


    @Override
    public Integer toDeliver(DeliverVehicleCmd deliverVehicleCmd, TokenInfo tokenInfo) {
        return deliverVehicleExe.execute(deliverVehicleCmd, tokenInfo);
    }

    @Override
    public LinkmanVo getDeliverByDeliverNo(Integer customerId) {
//        Result<List<LinkmanVo>> customerIds = customerAggregateRootApi.getLinkMansByCusomerId(customerId);
//        List<LinkmanVo> objects = new ArrayList<>();
//        List<LinkmanVo> data = customerIds.getData();
//        if (data != null && data.size()>0) {
//            for (LinkmanVo v : data) {
//                if (v.getType() == 2) {
//                    objects.add(v);
//                }
//            }
//            return objects.get(0);
//        }
//        return new LinkmanVo();

        List<LinkmanVo> linkmanVoList = ResultDataUtils.getInstance(customerAggregateRootApi.getLinkMansByCusomerId(customerId)).getDataOrException();
        if (CollUtil.isEmpty(linkmanVoList)) {
            throw new CommonException(-1, "未找到该客户的联系人信息！");
        }

        List<LinkmanVo> artificialVoList = linkmanVoList.stream().filter(v -> v.getType() == 2).collect(Collectors.toList());
        if (CollUtil.isEmpty(artificialVoList)) {
            throw new CommonException(-1, "未找到该客户的法人信息！");
        }

        return artificialVoList.get(0);
    }

    @Override
    public LinkmanVo getLinkmanByCustomerId(Integer customerId) {
        List<LinkmanVo> linkmanVoList = ResultDataUtils.getInstance(customerAggregateRootApi.getLinkMansByCusomerId(customerId)).getDataOrException();
        if (CollUtil.isEmpty(linkmanVoList)) {
            throw new CommonException(-1, "未找到该客户的联系人信息！");
        }

        List<LinkmanVo> artificialVoList = linkmanVoList.stream().filter(v -> v.getType() == 2).collect(Collectors.toList());
        if (CollUtil.isEmpty(artificialVoList)) {
            throw new CommonException(-1, "未找到该客户的法人信息！");
        }

        return artificialVoList.get(0);
    }

    @Override
    public Integer offlineDeliver(DeliverVehicleCmd cmd, TokenInfo tokenInfo) {
        cmd.setDeliverMethod(DeliverMethodEnum.OFFLINE_NORMAL.getCode());
        return deliverVehicleExe.execute(cmd, tokenInfo);
    }

}
