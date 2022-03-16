package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.constant.ServeRenewalTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.domainservice.ServeDomainServiceI;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
import com.mfexpress.rent.deliver.entity.ServeChangeRecordPO;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/serve")
@Api(tags = "domain--交付--1.4租赁服务单聚合")
@Slf4j
public class ServeAggregateRootApiImpl implements ServeAggregateRootApi {

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private ServeChangeRecordGateway serveChangeRecordGateway;

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;

    @Resource
    private DeliverEntityApi deliverEntityApi;
    @Resource
    private ServeEntityApi serveEntityApi;
    @Resource
    private ServeDomainServiceI serveDomainServiceI;
    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    @Override
    @PostMapping("/getServeDtoByServeNo")
    @PrintParam
    public Result<ServeDTO> getServeDtoByServeNo(@RequestParam("serveNo") String serveNo) {
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        ServeDTO serveDTO = new ServeDTO();
        if (serve != null) {
            BeanUtils.copyProperties(serve, serveDTO);
            if (!StringUtils.isEmpty(serve.getLeaseEndDate())) {
                serveDTO.setLeaseEndDate(DateUtil.parseDate(serve.getLeaseEndDate()));
                serveDTO.setLeaseBeginDate(DateUtil.parseDate(serve.getLeaseBeginDate()));
            }
            return Result.getInstance(serveDTO).success();
        }

        return Result.getInstance((ServeDTO) null).success();
    }

    @Override
    @PostMapping("/addServe")
    @PrintParam
    public Result<String> addServe(@RequestBody ServeAddDTO serveAddDTO) {
        List<ServeEntity> serveList = new LinkedList<>();
        List<ServeVehicleDTO> vehicleDTOList = serveAddDTO.getVehicleDTOList();
        if (vehicleDTOList == null) {
            return Result.getInstance(ResultErrorEnum.VILAD_ERROR.getName()).fail(ResultErrorEnum.VILAD_ERROR.getCode(), "车辆信息为空");
        }
        //订单号放入redis
        redisTools.set(serveAddDTO.getOrderId().toString(), serveAddDTO.getOrderId(), 10000);
        for (ServeVehicleDTO serveVehicleDTO : vehicleDTOList) {
            Integer num = serveVehicleDTO.getNum();
            for (int i = 0; i < num; i++) {
                ServeEntity serve = new ServeEntity();
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_SERVE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String serveNo = DeliverUtils.getNo(Constants.REDIS_SERVE_KEY, incr);
                Long bizId = redisTools.getBizId(Constants.REDIS_BIZ_ID_SERVER);
                serve.setServeId(bizId);
                //创建服务单订单传orgId
                serve.setOrgId(serveAddDTO.getOrgId());
                serve.setSaleId(serveAddDTO.getSaleId());
                serve.setServeNo(serveNo);
                serve.setCreateId(serveAddDTO.getCreateId());
                serve.setOrderId(serveAddDTO.getOrderId());
                serve.setCustomerId(serveAddDTO.getCustomerId());
                serve.setCarModelId(serveVehicleDTO.getCarModelId());
                serve.setBrandId(serveVehicleDTO.getBrandId());
                serve.setLeaseModelId(serveVehicleDTO.getLeaseModelId());
                serve.setCreateTime(new Date());
                serve.setUpdateTime(new Date());
                serve.setCarServiceId(0);
                serve.setCityId(0);
                serve.setUpdateId(0);
                serve.setReplaceFlag(JudgeEnum.NO.getCode());
                serve.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
                serve.setRemark("");
                serve.setRent(serveVehicleDTO.getRent());
                serve.setGoodsId(serveVehicleDTO.getGoodsId());

                serve.setContractId(serveVehicleDTO.getContractId());
                serve.setOaContractCode(serveVehicleDTO.getOaContractCode());
                serve.setDeposit(serveVehicleDTO.getDeposit());
                serve.setLeaseBeginDate(serveVehicleDTO.getLeaseBeginDate());
                serve.setLeaseMonths(serveVehicleDTO.getLeaseMonths());
                serve.setLeaseEndDate(serveVehicleDTO.getLeaseEndDate());
                serve.setBillingAdjustmentDate("");
                serve.setRenewalType(0);
                serve.setExpectRecoverDate("");
                serve.setPaidInDeposit(BigDecimal.ZERO);
                serve.setPaidInDeposit(BigDecimal.ZERO);
                serveList.add(serve);
            }
        }
        try {
            serveGateway.addServeList(serveList);
        } catch (Exception e) {
            return Result.getInstance(serveAddDTO.getOrderId().toString()).fail(ResultErrorEnum.CREATE_ERROR.getCode(), ResultErrorEnum.CREATE_ERROR.getName());
        }

        return Result.getInstance("服务单生成成功").success();
    }

