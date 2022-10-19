package com.mfexpress.rent.deliver.recovervehicle.executor;

import java.util.Optional;

import javax.annotation.Resource;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.proxy.backmarket.BackmarketMaintenanceAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverCompletedCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverCancelByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverInvalidCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RecoverCancelByDeliverExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi;


    public Integer execute(RecoverCancelByDeliverCmd cmd, TokenInfo tokenInfo) {

        // 2022-05-31 增加不可取消收车规则判断
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(cmd.getDeliverNo());

        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrNull();
        if (deliverDTO == null) {
            throw new CommonException(ResultStatusEnum.UNKNOWS.getCode(), "数据错误");
        }

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        if (Optional.ofNullable(serveDTO).filter(serve -> ServeEnum.REPAIR.getCode().equals(serve.getStatus())).isPresent()) {
            String sourceServeNo = serveDTOResult.getData().getServeNo();

            // 查询替换单
            String replaceServeNo = MainServeUtil.getReplaceServeNoBySourceServeNo(backmarketMaintenanceAggregateRootApi, sourceServeNo);
            if (!StringUtils.isEmpty(replaceServeNo)) {
                // 查询是否存在调整工单
                ServeAdjustQry serveAdjustQry = ServeAdjustQry.builder().serveNo(replaceServeNo).build();
                ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(serveAggregateRootApi.getServeAdjust(serveAdjustQry)).getDataOrNull();
                // 存在调整工单 不允许取消收车
                if (Optional.ofNullable(serveAdjustDTO).isPresent()) {
                    throw new CommonException(ResultStatusEnum.UNKNOWS.getCode(), "不可取消收车");
                }
            }
        }

        // 交付单退回到已发车状态
        DeliverCompletedCmd deliverCompletedCmd = new DeliverCompletedCmd();
        deliverCompletedCmd.setDeliverNo(deliverDTO.getDeliverNo());
        deliverCompletedCmd.setOperatorId(cmd.getOperatorId());

        deliverAggregateRootApi.completedDeliver(deliverCompletedCmd);

        // 之前的收车实体置为无效
        RecoverInvalidCmd recoverInvalidCmd = new RecoverInvalidCmd();
        recoverInvalidCmd.setDeliverNo(deliverDTO.getDeliverNo());
        recoverInvalidCmd.setOperatorId(cmd.getOperatorId());

        ResultValidUtils.checkResultException(recoverVehicleAggregateRootApi.invalidRecover(recoverInvalidCmd));

        return 0;
    }
}
