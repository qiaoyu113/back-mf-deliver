package com.mfexpress.rent.deliver.utils;

import java.util.List;

import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.common.domain.enums.OfficeCodeMsgEnum;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import org.springframework.http.HttpStatus;

public class PermissionUtil {

    public static void dataPermissionCheck(OfficeAggregateRootApi officeAggregateRootApi, TokenInfo tokenInfo) {
        // 权限数据查询
        Result<List<SysOfficeDto>> officeCityListResult = officeAggregateRootApi.getOfficeCityListByRegionId(tokenInfo.getOfficeId());
        if (ResultStatusEnum.UNKNOWS.getCode() == officeCityListResult.getCode() || HttpStatus.INTERNAL_SERVER_ERROR.value() == officeCityListResult.getCode()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
        }
        if (OfficeCodeMsgEnum.OFFICE_NOT_EXIST.getCode() == officeCityListResult.getCode()) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
    }
}
