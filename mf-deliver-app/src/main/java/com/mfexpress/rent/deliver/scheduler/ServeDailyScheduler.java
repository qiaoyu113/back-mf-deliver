package com.mfexpress.rent.deliver.scheduler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.DailyDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 租赁日报
 * 统计昨天在租服务单信息
 */
@Slf4j
@Component
public class ServeDailyScheduler {

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;
    @Resource
    DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private RedisLockRegistry redisLockRegistry;
    private static final String REDIS_LOCK_KEY_SERVE_DAILY = "mf-deliver:ServeDaily:time:";

    @Value("${spring.profiles}")
    private String envVariable;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.REPAIR.getCode());


    @Scheduled(cron = "0 30 23 * * ? ")
    public void process() {
        String key = envVariable + REDIS_LOCK_KEY_SERVE_DAILY + DateUtil.format(new Date(), DatePattern.NORM_DATE_FORMAT);
        Lock lock = redisLockRegistry.obtain(key);
        if (!lock.tryLock()) {
            log.info("定时已经执行");
            return;
        }
        boolean flag = true;
        ServeListQry qry = new ServeListQry();
        qry.setStatuses(defaultStatuses);
        qry.setPage(1);
        qry.setLimit(1000);
        while (flag) {
            Result<PagePagination<Serve>> pagePaginationResult = serveAggregateRootApi.getPageServeByQry(qry);
            if (!ResultValidUtils.checkResult(pagePaginationResult)) {
                break;
            }
            PagePagination<Serve> pagePagination = pagePaginationResult.getData();

            List<Serve> serveList = pagePagination.getList();
            List<String> serveNoList = serveList.stream().map(Serve::getServeNo).collect(Collectors.toList());
            //只有一个有效的交付单
            Result<Map<String, Deliver>> deliverResult = deliverAggregateRootApi.getDeliverByServeNoList(serveNoList);
            Map<String, Deliver> deliverMap = deliverResult.getData();
            List<String> deliverNoList = deliverMap.values().stream().map(Deliver::getDeliverNo).collect(Collectors.toList());
            //发车单信息
            Result<List<DeliverVehicleDTO>> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleByDeliverNoList(deliverNoList);
            List<DeliverVehicleDTO> deliverVehicleDTOList = deliverVehicleResult.getData();
            Map<String, DeliverVehicleDTO> deliverVehicleMap = deliverVehicleDTOList.stream().collect(Collectors.toMap(DeliverVehicleDTO::getServeNo, Function.identity()));
            List<DailyDTO> dailyList = new ArrayList<>(1000);
            for (Serve serve : serveList) {
                DeliverVehicleDTO deliverVehicleDTO = deliverVehicleMap.get(serve.getServeNo());
                Deliver deliver = deliverMap.get(serve.getServeNo());
                if (Objects.isNull(deliver) || Objects.isNull(deliverVehicleDTO)) {
                    log.error("交付单或者发车单不存在，服务单编号：" + serve.getServeNo());
                    continue;
                }
                //发车时间
                Date deliverVehicleTime = deliverVehicleDTO.getDeliverVehicleTime();
                DateTime todayDate = DateUtil.parseDate(DateUtil.formatDate(new Date()));
                //发车日期在当天或之后
                if (todayDate.isBeforeOrEquals(deliverVehicleTime)) {
                    continue;
                }
                DailyDTO daily = new DailyDTO();
                BeanUtil.copyProperties(serve, daily);
                daily.setDelFlag(0);
                daily.setRentDate(DateUtil.formatDate(new Date()));
                if (serve.getStatus().equals(ServeEnum.REPAIR.getCode())) {
                    daily.setRepairFlag(1);
                } else {
                    daily.setRepairFlag(0);
                }
                daily.setVehicleId(deliver.getCarId());
                daily.setCarNum(deliver.getCarNum());
                daily.setChargeFlag(1);
                dailyList.add(daily);
            }
            dailyAggregateRootApi.createDaily(dailyList);
            int totalPages = pagePagination.getPagination().getTotalPages();
            int nowPage = pagePagination.getPagination().getPage();
            int nextPage = nowPage + 1;
            qry.setPage(nextPage);
            if (nextPage > totalPages) {
                flag = false;
            }

        }
    }


}
