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
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverTypeEnum;
import com.mfexpress.rent.deliver.constant.ElecContractOperationTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecContractOperationRecordVO;
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

    public List<ElecContractOperationRecordVO> execute(ContractQry qry, TokenInfo tokenInfo) {
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(qry.getContractId());
        ElecContractDTO elecContractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        if(StringUtils.isEmpty(elecContractDTO.getContractForeignNo())){
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "合同状态异常");
        }

        Result<List<ContractRecordDTO>> recordListResult = foreignContractAggregateRootApi.queryContractRecordList(elecContractDTO.getContractForeignNo());
        List<ContractRecordDTO> recordList = ResultDataUtils.getInstance(recordListResult).getDataOrException();
        if(recordList.isEmpty()){
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "合同操作记录暂无");
        }

        Map<Integer, ContractRecordDTO> recordMap = recordList.stream().collect(Collectors.toMap(ContractRecordDTO::getOperationType, Function.identity(), (v1, v2) -> v1));

        List<ElecContractOperationRecordVO> recordVOS = new ArrayList<>();
        ContractRecordDTO newestRecord = recordList.get(recordList.size() - 1);
        if (ContractRecordEnum.CREATE_SUCCESS.getCode().equals(newestRecord.getOperationType())) {
            createdProcess(recordVOS, recordMap, elecContractDTO);
        } else if (ContractRecordEnum.SIGN_SUCCESS.getCode().equals(newestRecord.getOperationType())) {
            signedProcess(recordVOS, recordMap, elecContractDTO);
        }

        return recordVOS;
    }

    private void signedProcess(List<ElecContractOperationRecordVO> recordVOS, Map<Integer, ContractRecordDTO> recordMap, ElecContractDTO elecContractDTO) {
        ElecContractOperationRecordVO createdRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.CREATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.CREATED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        Date sendSmsDate = FormatUtil.ymdHmsFormatStringToDate(elecContractDTO.getSendSmsDate());
        recordVOS.add(createdRecord);
        if (null == sendSmsDate) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "短信发送日期查询失败");
        }
        ElecContractOperationRecordVO sendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SEND_SMS.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SEND_SMS.getName())
                .operationTime(sendSmsDate).build();
        recordVOS.add(sendSmsRecord);

        // 合同已签署完成默认客户已认证
        ElecContractOperationRecordVO authRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.AUTHENTICATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.AUTHENTICATED.getName()).build();
        recordVOS.add(authRecord);

        ElecContractOperationRecordVO signRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SIGNED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SIGNED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.SIGN_SUCCESS.getCode()).getCreateDate()).build();
        recordVOS.add(signRecord);

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

    private void createdProcess(List<ElecContractOperationRecordVO> recordVOS, Map<Integer, ContractRecordDTO> recordMap, ElecContractDTO elecContractDTO) {
        ElecContractOperationRecordVO createdRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.CREATED.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.CREATED.getName())
                .operationTime(recordMap.get(ContractRecordEnum.CREATE_SUCCESS.getCode()).getCreateDate()).build();
        Date sendSmsDate = FormatUtil.ymdHmsFormatStringToDate(elecContractDTO.getSendSmsDate());
        recordVOS.add(createdRecord);
        if (null == sendSmsDate) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "短信发送日期查询失败");
        }
        ElecContractOperationRecordVO sendSmsRecord = ElecContractOperationRecordVO.builder().operationType(ElecContractOperationTypeEnum.SEND_SMS.getCode())
                .operationTypeDisplay(ElecContractOperationTypeEnum.SEND_SMS.getName())
                .operationTime(sendSmsDate).build();
        recordVOS.add(sendSmsRecord);

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
    }

}
