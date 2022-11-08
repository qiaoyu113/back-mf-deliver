package com.mfexpress.rent.deliver.web;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.ContractExpireNotifyDTO;
import com.mfexpress.rent.deliver.scheduler.ContractExpireRemindScheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController("testController")
@RequestMapping("/api/deliver/v3/test/web")
@Api(tags = "api--测试", value = "TestController")
public class TestController {

    @Resource
    private ContractExpireRemindScheduler contractExpireRemindScheduler;

    @Value("${spring.profiles}")
    private String env;

    @PostMapping("/testNoticeBuild")
    @ApiOperation("测试 合同到期提醒定时任务消息构建")
    public Result<String> testNoticeBuild(@RequestBody ContractExpireNotifyDTO contractExpireNotifyDTO) {
        checkEnv();
        String s = contractExpireRemindScheduler.formatTemplate(contractExpireNotifyDTO);
        return Result.getInstance(s).success();
    }

    @PostMapping("/testContractExpireScheduler")
    @ApiOperation("测试执行合同到期提醒定时任务")
    public Result<Boolean> testContractExpireScheduler() {
        checkEnv();
        contractExpireRemindScheduler.process();
        return Result.getInstance(true).success();
    }

    private static final List<String> DEV_ENV_LIST = new ArrayList<String>() {
        {
            add("test");
            add("dev2m1");
            add("dev2m2");
            add("dev2local");
        }
    };

    private void checkEnv() {
        if (!DEV_ENV_LIST.contains(env)) {
            log.error("环境错误,禁止使用 env:{}", env);
            throw new CommonException(ResultErrorEnum.FORBIDDEN.getCode(), ResultErrorEnum.FORBIDDEN.getName());
        }
    }

}
