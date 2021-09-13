package com.mfexpress.rent.deliver.domain;

import cn.hutool.core.bean.BeanUtil;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.utils.Utils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/deliver")
@Api(tags = "domain-交付--1.2交付单聚合")
public class DeliverAggregateRootApiImpl implements DeliverAggregateRootApi {

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;


    @Override
    @PostMapping("/getDeliverByServeNo")
    public Result<DeliverDTO> getDeliverByServeNo(@RequestParam("serveNo") String serveNo) {
        DeliverDTO deliverDTO = new DeliverDTO();
        Deliver deliver = deliverGateway.getDeliverByServeNo(serveNo);
        if (deliver != null) {
            BeanUtil.copyProperties(deliver, deliverDTO);
            return Result.getInstance(deliverDTO).success();
        }
        return Result.getInstance((DeliverDTO) null).success();
    }

    @Override
    @PostMapping("/addDeliver")
    public Result<String> addDeliver(@RequestBody List<DeliverDTO> list) {
        List<Deliver> deliverList = list.stream().map(deliverDTO -> {
            long incr = redisTools.incr(Utils.getEnvVariable(Constants.REDIS_DELIVER_KEY) + Utils.getDateByYYMMDD(new Date()), 1);
            Deliver deliver = new Deliver();
            BeanUtil.copyProperties(deliverDTO, deliver);
            deliver.setDeliverNo(Utils.getNo(Constants.REDIS_DELIVER_KEY, incr));
            return deliver;
        }).collect(Collectors.toList());
        deliverGateway.addDeliver(deliverList);
        return Result.getInstance("预选成功").success();
    }

    @Override
    @PostMapping("/toCheck")
    public Result<String> toCheck(@RequestParam("serveNo") String serveNo) {
        Deliver deliver = Deliver.builder().isCheck(JudgeEnum.YES.getCode()).build();
        deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return Result.getInstance("验车完成").success();
    }

    @Override
    @PostMapping("/toReplace")
    public Result<String> toReplace(@RequestBody DeliverDTO deliverDTO) {
        //原交付单设为失效
        Deliver deliver = Deliver.builder().status(ValidStatusEnum.INVALID.getCode()).build();
        deliverGateway.updateDeliverByServeNo(deliverDTO.getServeNo(), deliver);
        //生成新的交付单
        List<DeliverDTO> deliverDtoList = new LinkedList<>();
        deliverDtoList.add(deliverDTO);
        addDeliver(deliverDtoList);
        return Result.getInstance("更换成功").success();
    }

    @Override
    @PostMapping("/toInsure")
    public Result<String> toInsure(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder().isInsurance(JudgeEnum.YES.getCode()).build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toDeliver")
    public Result<String> toDeliver(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder()
                .deliverStatus(DeliverEnum.DELIVER.getCode())
                .isCheck(JudgeEnum.NO.getCode())
                .isInsurance(JudgeEnum.NO.getCode())
                .build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/applyRecover")
    public Result<String> applyRecover(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.IS_RECOVER.getCode()).build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/cancelRecover")
    public Result<String> cancelRecover(@RequestParam("serveNo") String serveNo) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.DELIVER.getCode()).build();
        deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toBackInsure")
    public Result<String> toBackInsure(@RequestBody DeliverBackInsureDTO deliverBackInsureDTO) {
        Deliver deliver = Deliver.builder().isInsurance(JudgeEnum.YES.getCode())
                .deliverStatus(DeliverEnum.RECOVER.getCode())
                .insuranceRemark(deliverBackInsureDTO.getInsuranceRemark()).build();
        deliverGateway.updateDeliverByServeNoList(deliverBackInsureDTO.getServeNoList(), deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toDeduction")
    public Result<String> toDeduction(@RequestBody DeliverDTO deliverDTO) {
        Deliver deliver = new Deliver();
        BeanUtil.copyProperties(deliverDTO, deliver);
        deliver.setIsDeduction(JudgeEnum.YES.getCode());
        deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), deliver);

        return Result.getInstance("").success();
    }
}
