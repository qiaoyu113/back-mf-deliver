package com.mfexpress.rent.deliver.scheduler;

import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeChangeRecordDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.UndoReactiveServeCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RestController
@RequestMapping("/reactiveServeCheckScheduler")
public class ReactiveServeCheckScheduler {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private RedisTools redisTools;

    @Value("${spring.profiles}")
    private String envVariable;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.NOT_PRESELECTED.getCode(), ServeEnum.PRESELECTED.getCode());

    private final int limit = 100;

    // 每天的0时10分0秒执行一次
    @Scheduled(cron = "0 30 23 * * *")
    @GetMapping("/execute")
    public void process() {
        long l = redisTools.sSetAndTime(envVariable + ":mf-deliver:reactive_serve_check_lock_key", 60, 0);
        // 设置不成功会返回0
        if (l == 0) {
            return;
        }
        log.info("检查重新激活服务单定时任务开始执行");

        ServeListQry qry = new ServeListQry();
        qry.setStatuses(defaultStatuses);
        qry.setReplaceFlag(JudgeEnum.NO.getCode());
        Result<Long> countResult = serveAggregateRootApi.getCountByQry(qry);
        Long count = ResultDataUtils.getInstance(countResult).getDataOrException();
        if (count == null || 0 == count) {
            log.error("检查重新激活服务单失败，查询到符合条件的服务单数量失败或者数量为0");
            return;
        }
        // 找出所有在未预选和待发车的服务单
        Date nowDate = new Date();
        int page = 1;
        for (int i = 0; i < count; i += limit) {
            qry.setPage(page);
            page++;
            qry.setLimit(limit);
            Result<PagePagination<ServeDTO>> pageResult = serveAggregateRootApi.getServePageByQry(qry);
            PagePagination<ServeDTO> servePage = ResultDataUtils.getInstance(pageResult).getDataOrException();
            if (null != servePage && null != servePage.getList() && !servePage.getList().isEmpty()) {
                List<ServeDTO> serves = servePage.getList();
                for (ServeDTO serve : serves) {
                    Result<List<ServeChangeRecordDTO>> serveChangeRecordSResult = serveAggregateRootApi.getServeChangeRecordListByServeNo(serve.getServeNo());
                    List<ServeChangeRecordDTO> serveChangeRecordDTOS = ResultDataUtils.getInstance(serveChangeRecordSResult).getDataOrException();
                    if (null != serveChangeRecordDTOS && !serveChangeRecordDTOS.isEmpty()) {
                        for (ServeChangeRecordDTO serveChangeRecordDTO : serveChangeRecordDTOS) {
                            if (ServeChangeRecordEnum.REACTIVE.getCode() == serveChangeRecordDTO.getType()) {
                                // 如果服务单进行过重新激活操作，判断预计收车日期，如果当天是预计收车日期，回退服务单到已收车状态，删除交付单
                                if (DateUtil.formatDate(nowDate).equals(serve.getExpectRecoverDate())) {
                                    Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(serve.getServeNo());
                                    DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverResult).getDataOrNull();
                                    if (null != deliverDTO) {
                                        if (DeliverContractStatusEnum.GENERATING.getCode() == deliverDTO.getDeliverContractStatus() || DeliverContractStatusEnum.SIGNING.getCode() == deliverDTO.getDeliverContractStatus()) {
                                            // 如果发车电子交接单正在签署中，延后其预计收车日期
                                            serveAggregateRootApi.extendExpectRecoverDate(serve.getServeNo());
                                        } else {
                                            UndoReactiveServeCmd undoReactiveServeCmd = new UndoReactiveServeCmd();
                                            undoReactiveServeCmd.setServeNo(serve.getServeNo());
                                            undoReactiveServeCmd.setOperatorId(-999);
                                            serveAggregateRootApi.undoReactiveServe(undoReactiveServeCmd);
                                        }
                                    } else {
                                        UndoReactiveServeCmd undoReactiveServeCmd = new UndoReactiveServeCmd();
                                        undoReactiveServeCmd.setServeNo(serve.getServeNo());
                                        undoReactiveServeCmd.setOperatorId(-999);
                                        serveAggregateRootApi.undoReactiveServe(undoReactiveServeCmd);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