    @Override
    @PostMapping("/toPreselected")
    @PrintParam
    public Result<String> toPreselected(@RequestBody List<String> serveNoList) {
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.PRESELECTED.getCode()).build();
        try {
            serveGateway.updateServeByServeNoList(serveNoList, serve);
            return Result.getInstance("预选成功").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance(ResultErrorEnum.UPDATE_ERROR.getName()).fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "预选失败");
        }

    }

    @Override
    @PostMapping("/toReplace")
    @PrintParam
    public Result<String> toReplace(@RequestParam("serveNo") String serveNo) {
        try {
            ServeEntity serve = ServeEntity.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
            serveGateway.updateServeByServeNo(serveNo, serve);
            return Result.getInstance(ResultErrorEnum.SUCCESSED.getName()).success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance(ResultErrorEnum.UPDATE_ERROR.getName()).fail(ResultErrorEnum.UPDATE_ERROR.getCode(), ResultErrorEnum.UPDATE_ERROR.getName());
        }
    }

    @Override
    @PostMapping("/deliver")
    @PrintParam
    public Result<String> deliver(@RequestBody List<String> serveNoList) {
        try {
            ServeEntity serve = ServeEntity.builder().status(ServeEnum.DELIVER.getCode()).build();
            serveGateway.updateServeByServeNoList(serveNoList, serve);
            return Result.getInstance(ResultErrorEnum.SUCCESSED.getName()).success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance(ResultErrorEnum.UPDATE_ERROR.getName()).fail(ResultErrorEnum.UPDATE_ERROR.getCode(), ResultErrorEnum.UPDATE_ERROR.getName());
        }
    }

    @Override
    @PostMapping("/recover")
    @PrintParam
    public Result<String> recover(@RequestBody List<String> serveNoList) {
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.RECOVER.getCode()).build();
        try {
            serveGateway.updateServeByServeNoList(serveNoList, serve);
            return Result.getInstance("收车完成").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance("收车失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "收车失败");
        }
    }

    @Override
    @PostMapping("/completed")
    @PrintParam
    public Result<String> completed(@RequestParam("serveNo") String serveNo) {
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.COMPLETED.getCode()).build();
        try {
            serveGateway.updateServeByServeNo(serveNo, serve);
            return Result.getInstance("处理违章完成").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance("处理违章失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "处理违章失败");
        }
    }


    @PostMapping("/getServePreselectedDTO")
    @Override
    @PrintParam
    public Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId) {
        return Result.getInstance(serveGateway.getServePreselectedByOrderId(orderId)).success();
    }

    @Override
    @PostMapping("/cancelSelected")
    @PrintParam
    public Result<String> cancelSelected(@RequestParam("serveNo") String serveNo) {

        ServeEntity serve = ServeEntity.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        try {
            serveGateway.updateServeByServeNo(serveNo, serve);
            return Result.getInstance("取消预选成功").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance("取消预选失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "取消预选失败");
        }
    }

    @Override
    @PostMapping("/cancelSelectedList")
    @PrintParam
    public Result<String> cancelSelectedList(@RequestBody List<String> serveNoList) {
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        try {
            serveGateway.updateServeByServeNoList(serveNoList, serve);
            return Result.getInstance("取消预选成功").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance("取消预选失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "取消预选失败");
        }


    }

    @Override
    @PostMapping("/getServeNoListAll")
    @PrintParam
    public Result<List<String>> getServeNoListAll() {
        List<String> serveNoListAll = serveGateway.getServeNoListAll();
        return Result.getInstance(serveNoListAll);
    }

    @Override
    @PostMapping("/getServeDailyDTO")
    public Result<PagePagination<ServeDailyDTO>> getServeDailyDTO(@RequestBody ListQry listQry) {
        try {
            PageHelper.clearPage();
            if (listQry.getPage() == 0) {
                PageHelper.startPage(1, listQry.getLimit());
            }
            PageHelper.startPage(listQry.getPage(), listQry.getLimit());
            //查询当前状态为已发车或维修中
            List<ServeEntity> serveList = serveGateway.getServeByStatus();
            if (serveList != null) {
                List<ServeDailyDTO> serveDailyDTOList = serveList.stream().map(serve -> {
                    ServeDailyDTO serveDailyDTO = new ServeDailyDTO();
                    serveDailyDTO.setServeNo(serve.getServeNo());
                    serveDailyDTO.setCustomerId(serve.getCustomerId());
                    return serveDailyDTO;
                }).collect(Collectors.toList());
                PagePagination<ServeEntity> pagePagination1 = PagePagination.getInstance(serveList);
                PagePagination<ServeDailyDTO> pagePagination = new PagePagination<>();
                BeanUtils.copyProperties(pagePagination1, pagePagination);
                pagePagination.setList(serveDailyDTOList);
                return Result.getInstance(pagePagination).success();
            }
            return Result.getInstance((PagePagination<ServeDailyDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance((PagePagination<ServeDailyDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
    }

    @Override
    @PostMapping("/getServeMapByServeNoList")
    public Result<Map<String, Serve>> getServeMapByServeNoList(@RequestBody List<String> serveNoList) {
        try {
            List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
            Map<String, Serve> serveMap = new HashMap<>(serveList.size());
            serveList.stream().forEach(serveEntity -> {
                Serve serve = BeanUtil.copyProperties(serveEntity, Serve.class);
                serveMap.put(serveEntity.getServeNo(), serve);

            });
            return Result.getInstance(serveMap).success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance((Map<String, Serve>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
    }

    @Override
    @PostMapping("/getServeDTOByServeNoList")
    public Result<List<ServeDTO>> getServeDTOByServeNoList(@RequestBody List<String> serveNoList) {
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        if (CollectionUtil.isEmpty(serveList)) {
            return Result.getInstance((List<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<ServeDTO> serveDTOList = BeanUtil.copyToList(serveList, ServeDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(serveDTOList).success();
    }

    @Override
    @PostMapping("/getCycleServe")
    public Result<PagePagination<ServeDTO>> getCycleServe(@RequestBody ServeCycleQryCmd serveCycleQryCmd) {


        try {
            PageHelper.clearPage();
            if (serveCycleQryCmd.getPage() == 0) {
                PageHelper.startPage(1, serveCycleQryCmd.getLimit());
            }
            PageHelper.startPage(serveCycleQryCmd.getPage(), serveCycleQryCmd.getLimit());
            //查询当前状态为已发车或维修中
            List<ServeEntity> serveList = serveGateway.getCycleServe(serveCycleQryCmd.getCustomerIdList());
            if (serveList != null) {
                List<ServeDTO> serveDailyDTOList = serveList.stream().map(serve -> {
                    ServeDTO serveDTO = new ServeDTO();
                    BeanUtils.copyProperties(serve, serveDTO);
                    return serveDTO;
                }).collect(Collectors.toList());
                PagePagination<ServeEntity> pagePagination = PagePagination.getInstance(serveList);
                PagePagination<ServeDTO> pagePagination1 = new PagePagination<>();
                BeanUtils.copyProperties(pagePagination, pagePagination1);
                pagePagination1.setList(serveDailyDTOList);
                return Result.getInstance(pagePagination1).success();
            }
            return Result.getInstance((PagePagination<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.UPDATE_ERROR.getName());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance((PagePagination<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.UPDATE_ERROR.getName());
        }

    }

    /* luzheng add */
    @Override
    @PostMapping("/toRepair")
    @PrintParam
    public Result<String> toRepair(@RequestParam("serveNo") String serveNo) {
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        if (null == serve) {
            return Result.getInstance("服务单不存在").fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单不存在");
        }
        if (!ServeEnum.DELIVER.getCode().equals(serve.getStatus())) {
            return Result.getInstance("服务单状态异常").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单状态异常");
        }
        ServeEntity serveToUpdate = new ServeEntity();
        serveToUpdate.setStatus(ServeEnum.REPAIR.getCode());
        try {
            serveGateway.updateServeByServeNo(serveNo, serveToUpdate);
            return Result.getInstance("修改成功").success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.getInstance("修改失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "修改失败");
        }
    }

    @Override
    @PostMapping("/cancelOrCompleteRepair")
    @PrintParam
    public Result<String> cancelOrCompleteRepair(@RequestParam("serveNo") String serveNo) {
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        if (null == serve) {
            return Result.getInstance("").fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单不存在");
        }
        if (!ServeEnum.REPAIR.getCode().equals(serve.getStatus())) {
            return Result.getInstance("").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单状态异常");
        }
        ServeEntity serveToUpdate = new ServeEntity();
        serveToUpdate.setStatus(ServeEnum.DELIVER.getCode());
        try {
            serveGateway.updateServeByServeNo(serveNo, serveToUpdate);
            return Result.getInstance("修改成功").success();
        } catch (Exception e) {
            return Result.getInstance("修改失败").fail(ResultErrorEnum.UPDATE_ERROR.getCode(), "修改失败");
        }


    }

    @Override
    @PostMapping("/addServeForReplaceVehicle")
    @PrintParam
    public Result<String> addServeForReplaceVehicle(@RequestBody ServeReplaceVehicleAddDTO serveAddDTO) {

        String serveNo = serveAddDTO.getServeNo();
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        if (null == serve) {
            return Result.getInstance("原服务单不存在").fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "原服务单不存在");
        }
        if (!ServeEnum.REPAIR.getCode().equals(serve.getStatus())) {
            return Result.getInstance("原服务单状态异常").fail(ResultErrorEnum.CREATE_ERROR.getCode(), "原服务单状态异常");
        }

        Long newServeId = redisTools.getBizId(Constants.REDIS_BIZ_ID_SERVER);
        long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_SERVE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
        String newServeNo = DeliverUtils.getNo(Constants.REDIS_SERVE_KEY, incr);

        serve.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
        serve.setCarModelId(serveAddDTO.getModelsId());
        serve.setBrandId(serveAddDTO.getBrandId());
        serve.setCreateId(serveAddDTO.getCreatorId());
        serve.setUpdateId(serveAddDTO.getCreatorId());
        serve.setServeId(newServeId);
        serve.setServeNo(newServeNo);
        serve.setCreateTime(new Date());
        serve.setUpdateTime(new Date());
        serve.setReplaceFlag(JudgeEnum.YES.getCode());
        // 替换车申请的服务单 其月租金应为0
        serve.setRent(BigDecimal.ZERO);

        try {
            serveGateway.addServeList(Collections.singletonList(serve));
        } catch (Exception e) {
            return Result.getInstance(serveAddDTO.getServeNo()).fail(ResultErrorEnum.CREATE_ERROR.getCode(), "替换车服务单生成失败");
        }

        return Result.getInstance(newServeNo).success();
    }

    @Override
    @PostMapping("/getServeListByOrderIds")
    @PrintParam
    public Result<List<ServeDTO>> getServeListByOrderIds(@RequestBody List<Long> orderIds) {
        List<ServeEntity> serveListByOrderIds = serveGateway.getServeListByOrderIds(orderIds);
        if (CollectionUtil.isEmpty(serveListByOrderIds)) {
            return Result.getInstance((List<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<ServeDTO> serveDTOS = BeanUtil.copyToList(serveListByOrderIds, ServeDTO.class, CopyOptions.create());
        return Result.getInstance(serveDTOS).success();
    }

    @Override
    @PostMapping("/renewalServe")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> renewalServe(@RequestBody @Validated RenewalCmd cmd) {
        List<RenewalServeCmd> renewalServeCmdList = cmd.getServeCmdList();
        Map<String, RenewalServeCmd> renewalServeCmdMap = renewalServeCmdList.stream().collect(Collectors.toMap(RenewalServeCmd::getServeNo, Function.identity(), (v1, v2) -> v1));
        List<String> serveNoList = new ArrayList<>(renewalServeCmdMap.keySet());

        // 租赁开始日期必须大于发车日期校验
        List<DeliverVehicleEntity> deliverVehicleList = deliverVehicleGateway.getDeliverVehicleByServeNo(serveNoList);
        if (deliverVehicleList.size() != serveNoList.size()) {
            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "交车单查询失败");
        }
        Map<String, DeliverVehicleEntity> deliverVehicleMap = deliverVehicleList.stream().collect(Collectors.toMap(DeliverVehicleEntity::getServeNo, Function.identity(), (v1, v2) -> v1));
        serveNoList.forEach(serveNo -> {
            RenewalServeCmd renewalServeCmd = renewalServeCmdMap.get(serveNo);
            DeliverVehicleEntity deliverVehicle = deliverVehicleMap.get(serveNo);
            if (null == renewalServeCmd || null == deliverVehicle) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "交车单查询失败");
            }
            Date deliverVehicleTime = deliverVehicle.getDeliverVehicleTime();
            DateTime leaseBeginDate = DateUtil.parse(renewalServeCmd.getLeaseBeginDate());
            if (leaseBeginDate.isBefore(deliverVehicleTime)) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), renewalServeCmd.getCarNum() + "车辆在续约时租赁开始日期小于发车日期，续约失败");
            }
        });


        // 判断服务单是否存在，状态是否为已发车或维修中
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        if (serveNoList.size() != serveList.size()) {
            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单查询失败");
        }
        Map<String, ServeEntity> serveMap = serveList.stream().peek(serve -> {
            // 只有状态为已发车或维修中的服务单才能被续签
            if (!Objects.equals(ServeEnum.DELIVER.getCode(), serve.getStatus()) && !Objects.equals(ServeEnum.REPAIR.getCode(), serve.getStatus())) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单状态异常");
            }
        }).collect(Collectors.toMap(ServeEntity::getServeNo, Function.identity(), (v1, v2) -> v1));

        List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        if (serveNoList.size() != deliverList.size()) {
            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "交付单查询失败");
        }
        Map<String, DeliverEntity> deliverMap = deliverList.stream().collect(Collectors.toMap(DeliverEntity::getServeNo, Function.identity(), (v1, v2) -> v1));
        // 修改服务单相关信息，顺带生成操作记录对象
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        List<ServeEntity> serveListToUpdate = renewalServeCmdList.stream().map(renewalServeCmd -> {
            ServeEntity serve = new ServeEntity();
            BeanUtils.copyProperties(renewalServeCmd, serve);
            serve.setContractId(cmd.getContractId());
            serve.setOaContractCode(cmd.getOaContractCode());
            serve.setRent(BigDecimal.valueOf(renewalServeCmd.getRent()));
            serve.setUpdateId(cmd.getOperatorId());
            serve.setRenewalType(ServeRenewalTypeEnum.ACTIVE.getCode());
            serve.setExpectRecoverDate(renewalServeCmd.getLeaseEndDate());

            ServeChangeRecordPO record = new ServeChangeRecordPO();
            ServeEntity rawDataServe = serveMap.get(serve.getServeNo());
            if (null == rawDataServe) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单获取失败" + serve.getServeNo());
            }
            record.setServeNo(serve.getServeNo());
            record.setRenewalType(ServeRenewalTypeEnum.ACTIVE.getCode());
            record.setRawGoodsId(rawDataServe.getGoodsId());
            record.setRawData(JSONUtil.toJsonStr(rawDataServe));
            record.setNewGoodsId(serve.getGoodsId());
            record.setNewData(JSONUtil.toJsonStr(serve));
            record.setCreatorId(cmd.getOperatorId());
            record.setNewBillingAdjustmentDate(serve.getBillingAdjustmentDate());
            recordList.add(record);

            //发生计费
            //在这里查询交付单 后续看情况做修改
            DeliverEntity deliver = deliverMap.get(serve.getServeNo());
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(serve.getServeNo());
            renewalChargeCmd.setCreateId(cmd.getOperatorId());
            renewalChargeCmd.setCustomerId(rawDataServe.getCustomerId());
            renewalChargeCmd.setDeliverNo(deliver.getDeliverNo());
            renewalChargeCmd.setVehicleId(deliver.getCarId());
            // 根据计费调整日期是否有值来决定计费价格是否发生变化
            if (StringUtils.isEmpty(renewalServeCmd.getBillingAdjustmentDate())) {
                renewalChargeCmd.setEffectFlag(false);
            } else {
                renewalChargeCmd.setEffectFlag(true);
                renewalChargeCmd.setRent(serve.getRent());
                renewalChargeCmd.setRentEffectDate(renewalServeCmd.getBillingAdjustmentDate());
            }
            /*if (rawDataServe.getRent().equals(serve.getRent())) {
                renewalChargeCmd.setEffectFlag(false);
            } else {
                renewalChargeCmd.setEffectFlag(true);
                renewalChargeCmd.setRent(serve.getRent());
                renewalChargeCmd.setRentEffectDate(renewalServeCmd.getBillingAdjustmentDate());
            }*/
            renewalChargeCmd.setRenewalDate(renewalServeCmd.getLeaseEndDate());
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
            return serve;
        }).collect(Collectors.toList());
        serveListToUpdate.forEach(serve -> {
            serveGateway.updateServeByServeNo(serve.getServeNo(), serve);
        });

        // 保存serve的修改记录
        serveChangeRecordGateway.insertList(recordList);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/renewalReplaceServe")
    @PrintParam
    public Result<Integer> renewalReplaceServe(@RequestBody @Validated RenewalReplaceServeCmd cmd) {
        Map<String, String> serveNoWithReplaceServeNoMap = cmd.getServeNoWithReplaceServeNoMap();
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(new ArrayList<>(serveNoWithReplaceServeNoMap.keySet()));
        List<ServeEntity> replaceServeList = serveGateway.getServeByServeNoList(new ArrayList<>(serveNoWithReplaceServeNoMap.values()));
        if (serveList.isEmpty() || replaceServeList.isEmpty()) {
            return Result.getInstance(0).success();
        }

        // 找出服务单和替换车服务单
        Map<String, ServeEntity> serveMap = serveList.stream().collect(Collectors.toMap(ServeEntity::getServeNo, Function.identity(), (v1, v2) -> v1));
        Map<String, ServeEntity> replaceServeMap = replaceServeList.stream().collect(Collectors.toMap(ServeEntity::getServeNo, Function.identity(), (v1, v2) -> v1));

        // 替换车更新预计收车日期，如果已发车，查出交付单，调用计费域
        List<String> replaceServeAlreadyDeliverNoList = new ArrayList<>();
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        List<ServeEntity> serveToUpdateList = serveNoWithReplaceServeNoMap.keySet().stream().map(serveNo -> {
            String replaceServeNo = serveNoWithReplaceServeNoMap.get(serveNo);
            ServeEntity serve = serveMap.get(serveNo);
            ServeEntity replaceServe = replaceServeMap.get(replaceServeNo);
            if (ServeEnum.DELIVER.getCode().equals(replaceServe.getStatus())) {
                replaceServeAlreadyDeliverNoList.add(replaceServe.getServeNo());
            }

            ServeEntity serveToUpdate = ServeEntity.builder().serveNo(replaceServe.getServeNo())
                    .oaContractCode(serve.getOaContractCode())
                    .goodsId(serve.getGoodsId())
                    .expectRecoverDate(serve.getExpectRecoverDate())
                    .updateId(cmd.getOperatorId())
                    .build();
            ServeChangeRecordPO record = new ServeChangeRecordPO();
            record.setServeNo(replaceServe.getServeNo());
            record.setRenewalType(ServeRenewalTypeEnum.ACTIVE.getCode());
            record.setRawGoodsId(replaceServe.getGoodsId());
            record.setRawData(JSONUtil.toJsonStr(replaceServe));
            record.setNewGoodsId(serve.getGoodsId());
            record.setNewData(JSONUtil.toJsonStr(serveToUpdate));
            record.setCreatorId(cmd.getOperatorId());
            record.setNewBillingAdjustmentDate(serve.getBillingAdjustmentDate());
            recordList.add(record);

            replaceServe.setExpectRecoverDate(serve.getExpectRecoverDate());
            return serveToUpdate;
        }).collect(Collectors.toList());

        // 更新预计收车日期
        serveToUpdateList.forEach(serve -> {
            serveGateway.updateServeByServeNo(serve.getServeNo(), serve);
        });
        // 调用计费域
        if (!replaceServeAlreadyDeliverNoList.isEmpty()) {
            List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(replaceServeAlreadyDeliverNoList);
            Map<String, DeliverEntity> deliverMap = deliverList.stream().collect(Collectors.toMap(DeliverEntity::getServeNo, Function.identity(), (v1, v2) -> v1));
            replaceServeAlreadyDeliverNoList.forEach(serveNo -> {
                //发生计费
                //在这里查询交付单 后续看情况做修改
                DeliverEntity deliver = deliverMap.get(serveNo);
                ServeEntity replaceServe = replaceServeMap.get(serveNo);
                if (null != replaceServe) {
                    RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
                    renewalChargeCmd.setServeNo(serveNo);
                    renewalChargeCmd.setCreateId(cmd.getOperatorId());
                    renewalChargeCmd.setCustomerId(replaceServe.getCustomerId());
                    renewalChargeCmd.setDeliverNo(deliver.getDeliverNo());
                    renewalChargeCmd.setVehicleId(deliver.getCarId());
                    renewalChargeCmd.setEffectFlag(false);
                    renewalChargeCmd.setRenewalDate(replaceServe.getExpectRecoverDate());
                    mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
                }
            });
        }

        // 保存serve的修改记录
        serveChangeRecordGateway.insertList(recordList);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/passiveRenewalServe")
    @PrintParam
    public Result<Integer> passiveRenewalServe(@RequestBody @Validated PassiveRenewalServeCmd cmd) {
        /*ServeListQry qry = new ServeListQry();
        qry.setStatuses(cmd.getStatuses());
        qry.setReplaceFlag(JudgeEnum.NO.getCode());
        Integer count = serveGateway.getCountByQry(qry);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = new Date();
        try {
            nowDate = dateFormat.parse(dateFormat.format(nowDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), "日期格式化失败");
        }

        // 找出所有的需要自动续约的服务单
        List<Serve> needPassiveRenewalServeList = new ArrayList<>();
        int page = 1;
        for (int i = 0; i < count; i += cmd.getLimit()) {
            qry.setPage(page);
            page++;
            qry.setLimit(cmd.getLimit());
            PagePagination<Serve> pagePagination = serveGateway.getPageServeByQry(qry);
            List<Serve> serves = pagePagination.getList();
            Date finalNowDate = nowDate;
            serves.forEach(serve -> {
                String leaseEndDateChar = serve.getLeaseEndDate();
                if (!StringUtils.isEmpty(leaseEndDateChar)) {
                    Date leaseEndDate = DateUtil.parse(leaseEndDateChar);
                    if (leaseEndDate.before(finalNowDate)) {
                        // 租赁结束日期在当前日期之前，那么此服务单需要被自动续约，只判断到天
                        needPassiveRenewalServeList.add(serve);
                    }
                }
            });
        }*/
        List<Serve> needPassiveRenewalServeList = cmd.getNeedPassiveRenewalServeList();
        if (null == needPassiveRenewalServeList || needPassiveRenewalServeList.isEmpty()) {
            return Result.getInstance(0).success();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 自动续约，收车日期顺延一个月，租赁期限不变，并发送mq到计费域(逻辑暂无)，自动续约不传计费调整日期
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        List<ServeEntity> serveToUpdateList = needPassiveRenewalServeList.stream().map(serve -> {
            ServeEntity serveToUpdate = new ServeEntity();
            serveToUpdate.setId(serve.getId());
            DateTime expectRecoverDate = DateUtil.parse(serve.getExpectRecoverDate());
            String updatedExpectRecoverDate = getExpectRecoverDate(expectRecoverDate, 1);
            serveToUpdate.setLeaseEndDate(updatedExpectRecoverDate);
            serveToUpdate.setExpectRecoverDate(updatedExpectRecoverDate);
            serveToUpdate.setRenewalType(ServeRenewalTypeEnum.PASSIVE.getCode());
            serveToUpdate.setUpdateId(cmd.getOperatorId());

            ServeChangeRecordPO record = new ServeChangeRecordPO();
            record.setServeNo(serve.getServeNo());
            record.setRenewalType(ServeRenewalTypeEnum.PASSIVE.getCode());
            record.setRawData(JSONUtil.toJsonStr(serve));
            record.setNewData(JSONUtil.toJsonStr(serveToUpdate));
            record.setCreatorId(cmd.getOperatorId());
            recordList.add(record);

            //发生计费
            //在这里查询交付单 后续看情况做修改
            DeliverEntity deliver = deliverGateway.getDeliverByServeNo(serve.getServeNo());
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(serve.getServeNo());
            renewalChargeCmd.setCreateId(cmd.getOperatorId());
            renewalChargeCmd.setCustomerId(serve.getCustomerId());
            renewalChargeCmd.setDeliverNo(deliver.getDeliverNo());
            renewalChargeCmd.setVehicleId(deliver.getCarId());
            renewalChargeCmd.setEffectFlag(false);
            renewalChargeCmd.setRenewalDate(updatedExpectRecoverDate);
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));

            return serveToUpdate;
        }).collect(Collectors.toList());

        serveGateway.batchUpdate(serveToUpdateList);

        // 保存serve的修改记录
        serveChangeRecordGateway.insertList(recordList);

        return Result.getInstance(serveToUpdateList.size()).success();
    }

    @Override
    @PostMapping("/getServeChangeRecordList")
    @PrintParam
    public Result<List<ServeChangeRecordDTO>> getServeChangeRecordList(@RequestParam("serveNo") String serveNo) {
        List<ServeChangeRecordPO> recordList = serveChangeRecordGateway.getList(serveNo);
        if (recordList.isEmpty()) {
            return Result.getInstance((List<ServeChangeRecordDTO>) null).success();
        }
        SimpleDateFormat ymdFormat = new SimpleDateFormat(FormatUtil.ymdFormatChar);
        List<ServeChangeRecordDTO> recordDTOList = recordList.stream().map(record -> {
            ServeChangeRecordDTO recordDTO = new ServeChangeRecordDTO();
            BeanUtils.copyProperties(record, recordDTO);
            if (!StringUtils.isEmpty(record.getNewBillingAdjustmentDate())) {
                try {
                    recordDTO.setNewBillingAdjustmentDate(ymdFormat.parse(record.getNewBillingAdjustmentDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return recordDTO;
        }).collect(Collectors.toList());
        return Result.getInstance(recordDTOList).success();
    }

    @Override
    @PostMapping("/getServeByCustomerIdAndDeliver")
    @PrintParam
    public Result<List<ServeDTO>> getServeByCustomerIdAndDeliver(@RequestBody List<Integer> customerIdList) {
        List<ServeEntity> serveList = serveGateway.getServeByCustomerIdDeliver(customerIdList);
        if (CollectionUtil.isEmpty(serveList)) {
            return Result.getInstance((List<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<ServeDTO> serveDTOList = BeanUtil.copyToList(serveList, ServeDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(serveDTOList).success();
    }

    @Override
    @PostMapping("/getServeByCustomerIdAndRecover")
    @PrintParam
    public Result<List<ServeDTO>> getServeByCustomerIdAndRecover(@RequestBody List<Integer> customerIdList) {
        List<ServeEntity> serveList = serveGateway.getServeByCustomerIdRecover(customerIdList);
        if (CollectionUtil.isEmpty(serveList)) {
            return Result.getInstance((List<ServeDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<ServeDTO> serveDTOList = BeanUtil.copyToList(serveList, ServeDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(serveDTOList).success();
    }

    @Override
    @PostMapping("/getCountByQry")
    @PrintParam
    public Result<Long> getCountByQry(@RequestBody ServeListQry qry) {
        Integer count = serveGateway.getCountByQry(qry);
        return Result.getInstance(Long.valueOf(count)).success();
    }

    @Override
    @PostMapping("/getPageServeByQry")
    @PrintParam
    public Result<PagePagination<Serve>> getPageServeByQry(@RequestBody ServeListQry qry) {

        PagePagination<ServeEntity> pagePagination = serveGateway.getPageServeByQry(qry);
        PagePagination<Serve> servePagePagination = new PagePagination<>();
        BeanUtil.copyProperties(pagePagination, servePagePagination);
        List<ServeEntity> serveEntityList = pagePagination.getList();
        List<Serve> serveList = BeanUtil.copyToList(serveEntityList, Serve.class, new CopyOptions().ignoreError());
        servePagePagination.setList(serveList);
        return Result.getInstance(servePagePagination).success();
    }

    @Override
    @PostMapping("/getPageServeDepositList")
    @PrintParam
    public Result<PagePagination<ServeDepositDTO>> getPageServeDepositList(@RequestBody CustomerDepositListDTO customerDepositLisDTO) {
        return Result.getInstance(serveDomainServiceI.getPageServeDeposit(customerDepositLisDTO)).success();
    }

    @Override
    @PostMapping("/unLockDeposit")
    @PrintParam
    public Result unLockDeposit(@RequestParam("serveNoList") List<String> serveNoList, @RequestParam("creatorId") Integer creatorId) {
        //查询需要解锁得服务单押金列表
        List<ServeDTO> serveList = serveEntityApi.getServeListByServeNoList(serveNoList);
        Map<String, BigDecimal> updateDepositMap = new HashMap<>();
        serveList.stream().forEach(serveDTO -> {
            updateDepositMap.put(serveDTO.getServeNo(), serveDTO.getPaidInDeposit().negate());
        });
        serveEntityApi.updateServeDepositByServeNoList(updateDepositMap, creatorId);
        return Result.getInstance(true).success();
    }

    @Override
    @PostMapping("/getCustomerDepositLockList")
    @PrintParam
    public Result<List<CustomerDepositLockListDTO>> getCustomerDepositLockList(@RequestBody List<String> serveNoList) {
        return Result.getInstance(serveDomainServiceI.getCustomerDepositLockList(serveNoList));
    }

    @Override
    @PostMapping("/lockDeposit")
    @PrintParam
    public Result lockDeposit(@RequestBody List<CustomerDepositLockConfirmDTO> confirmDTOList) {
        Map<String, BigDecimal> updateDepositMap = new HashMap<>();
        confirmDTOList.stream().forEach(confirmDTO -> {
            updateDepositMap.put(confirmDTO.getServeNo(), confirmDTO.getLockAmount());
        });
        serveEntityApi.updateServeDepositByServeNoList(updateDepositMap, confirmDTOList.get(0).getCreatorId());
        return Result.getInstance(true).success();
    }


    private String getExpectRecoverDate(Date deliverVehicleDate, int offset) {
        DateTime dateTime = DateUtil.endOfMonth(deliverVehicleDate);
        String deliverDate = DateUtil.formatDate(deliverVehicleDate);
        String endDate = DateUtil.formatDate(dateTime);
        if (deliverDate.equals(endDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.endOfMonth(dateTime));
            calendar.add(Calendar.MONTH, offset);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return DateUtil.formatDate(calendar.getTime());
        } else {
            return DateUtil.formatDate(DateUtil.offsetMonth(deliverVehicleDate, offset));
        }
    }

}
