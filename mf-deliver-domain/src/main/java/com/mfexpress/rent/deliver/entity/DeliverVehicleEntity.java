package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.entity.api.DeliverVehicleEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deliver_vehicle")
@Component
public class DeliverVehicleEntity implements DeliverVehicleEntityApi {

    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;
    @Id
    private Integer id;

    private String deliverVehicleNo;

    private String deliverNo;

    private String serveNo;

    private String contactsName;

    private String contactsPhone;

    private String contactsCard;

    private Date deliverVehicleTime;

    private String imgUrl;

    private Date createTime;

    private Date updateTime;

    @Override
    public List<DeliverVehicleDTO> getDeliverVehicleListByDeliverNoList(List<String> deliverNoList) {
        if (CollectionUtil.isEmpty(deliverNoList)) {
            return CollectionUtil.newArrayList();
        }
        List<DeliverVehicleEntity> deliverList = deliverVehicleGateway.getDeliverVehicleByDeliverNoList(deliverNoList);
        return CollectionUtil.isEmpty(deliverList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(deliverList, DeliverVehicleDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public DeliverVehicleDTO getDeliverVehicleByDeliverNo(String deliverNo) {
        DeliverVehicleEntity deliverVehicle = deliverVehicleGateway.getDeliverVehicleByDeliverNo(deliverNo);
        if (Objects.isNull(deliverNo)) {
            return null;
        }
        return BeanUtil.copyProperties(deliverVehicle, DeliverVehicleDTO.class);
    }

    @Override
    public List<DeliverVehicleDTO> getDeliverVehicleByServeNoList(List<String> serveNoList) {
        List<DeliverVehicleEntity> deliverVehicle = deliverVehicleGateway.getDeliverVehicleByServeNoList(serveNoList);
        if (CollectionUtil.isEmpty(deliverVehicle)) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(deliverVehicle, DeliverVehicleDTO.class, CopyOptions.create().ignoreError());
    }
}
