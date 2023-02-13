package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.domainservice.ServeDomainServiceI;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverCancelCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.*;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ContractWillExpireInfoDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServePrepaymentDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ContractWillExpireQry;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.ServeChangeRecordPO;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeAdjustEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.*;
import com.mfexpress.rent.deliver.po.ServeAdjustPO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceStatusEnum;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
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
    private DeliverEntityApi deliverEntityApi;
    @Resource
    private ServeEntityApi serveEntityApi;
    @Resource
    private ServeDomainServiceI serveDomainServiceI;
    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private ServeAdjustGateway serveAdjustGateway;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ServeAdjustEntityApi serveAdjustEntityApi;

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
            }
            if (!StringUtils.isEmpty(serve.getLeaseBeginDate())) {
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
                serve.setRentRatio(BigDecimal.ONE);
                serve.setContractCommodityId(serveVehicleDTO.getContractCommodityId());

                serve.setContractId(serveVehicleDTO.getContractId());
                serve.setOaContractCode(serveVehicleDTO.getOaContractCode());
                serve.setDeposit(BigDecimal.valueOf(serveVehicleDTO.getDeposit()));
                serve.setLeaseBeginDate(serveVehicleDTO.getLeaseBeginDate());
                serve.setLeaseMonths(serveVehicleDTO.getLeaseMonths());
                serve.setLeaseDays(serveVehicleDTO.getLeaseDays());
                serve.setLeaseEndDate(serveVehicleDTO.getLeaseEndDate());
                serve.setBillingAdjustmentDate("");
                serve.setRenewalType(0);
                serve.setExpectRecoverDate("");
                serve.setPayableDeposit(BigDecimal.valueOf(serveVehicleDTO.getDeposit()));
                serve.setPaidInDeposit(BigDecimal.valueOf(serveVehicleDTO.getDeposit()));
                serve.setRentRatio(BigDecimal.valueOf(serveVehicleDTO.getRentRatio()));
                //增加具体业务类型
                ServeContractTemplateEnum serveContractTemplate = ServeContractTemplateEnum.getServeContractTemplate(serveVehicleDTO.getTemplateName());
                serve.setBusinessType(Objects.isNull(serveContractTemplate) ? ServeContractTemplateEnum.RENT.getBusinessType() : serveContractTemplate.getBusinessType());

                serveList.add(serve);

                //预付款
                if (serveVehicleDTO.getAdvancePaymentAmount().compareTo(BigDecimal.ZERO) != 0) {
                    if (serve.getRent().compareTo(BigDecimal.ZERO) != 0) {
                        ServePrepaymentDTO servePrepaymentDTO = new ServePrepaymentDTO();
                        servePrepaymentDTO.setServeNo(serve.getServeNo());
                        servePrepaymentDTO.setPrepaymentAmount(serveVehicleDTO.getAdvancePaymentAmount());
                        servePrepaymentDTO.setCustomerId(serve.getCustomerId());
                        servePrepaymentDTO.setOrgId(serve.getOrgId());
                        servePrepaymentDTO.setCityId(serve.getCityId());
                        mqTools.send(redisTools.getEnv() + "_event", "prepayment_serve", null, JSON.toJSONString(servePrepaymentDTO));
                    }
                }


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

        // 补充是否为重新激活标志位
        Map<String, ServeChangeRecordPO> serveReactiveFlagMap = null;
        List<ServeChangeRecordPO> recordPOS = serveChangeRecordGateway.getListByServeNoListAndType(serveNoList, ServeChangeRecordEnum.REACTIVE.getCode());
        if (!recordPOS.isEmpty()) {
            serveReactiveFlagMap = recordPOS.stream().collect(Collectors.toMap(ServeChangeRecordPO::getServeNo, Function.identity(), (v1, v2) -> v1));
        }

        Map<String, ServeChangeRecordPO> finalServeReactiveFlagMap = serveReactiveFlagMap;
        List<ServeDTO> serveDTOList = serveList.stream().map(serveEntity -> {
            ServeDTO serveDTO = new ServeDTO();
            BeanUtils.copyProperties(serveEntity, serveDTO);
            if (null != finalServeReactiveFlagMap && null != finalServeReactiveFlagMap.get(serveEntity.getServeNo())) {
                serveDTO.setReactiveFlag(JudgeEnum.YES.getCode());
            } else {
                serveDTO.setReactiveFlag(JudgeEnum.NO.getCode());
            }
            return serveDTO;
        }).collect(Collectors.toList());

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
        // 替换车申请的服务单 其月租金和押金应为0
        serve.setRent(serveAddDTO.getRent() != null ? serveAddDTO.getRent() : BigDecimal.ZERO);
        serve.setRentRatio(serveAddDTO.getRentRatio());
        serve.setDeposit(BigDecimal.ZERO);
        serve.setPaidInDeposit(BigDecimal.ZERO);
        serve.setPayableDeposit(BigDecimal.ZERO);

        try {
            serveGateway.addServeList(Collections.singletonList(serve));
        } catch (Exception e) {
            e.printStackTrace();
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
        List<ServeDTO> serveDTOS = BeanUtil.copyToList(serveListByOrderIds, ServeDTO.class, CopyOptions.create().ignoreCase().ignoreError());
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

        // 租赁开始日期必须大于发车日期校验 ----- 20220824 逻辑屏蔽，该校验逻辑已前置到创建编辑合同时
        /*List<DeliverEntity> deliverEntityList = deliverGateway.getDeliverByServeNoList(serveNoList);
        if (deliverEntityList.size() != serveNoList.size()) {
            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "交车单查询失败");
        }
        List<String> deliverNoList = deliverEntityList.stream().map(DeliverEntity::getDeliverNo).collect(Collectors.toList());
        List<DeliverVehicleEntity> deliverVehicleList = deliverVehicleGateway.getDeliverVehicleByDeliverNoList(deliverNoList);
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
        });*/

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

        List<Integer> commodityIds = renewalServeCmdList.stream().map(RenewalServeCmd::getContractCommodityId).distinct().collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIds);
        Map<Integer, CommodityDTO> commodityDTOMap = commodityListResult.getData().stream().collect(Collectors.toMap(CommodityDTO::getId, a -> a));
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
            // goodsId为合同商品id，不应更改服务单的商品id字段
            serve.setContractCommodityId(renewalServeCmd.getContractCommodityId());
            serve.setGoodsId(null);
            BigDecimal deposit = new BigDecimal(renewalServeCmd.getDeposit().toString());
            serve.setDeposit(deposit);
            serve.setPayableDeposit(deposit);

            ServeChangeRecordPO record = new ServeChangeRecordPO();
            ServeEntity rawDataServe = serveMap.get(serve.getServeNo());
            if (null == rawDataServe) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "服务单获取失败" + serve.getServeNo());
            }
            record.setServeNo(serve.getServeNo());
            record.setType(ServeChangeRecordEnum.RENEWAL.getCode());
            record.setRenewalType(ServeRenewalTypeEnum.ACTIVE.getCode());
            record.setRawGoodsId(rawDataServe.getContractCommodityId());
            record.setRawData(JSONUtil.toJsonStr(rawDataServe));
            record.setNewGoodsId(serve.getContractCommodityId());
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
            renewalChargeCmd.setBusinessType(serve.getBusinessType());
            // 根据计费调整日期是否有值来决定计费价格是否发生变化
            if (StringUtils.isEmpty(renewalServeCmd.getBillingAdjustmentDate())) {
                renewalChargeCmd.setEffectFlag(false);
            } else {
                // 续签时，选择的计费调整日期，如选择的本月，但是续签合同在次月生效后，则系统中客户月账单从次月1日开始根据新月租金计费
                renewalChargeCmd.setEffectFlag(true);
                renewalChargeCmd.setRent(serve.getRent());
                String billingAdjustmentDate = renewalServeCmd.getBillingAdjustmentDate();
                int billingAdjustmentYear = DateUtil.parseDate(billingAdjustmentDate).getField(DateField.YEAR);
                int billingAdjustmentMonth = DateUtil.parseDate(billingAdjustmentDate).getField(DateField.MONTH);
                DateTime nowDateTime = new DateTime();
                int nowDateYear = nowDateTime.getField(DateField.YEAR);
                int nowDateMonth = nowDateTime.getField(DateField.MONTH);
                if (billingAdjustmentYear != nowDateYear) {
                    renewalChargeCmd.setRentEffectDate(DateUtil.beginOfMonth(nowDateTime).toString("yyyy-MM-dd"));
                } else {
                    if (billingAdjustmentMonth < nowDateMonth) {
                        renewalChargeCmd.setRentEffectDate(DateUtil.beginOfMonth(nowDateTime).toString("yyyy-MM-dd"));
                    } else {
                        renewalChargeCmd.setRentEffectDate(renewalServeCmd.getBillingAdjustmentDate());
                    }
                }
            }
            if (Objects.nonNull(commodityDTOMap.get(serve.getContractCommodityId()))) {
                renewalChargeCmd.setRentRatio(commodityDTOMap.get(serve.getContractCommodityId()).getRentRatio());
            } else {
                renewalChargeCmd.setRentRatio(0.00);
            }
            /*if (rawDataServe.getRent().equals(serve.getRent())) {
                renewalChargeCmd.setEffectFlag(false);
            } else {
                renewalChargeCmd.setEffectFlag(true);
                renewalChargeCmd.setRent(serve.getRent());
                renewalChargeCmd.setRentEffectDate(renewalServeCmd.getBillingAdjustmentDate());
            }*/
            renewalChargeCmd.setRenewalDate(renewalServeCmd.getLeaseEndDate());
            renewalChargeCmd.setVehicleBusinessMode(deliver.getVehicleBusinessMode());
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

        List<Integer> commodityIds = replaceServeList.stream().map(ServeEntity::getContractCommodityId).distinct().collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIds);
        if (CollectionUtil.isEmpty(commodityListResult.getData())) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到商品信息");
        }

        Map<Integer, CommodityDTO> commodityDTOMap = commodityListResult.getData().stream().collect(Collectors.toMap(CommodityDTO::getId, a -> a));

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
            record.setType(ServeChangeRecordEnum.RENEWAL.getCode());
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
                    renewalChargeCmd.setRentRatio(commodityDTOMap.get(replaceServe.getContractCommodityId()).getRentRatio());
                    renewalChargeCmd.setVehicleBusinessMode(deliver.getVehicleBusinessMode());
                    renewalChargeCmd.setBusinessType(replaceServe.getBusinessType());
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

        List<Integer> commodityIds = needPassiveRenewalServeList.stream().map(Serve::getContractCommodityId).distinct().collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIds);
        Map<Integer, CommodityDTO> commodityDTOMap = commodityListResult.getData().stream().collect(Collectors.toMap(CommodityDTO::getId, a -> a));
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
            record.setType(ServeChangeRecordEnum.RENEWAL.getCode());
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
            renewalChargeCmd.setBusinessType(serve.getBusinessType());
            if (Objects.nonNull(commodityDTOMap.get(serve.getContractCommodityId()))) {
                renewalChargeCmd.setRentRatio(commodityDTOMap.get(serve.getContractCommodityId()).getRentRatio());
            } else {
                renewalChargeCmd.setRentRatio(0.00);
            }
            renewalChargeCmd.setVehicleBusinessMode(deliver.getVehicleBusinessMode());
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
        List<ServeChangeRecordPO> recordList = serveChangeRecordGateway.getList(serveNo, ServeChangeRecordEnum.RENEWAL.getCode());
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
        BeanUtil.copyProperties(pagePagination, servePagePagination, new CopyOptions().ignoreError());
        List<ServeEntity> serveEntityList = pagePagination.getList();
        List<Serve> serveList = BeanUtil.copyToList(serveEntityList, Serve.class, new CopyOptions().ignoreError());
        servePagePagination.setList(serveList);
        return Result.getInstance(servePagePagination).success();
    }

    @Override
    @PostMapping("/getServePageByQry")
    @PrintParam
    public Result<PagePagination<ServeDTO>> getServePageByQry(@RequestBody ServeListQry qry) {
        PagePagination<ServeEntity> pagePagination = serveGateway.getPageServeByQry(qry);
        PagePagination<ServeDTO> servePagePagination = new PagePagination<>();
        BeanUtil.copyProperties(pagePagination, servePagePagination, new CopyOptions().ignoreError());
        List<ServeEntity> serveEntityList = pagePagination.getList();
        List<ServeDTO> serveList = BeanUtil.copyToList(serveEntityList, ServeDTO.class, new CopyOptions().ignoreError());
        servePagePagination.setList(serveList);
        return Result.getInstance(servePagePagination).success();
    }

    @Override
    @PostMapping("/extendExpectRecoverDate")
    @PrintParam
    public Result<Integer> extendExpectRecoverDate(@RequestParam("serveNo") String serveNo) {
        ServeEntity serveEntity = serveGateway.getServeByServeNo(serveNo);
        if (null == serveEntity) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单查询失败");
        }
        String expectRecoverDate = getExpectRecoverDate(DateUtil.parseDate(serveEntity.getExpectRecoverDate()), 1);
        return Result.getInstance(serveEntityApi.extendExpectRecoverDate(serveNo, expectRecoverDate)).success();
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
    public Result unLockDeposit(@RequestParam("serveNoList") List<String> serveNoList, @RequestParam("creatorId") Integer creatorId, @RequestParam("isTermination") Boolean isTermination) {
        List<ServeDTO> serveDTOList = serveEntityApi.getServeListByServeNoList(serveNoList);
        if (CollectionUtil.isNotEmpty(serveDTOList)) {
            serveDTOList.forEach(serveDTO -> {
                //不是终止服务单
                if (!isTermination) {
                    if (!ServeEnum.RECOVER.getCode().equals(serveDTO.getStatus())) {
                        log.error("解锁 ------- 服务单不满足解锁条件 参数：{}", serveDTO);
                        throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "服务单不满足解锁条件");
                    }
                }
            });
        }
        //查询需要解锁得服务单押金列表
        List<ServeDTO> serveList = serveEntityApi.getServeListByServeNoList(serveNoList);
        Map<String, BigDecimal> updateDepositMap = new HashMap<>();
        serveList.forEach(serveDTO -> {
            updateDepositMap.put(serveDTO.getServeNo(), serveDTO.getPaidInDeposit().negate());
        });
        serveEntityApi.updateServeDepositByServeNoList(updateDepositMap, creatorId, false, isTermination);
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
        List<String> serveNos = confirmDTOList.stream().map(CustomerDepositLockConfirmDTO::getServeNo).distinct().collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(serveNos)) {
            List<ServeDTO> serveDTOList = serveEntityApi.getServeListByServeNoList(serveNos);
            if (CollectionUtil.isNotEmpty(serveDTOList)) {
                //判断服务单状态
                serveDTOList.forEach(serveDTO -> {
                    if (ServeEnum.COMPLETED.getCode().equals(serveDTO.getStatus()) || ServeEnum.RECOVER.getCode().equals(serveDTO.getStatus())) {
                        log.error("锁定 ------- 服务单不满足锁定条件 参数：{}", serveDTO);
                        throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "服务单不满足锁定条件");
                    }
                });
            }
        }
        Map<String, BigDecimal> updateDepositMap = new HashMap<>();
        confirmDTOList.forEach(confirmDTO -> {
            updateDepositMap.put(confirmDTO.getServeNo(), confirmDTO.getLockAmount());
        });
        serveEntityApi.updateServeDepositByServeNoList(updateDepositMap, confirmDTOList.get(0).getCreatorId(), true, false);
        return Result.getInstance(true).success();
    }

    @Override
    @PostMapping("/getReplaceNumByCustomerIds")
    @PrintParam
    public Result<Map<Integer, Integer>> getReplaceNumByCustomerIds(@RequestBody List<Integer> customerIds) {
        return Result.getInstance(serveGateway.getReplaceNumByCustomerIds(customerIds)).success();
    }

    @Override
    @PostMapping("/getRentingServeNumByCustomerId")
    @PrintParam
    public Result<Integer> getRentingServeNumByCustomerId(@RequestParam("customerId") Integer customerId) {
        return Result.getInstance(serveGateway.getRentingServeNumByCustomerId(customerId)).success();
    }


    @Override
    @PostMapping("/reactiveServe")
    @PrintParam
    @Transactional
    public Result<Integer> reactiveServe(@RequestBody @Validated ReactivateServeCmd cmd) {
        ServeEntity serveEntity = serveGateway.getServeByServeNo(cmd.getServeNo());
        if (null == serveEntity || !ServeEnum.RECOVER.getCode().equals(serveEntity.getStatus())) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单状态异常");
        }
        if (LeaseModelEnum.NORMAL.getCode() != serveEntity.getLeaseModelId() && LeaseModelEnum.DISCOUNT.getCode() != serveEntity.getLeaseModelId() && LeaseModelEnum.SHOW.getCode() != serveEntity.getLeaseModelId()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前租赁方式不允许重新激活");
        }
        DeliverEntity deliverEntity = deliverGateway.getDeliverByServeNo(serveEntity.getServeNo());
        if (null == deliverEntity || (!DeliverEnum.RECOVER.getCode().equals(deliverEntity.getDeliverStatus()) && !DeliverEnum.COMPLETED.getCode().equals(deliverEntity.getDeliverStatus()))) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
        }
        /*RecoverVehicleEntity recoverVehicleEntity = recoverVehicleGateway.getRecoverVehicleByDeliverNo(deliverEntity.getDeliverNo());
        if (null == recoverVehicleEntity) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车单查询失败");
        }

        long betweenDays = DateUtil.between(recoverVehicleEntity.getRecoverVehicleTime(), DateUtil.parse(serveEntity.getExpectRecoverDate()), DateUnit.DAY);
        if (betweenDays < 15) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期与预计收车日期过近，服务单不允许激活");
        }*/
        String expectRecoverDateChar = serveEntity.getExpectRecoverDate();
        DateTime expectRecoverDate = DateUtil.parseDate(expectRecoverDateChar);
        Date nowDate = DateUtil.parseDate(DateUtil.formatDate(new Date()));
        if (nowDate.after(expectRecoverDate)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "已过预计收车日期，服务单不允许激活");
        }
        if (2 > DateUtil.between(nowDate, expectRecoverDate, DateUnit.DAY)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期与当前日期过近，服务单不允许激活");
        }

        // 重新激活服务单，置服务单状态为待预选
        cmd.setDeliverNo(deliverEntity.getDeliverNo());
        serveEntityApi.reactiveServe(cmd);
        // 置交付单状态为历史所属
        deliverEntityApi.toHistory(cmd);
        return Result.getInstance(0).success();
    }

    @Override
    public Result<PagePagination<String>> getServeNoListByPage(ListQry listQry) {
        PagePagination<ServeEntity> pagePagination = serveGateway.getServeNoListByPage(listQry);
        List<ServeEntity> serveEntityList = pagePagination.getList();
        List<String> serveNoList = serveEntityList.stream().map(ServeEntity::getServeNo).collect(Collectors.toList());

        PagePagination<String> serveNoListPagePagination = new PagePagination<>();
        serveNoListPagePagination.setPage(pagePagination.getPage());
        serveNoListPagePagination.setPagination(pagePagination.getPagination());
        serveNoListPagePagination.setList(serveNoList);
        return Result.getInstance(serveNoListPagePagination).success();
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

    @Override
    @PostMapping(value = "/cancel")
    @PrintParam
    @Transactional
    public Result<Integer> cancelServe(ServeCancelCmd cmd) {

        // 取消服务单
        serveEntityApi.cancelServe(cmd);

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrNull();
        if (deliverDTO != null) {

            DeliverCancelCmd deliverCancelCmd = new DeliverCancelCmd();
            deliverCancelCmd.setServeNo(cmd.getServeNo());
            deliverCancelCmd.setOperatorId(cmd.getOperatorId());

            // 取消交付单
            deliverAggregateRootApi.cancelDeliver(deliverCancelCmd);

            // 修改车辆状态
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(Collections.singletonList(deliverDTO.getCarId()));
            vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
            vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());

            ResultValidUtils.checkResultException(vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd));
        }

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping(value = "/recover/check/judge")
    @PrintParam
    public Result<Integer> recoverCheckJudge(RecoverCheckJudgeCmd cmd) {

        Result<ServeDTO> serveDTOResult = getServeDtoByServeNo(cmd.getServeNo());

        if (!Optional.ofNullable(serveDTOResult).map(r -> r.getData()).isPresent()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), ResultErrorEnum.OPER_ERROR.getName());
        }

        if (ServeEnum.REPAIR.getCode().equals(serveDTOResult.getData().getStatus())) {

            // 查询车辆维修单
            MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceByServeNo(maintenanceAggregateRootApi, cmd.getServeNo());

            if (Optional.ofNullable(maintenanceDTO).filter(m -> (MaintenanceStatusEnum.MAINTAINING.getCode().compareTo(m.getStatus()) == 0
                    || MaintenanceStatusEnum.MAINTAINED.getCode().compareTo(m.getStatus()) == 0) &&
                    MaintenanceTypeEnum.ACCIDENT.getCode().equals(m.getType())).isPresent()) {
                // 事故维修单
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆处于事故维修中，无法进行收车。");
            } else {
                // 查找替换车服务单
                ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, cmd.getServeNo());

                log.info("replaceVehicleDTO----->{}", replaceVehicleDTO);

                if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {

                    String replaceServeNo = replaceVehicleDTO.getServeNo();

                    ServeDTO replaceServeDTO = ResultDataUtils.getInstance(getServeDtoByServeNo(replaceServeNo)).getDataOrException();
                    // 替换服务单状态为0、1、2、5时，判断是否有调整工单，如果没有则不允许原车验车
                    if (Optional.ofNullable(replaceServeDTO).filter(o -> ServeEnum.NOT_PRESELECTED.getCode().equals(o.getStatus())
                            || ServeEnum.PRESELECTED.getCode().equals(o.getStatus()) || ServeEnum.DELIVER.getCode().equals(o.getStatus())
                            || ServeEnum.REPAIR.getCode().equals(o.getStatus())).isPresent()) {

                        // 查询是否存在调整工单
                        ServeAdjustQry serveAdjustQry = ServeAdjustQry.builder().serveNo(replaceServeNo).build();

                        ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(getServeAdjust(serveAdjustQry)).getDataOrNull();

                        if (!Optional.ofNullable(serveAdjustDTO).isPresent()) {
                            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在未发车的替换单或存在替换车，无法进行收车。");
                        }
                    }
                }

            }
        }

        return Result.getInstance(0).success();
    }

    @Override
    @PrintParam
    public Result<ServeAdjustDTO> getServeAdjust(ServeAdjustQry qry) {

        ServeAdjustPO po = serveAdjustGateway.getByServeNo(qry.getServeNo());

        if (po == null) {
            return null;
        }

        ServeAdjustDTO dto = new ServeAdjustDTO();
        BeanUtils.copyProperties(po, dto);

        return Result.getInstance(dto).success();
    }

    @Override
    @PostMapping(value = "/serve/adjust")
    @PrintParam
    public Result<Integer> serveAdjustment(@RequestBody ServeAdjustCmd cmd) {

        serveAdjustEntityApi.save(cmd);

        return Result.getInstance(0).success();
    }

    @Override
    @PrintParam
    public Result<Integer> serveAdjustStartBilling(ServeAdjustStartBillingCmd cmd) {

        return Result.getInstance(serveAdjustEntityApi.startBilling(cmd)).success();
    }

    @Override
    @PrintParam
    public Result<Integer> serveAdjustCompleted(ServeAdjustCompletedCmd cmd) {

        return Result.getInstance(serveAdjustEntityApi.completed(cmd)).success();
    }

    @Override
    public Result<Integer> updateServePaidInDeposit(ServePaidInDepositUpdateCmd cmd) {
        Integer servePaidInDeposit = serveEntityApi.updateServePaidInDeposit(cmd);
        if (servePaidInDeposit > 0) {
            return Result.getInstance(servePaidInDeposit).success();
        }
        return Result.getInstance(0).fail(ResultErrorEnum.OPER_ERROR.getCode(), "更新服务单失败");
    }

    @Override
    @PostMapping(value = "/terminationServe")
    @PrintParam
    public Result<Boolean> terminationServe(@RequestBody ServeDTO serveDTO) {

        return Result.getInstance(serveEntityApi.terminationServe(serveDTO)).success();

    }

    @Override
    @PostMapping(value = "/getServeDTOByCustomerId")
    @PrintParam
    public Result<List<ServeDTO>> getServeDTOByCustomerId(@RequestBody Integer customerId) {
        return Result.getInstance(serveEntityApi.getServeDTOByCustomerId(customerId)).success();
    }

    @Override
    @PostMapping(value = "/getContractThatWillExpire")
    @PrintParam
    public Result<List<ContractWillExpireInfoDTO>> getContractThatWillExpire(@RequestBody ContractWillExpireQry contractWillExpireQry) {
        return Result.getInstance(serveDomainServiceI.getContractThatWillExpire(contractWillExpireQry)).success();
    }

    @Override
    @PostMapping(value = "/getServeChangeRecordListByServeNo")
    @PrintParam
    public Result<List<ServeChangeRecordDTO>> getServeChangeRecordListByServeNo(@RequestParam("serveNo") String serveNo) {
        List<ServeChangeRecordPO> recordList = serveChangeRecordGateway.getList(serveNo);
        return Result.getInstance(BeanUtil.copyToList(recordList, ServeChangeRecordDTO.class, CopyOptions.create().ignoreError()));
    }

    @Override
    @PostMapping(value = "/undoReactiveServe")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> undoReactiveServe(@RequestBody @Validated UndoReactiveServeCmd cmd) {
        serveEntityApi.undoReactiveServe(cmd);
        deliverEntityApi.undoHistory(cmd);
        return Result.getInstance(0).success();
    }


    /*@Override
    @PostMapping(value = "/serve/update/payableDeposit")
    @PrintParam
    public Result<Integer> updateServePayableDeposit(@RequestBody ServeUpdatePayableDepositCmd cmd) {
        Integer updateServePayableDeposit = serveEntityApi.updateServePayableDeposit(cmd);
        if (updateServePayableDeposit > 0) {
            return Result.getInstance(updateServePayableDeposit).success();
        }
        return Result.getInstance(0).fail(ResultErrorEnum.OPER_ERROR.getCode(), "更新服务单应缴押金失败");
    }*/
}
