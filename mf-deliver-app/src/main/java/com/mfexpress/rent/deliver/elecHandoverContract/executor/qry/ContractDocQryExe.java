package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecDocDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecHandoverDocVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ContractDocQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    public ElecHandoverDocVO execute(ContractQry qry) {
        Result<ElecDocDTO> docDTOResult = contractAggregateRootApi.getDocDTOByContractId(qry.getContractId());
        ElecDocDTO docDTO = ResultDataUtils.getInstance(docDTOResult).getDataOrException();
        if(null == docDTO){
            return null;
        }
        ElecHandoverDocVO elecHandoverDocVO = new ElecHandoverDocVO();
        //elecHandoverDocVO.setContractId(docDTO.getContractId().toString());
        elecHandoverDocVO.setFileUrl(docDTO.getFileUrl());
        return elecHandoverDocVO;

    }
}
