package com.mfexpress.rent.deliver.consumer.common;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.common.app.template.dto.TemplateSourceDTO;
import com.mfexpress.common.domain.api.TemplateAggregateRootApi;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVehicleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenEventTopic")
public class DeliverMqCommand {
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RedisTools redisTools;
    /*@Resource
    private SyncServiceI syncServiceI;*/
    @Resource
    private TemplateAggregateRootApi templateAggregateRootApi;
	
    @MFMqCommonProcessMethod(tag = Constants.DELIVER_ORDER_TAG)
    public void execute(String body) {
        log.info(body);
        ServeAddDTO serveAddDTO = JSON.parseObject(body, ServeAddDTO.class);
        //暂时使用redis 增加幂等性校验
        Object o = redisTools.get(serveAddDTO.getOrderId().toString());
        if (o == null) {

            //2022-11-29 处理查询合同模板映射业务类型
            List<ServeVehicleDTO> vehicleDTOList = serveAddDTO.getVehicleDTOList();

            List<Long> contractIdList = vehicleDTOList.stream().map(ServeVehicleDTO::getContractId).distinct().collect(Collectors.toList());
            Result<List<TemplateSourceDTO>> templateResult = templateAggregateRootApi.getTemplateNameBySourceIdList(contractIdList);
            List<TemplateSourceDTO> templateSourceDTOS = ResultDataUtils.getInstance(templateResult).getDataOrNull();
            if (CollectionUtil.isNotEmpty(templateSourceDTOS)) {
                Map<Long, TemplateSourceDTO> sourceDTOMap = templateSourceDTOS.stream().collect(Collectors.toMap(TemplateSourceDTO::getSourceId, Function.identity(),(a,b)->a));
                vehicleDTOList.forEach(serveVehicleDTO -> {
                    TemplateSourceDTO templateSourceDTO = sourceDTOMap.get(serveVehicleDTO.getContractId());
                    serveVehicleDTO.setTemplateName(Objects.isNull(templateSourceDTO) ? "" : templateSourceDTO.getName());
                });
            }
            Result<String> result = serveAggregateRootApi.addServe(serveAddDTO);
        }

    }
}
