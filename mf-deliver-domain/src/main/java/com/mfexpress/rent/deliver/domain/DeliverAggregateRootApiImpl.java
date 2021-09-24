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
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
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
            long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
            Deliver deliver = new Deliver();
            BeanUtil.copyProperties(deliverDTO, deliver);
            Long bizId = redisTools.getBizId(Constants.REDIS_BIZ_ID_DELIVER);
            deliver.setDeliverId(bizId);
            deliver.setDeliverNo(DeliverUtils.getNo(Constants.REDIS_DELIVER_KEY, incr));
            return deliver;
        }).collect(Collectors.toList());
        int i = deliverGateway.addDeliver(deliverList);
        return i > 0 ? Result.getInstance("预选成功").success() : Result.getInstance("预选失败").fail(-1, "预选失败");

    }

    @Override
    @PostMapping("/toCheck")
    public Result<String> toCheck(@RequestParam("serveNo") String serveNo) {
        Deliver deliver = Deliver.builder().isCheck(JudgeEnum.YES.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance("验车成功").success() : Result.getInstance("验车失败").fail(-1, "验车失败");
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
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("投保成功").success() : Result.getInstance("投保失败").fail(-1, "投保失败");
    }

    @Override
    @PostMapping("/toDeliver")
    public Result<String> toDeliver(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder()
                .deliverStatus(DeliverEnum.DELIVER.getCode())
                .isCheck(JudgeEnum.NO.getCode())
                .isInsurance(JudgeEnum.NO.getCode())
                .build();
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("发车成功").success() : Result.getInstance("发车失败").fail(-1, "发车失败");
    }

    @Override
    @PostMapping("/applyRecover")
    public Result<String> applyRecover(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.IS_RECOVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("申请收车成功").success() : Result.getInstance("申请收车失败").fail(-1, "申请收车失败");

    }

    @Override
    @PostMapping("/cancelRecover")
    public Result<String> cancelRecover(@RequestParam("serveNo") String serveNo) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.DELIVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance("取消收车成功").success() : Result.getInstance("取消收车失败").fail(-1, "取消收车失败");
    }

    @Override
    @PostMapping("/toBackInsure")
    public Result<List<String>> toBackInsure(@RequestBody DeliverBackInsureDTO deliverBackInsureDTO) {
        Deliver deliver = Deliver.builder().isInsurance(JudgeEnum.YES.getCode())
                .deliverStatus(DeliverEnum.RECOVER.getCode())
                .insuranceRemark(deliverBackInsureDTO.getInsuranceRemark()).build();
        int i = deliverGateway.updateDeliverByServeNoList(deliverBackInsureDTO.getServeNoList(), deliver);
        //查看是否已经处理违章
        List<Deliver> deliverList = deliverGateway.getDeliverDeductionByServeNoList(deliverBackInsureDTO.getServeNoList());
        List<String> serveNoList = new ArrayList<>();
        if (deliverList != null) {
            serveNoList = deliverList.stream().map(Deliver::getServeNo).collect(Collectors.toList());

            return Result.getInstance(serveNoList).success();
        }
        return Result.getInstance(serveNoList).success();
    }

    @Override
    @PostMapping("/toDeduction")
    public Result<String> toDeduction(@RequestBody DeliverDTO deliverDTO) {
        Deliver deliver = new Deliver();
        BeanUtil.copyProperties(deliverDTO, deliver);
        deliver.setIsDeduction(JudgeEnum.YES.getCode());
        //设置收车中
        deliver.setDeliverStatus(DeliverEnum.IS_RECOVER.getCode());
        int i = deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), deliver);
        //查看是否处理保险
        Deliver deliver1 = deliverGateway.getDeliverByServeNo(deliver.getServeNo());
        if (deliver1.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            //已经处理保险更新完成状态
            Deliver deliver2 = Deliver.builder().deliverStatus(DeliverEnum.RECOVER.getCode()).build();
            deliverGateway.updateDeliverByServeNo(deliver1.getServeNo(), deliver2);
            //返回已完成的服务单编号
            return Result.getInstance(deliver.getServeNo()).success();
        }
        return i > 0 ? Result.getInstance("处理违章完成").success() : Result.getInstance("处理违章失败").fail(-1, "处理违章失败");
    }

    @Override
    @PostMapping("/cancelSelected")
    public Result<String> cancelSelected(@RequestParam("carId") Integer carId) {
        Deliver deliver = deliverGateway.getDeliverByCarId(carId);
        if (deliver != null) {
            //设为失效
            Deliver build = Deliver.builder().status(ValidStatusEnum.INVALID.getCode()).build();
            deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), build);
            return Result.getInstance(deliver.getServeNo()).success();
        }
        return Result.getInstance("").fail(-1, "");
    }

    @Override
    @PostMapping("/syncInsureStatus")
    public Result<String> syncInsureStatus(@RequestParam("carId") Integer carId, @RequestParam("insureStatus") Integer insureStatus) {
        if (insureStatus.equals(ValidStatusEnum.VALID.getCode())) {
            //发车中交付单改为已操作、收车中交付单改为未操作
            int i = deliverGateway.updateInsuranceStatusByCarId(carId, JudgeEnum.YES.getCode(), JudgeEnum.NO.getCode());
        } else {
            //发车中交付单改为未操作、收车中交付单改为已操作
            int i = deliverGateway.updateInsuranceStatusByCarId(carId, JudgeEnum.NO.getCode(), JudgeEnum.YES.getCode());

        }
        return Result.getInstance("保险状态更新成功").success();
    }

    @Override
    @PostMapping("/syncVehicleMileage")
    public Result<String> syncVehicleMileage(@RequestParam("carId") Integer carId, @RequestParam("mileage") Double mileage) {
        int i = deliverGateway.updateMileageByCarId(carId, mileage);

        return Result.getInstance("").success();
    }
}
