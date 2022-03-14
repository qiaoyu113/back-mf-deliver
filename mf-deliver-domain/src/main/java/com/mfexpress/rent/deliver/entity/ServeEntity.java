package com.mfexpress.rent.deliver.entity;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "serve")
@Builder
@Component
public class ServeEntity implements ServeEntityApi {

    @Id
    private Integer id;

    private Long orderId;

    private Long serveId;

    private Integer customerId;

    private String serveNo;

    private Integer carModelId;

    private Integer leaseModelId;

    private Integer brandId;

    private Integer status;

    private Integer carServiceId;

    private Integer saleId;

    private String remark;

    private Integer createId;

    private Integer updateId;

    private Integer cityId;

    private Integer orgId;

    private BigDecimal rent;

    private Date createTime;

    private Date updateTime;

    private Integer replaceFlag;

    private Integer goodsId;

    // 续签合同迭代增加的字段-----------start
    private Long contractId;

    private String oaContractCode;

    private Double deposit;

    private String leaseBeginDate;

    private Integer leaseMonths;

    private String leaseEndDate;

    private String billingAdjustmentDate;

    private Integer renewalType;

    private String expectRecoverDate;

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private ServeChangeRecordGateway serveChangeRecordGateway;

    @Override
    public void reactiveServe(ReactivateServeCmd cmd) {
        ServeEntity newServe = new ServeEntity();
        newServe.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
        serveGateway.updateServeByServeNo(cmd.getServeNo(), newServe);

        // 保存操作记录
        ServeEntity rawServe = serveGateway.getServeByServeNo(cmd.getServeNo());
        saveChangeRecordWithReactive(cmd, rawServe, newServe);
    }

    // 保存重新激活类型的操作记录
    private void saveChangeRecordWithReactive(ReactivateServeCmd cmd, ServeEntity rawServe, ServeEntity newServe) {
        ServeChangeRecordPO serveChangeRecordPO = new ServeChangeRecordPO();
        serveChangeRecordPO.setServeNo(rawServe.getServeNo());
        serveChangeRecordPO.setType(ServeChangeRecordEnum.REACTIVE.getCode());
        serveChangeRecordPO.setRawData(JSONUtil.toJsonStr(rawServe));
        serveChangeRecordPO.setNewData(JSONUtil.toJsonStr(newServe));
        serveChangeRecordPO.setDeliverNo(cmd.getDeliverNo());
        serveChangeRecordPO.setReactiveReason(cmd.getReason());
        serveChangeRecordPO.setRemark(cmd.getRemark());
        serveChangeRecordPO.setCreatorId(cmd.getOperatorId());
        serveChangeRecordGateway.insert(serveChangeRecordPO);
    }

}
