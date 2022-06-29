package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.daily.DailyDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyOperateCmd;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.entity.Daily;
import com.mfexpress.rent.deliver.entity.api.DailyEntityApi;
import com.mfexpress.rent.deliver.gateway.DailyGateway;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping(value = "/domain/deliver/v3/daily")
@RestController
@Api(tags = "domain--交付--1.5租赁日报聚合")
@Slf4j
public class DailyAggregateRootApiImpl implements DailyAggregateRootApi {

    @Resource
    private DailyGateway dailyGateway;
    @Resource
    private DailyEntityApi dailyEntityApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;


    @Override
    @PostMapping("/createDaily")
    @PrintParam
    public Result createDaily(@RequestBody List<DailyDTO> dailyDTOList) {
        if (CollectionUtil.isEmpty(dailyDTOList)) {
            return Result.getInstance(dailyDTOList.size()).success();
        }
        List<String> serveNoList = dailyDTOList.stream().map(DailyDTO::getServeNo).collect(Collectors.toList());
        String rentDate = dailyDTOList.get(0).getRentDate();
        List<Daily> alreadyList = dailyGateway.getDailyByServeNoListAndRentDate(serveNoList, rentDate);
        List<DailyDTO> dailyList = new ArrayList<>();
        //过滤已经存在的日报
        for (DailyDTO dailyDTO : dailyDTOList) {
            long count = alreadyList.stream().filter(daily -> daily.getServeNo().equals(dailyDTO.getServeNo()) && daily.getChargeFlag().equals(dailyDTO.getChargeFlag())).count();
            if (count > 0) {
                continue;
            }
            dailyList.add(dailyDTO);
        }
        if (CollectionUtil.isNotEmpty(dailyList)){
            dailyGateway.addDailyList(BeanUtil.copyToList(dailyList, Daily.class, new CopyOptions().ignoreError()));
        }
        return Result.getInstance(dailyList.size()).success();
    }

    @Override
    @PostMapping("/deliverDaily")
    @PrintParam
    public Result deliverDaily(@RequestBody DailyOperateCmd dailyOperateCmd) {
        //发车日期
        DateTime today = DateUtil.parseDate(DateUtil.formatDate(new Date()));
        Date deliverDate = dailyOperateCmd.getDate();
        List<Serve> serveList = dailyOperateCmd.getServeList();
        Map<String, Deliver> deliverMap = dailyOperateCmd.getDeliverMap();
        List<Daily> dailyList = new ArrayList<>();
        for (Serve serve : serveList) {
            Deliver deliver = deliverMap.get(serve.getServeNo());
            if (Objects.isNull(deliver)) {
                log.error("交付单不存在，服务单编号：" + serve.getServeNo());
                continue;
            }
            if (deliverDate.before(today)) {
                //发车日期小于当天 需要补充至当天
                long diff = DateUtil.between(deliverDate, today, DateUnit.DAY, true);
                for (int i = 0; i <= diff; i++) {
                    Daily daily = getDaily(serve, deliver);
                    if (i == 0) {
                        daily.setChargeFlag(0);
                    } else {
                        daily.setChargeFlag(1);
                    }
                    daily.setRentDate(DateUtil.formatDate(DateUtil.offsetDay(deliverDate, i)));
                    dailyList.add(daily);
                }
            } else {
                //生成当天不计费租赁日报
                Daily daily = getDaily(serve, deliver);
                daily.setChargeFlag(0);
                daily.setRentDate(DateUtil.formatDate(deliverDate));
                dailyList.add(daily);
            }
        }
        if (CollectionUtil.isNotEmpty(dailyList)){
            dailyGateway.addDailyList(BeanUtil.copyToList(dailyList, Daily.class, new CopyOptions().ignoreError()));
        }
        return Result.getInstance(dailyList.size()).success();
    }

    @Override
    @PostMapping("/recoverDaily")
    @PrintParam
    public Result recoverDaily(@RequestBody DailyOperateCmd dailyOperateCmd) {
        //发车日期
        DateTime today = DateUtil.parseDate(DateUtil.formatDate(new Date()));
        Date recoverDate = dailyOperateCmd.getDate();
        List<Serve> serveList = dailyOperateCmd.getServeList();
        Map<String, Deliver> deliverMap = dailyOperateCmd.getDeliverMap();
        List<Daily> dailyList = new ArrayList<>();
        for (Serve serve : serveList) {
            Deliver deliver = deliverMap.get(serve.getServeNo());
            if (Objects.isNull(deliver)) {
                log.error("交付单不存在，服务单编号：" + serve.getServeNo());
                continue;
            }
            if (recoverDate.before(today)) {
                //逻辑删除日报
                dailyGateway.deleteDailyByServeNoAndRentDate(serve.getServeNo(), DateUtil.formatDate(recoverDate));
            } else {

                //补充至收车当日报
                long diff = DateUtil.between(today, recoverDate, DateUnit.DAY);
                int i = 0;
                if (!today.equals(recoverDate)) {
                    i = 1;
                }
                for (int j = i; j <= diff; j++) {
                    Daily daily = getDaily(serve, deliver);
                    daily.setChargeFlag(1);
                    daily.setRentDate(DateUtil.formatDate(DateUtil.offsetDay(today, j)));
                    dailyList.add(daily);
                }
            }
        }
        if (CollectionUtil.isNotEmpty(dailyList)) {
            dailyGateway.addDailyList(dailyList);
        }
        return Result.getInstance(dailyList.size()).success();
    }

    @Override
    @PostMapping("/maintainDaily")
    public Result maintainDaily(@RequestBody DailyMaintainDTO dailyMaintainDTO) {
        dailyEntityApi.operateMaintain(dailyMaintainDTO);
        return Result.getInstance(true).success();
    }


    private Daily getDaily(Serve serve, Deliver deliver) {
        Daily daily = new Daily();
        daily.setServeNo(serve.getServeNo());
        daily.setCarNum(deliver.getCarNum());
        daily.setCustomerId(serve.getCustomerId());
        daily.setDelFlag(0);
        daily.setLeaseModelId(serve.getLeaseModelId());
        daily.setVehicleId(deliver.getCarId());
        daily.setOrgId(serve.getOrgId());
        if (serve.getStatus().equals(ServeEnum.REPAIR.getCode())) {
            daily.setRepairFlag(1);
        } else {
            daily.setRepairFlag(0);
        }
        daily.setReplaceFlag(serve.getReplaceFlag());

        return daily;
    }

    @Override
    @PostMapping(value = "/deliver/recover/daily")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<Integer> createDaily(@RequestBody CreateDailyCmd cmd) {

        Result<Map<String, Serve>> serveResult = serveAggregateRootApi.getServeMapByServeNoList(cmd.getServeNoList());
        Map<String, Serve> serveMap = serveResult.getData();
        List<Serve> serveList = serveMap.values().stream().collect(Collectors.toList());
        Result<Map<String, Deliver>> deliverResult = deliverAggregateRootApi.getDeliverByServeNoList(cmd.getServeNoList());
        Map<String, Deliver> deliverMap = deliverResult.getData();
        DailyOperateCmd dailyCreateCmd = DailyOperateCmd.builder().serveList(serveList).deliverMap(deliverMap).date(cmd.getDeliverRecoverDate()).build();
        //发车标识

        if (1 == cmd.getDeliverFlag()) {
            //发车
            deliverDaily(dailyCreateCmd);
        } else {
            //收车
            recoverDaily(dailyCreateCmd);
        }

        return Result.getInstance(0).success();
    }
}
