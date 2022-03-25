package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.enums.business.BusinessTypeEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.starter.tools.excel.MFExcelTools;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportDeliverEachLeaseTermAmountCmdExe {

    @Resource
    private MFExcelTools mfExcelTools;

    public Integer execute(ServeQryCmd qry, TokenInfo tokenInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("businessType", BusinessTypeEnum.SERVICE.getName());
        map.put("userId", tokenInfo.getId());
        map.put("instanceName", "mf-deliver");
        map.put("uri", "/api/deliver/v3/deliver/web/exportDeliverLeaseTermAmountData");
        map.put("fileName", "租赁服务单详情");
        map.put("qry", qry);

        List<String> headers = new ArrayList<>();
        headers.add("customerName,客户名称");
        headers.add("oaContractCode,OA合同编号");
        headers.add("plateNumber,车牌号");
        headers.add("modelDisplay,品牌车型");
        headers.add("leaseModelDisplay,租赁方式");
        headers.add("rent,月租金（元/月/台）");
        headers.add("leaseMonth,租期");
        headers.add("leaseMonthStartWithEndDay,具体租赁周期");
        headers.add("unitPrice,费用金额（元）");
        headers.add("unpaidAmount,待还金额（元）");
        headers.add("repaymentStatusDisplay,回款状态");
        headers.add("totalAdjustAmount,累计调账金额（元）");

        map.put("header", headers);

        String export = mfExcelTools.export(map);
        if (StringUtils.isEmpty(export)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), ResultErrorEnum.OPER_ERROR.getName());
        }
        return 0;
    }
}
