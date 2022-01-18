package com.mfexpress.rent.deliver.scheduler;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.PassiveRenewalServeCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ServeRenewalScheduler extends JavaProcessor {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.RECOVER.getCode());

    private final int limit = 100;

    @Override
    public ProcessResult process(JobContext jobContext) throws Exception {
        PassiveRenewalServeCmd cmd = new PassiveRenewalServeCmd();
        cmd.setStatuses(defaultStatuses);
        cmd.setLimit(limit);
        Result<Integer> renewalResult = serveAggregateRootApi.passiveRenewalServe(cmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != renewalResult.getCode() || null == renewalResult.getData()) {
            return new ProcessResult(false, "自动续约失败，msg：" + renewalResult.getMsg());
        }

        return new ProcessResult(true, "自动续约了" + renewalResult.getData() + "条服务单");
    }
}
