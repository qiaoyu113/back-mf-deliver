package com.mfexpress.rent.deliver.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.*;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.*;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverCancelByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.RecoverVehicleGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/deliver")
@Api(tags = "domain-交付--1.4交付单聚合")
@Slf4j
public class DeliverAggregateRootApiImpl implements DeliverAggregateRootApi {

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private DeliverEntityApi deliverEntityApi;

    @Resource
    private ServeEntityApi serveEntityApi;

    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Override
    @PostMapping("/getDeliverByServeNo")
    @PrintParam
    public Result<DeliverDTO> getDeliverByServeNo(@RequestParam("serveNo") String serveNo) {
        DeliverDTO deliverDTO = new DeliverDTO();
        DeliverEntity deliver = deliverGateway.getDeliverByServeNo(serveNo);
        if (deliver != null) {
            if (StringUtils.isEmpty(deliver.getInsuranceStartTime())) {
                deliver.setInsuranceStartTime(null);
            }
            if (StringUtils.isEmpty(deliver.getInsuranceEndTime())) {
                deliver.setInsuranceEndTime(null);
            }
            BeanUtil.copyProperties(deliver, deliverDTO);
            return Result.getInstance(deliverDTO).success();
        }
        return Result.getInstance((DeliverDTO) null).success();
    }

    @Override
    @PostMapping("/addDeliver")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addDeliver(@RequestBody List<DeliverDTO> list) {
        List<DeliverEntity> deliverList = list.stream().map(deliverDTO -> {
            long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
            DeliverEntity deliver = new DeliverEntity();
            BeanUtil.copyProperties(deliverDTO, deliver);
            Long bizId = redisTools.getBizId(Constants.REDIS_BIZ_ID_DELIVER);
            deliver.setDeliverId(bizId);
            String deliverNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_KEY, incr);
            deliver.setDeliverNo(deliverNo);
            deliverDTO.setDeliverNo(deliverNo);
            if (null != deliverDTO.getInsuranceStartTime()) {
                deliver.setInsuranceStartTime(DeliverUtils.dateToStringYyyyMMddHHmmss(deliverDTO.getInsuranceStartTime()));
            }
            return deliver;
        }).collect(Collectors.toList());
        int i = deliverGateway.addDeliver(deliverList);

