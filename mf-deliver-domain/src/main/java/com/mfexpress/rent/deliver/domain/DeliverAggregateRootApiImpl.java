package com.mfexpress.rent.deliver.domain;

import cn.hutool.core.bean.BeanUtil;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleMqDTO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/deliver")
@Api(tags = "domain-交付--1.2交付单聚合")
@Slf4j
public class DeliverAggregateRootApiImpl implements DeliverAggregateRootApi {

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;


    @Override
    @PostMapping("/getDeliverByServeNo")
    @PrintParam
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
    @PrintParam
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
    @PrintParam
    public Result<Integer> toCheck(@RequestParam("serveNo") String serveNo) {
        //2021-10-13修改 收车验车时交付单变更为已收车
        Deliver deliver = deliverGateway.getDeliverByServeNo(serveNo);
        deliver.setIsCheck(JudgeEnum.YES.getCode());
        if (deliver.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())) {
            deliver.setStatus(DeliverEnum.RECOVER.getCode());
        }
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance(deliver.getCarId()).success() : Result.getInstance(0).fail(-1, "验车失败");
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
    @PrintParam
    public Result<String> toInsure(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder().isInsurance(JudgeEnum.YES.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("投保成功").success() : Result.getInstance("投保失败").fail(-1, "投保失败");
    }

    @Override
    @PostMapping("/toDeliver")
    @PrintParam
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
    @PrintParam
    public Result<String> applyRecover(@RequestBody List<String> serveNoList) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.IS_RECOVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("申请收车成功").success() : Result.getInstance("申请收车失败").fail(-1, "申请收车失败");

    }

