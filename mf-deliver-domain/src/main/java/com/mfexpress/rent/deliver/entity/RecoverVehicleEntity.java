package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.entity.api.RecoverVehicleEntityApi;
import com.mfexpress.rent.deliver.gateway.RecoverVehicleGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recover_vehicle")
@Builder
@Component
public class RecoverVehicleEntity implements RecoverVehicleEntityApi {


    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;

    @Id
    private Integer id;

    private String deliverNo;

    private String recoverVehicleNo;

    private String serveNo;

    private String contactsName;

    private String contactsPhone;

    private String contactsCard;

    private Date recoverVehicleTime;

    private String imgUrl;

    private Date createTime;

    private Integer saleId;

    private Integer carServiceId;

    private Date updateTime;

    private Integer createId;

    private Integer updateId;

    private Date expectRecoverTime;

    private Integer status;

    private Integer cancelRemarkId;

    private String cancelRemark;

    private Integer wareHouseId;

    private Integer carId;

    private Double damageFee;

    private Double parkFee;

    @Override
    public List<RecoverVehicleDTO> getRecoverListByDeliverNoList(List<String> deliverNoList) {
        if (CollectionUtil.isEmpty(deliverNoList)) {
            return CollectionUtil.newArrayList();
        }
        List<RecoverVehicleEntity> recoverVehicleList = recoverVehicleGateway.getRecoverVehicleByDeliverNos(deliverNoList);
        return CollectionUtil.isEmpty(recoverVehicleList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(recoverVehicleList, RecoverVehicleDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public RecoverVehicleDTO getRecoverVehicleByDeliverNo(String deliverNo) {
        RecoverVehicleEntity recoverVehicleEntity = recoverVehicleGateway.getRecoverVehicleByDeliverNo(deliverNo);
        return Objects.isNull(recoverVehicleEntity) ? null : BeanUtil.copyProperties(recoverVehicleEntity, RecoverVehicleDTO.class);
    }
}
