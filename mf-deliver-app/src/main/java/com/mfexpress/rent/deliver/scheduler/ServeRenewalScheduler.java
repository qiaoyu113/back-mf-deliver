package com.mfexpress.rent.deliver.scheduler;

import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.PassiveRenewalServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewalReplaceServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class ServeRenewalScheduler {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private RedisTools redisTools;

    @Value("${spring.profiles}")
    private String envVariable;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.REPAIR.getCode());

    private final int limit = 100;

    // 每天的0时10分0秒执行一次
    @Scheduled(cron = "0 10 0 * * *")
    public void process() {
        // 生产环境同一个服务多机器部署，可能会出现多机器同时执行定时任务的情况，这里用了全局锁，使用redis的set类型，谁设置成功谁执行
        // 过期时间1分钟
        long l = redisTools.sSetAndTime(envVariable + ":mf-deliver:serve_recover_time_out_passive_renewal_lock_key", 60, 0);
        // 设置不成功会返回0
        if(l == 0){
            return;
        }
        log.info("超时未收车的服务单自动续约定时任务开始执行");

        ServeListQry qry = new ServeListQry();
        qry.setStatuses(defaultStatuses);
        qry.setReplaceFlag(JudgeEnum.NO.getCode());
        Result<Long> countResult = serveAggregateRootApi.getCountByQry(qry);
        Long count = ResultDataUtils.getInstance(countResult).getDataOrException();
        if(count == null || 0 == count){
            log.error("自动续约失败，查询到符合条件的服务单数量失败或者数量为0");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = new Date();
        try {
            nowDate = dateFormat.parse(dateFormat.format(nowDate));
        } catch (ParseException e) {
            log.error("自动续约失败，日期格式化失败");
            e.printStackTrace();
            return;
        }

        // 找出所有的需要自动续约的服务单
        int page = 1;
        for (int i = 0; i < count; i += limit) {
            List<Serve> needPassiveRenewalServeList = new ArrayList<>();
            List<String> repairServeNoList = new ArrayList<>();
            qry.setPage(page);
            page++;
            qry.setLimit(limit);
            Result<PagePagination<Serve>> pagePaginationResult = serveAggregateRootApi.getPageServeByQry(qry);
            PagePagination<Serve> pagePagination = ResultDataUtils.getInstance(pagePaginationResult).getDataOrException();
            if(null != pagePagination && null != pagePagination.getList() && !pagePagination.getList().isEmpty()){
                List<Serve> serves = pagePagination.getList();
                Date finalNowDate = nowDate;
                serves.forEach(serve -> {
                    if("FWD2021112600007".equals(serve.getServeNo())){
                        System.out.println(1);
                    }
                    String leaseEndDateChar = serve.getLeaseEndDate();
                    if (!StringUtils.isEmpty(leaseEndDateChar)) {
                        Date leaseEndDate = DateUtil.parse(leaseEndDateChar);
                        if (leaseEndDate.before(finalNowDate)) {
                            // 租赁结束日期在当前日期之前，那么此服务单需要被自动续约，只判断到天
                            needPassiveRenewalServeList.add(serve);
                            if(ServeEnum.REPAIR.getCode().equals(serve.getStatus())){
                                repairServeNoList.add(serve.getServeNo());
                            }
                        }
                    }
                });
            }
            if(!needPassiveRenewalServeList.isEmpty()){
                PassiveRenewalServeCmd passiveRenewalServeCmd = new PassiveRenewalServeCmd();
                // -999 代表系统
                passiveRenewalServeCmd.setOperatorId(-999);
                passiveRenewalServeCmd.setNeedPassiveRenewalServeList(needPassiveRenewalServeList);
                Result<Integer> renewalResult = serveAggregateRootApi.passiveRenewalServe(passiveRenewalServeCmd);
                if (ResultErrorEnum.SUCCESSED.getCode() != renewalResult.getCode() || null == renewalResult.getData()) {
                    log.error("分批自动续约失败，msg：" + renewalResult.getMsg());
                }
                log.info("分批自动续约了" + renewalResult.getData() + "条服务单");

                // 被续约的服务单状态如果是维修中的话，访问维修域获取其替换车的服务单，如果有的话
                if(!repairServeNoList.isEmpty()){
                    Result<Map<String, String>> serveNoWithReplaceServeNoMapResult = maintenanceAggregateRootApi.getReplaceServeNoListByRepairServeNoList(repairServeNoList);
                    log.info("续签合同操作，访问维修域，根据维修状态的服务单号：{}，查询到的替换车的服务单号：{}", repairServeNoList, serveNoWithReplaceServeNoMapResult);
                    Map<String, String> serveNoWithReplaceServeNoMap = ResultDataUtils.getInstance(serveNoWithReplaceServeNoMapResult).getDataOrException();
                    if(null != serveNoWithReplaceServeNoMap && !serveNoWithReplaceServeNoMap.isEmpty()){
                        // 替换车续约
                        RenewalReplaceServeCmd renewalReplaceServeCmd = new RenewalReplaceServeCmd();
                        renewalReplaceServeCmd.setOperatorId(-999);
                        renewalReplaceServeCmd.setServeNoWithReplaceServeNoMap(serveNoWithReplaceServeNoMap);
                        Result<Integer> result1 = serveAggregateRootApi.renewalReplaceServe(renewalReplaceServeCmd);
                        log.info("续签合同操作，续约替换车的服务单返回结果：{}",result1);
                    }
                }
            }
        }
    }

}
