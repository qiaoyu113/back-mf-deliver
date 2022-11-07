package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.enums.business.BusinessTypeEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.starter.tools.excel.MFExcelTools;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportServeLeaseTermAmountCmdExe {

    @Resource
    private MFExcelTools mfExcelTools;

    public Integer execute(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("businessType", BusinessTypeEnum.SERVICE.getName());
        map.put("userId", tokenInfo.getId());
        map.put("instanceName", "mf-deliver");
        map.put("uri", "/api/deliver/v3/serve/web/exportServeLeaseTermAmountData");
        map.put("fileName", "租赁服务单列表");
        if (null == qry.getOrgId() || 0 == qry.getOrgId()) {
            qry.setUserOfficeId(tokenInfo.getOfficeId());
        }
        map.put("qry", qry);

        List<String> headers = new ArrayList<>();
        headers.add("serveNo,租赁服务单编号");
        headers.add("plateNumber,车牌号");
        headers.add("carModelDisplay,品牌车型");
        headers.add("vehicleBusinessModeDisplay,运营模式");
        headers.add("rentFee,租赁价格（元/月/台）");
        headers.add("serviceFee,服务费金额（元/月/台）");
        headers.add("rent,月租金（元/月/台）\n（租赁价格+服务费金额）");
        headers.add("deposit,应缴押金（元/台）");
        headers.add("actualDeposit,实缴押金（元/台）");
        headers.add("totalArrears,历史租期欠费情况\n（红色表示欠费）");
        headers.add("leaseModelDisplay,租赁方式");
        headers.add("serveStatusDisplay,租赁服务单状态");
        headers.add("customerName,客户名称");
        headers.add("oaContractCode,OA合同编号");
        headers.add("orgName,所属管理区");
        headers.add("expectRecoverDateChar,预计收车日期");
        headers.add("firstIssueDate,首次发车日期");
        headers.add("recentlyIssueDate,最近发车日期");
        headers.add("recentlyRecoverDate,最近收车日期");

        map.put("header", headers);

        String export = mfExcelTools.export(map);
        if (StringUtils.isEmpty(export)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), ResultErrorEnum.OPER_ERROR.getName());
        }
        return 0;
    }
}
