package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ElecContractCreateStatusEnum;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ContractStatusQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    public Integer execute(ContractQry qry, TokenInfo tokenInfo) {
        Result<ElecContractDTO> elecContractDTOResult = contractAggregateRootApi.getContractDTOByContractId(qry.getContractId());
        if (!ResultErrorEnum.SUCCESSED.getCode().equals(elecContractDTOResult.getCode())) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "查询失败");
        }
        ElecContractDTO elecContractDTO = elecContractDTOResult.getData();
        if (null == elecContractDTO) {
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), ResultErrorEnum.NOT_FOUND.getName());
        }

        // 返回结果与前端的约定，1：创建中，2：成功，3：失败
        Integer status = elecContractDTO.getStatus();
        if (ElecHandoverContractStatus.GENERATING.getCode() == status) {
            return ElecContractCreateStatusEnum.CREATING.getCode();
        } else if (ElecHandoverContractStatus.FAIL.getCode() == status) {
            return ElecContractCreateStatusEnum.FAIL.getCode();
        }
        return ElecContractCreateStatusEnum.SUCCESS.getCode();
    }

}
