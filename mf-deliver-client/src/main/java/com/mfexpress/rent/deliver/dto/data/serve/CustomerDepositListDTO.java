package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "客户押金列表 DTO")
public class CustomerDepositListDTO extends ListQry {

    @Nullable
    @ApiModelProperty("所属管理区")
    private List<Integer> orgIdList;

    @Nullable
    @ApiModelProperty("客户名称")
    private Integer customerId;

    @ApiModelProperty("租赁服务单状态")
    private List<Integer> statusList;

    @ApiModelProperty("是否有实缴押金")
    private Boolean hasPaidDeposit;

    @ApiModelProperty("车辆Id")
    private Integer carId;

    @ApiModelProperty("服务单号")
    private String serveNo;

    @ApiModelProperty("数据权限 客户名称")
    private List<Integer> customerIdList;

}
