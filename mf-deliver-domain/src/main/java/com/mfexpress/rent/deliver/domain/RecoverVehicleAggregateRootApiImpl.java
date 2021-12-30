package com.mfexpress.rent.deliver.domain;


import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.GroupPhotoVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalQry;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.entity.*;
import com.mfexpress.rent.deliver.gateway.*;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/recovervehicle")
@Api(tags = "domain--交付--1.4收车单聚合")
public class RecoverVehicleAggregateRootApiImpl implements RecoverVehicleAggregateRootApi {

    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private RecoverAbnormalGateway recoverAbnormalGateway;

    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private ElecHandoverContractGateway contractGateway;

    @Override
    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    @PrintParam
    public Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(@RequestParam("deliverNo") String deliverNo) {
        RecoverVehicle recoverVehicle = recoverVehicleGateway.getRecoverVehicleByDeliverNo(deliverNo);
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        if (recoverVehicle != null) {
            BeanUtils.copyProperties(recoverVehicle, recoverVehicleDTO);
            return Result.getInstance(recoverVehicleDTO).success();
        }
        // 查询有效的收车单
        return Result.getInstance((RecoverVehicleDTO) null).success();
    }

    @Override
    @PostMapping("/addRecoverVehicle")
    @PrintParam
    public Result<String> addRecoverVehicle(@RequestBody List<RecoverVehicleDTO> recoverVehicleDTOList) {
        if (recoverVehicleDTOList != null) {
            List<RecoverVehicle> recoverVehicleList = recoverVehicleDTOList.stream().map(recoverVehicleDTO -> {
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_RECOVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String recoverVehicleNo = DeliverUtils.getNo(Constants.REDIS_RECOVER_VEHICLE_KEY, incr);
                RecoverVehicle recoverVehicle = new RecoverVehicle();
                BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
                recoverVehicle.setRecoverVehicleNo(recoverVehicleNo);
                return recoverVehicle;
            }).collect(Collectors.toList());
            recoverVehicleGateway.addRecoverVehicle(recoverVehicleList);
        }

        return Result.getInstance("申请收车成功").success();
    }

    @Override
    @PostMapping("/cancelRecover")
    @PrintParam
    public Result<String> cancelRecover(@RequestBody RecoverVehicleDTO recoverVehicleDTO) {
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        recoverVehicle.setStatus(ValidStatusEnum.INVALID.getCode());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return Result.getInstance("").success();
    }

    // 这里的验车是补充收车单信息
    /*@Override
    @PostMapping("/toCheck")
    @PrintParam
    public Result<String> toCheck(@RequestBody RecoverVehicleDTO recoverVehicleDTO) {
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        int i = recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return i > 0 ? Result.getInstance("验车成功").success() : Result.getInstance("验车失败").fail(-1, "验车失败");

    }*/

    @Override
    @PostMapping("/toBackInsure")
    @PrintParam
    public Result<List<RecoverVehicleDTO>> toBackInsure(@RequestBody List<String> serveNoList) {
        List<RecoverVehicle> recoverVehicleList = recoverVehicleGateway.selectRecoverByServeNoList(serveNoList);
        List<RecoverVehicleDTO> recoverVehicleDTOList = new LinkedList<>();
        if (recoverVehicleList != null) {
            recoverVehicleDTOList = recoverVehicleList.stream().map(recoverVehicle -> {
                RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
                BeanUtils.copyProperties(recoverVehicle, recoverVehicleDTO);
                return recoverVehicleDTO;
            }).collect(Collectors.toList());
            return Result.getInstance(recoverVehicleDTOList);
        }

        return Result.getInstance(recoverVehicleDTOList).success();
    }

    @Override
    @PostMapping("getRecoverVehicleByServeNo")
    public Result<Map<String, RecoverVehicle>> getRecoverVehicleByServeNo(@RequestBody List<String> serveNoList) {
        List<RecoverVehicle> recoverVehicleList = recoverVehicleGateway.selectRecoverByServeNoList(serveNoList);
        Map<String, RecoverVehicle> map = recoverVehicleList.stream().collect(Collectors.toMap(RecoverVehicle::getServeNo, Function.identity()));

        return Result.getInstance(map).success();
    }

    @Override
    @PostMapping("/abnormalRecover")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> abnormalRecover(@RequestBody @Validated RecoverAbnormalCmd cmd) {
        // 判断deliver中的合同状态，如果不是签署中和生成中状态，不可进行此操作
        Deliver deliver = deliverGateway.getDeliverByServeNo(cmd.getServeNo());
        if(DeliverContractStatusEnum.GENERATING.getCode() != deliver.getRecoverContractStatus() && DeliverContractStatusEnum.SIGNING.getCode() != deliver.getRecoverContractStatus()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前状态不允许异常收车");
        }

        DeliverVehicle deliverVehicle = deliverVehicleGateway.getDeliverVehicleByDeliverNo(deliver.getDeliverNo());
        if(null == deliverVehicle){
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "发车单查询失败");
        }
        Date deliverVehicleTime = deliverVehicle.getDeliverVehicleTime();
        if(!deliverVehicleTime.equals(cmd.getRecoverTime())){
            if(!deliverVehicleTime.before(cmd.getRecoverTime())){
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期不能早于发车日期");
            }
        }

