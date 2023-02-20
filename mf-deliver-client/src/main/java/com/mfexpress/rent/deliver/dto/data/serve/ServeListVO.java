package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.ListVO;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "交付信息列表")
@Data
public class ServeListVO extends ListVO {
    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "部门所属")
    private Integer orgId;

    @ApiModelProperty(value = "部门名称")
    private String orgName;

    @ApiModelProperty(value = "提车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;

    @ApiModelProperty(value = "租赁服务单列表")
    private List<ServeVO> serveVOList;

    @ApiModelProperty(value = "批量投保")
    private Integer batchInsureButtonSwitch;


}
