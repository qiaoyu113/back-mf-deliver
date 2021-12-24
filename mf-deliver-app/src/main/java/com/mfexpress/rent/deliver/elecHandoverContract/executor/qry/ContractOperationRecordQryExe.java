package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import com.mfexpress.common.domain.api.ContractAggregateRootApi;
import com.mfexpress.common.domain.dto.contract.ContractRecordDTO;
import com.mfexpress.common.domain.dto.contract.constant.enums.ContractRecordEnum;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.dto.contract.ContractOperateDTO;
import com.mfexpress.component.enums.contract.ContractModeEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecContractOperationRecordVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecContractOperationRecordWithSmsInfoVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.SmsInfoVO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContractOperationRecordQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ContractAggregateRootApi foreignContractAggregateRootApi;

    @Resource
    private RedisTools redisTools;

    public ElecContractOperationRecordWithSmsInfoVO execute(ContractQry qry, TokenInfo tokenInfo) {
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(qry.getContractId());
        ElecContractDTO elecContractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();

        ElecContractOperationRecordWithSmsInfoVO recordWithSmsInfoVO = new ElecContractOperationRecordWithSmsInfoVO();
        List<ElecContractOperationRecordVO> recordVOS = new ArrayList<>();
        // 合同生成中记录
        ElecContractOperationRecordVO createdRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.CREATING.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.CREATING.getName())
                .operationTime(elecContractDTO.getCreateTime()).build();
        recordVOS.add(createdRecord);

        if (ElecHandoverContractStatus.FAIL.getCode() == elecContractDTO.getStatus() && ContractFailureReasonEnum.CREATE_FAIL.getCode() == elecContractDTO.getFailureReason()) {
            // 失败记录
            ElecContractOperationRecordVO failRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.FAIL.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.FAIL.getName())
                    .operationTime(elecContractDTO.getUpdateTime()).build();
            recordVOS.add(failRecord);
            recordWithSmsInfoVO.setFailureReason(ContractFailureReasonEnum.CREATE_FAIL.getCode());
        } else {
            if (!StringUtils.isEmpty(elecContractDTO.getContractForeignNo())) {
                Result<List<ContractRecordDTO>> recordListResult = foreignContractAggregateRootApi.queryContractRecordList(elecContractDTO.getContractForeignNo());
                List<ContractRecordDTO> recordList = ResultDataUtils.getInstance(recordListResult).getDataOrException();
                if (!recordList.isEmpty()) {
                    Map<Integer, ContractRecordDTO> recordMap = recordList.stream().collect(Collectors.toMap(ContractRecordDTO::getOperationType, Function.identity(), (v1, v2) -> v1));
                    ContractRecordDTO newestRecord = recordList.get(recordList.size() - 1);
                    if (ContractRecordEnum.CREATE_SUCCESS.getCode().equals(newestRecord.getOperationType())) {

                        // 短信是否可发送判断，目前是一天可以发一条
                        // 签署中的合同才可发送短信
                        // 先判断日期是否是今天
                        if (ElecHandoverContractStatus.SIGNING.getCode() == elecContractDTO.getStatus()) {
                            recordWithSmsInfoVO.setSmsInfoVO(getSmsInfoVO(elecContractDTO));
                        }
                        createdProcess(recordVOS, recordMap, elecContractDTO, recordWithSmsInfoVO);
                    } else if (ContractRecordEnum.SIGN_SUCCESS.getCode().equals(newestRecord.getOperationType())) {
                        signedProcess(recordVOS, recordMap, elecContractDTO);
                    }
                }
            }
        }

        recordWithSmsInfoVO.setRecords(recordVOS);
        return recordWithSmsInfoVO;
    }

    private SmsInfoVO getSmsInfoVO(ElecContractDTO elecContractDTO) {
        SmsInfoVO smsInfoVO = new SmsInfoVO();
        String sendSmsDate = elecContractDTO.getSendSmsDate();
        if (StringUtils.isEmpty(sendSmsDate)) {
            smsInfoVO.setSendSmsFlag(JudgeEnum.YES.getCode());
        } else {
            String nowYmd = FormatUtil.ymdFormatDateToString(new Date());
            if(nowYmd.equals(sendSmsDate.substring(0, 10))){
                // 如果是今天，判断次数达到了限制没有
                if(elecContractDTO.getSendSmsCount() < Constants.EVERY_DAY_ENABLE_SEND_SMS_COUNT){
                    smsInfoVO.setSendSmsFlag(JudgeEnum.YES.getCode());
                }else{
                    smsInfoVO.setSendSmsFlag(JudgeEnum.NO.getCode());
                }
            }else{
                // 如果不是今天，可发送
                smsInfoVO.setSendSmsFlag(JudgeEnum.YES.getCode());
            }
        }

        // 如果短信可发送，查redis，获取短信发送倒计时
        if(JudgeEnum.YES.getCode().equals(smsInfoVO.getSendSmsFlag())){
            String key = DeliverUtils.concatCacheKey(Constants.ELEC_CONTRACT_LAST_TIME_SEND_SMS_KEY, elecContractDTO.getContractId().toString());
            // 取出来的是毫秒值
            Long lastTime = redisTools.get(key);
            if(null != lastTime){
                long now = System.currentTimeMillis();
                int i = (int) (60 - ((now - lastTime)/1000));
                if(i >= 0){
                    smsInfoVO.setSmsCountDown(i);
                }
            }
        }

        return smsInfoVO;
    }

    private void signedProcess(List<ElecContractOperationRecordVO> recordVOS, Map<Integer, ContractRecordDTO> recordMap, ElecContractDTO elecContractDTO) {
        // 合同生成成功记录
        ElecContractOperationRecordVO createdRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.CREATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.CREATED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(createdRecord);

        // 短信发送成功记录
        ElecContractOperationRecordVO sendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SEND_SMS.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SEND_SMS.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(sendSmsRecord);

        // 短信重新发送成功记录
        Date sendSmsDate = FormatUtil.ymdHmsFormatStringToDate(elecContractDTO.getSendSmsDate());
        if (null != sendSmsDate) {
            ElecContractOperationRecordVO resendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.RESEND_SMS.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.RESEND_SMS.getName())
                    .operationTime(sendSmsDate).build();
            recordVOS.add(resendSmsRecord);
        }

        // 合同已签署完成默认客户已认证
        ElecContractOperationRecordVO authRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.AUTHENTICATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.AUTHENTICATED.getName()).build();
        recordVOS.add(authRecord);

        // 交接单签署成功记录
        ElecContractOperationRecordVO signRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SIGNED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SIGNED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.SIGN_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(signRecord);

        // 发车成功/收车成功记录
        if (DeliverTypeEnum.DELIVER.getCode() == elecContractDTO.getDeliverType()) {
            ElecContractOperationRecordVO deliverRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.DELIVERED.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.DELIVERED.getName())
                    .operationTime(recordMap.get(ContractRecordEnum.SIGN_SUCCESS.getCode()).getCreateDate()).build();
            recordVOS.add(deliverRecord);
        } else {
            ElecContractOperationRecordVO recoverRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.RECOVERED.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.RECOVERED.getName())
                    .operationTime(recordMap.get(ContractRecordEnum.SIGN_SUCCESS.getCode()).getCreateDate()).build();
            recordVOS.add(recoverRecord);
        }
    }

    private void createdProcess(List<ElecContractOperationRecordVO> recordVOS, Map<Integer, ContractRecordDTO> recordMap, ElecContractDTO elecContractDTO, ElecContractOperationRecordWithSmsInfoVO recordWithSmsInfoVO) {
        // 合同生成成功记录
        ElecContractOperationRecordVO createdRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.CREATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.CREATED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(createdRecord);

        // 短信发送成功记录
        ElecContractOperationRecordVO sendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SEND_SMS.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SEND_SMS.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(sendSmsRecord);

        // 短信重新发送成功记录
        Date sendSmsDate = FormatUtil.ymdHmsFormatStringToDate(elecContractDTO.getSendSmsDate());
        if (null != sendSmsDate) {
            ElecContractOperationRecordVO resendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.RESEND_SMS.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.RESEND_SMS.getName())
                    .operationTime(sendSmsDate).build();
            recordVOS.add(resendSmsRecord);
        }

        // 客户已认证记录
        ContractOperateDTO contractOperateDTO = new ContractOperateDTO();
        contractOperateDTO.setType(ContractModeEnum.DELIVER.getName());
        contractOperateDTO.setContractId(Long.valueOf(elecContractDTO.getContractForeignNo()));
        Result<Boolean> customerAuthResult = foreignContractAggregateRootApi.checkAuth(contractOperateDTO);
        Boolean customerAuthFlag = ResultDataUtils.getInstance(customerAuthResult).getDataOrException();
        if (customerAuthFlag) {
            ElecContractOperationRecordVO authRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.AUTHENTICATED.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.AUTHENTICATED.getName()).build();
            recordVOS.add(authRecord);
        }

        // 交接单已过期记录
        if(ElecHandoverContractStatus.FAIL.getCode() == elecContractDTO.getStatus() && ContractFailureReasonEnum.OVERDUE.getCode() == elecContractDTO.getFailureReason()){
            ElecContractOperationRecordVO overdueRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.FAIL.getCode())
                    .operationTypeDisplay(ElecContractOperationTypeEnum.OVERDUE.getName())
                    .operationTime(elecContractDTO.getUpdateTime()).build();
            recordVOS.add(overdueRecord);
            recordWithSmsInfoVO.setFailureReason(ContractFailureReasonEnum.OVERDUE.getCode());
        }
    }

}