    @Override
    @PostMapping("/cancelRecover")
    @PrintParam
    public Result<String> cancelRecover(@RequestParam("serveNo") String serveNo) {
        Deliver deliver = Deliver.builder().deliverStatus(DeliverEnum.DELIVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance("取消收车成功").success() : Result.getInstance("取消收车失败").fail(-1, "取消收车失败");
    }

    @Override
    @PostMapping("/toBackInsure")
    @PrintParam
    public Result<String> toBackInsure(@RequestBody DeliverBackInsureDTO deliverBackInsureDTO) {
        Deliver deliver = Deliver.builder().isInsurance(JudgeEnum.YES.getCode())
                .insuranceRemark(deliverBackInsureDTO.getInsuranceRemark()).build();
        int i = deliverGateway.updateDeliverByServeNoList(deliverBackInsureDTO.getServeNoList(), deliver);

        //修改 退保之后在处理才能处理违章
      /*  //查看是否已经处理违章
        List<Deliver> deliverList = deliverGateway.getDeliverDeductionByServeNoList(deliverBackInsureDTO.getServeNoList());
        List<String> serveNoList = new ArrayList<>();
        if (deliverList != null) {
            serveNoList = deliverList.stream().map(Deliver::getServeNo).collect(Collectors.toList());

            return Result.getInstance(serveNoList).success();
        }*/
        return i > 0 ? Result.getInstance("退保成功").success() : Result.getInstance("退保失败").fail(-1, "退保失败");

    }

    @Override
    @PostMapping("/toDeduction")
    @PrintParam
    public Result<String> toDeduction(@RequestBody DeliverDTO deliverDTO) {
        Deliver deliver = new Deliver();
        BeanUtil.copyProperties(deliverDTO, deliver);
        deliver.setIsDeduction(JudgeEnum.YES.getCode());
        int i = deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), deliver);

/*        //查看是否处理保险
        Deliver deliver1 = deliverGateway.getDeliverByServeNo(deliver.getServeNo());
        if (deliver1.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            //已经处理保险更新完成状态
            Deliver deliver2 = Deliver.builder().deliverStatus(DeliverEnum.RECOVER.getCode()).build();
            deliverGateway.updateDeliverByServeNo(deliver1.getServeNo(), deliver2);
            //返回已完成的服务单编号
            return Result.getInstance(deliver.getServeNo()).success();
        }*/
        return i > 0 ? Result.getInstance("处理违章完成").success() : Result.getInstance("处理违章失败").fail(-1, "处理违章失败");
    }

    @Override
    @PostMapping("/cancelSelected")
    @PrintParam
    public Result<String> cancelSelected(@RequestParam("carId") Integer carId) {
        Deliver deliver = deliverGateway.getDeliverByCarIdAndDeliverStatus(carId, DeliverEnum.IS_DELIVER.getCode());
        if (deliver != null) {
            //设为失效
            Deliver build = Deliver.builder().status(ValidStatusEnum.INVALID.getCode()).build();
            deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), build);
            return Result.getInstance(deliver.getServeNo()).success();
        }
        return Result.getInstance("").fail(-1, "");
    }

    @Override
    @PostMapping("/cancelSelectedByServeNoList")
    @PrintParam
    public Result<List<Integer>> cancelSelectedByServeNoList(@RequestBody List<String> serveNoList) {
        List<Deliver> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        Deliver deliver = Deliver.builder().status(ValidStatusEnum.INVALID.getCode()).build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        List<Integer> carIdList = deliverList.stream().map(Deliver::getCarId).collect(Collectors.toList());
        return Result.getInstance(carIdList).success();
    }

    @Override
    @PostMapping("/syncInsureStatus")
    @PrintParam
    public Result<String> syncInsureStatus(@RequestBody List<DeliverVehicleMqDTO> deliverVehicleMqDTOList) {

        List<Integer> carIdList = deliverVehicleMqDTOList.stream().map(DeliverVehicleMqDTO::getCarId).collect(Collectors.toList());
        if (deliverVehicleMqDTOList.get(0).getInsuranceStatus().equals(ValidStatusEnum.VALID.getCode())) {
            //发车中交付单改为已操作、收车中交付单改为未操作
            int i = deliverGateway.updateInsuranceStatusByCarId(carIdList, JudgeEnum.YES.getCode(), JudgeEnum.NO.getCode());
        } else {
            //发车中交付单改为未操作、收车中交付单改为已操作
            int i = deliverGateway.updateInsuranceStatusByCarId(carIdList, JudgeEnum.NO.getCode(), JudgeEnum.YES.getCode());

        }
        return Result.getInstance("保险状态更新成功").success();
    }

    @Override
    @PostMapping("/syncVehicleAgeAndMileage")
    @PrintParam
    public Result<String> syncVehicleAgeAndMileage(@RequestBody List<DeliverVehicleMqDTO> deliverVehicleMqDTOList) {
        for (DeliverVehicleMqDTO deliverVehicleMqDTO : deliverVehicleMqDTOList) {
            if (deliverVehicleMqDTO.getMileage() != null) {
                Deliver deliver = Deliver.builder().mileage(deliverVehicleMqDTO.getMileage()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);

            }
            if (deliverVehicleMqDTO.getVehicleAge() != null) {
                Deliver deliver = Deliver.builder().mileage(deliverVehicleMqDTO.getVehicleAge()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);
            }
            if (deliverVehicleMqDTO.getCarNum() != null) {
                Deliver deliver = Deliver.builder().carNum(deliverVehicleMqDTO.getCarNum()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);
            }
        }

        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/saveCarServiceId")
    @PrintParam
    public Result<String> saveCarServiceId(@RequestBody DeliverCarServiceDTO deliverCarServiceDTO) {
        Deliver deliver = Deliver.builder().carServiceId(deliverCarServiceDTO.getCarServiceId()).build();
        deliverGateway.updateDeliverByServeNoList(deliverCarServiceDTO.getServeNoList(), deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/getDeliverByServeNoList")
    public Result<Map<String, Deliver>> getDeliverByServeNoList(List<String> serveNoList) {
        List<Deliver> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        Map<String, Deliver> map = deliverList.stream().collect(Collectors.toMap(Deliver::getServeNo, Function.identity()));
        return Result.getInstance(map).success();
    }

    @Override
    @PostMapping("/getDeduct")
    public Result<List<DeliverDTO>> getDeduct(@RequestBody List<String> serveNoList) {
        List<Deliver> deliverList = deliverGateway.getDeliverByDeductStatus(serveNoList);
        if (deliverList != null) {
            List<DeliverDTO> deliverDTOList = deliverList.stream().map(deliver -> {
                DeliverDTO deliverDTO = new DeliverDTO();
                BeanUtils.copyProperties(deliver, deliverDTO);
                return deliverDTO;
            }).collect(Collectors.toList());
            return Result.getInstance(deliverDTOList).success();
        }
        return Result.getInstance((List<DeliverDTO>) null).success();

    }

    /* luzheng add */
    @Override
    @PostMapping("/getDeliveredDeliverDTOByCarId")
    public Result<DeliverDTO> getDeliveredDeliverDTOByCarId(@RequestParam("carId") Integer carId) {
        Deliver deliver = deliverGateway.getDeliverByCarIdAndDeliverStatus(carId, DeliverEnum.DELIVER.getCode());
        if (null == deliver) {
            return Result.getInstance((DeliverDTO) null).success();
        }
        DeliverDTO deliverDTO = new DeliverDTO();
        BeanUtils.copyProperties(deliver, deliverDTO);
        return Result.getInstance(deliverDTO).success();
    }
}
