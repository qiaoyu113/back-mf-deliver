package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalQry;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverAbnormalQryExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    public RecoverAbnormalVO execute(RecoverAbnormalQry qry) {
        Result<RecoverAbnormalDTO> recoverAbnormalDTOResult = recoverVehicleAggregateRootApi.getRecoverAbnormalByQry(qry);
        RecoverAbnormalDTO recoverAbnormalDTO = ResultDataUtils.getInstance(recoverAbnormalDTOResult).getDataOrException();
        if(null == recoverAbnormalDTO){
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), ResultErrorEnum.NOT_FOUND.getName());
        }
        RecoverAbnormalVO recoverAbnormalVO = new RecoverAbnormalVO();
        BeanUtils.copyProperties(recoverAbnormalDTO, recoverAbnormalVO);
        recoverAbnormalVO.setReason(recoverAbnormalDTO.getCause());
        recoverAbnormalVO.setImgUrls(JSONUtil.toList(recoverAbnormalDTO.getImgUrl(), String.class));
        return recoverAbnormalVO;
    }
}