        deliverEntityApi.preSelectedSupplyInsurance(list);
        return i > 0 ? Result.getInstance("预选成功").success() : Result.getInstance("预选失败").fail(-1, "预选失败");

    }

    @Override
    @PostMapping("/toCheck")
    @PrintParam
    // 发车验车和收车验车对deliver的更改都走的是这个接口
    public Result<Integer> toCheck(@RequestParam("serveNo") String serveNo, @RequestParam("operatorId") Integer operatorId) {
        //2021-10-13修改 收车验车时交付单变更为已收车
        DeliverEntity deliver = deliverGateway.getDeliverByServeNo(serveNo);

        // 2022-05-31 如果是收车 需要校验验车是否可以进行
        if (DeliverEnum.IS_RECOVER.getCode().equals(deliver.getDeliverStatus())) {
            RecoverCheckJudgeCmd cmd = new RecoverCheckJudgeCmd();
            cmd.setServeNo(serveNo);
            ResultValidUtils.checkResultException(serveAggregateRootApi.recoverCheckJudge(cmd));
        }

        deliver.setIsCheck(JudgeEnum.YES.getCode());
        deliver.setCarServiceId(operatorId);
        deliver.setUpdateId(operatorId);
        /*if (deliver.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())) {
            deliver.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        }*/
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance(deliver.getCarId()).success() : Result.getInstance(0).fail(-1, "验车失败");
    }

    @Override
    @PostMapping("/toReplace")
    public Result<String> toReplace(@RequestBody DeliverDTO deliverDTO) {
        //原交付单设为失效
        DeliverEntity deliver = DeliverEntity.builder().status(ValidStatusEnum.INVALID.getCode()).build();
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
    public Result<String> toInsure(@RequestBody DeliverInsureCmd cmd) {
        DeliverEntity deliver = DeliverEntity.builder()
                .isInsurance(JudgeEnum.YES.getCode())
                .insuranceStartTime(DeliverUtils.dateToStringYyyyMMddHHmmss(cmd.getStartInsureDate()))
                .build();
        int i = deliverGateway.updateDeliverByServeNoList(cmd.getServeNoList(), deliver);
        return i > 0 ? Result.getInstance("投保成功").success() : Result.getInstance("投保失败").fail(-1, "投保失败");
    }

    @Override
    @PostMapping("/toDeliver")
    @PrintParam
    public Result<String> toDeliver(@RequestBody List<String> serveNoList) {
        DeliverEntity deliver = DeliverEntity.builder()
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
        DeliverEntity deliver = DeliverEntity.builder().deliverStatus(DeliverEnum.IS_RECOVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        return i > 0 ? Result.getInstance("申请收车成功").success() : Result.getInstance("申请收车失败").fail(-1, "申请收车失败");

    }

    @Override
    @PostMapping("/cancelRecover")
    @PrintParam
    public Result<String> cancelRecover(@RequestParam("serveNo") String serveNo) {
        DeliverEntity deliver = DeliverEntity.builder().deliverStatus(DeliverEnum.DELIVER.getCode()).build();
        int i = deliverGateway.updateDeliverByServeNo(serveNo, deliver);
        return i > 0 ? Result.getInstance("取消收车成功").success() : Result.getInstance("取消收车失败").fail(-1, "取消收车失败");
    }

    @Override
    @PostMapping("/toBackInsure")
    @PrintParam
    public Result<String> toBackInsure(@RequestBody DeliverBackInsureDTO deliverBackInsureDTO) {
        DeliverEntity deliver = DeliverEntity.builder().isInsurance(JudgeEnum.YES.getCode())
                .insuranceRemark(deliverBackInsureDTO.getInsuranceRemark())
                .insuranceEndTime(DeliverUtils.dateToStringYyyyMMddHHmmss(deliverBackInsureDTO.getInsuranceTime()))
                .build();
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
        DeliverEntity deliver = new DeliverEntity();
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
        DeliverEntity deliver = deliverGateway.getDeliverByCarIdAndDeliverStatus(carId, Collections.singletonList(DeliverEnum.IS_DELIVER.getCode()));
        if (deliver != null) {
            //设为失效
            DeliverEntity build = DeliverEntity.builder().status(ValidStatusEnum.INVALID.getCode()).build();
            deliverGateway.updateDeliverByServeNo(deliver.getServeNo(), build);
            return Result.getInstance(deliver.getServeNo()).success();
        }
        return Result.getInstance("").fail(-1, "");
    }

    @Override
    @PostMapping("/cancelSelectedByServeNoList")
    @PrintParam
    public Result<List<Integer>> cancelSelectedByServeNoList(@RequestBody List<String> serveNoList) {
        List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        DeliverEntity deliver = DeliverEntity.builder().status(ValidStatusEnum.INVALID.getCode()).build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);
        List<Integer> carIdList = deliverList.stream().map(DeliverEntity::getCarId).collect(Collectors.toList());
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
                DeliverEntity deliver = DeliverEntity.builder().mileage(deliverVehicleMqDTO.getMileage()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);

            }
            if (deliverVehicleMqDTO.getVehicleAge() != null) {
                DeliverEntity deliver = DeliverEntity.builder().mileage(deliverVehicleMqDTO.getVehicleAge()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);
            }
            if (deliverVehicleMqDTO.getCarNum() != null) {
                DeliverEntity deliver = DeliverEntity.builder().carNum(deliverVehicleMqDTO.getCarNum()).build();
                int i = deliverGateway.updateMileageAndVehicleAgeByCarId(deliverVehicleMqDTO.getCarId(), deliver);
            }
        }

        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/saveCarServiceId")
    @PrintParam
    public Result<String> saveCarServiceId(@RequestBody DeliverCarServiceDTO deliverCarServiceDTO) {
        DeliverEntity deliver = DeliverEntity.builder().carServiceId(deliverCarServiceDTO.getCarServiceId()).build();
        deliverGateway.updateDeliverByServeNoList(deliverCarServiceDTO.getServeNoList(), deliver);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/getDeliverByServeNoList")
    public Result<Map<String, Deliver>> getDeliverByServeNoList(@RequestBody List<String> serveNoList) {
        List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        Map<String, Deliver> deliverMap = new HashMap<>(deliverList.size());
        deliverList.forEach(deliverEntity -> {
            Deliver deliver = BeanUtil.copyProperties(deliverEntity, Deliver.class);
            deliverMap.put(deliverEntity.getServeNo(), deliver);
        });
        return Result.getInstance(deliverMap).success();
    }

    @Override
    @PostMapping("getDeliverDTOListByServeNoList")
    @PrintParam
    public Result<List<DeliverDTO>> getDeliverDTOListByServeNoList(@RequestBody List<String> serveNoList) {
        List<DeliverDTO> deliverDTOList = deliverEntityApi.getDeliverDTOListByServeNoList(serveNoList);
        if (CollectionUtil.isEmpty(deliverDTOList)) {
            return Result.getInstance(deliverDTOList).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        return Result.getInstance(deliverDTOList).success();
    }

    @Override
    @PostMapping("/getDeduct")
    public Result<List<DeliverDTO>> getDeduct(@RequestBody List<String> serveNoList) {
        List<DeliverEntity> deliverList = deliverGateway.getDeliverByDeductStatus(serveNoList);
        if (deliverList != null) {
            List<DeliverDTO> deliverDTOList = deliverList.stream().map(deliver -> {
                DeliverDTO deliverDTO = new DeliverDTO();
                if (StringUtils.isEmpty(deliver.getInsuranceStartTime())) {
                    deliver.setInsuranceStartTime(null);
                }
                if (StringUtils.isEmpty(deliver.getInsuranceEndTime())) {
                    deliver.setInsuranceEndTime(null);
                }
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
    @PrintParam
    public Result<DeliverDTO> getDeliveredDeliverDTOByCarId(@RequestParam("carId") Integer carId) {
        // 状态为已发车和收车中的交车单所属的车辆都可以被维修
        DeliverEntity deliver = deliverGateway.getDeliverByCarIdAndDeliverStatus(carId, Arrays.asList(DeliverEnum.DELIVER.getCode(), DeliverEnum.IS_RECOVER.getCode()));
        if (null == deliver) {
            return Result.getInstance((DeliverDTO) null).success();
        }
        DeliverDTO deliverDTO = new DeliverDTO();
        if (StringUtils.isEmpty(deliver.getInsuranceStartTime())) {
            deliver.setInsuranceStartTime(null);
        }
        if (StringUtils.isEmpty(deliver.getInsuranceEndTime())) {
            deliver.setInsuranceEndTime(null);
        }
        BeanUtils.copyProperties(deliver, deliverDTO);
        return Result.getInstance(deliverDTO).success();
    }

    @Override
    @PostMapping("/contractGenerating")
    @PrintParam
    public Result<Integer> contractGenerating(@RequestBody DeliverContractGeneratingCmd cmd) {
        DeliverEntity deliverToUpdate = new DeliverEntity();
        List<String> serveNos = cmd.getServeNos();
        List<DeliverEntity> delivers = deliverGateway.getDeliverByServeNoList(serveNos);
        if (delivers.isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单查询失败");
        }
        if (DeliverTypeEnum.DELIVER.getCode() == cmd.getDeliverType()) {
            delivers.forEach(deliver -> {
                if (DeliverContractStatusEnum.NOSIGN.getCode() != deliver.getDeliverContractStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
                }
            });
            deliverToUpdate.setDeliverContractStatus(DeliverContractStatusEnum.GENERATING.getCode());
        } else {
            delivers.forEach(deliver -> {
                if (DeliverContractStatusEnum.NOSIGN.getCode() != deliver.getRecoverContractStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
                }
            });
            deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.GENERATING.getCode());
        }
        int i = deliverGateway.updateDeliverByServeNoList(cmd.getServeNos(), deliverToUpdate);
        return Result.getInstance(i).success();
    }

    @Override
    @PostMapping("/contractSigning")
    @PrintParam
    public Result<Integer> contractSigning(@RequestBody @Validated DeliverContractSigningCmd cmd) {
        DeliverEntity deliverToUpdate = new DeliverEntity();
        List<String> deliverNos = cmd.getDeliverNos();
        List<DeliverEntity> delivers = deliverGateway.getDeliverByDeliverNoList(deliverNos);
        if (delivers.isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单查询失败");
        }
        if (DeliverTypeEnum.DELIVER.getCode() == cmd.getDeliverType()) {
            delivers.forEach(deliver -> {
                if (DeliverContractStatusEnum.GENERATING.getCode() != deliver.getDeliverContractStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
                }
            });
            deliverToUpdate.setDeliverContractStatus(DeliverContractStatusEnum.SIGNING.getCode());
        } else {
            delivers.forEach(deliver -> {
                if (DeliverContractStatusEnum.GENERATING.getCode() != deliver.getRecoverContractStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
                }
            });
            deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.SIGNING.getCode());
        }
        int i = deliverGateway.updateDeliverByDeliverNos(deliverNos, deliverToUpdate);
        return Result.getInstance(i).success();
    }

    @Override
    @PostMapping("/makeNoSignByDeliverNo")
    @PrintParam
    public Result<Integer> makeNoSignByDeliverNo(@RequestParam("deliverNos") String deliverNos, @RequestParam("deliverType") Integer deliverType) {
        DeliverEntity deliverToUpdate = new DeliverEntity();
        if (DeliverTypeEnum.DELIVER.getCode() == deliverType) {
            deliverToUpdate.setDeliverContractStatus(DeliverContractStatusEnum.NOSIGN.getCode());
        } else {
            deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.NOSIGN.getCode());
        }
        int i = deliverGateway.updateDeliverByDeliverNos(JSONUtil.toList(deliverNos, String.class), deliverToUpdate);
        return Result.getInstance(i).success();
    }

    @Override
    @PostMapping("/getDeliverByDeliverNo")
    @PrintParam
    public Result<DeliverDTO> getDeliverByDeliverNo(@RequestParam("deliverNo") String deliverNo) {
        DeliverEntity deliver = deliverGateway.getDeliverByDeliverNo(deliverNo);
        if (null == deliver) {
            return Result.getInstance((DeliverDTO) null).success();
        }

        DeliverDTO deliverDTO = new DeliverDTO();
        BeanUtils.copyProperties(deliver, deliverDTO);
        if (!StringUtils.isEmpty(deliver.getInsuranceStartTime())) {
            deliverDTO.setInsuranceStartTime(DateUtil.parse(deliver.getInsuranceStartTime()));
        }
        if (!StringUtils.isEmpty(deliver.getInsuranceEndTime())) {
            deliverDTO.setInsuranceEndTime(DateUtil.parse(deliver.getInsuranceEndTime()));
        }

        return Result.getInstance(deliverDTO).success();
    }

    @Override
    @PostMapping("/getLastDeliverByCarId")
    @PrintParam
    public Result<DeliverDTO> getLastDeliverByCarId(@RequestParam("carId") Integer carId) {
        List<DeliverEntity> deliverEntityList = deliverGateway.getDeliverByCarId(carId);
        if (CollectionUtil.isEmpty(deliverEntityList)){
            return Result.getInstance((DeliverDTO)null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        DeliverDTO deliverDTO = new DeliverDTO();
        BeanUtils.copyProperties(deliverEntityList.get(0), deliverDTO);
        return Result.getInstance(deliverDTO).success();
    }

    @Override
    @PostMapping("/getDeliverDTOSByCarIdList")
    @PrintParam
    public Result<List<DeliverDTO>> getDeliverDTOSByCarIdList(@RequestParam("carIds") List<Integer> carIds) {
        List<DeliverEntity> deliverDTOSByCarIdList = deliverGateway.getDeliverDTOSByCarIdList(carIds);
        if (CollectionUtil.isEmpty(deliverDTOSByCarIdList)) {
            return Result.getInstance((List<DeliverDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<DeliverDTO> deliverDTOS = BeanUtil.copyToList(deliverDTOSByCarIdList, DeliverDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(deliverDTOS).success();
    }

    @Override
    @PostMapping("/getDeliverNoListByPage")
    @PrintParam
    public Result<PagePagination<String>> getDeliverNoListByPage(@RequestBody DeliverQry qry) {
        PagePagination<DeliverEntity> pagePagination = deliverGateway.getDeliverNoListByPage(qry);
        List<DeliverEntity> deliverEntityList = pagePagination.getList();
        List<String> deliverNoList = deliverEntityList.stream().map(DeliverEntity::getDeliverNo).collect(Collectors.toList());

        PagePagination<String> serveNoListPagePagination = new PagePagination<>();
        serveNoListPagePagination.setPage(pagePagination.getPage());
        serveNoListPagePagination.setPagination(pagePagination.getPagination());
        serveNoListPagePagination.setList(deliverNoList);
        return Result.getInstance(serveNoListPagePagination).success();
    }

    @Override
    @PostMapping("/cancelRecoverByDeliver")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    // 因取消收车逻辑向上抽离到app层而废弃
    public Result<Integer> cancelRecoverByDeliver(@RequestBody @Validated RecoverCancelByDeliverCmd cmd) {

        // 2022-05-31 增加不可取消收车规则判断
        /*DeliverEntity deliverEntity = deliverGateway.getDeliverByDeliverNo(cmd.getDeliverNo());
        if (deliverEntity == null) {
            return Result.getInstance(0).fail(ResultStatusEnum.UNKNOWS.getCode(), "数据错误");
        }

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverEntity.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        if (Optional.ofNullable(serveDTO).filter(serve -> ServeEnum.REPAIR.getCode().equals(serve.getStatus())).isPresent()) {
            String serveNo = serveDTOResult.getData().getServeNo();

            // 查询替换单
            ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceServeNoBySourceServeNo(maintenanceAggregateRootApi, serveNo);

            if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {
                String replaceServeNo = replaceVehicleDTO.getServeNo();
                Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceServeNo);
                if (Optional.ofNullable(replaceServeDTOResult).map(Result::getData)
                        .filter(s -> JudgeEnum.NO.getCode().equals(s.getReplaceFlag())).isPresent()) {
                    return Result.getInstance(0).fail(ResultStatusEnum.UNKNOWS.getCode(), "不可取消收车");
                }
            }
        }

        DeliverEntity deliver = DeliverEntity.builder().deliverStatus(DeliverEnum.DELIVER.getCode())
                .updateId(cmd.getOperatorId())
                .build();
        deliverGateway.updateDeliverByDeliverNo(cmd.getDeliverNo(), deliver);

        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        BeanUtils.copyProperties(cmd, recoverVehicle);
        recoverVehicle.setStatus(ValidStatusEnum.INVALID.getCode());
        recoverVehicle.setUpdateId(cmd.getOperatorId());
        recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);*/

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/toBackInsureByDeliver")
    @PrintParam
    public Result<Integer> toBackInsureByDeliver(@RequestBody @Validated RecoverBackInsureByDeliverCmd cmd) {
        DeliverEntity deliver = DeliverEntity.builder().isInsurance(JudgeEnum.YES.getCode())
                .insuranceRemark(cmd.getInsuranceRemark())
                .insuranceEndTime(DeliverUtils.dateToStringYyyyMMddHHmmss(cmd.getInsuranceTime()))
                .build();

        deliverGateway.updateDeliverByDeliverNos(cmd.getDeliverNoList(), deliver);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/toDeductionByDeliver")
    @PrintParam
    public Result<Integer> toDeductionByDeliver(@RequestBody DeliverDTO deliverDTOToUpdate) {
        DeliverEntity deliver = new DeliverEntity();
        BeanUtil.copyProperties(deliverDTOToUpdate, deliver);
        deliver.setIsDeduction(JudgeEnum.YES.getCode());
        deliver.setDeliverStatus(DeliverEnum.COMPLETED.getCode());
        deliverGateway.updateDeliverByDeliverNo(deliver.getDeliverNo(), deliver);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/getDeliverListByQry")
    @PrintParam
    public Result<List<DeliverDTO>> getDeliverListByQry(@RequestBody DeliverQry deliverQry) {
        List<DeliverEntity> deliverEntityList = deliverGateway.getDeliverListByQry(deliverQry);
        if(deliverEntityList.isEmpty()){
            return Result.getInstance((List<DeliverDTO>)null).success();
        }
        List<DeliverDTO> deliverDTOS = BeanUtil.copyToList(deliverEntityList, DeliverDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(deliverDTOS).success();
    }

    @Override
    @PostMapping("/getMakeDeliverDTOSByCarIdList")
    public Result<List<DeliverDTO>> getMakeDeliverDTOSByCarIdList(@RequestBody List<Integer> carIds,@RequestParam("status") Integer status) {
        List<DeliverEntity> deliverDTOSByCarIdList = deliverGateway.getMakeDeliverDTOSByCarIdList(carIds,status);
        if (CollectionUtil.isEmpty(deliverDTOSByCarIdList)){
            return Result.getInstance((List<DeliverDTO>)null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<DeliverDTO> deliverDTOS = BeanUtil.copyToList(deliverDTOSByCarIdList, DeliverDTO.class,new CopyOptions().ignoreError());
        return Result.getInstance(deliverDTOS).success();
    }

    @Override
    @Transactional
    @PrintParam
    public Result<Integer> cancelDeliver(DeliverCancelCmd cmd) {

        deliverEntityApi.cancelDeliver(cmd);

        return Result.getInstance(0).success();
    }

    @Override
    @Transactional
    @PrintParam
    public Result<Integer> completedDeliver(DeliverCompletedCmd cmd) {

        deliverEntityApi.completedDeliver(cmd);

        return Result.getInstance(0).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/insureByCompany")
    public Result<Integer> insureByCompany(@RequestBody @Validated DeliverInsureCmd cmd) {
        return Result.getInstance(deliverEntityApi.insureByCompany(cmd)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/insureByCustomer")
    public Result<Integer> insureByCustomer(@RequestBody @Validated DeliverInsureByCustomerCmd cmd) {
        return Result.getInstance(deliverEntityApi.insureByCustomer(cmd)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/insureComplete")
    public Result<Integer> insureComplete(InsureCompleteCmd cmd) {
        return Result.getInstance(deliverEntityApi.insureComplete(cmd)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/getInsuranceApplyListByDeliverNoList")
    public Result<List<InsuranceApplyDTO>> getInsuranceApplyListByDeliverNoList(@RequestBody List<String> deliverNoList) {
        return Result.getInstance(deliverEntityApi.getInsuranceApplyListByDeliverNoList(deliverNoList)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/cancelSelectedByDeliver")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> cancelSelectedByDeliver(@RequestBody CancelPreSelectedCmd cmd) {
        deliverEntityApi.cancelSelectedByDeliver(cmd);
        serveEntityApi.cancelSelected(cmd);
        return Result.getInstance(0).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/backInsure")
    public Result<Integer> backInsure(@RequestBody @Validated RecoverBackInsureByDeliverCmd cmd) {
        return Result.getInstance(deliverEntityApi.backInsure(cmd)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/getInsuranceApply")
    public Result<InsuranceApplyDTO> getInsuranceApply(@RequestBody @Validated InsureApplyQry qry) {
        return Result.getInstance(deliverEntityApi.getInsuranceApply(qry)).success();
    }

    @Override
    @PrintParam
    @PostMapping(value = "/getDeliverDTOListByDeliverNoList")
    public Result<List<DeliverDTO>> getDeliverDTOListByDeliverNoList(List<String> deliverNoList) {
        return Result.getInstance(deliverEntityApi.getDeliverDTOListByDeliverNoList(deliverNoList)).success();
    }

}
