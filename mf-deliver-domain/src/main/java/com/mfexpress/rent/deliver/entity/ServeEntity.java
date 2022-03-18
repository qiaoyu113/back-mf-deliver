package com.mfexpress.rent.deliver.entity;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;
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


    @Resource
    private ServeGateway serveGateway;
    @Resource
    private ServeChangeRecordGateway changeRecordGateway;


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

    private Integer contractCommodityId;

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

    //增加实缴、应缴押金

    private BigDecimal payableDeposit;

    private BigDecimal paidInDeposit;

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

    @Override
    public ServeDepositDTO getServeDepositByServeNo(CustomerDepositListDTO customerDepositLisDTO) {
        ServeEntity serveEntity = serveGateway.getServeDepositByServeNo(customerDepositLisDTO);
        if (Objects.isNull(serveEntity)) {
            return null;
        }
        ServeDepositDTO serveDepositDTO = BeanUtil.copyProperties(serveEntity, ServeDepositDTO.class);
        if (serveEntity.getStatus().equals(ServeEnum.REPAIR.getCode())) {
            serveDepositDTO.setMaintainFeeConfirmFlag(false);
        } else {
            serveDepositDTO.setMaintainFeeConfirmFlag(true);
        }
        return serveDepositDTO;

    }

    @Override
    public PagePagination<ServeDepositDTO> getServeDepositByQry(CustomerDepositListDTO customerDepositLisDTO) {
        PagePagination<ServeEntity> servePagePagination = serveGateway.pageServeDeposit(customerDepositLisDTO);
        List<ServeEntity> serveList = servePagePagination.getList();
        List<ServeDepositDTO> depositDTOList = new ArrayList<>();
        for (ServeEntity serveEntity : serveList) {
            ServeDepositDTO serveDepositDTO = BeanUtil.copyProperties(serveEntity, ServeDepositDTO.class);
            //维修费用确认情况
            serveDepositDTO.setMaintainFeeConfirmFlag(!serveEntity.getStatus().equals(ServeEnum.REPAIR.getCode()));
            depositDTOList.add(serveDepositDTO);
        }
        PagePagination<ServeDepositDTO> depositPagePagination = new PagePagination<>();
        BeanUtil.copyProperties(servePagePagination, depositPagePagination);
        depositPagePagination.setList(depositDTOList);
        return depositPagePagination;
    }

    @Override
    public List<ServeDTO> getServeListByServeNoList(List<String> serveNoList) {
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        return CollectionUtil.isEmpty(serveList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(serveList, ServeDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public ServeDTO getServeByServeNo(String serveNo) {
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        return Objects.isNull(serve) ? null : BeanUtil.copyProperties(serve, ServeDTO.class);
    }

    @Override
    public void updateServeDepositByServeNoList(Map<String, BigDecimal> updateDepositMap, Integer creatorId) {
        List<String> serveNoList = new ArrayList<>(updateDepositMap.keySet());
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        for (ServeEntity serveEntity : serveList) {
            ServeEntity updateDeposit = new ServeEntity();
            BigDecimal paidInDeposit = serveEntity.getPaidInDeposit();
            updateDeposit.setServeNo(serveEntity.getServeNo());
            updateDeposit.setPaidInDeposit(paidInDeposit.add(updateDepositMap.get(serveEntity.getServeNo())));
            updateDeposit.setStatus(updateDeposit.getPaidInDeposit().compareTo(BigDecimal.ZERO) == 0 ? ServeEnum.COMPLETED.getCode() : updateDeposit.getStatus());
            serveGateway.updateServeByServeNo(serveEntity.getServeNo(), updateDeposit);
            //变更记录
            ServeChangeRecordPO serveChangeRecord = new ServeChangeRecordPO();
            serveChangeRecord.setServeNo(serveEntity.getServeNo());
            String rawData = JSON.toJSONString(serveEntity);
            String newData = JSON.toJSONString(updateDeposit);
            serveChangeRecord.setRawData(rawData);
            serveChangeRecord.setNewData(newData);
            serveChangeRecord.setCreateTime(new Date());
            serveChangeRecord.setNewGoodsId(serveEntity.getGoodsId());
            serveChangeRecord.setRawGoodsId(serveEntity.getGoodsId());
            serveChangeRecord.setRenewalType(0);
            serveChangeRecord.setCreatorId(creatorId);
            serveChangeRecord.setNewBillingAdjustmentDate("");
            serveChangeRecord.setDeliverNo("");
            serveChangeRecord.setReactiveReason(0);
            serveChangeRecord.setRemark("");
            serveChangeRecord.setType(updateDeposit.getStatus().equals(ServeEnum.COMPLETED.getCode()) ?
                    ServeChangeRecordEnum.DEPOSIT_UNLOCK.getCode() : ServeChangeRecordEnum.DEPOSIT_LOCK.getCode());
            recordList.add(serveChangeRecord);
        }
        changeRecordGateway.insertList(recordList);
    }

}
