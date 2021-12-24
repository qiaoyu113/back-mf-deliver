package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.ElecHandoverContractServiceI;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

// elec 是 electric/electronic 的缩写
@RestController
@RequestMapping("/api/deliver/v3/elecHandoverContract")
@Api(tags = "api--交付--1.4电子交接合同", value = "ElecHandoverContractController")
public class ElecHandoverContractController {

    private final String project_version = "1.0";

    @Resource
    private ElecHandoverContractServiceI elecHandoverContractServiceI;

    // 生成电子交接合同
    @ApiOperation(value = "发车-生成电子交接合同_" + project_version)
    @PostMapping("/createDeliverContract")
    @PrintParam
    public Result<String> createDeliverContract(@RequestBody @Validated CreateDeliverContractCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.createDeliverContract(cmd, tokenInfo)).success();
    }

    @ApiOperation(value = "收车-生成电子交接合同_" + project_version)
    @PostMapping("/createRecoverContract")
    @PrintParam
    public Result<String> createRecoverContract(@RequestBody @Validated CreateRecoverContractFrontCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.createRecoverContract(cmd, tokenInfo)).success();
    }

    // 前端轮询合同创建状态接口
    @ApiOperation(value = "轮询合同创建状态_" + project_version)
    @PostMapping("/getContractCreateStatus")
    @PrintParam
    public Result<Integer> getContractCreateStatus(@RequestBody @Validated ContractQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.getContractCreateStatus(qry, tokenInfo)).success();
    }

    // 发车-签署中的电子交接合同列表查询
    @ApiOperation(value = "发车-签署中的电子交接合同列表查询_" + project_version)
    @PostMapping("/getDeliverContractList")
    @PrintParam
    public Result<DeliverContractListVO> getDeliverContractList(@RequestBody @Validated ContractListQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.getDeliverContractList(qry, tokenInfo)).success();
    }

    // 收车-签署中的电子交接合同列表查询 在 recoverVehicleController 中的 getRecoverTaskListVO 方法中

    // 合同信息查询
    @ApiOperation(value = "发车电子交接合同信息查询_" + project_version)
    @PostMapping("/getDeliverContractInfo")
    @PrintParam
    public Result<ElecDeliverContractVO> getDeliverContractInfo(@RequestBody @Validated ContractQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.getDeliverContractInfo(qry, tokenInfo)).success();
    }

    // 合同信息查询
    @ApiOperation(value = "收车电子交接合同信息查询_" + project_version)
    @PostMapping("/getRecoverContractInfo")
    @PrintParam
    public Result<ElecRecoverContractVO> getRecoverContractInfo(@RequestBody @Validated ContractQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.getRecoverContractInfo(qry, tokenInfo)).success();
    }

    // 电子合同操作记录查询
    @ApiOperation(value = "电子合同操作记录查询_" + project_version)
    @PostMapping("/getContractOperationRecord")
    @PrintParam
    public Result<ElecContractOperationRecordWithSmsInfoVO> getContractOperationRecord(@RequestBody @Validated ContractQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.getContractOperationRecord(qry, tokenInfo)).success();
    }

    // 发送短信
    @ApiOperation(value = "发送短信_" + project_version)
    @PostMapping("/sendSms")
    @PrintParam
    public Result<Integer> sendSms(@RequestBody @Validated SendSmsCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.sendSms(cmd, tokenInfo)).success();
    }

    // 撤销电子合同
    @ApiOperation(value = "撤销电子合同_" + project_version)
    @PostMapping("/cancelContract")
    @PrintParam
    public Result<Integer> cancelContract(@RequestBody @Validated CancelContractCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.cancelContract(cmd, tokenInfo)).success();
    }

    // 用户确认合同过期
    @ApiOperation(value = "用户确认合同过期_" + project_version)
    @PostMapping("/confirmFail")
    @PrintParam
    public Result<Integer> confirmFail(@RequestBody @Validated ConfirmFailCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(elecHandoverContractServiceI.confirmFail(cmd, tokenInfo)).success();
    }

}
