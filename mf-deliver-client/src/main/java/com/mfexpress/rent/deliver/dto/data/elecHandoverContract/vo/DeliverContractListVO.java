package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.ListVO;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@ApiModel(value = "发车-电子交接合同列表展示VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliverContractListVO extends ListVO {

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "提车日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;

    @ApiModelProperty(value = "电子交接合同主要信息以及其下的多个交付单")
    private List<ElecContractWithServesVO> contractWithServesVOS;

}
