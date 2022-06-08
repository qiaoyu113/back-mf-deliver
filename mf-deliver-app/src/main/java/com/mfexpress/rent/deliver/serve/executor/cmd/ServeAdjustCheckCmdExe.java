package com.mfexpress.rent.deliver.serve.executor.cmd;

import java.util.Optional;

import javax.annotation.Resource;

import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeAdjustChargeRentTypeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.deliver.utils.PermissionUtil;
import com.mfexpress.rent.deliver.utils.ServeDictDataUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServeAdjustCheckCmdExe {

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private BeanFactory beanFactory;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    public ServeAdjustRecordVo execute(ServeAdjustCheckCmd cmd, TokenInfo tokenInfo) {

        // 数据权限校验
        PermissionUtil.dataPermissionCheck(officeAggregateRootApi, tokenInfo);

        ServeDictDataUtil.initDictData(beanFactory);
        // 查询服务单
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        if (!Optional.ofNullable(serveDTOResult).map(result -> result.getData()).isPresent()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未找到服务单");
        }
        ServeDTO serveDTO = serveDTOResult.getData();
        String sourceServeNo = "";
        if (JudgeEnum.NO.getCode().equals(serveDTO.getReactiveFlag())) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前服务单不是替换单，无法进行服务单变更");
        } else {
            if (ServeEnum.CANCEL.getCode().equals(serveDTO.getStatus())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前服务单已作废，无法进行服务单变更");
            } else if (ServeEnum.NOT_PRESELECTED.getCode().equals(serveDTO.getStatus())
                    || ServeEnum.PRESELECTED.getCode().equals(serveDTO.getStatus())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "原车未申请收车，无法进行服务单变更");
            }
            // 查找原车维修单
            MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceDTOByReplaceServeNo(maintenanceAggregateRootApi, cmd.getServeNo());
            // 查找交付单
            Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(maintenanceDTO.getServeNo());
            if (Optional.ofNullable(deliverDTOResult).map(Result::getData).isPresent()
                    && DeliverEnum.IS_RECOVER.getCode().equals(deliverDTOResult.getData().getDeliverStatus())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "原车未申收车，无法进行服务单变更");
            }
            sourceServeNo = deliverDTOResult.getData().getServeNo();
            log.info("sourceServeNo---->{}", sourceServeNo);
        }

        if (StringUtils.isNotEmpty(sourceServeNo)) {
            Result<ServeDTO> sourceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(sourceServeNo);
            if (Optional.ofNullable(sourceServeDTOResult).map(Result::getData).isPresent()) {

                ServeAdjustRecordVo vo = new ServeAdjustRecordVo();
                vo.setServeNo(vo.getServeNo());
                vo.setChargeLeaseModelId(ServeAdjustChargeRentTypeEnum.NORMAL.getCode());
                vo.setChargeLeaseModel(ServeDictDataUtil.leaseModeMap.get(String.valueOf(vo.getChargeLeaseModelId())));
                vo.setExpectRecoverTime(FormatUtil.addDays(FormatUtil.ymdFormatStringToDate(sourceServeDTOResult.getData().getExpectRecoverDate()), 1));
                vo.setChargeDepositAmount(sourceServeDTOResult.getData().getDeposit());
                vo.setChargeRentAmount(serveDTO.getRent());

                return vo;
            }
        }

        return null;
    }
}