        // deliver 收车签署状态改为未签，并且异常收车flag改为真，状态改为已收车
        Deliver deliverToUpdate = new Deliver();
        deliverToUpdate.setDeliverNo(deliver.getDeliverNo());
        deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.NOSIGN.getCode());
        deliverToUpdate.setRecoverAbnormalFlag(JudgeEnum.YES.getCode());
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        deliverToUpdate.setCarServiceId(cmd.getOperatorId());
        deliverToUpdate.setUpdateId(cmd.getOperatorId());
        deliverGateway.updateDeliverByDeliverNos(Collections.singletonList(deliver.getDeliverNo()), deliverToUpdate);

        // 服务单状态更改为已收车
        Serve serve = Serve.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(Collections.singletonList(cmd.getServeNo()), serve);

        // 补充异常收车信息
        RecoverAbnormal recoverAbnormal = new RecoverAbnormal();
        BeanUtils.copyProperties(cmd, recoverAbnormal);
        recoverAbnormal.setDeliverNo(deliver.getDeliverNo());
        recoverAbnormal.setCause(cmd.getReason());
        recoverAbnormal.setImgUrl(JSONUtil.toJsonStr(cmd.getImgUrls()));
        recoverAbnormal.setCreatorId(cmd.getOperatorId());
        recoverAbnormal.setCreateTime(cmd.getRecoverTime());
        recoverAbnormalGateway.create(recoverAbnormal);

        // 取出合同信息修改收车单
        ElecContractDTO elecContractDTO = cmd.getElecContractDTO();
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        recoverVehicle.setServeNo(cmd.getServeNo());
        recoverVehicle.setContactsName(elecContractDTO.getContactsName());
        recoverVehicle.setContactsCard(elecContractDTO.getContactsCard());
        recoverVehicle.setContactsPhone(elecContractDTO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(cmd.getRecoverTime());
        recoverVehicle.setDamageFee(elecContractDTO.getRecoverDamageFee());
        recoverVehicle.setParkFee(elecContractDTO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(elecContractDTO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(elecContractDTO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/recovered")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> recovered(@RequestParam("deliverNo") String deliverNo, @RequestParam("foreignNo") String foreignNo) {
        // 判断deliver中的合同状态，如果不是签署中状态，不可进行此操作
        Deliver deliver = deliverGateway.getDeliverByDeliverNo(deliverNo);
        if (DeliverContractStatusEnum.SIGNING.getCode() != deliver.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前状态不允许收车");
        }

        // deliver 收车签署状态改为已完成，并且状态改为已收车
        Deliver deliverToUpdate = new Deliver();
        deliverToUpdate.setDeliverNo(deliver.getDeliverNo());
        deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.COMPLETED.getCode());
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        deliverGateway.updateDeliverByDeliverNos(Collections.singletonList(deliver.getDeliverNo()), deliverToUpdate);

        // 服务单状态更改为已收车
        Serve serve = Serve.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(Collections.singletonList(deliver.getServeNo()), serve);

        // 用合同信息补充收车单信息
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByForeignNo(foreignNo);
        if(null == contractPO){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "合同编号所属的合同查询失败，" + foreignNo);
        }
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        recoverVehicle.setServeNo(deliver.getServeNo());
        recoverVehicle.setContactsName(contractPO.getContactsName());
        recoverVehicle.setContactsCard(contractPO.getContactsCard());
        recoverVehicle.setContactsPhone(contractPO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        recoverVehicle.setDamageFee(contractPO.getRecoverDamageFee());
        recoverVehicle.setParkFee(contractPO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(contractPO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(contractPO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);

        return Result.getInstance(0).success();
    }

    @Override
    public Result<RecoverAbnormalDTO> getRecoverAbnormalByQry(RecoverAbnormalQry qry) {
        RecoverAbnormal recoverAbnormal = recoverAbnormalGateway.getRecoverAbnormalByServeNo(qry.getServeNo());
        if (null == recoverAbnormal) {
            return Result.getInstance((RecoverAbnormalDTO) null).success();
        }
        RecoverAbnormalDTO recoverAbnormalDTO = new RecoverAbnormalDTO();
        BeanUtils.copyProperties(recoverAbnormal, recoverAbnormalDTO);
        return Result.getInstance(recoverAbnormalDTO).success();
    }

}
