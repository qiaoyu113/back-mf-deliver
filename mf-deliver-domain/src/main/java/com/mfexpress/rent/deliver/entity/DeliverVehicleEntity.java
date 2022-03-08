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
import java.util.Date;
import java.util.List;

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
        List<DeliverVehicleEntity> deliverList = deliverVehicleGateway.getDeliverVehicleByDeliverNoList(deliverNoList);
        return CollectionUtil.isEmpty(deliverList) ? null : BeanUtil.copyToList(deliverList, DeliverVehicleDTO.class, new CopyOptions().ignoreError());
    }
}
