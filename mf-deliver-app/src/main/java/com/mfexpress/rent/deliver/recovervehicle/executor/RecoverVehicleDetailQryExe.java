package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mfexpress.common.app.userCentre.dto.EmployeeDTO;
import com.mfexpress.common.app.userCentre.dto.qry.UserListByEmployeeIdsQry;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.api.UserAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDetailQryCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDetailVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.api.RentalCustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerEnterpriseNcInfoDTO;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverVehicleDetailQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private UserAggregateRootApi userAggregateRootApi;

    public RecoverDetailVO execute(RecoverDetailQryCmd cmd) {
        String deliverNo = cmd.getDeliverNo();

        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByDeliverNo(deliverNo);
        if (!DeliverUtils.resultDataCheck(deliverResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单信息查询失败");
        }
        DeliverDTO deliverDTO = deliverResult.getData();

        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        if (!DeliverUtils.resultDataCheck(serveResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息查询失败");
        }
        ServeDTO serveDTO = serveResult.getData();

        // 车牌号、车架号

        Result<RecoverVehicleDTO> recoverVehicleResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "收车单信息查询失败");
        }
        // 收车日期、预计还车日期
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleResult.getData();

        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(serveDTO.getCustomerId());
        if (!DeliverUtils.resultDataCheck(customerResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "客户信息查询失败");
        }
        // 客户名称
        CustomerVO customerVO = customerResult.getData();

        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getCarModelId());
        // 车型
        carModelResult.getData();

        Result<DeliverVehicleDTO> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverVehicleResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "发车信息查询失败");
        }
        // 发车日期
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleResult.getData();

        // 数据拼装
        RecoverDetailVO recoverDetailVO = new RecoverDetailVO();
        recoverDetailVO.setCustomerName(customerVO.getName());
        recoverDetailVO.setContractNo(serveDTO.getOaContractCode());

        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        recoverVehicleVO.setCarNum(deliverDTO.getCarNum());
        recoverVehicleVO.setBrandModelDisplay(carModelResult.getData());
        recoverVehicleVO.setFrameNum(deliverDTO.getFrameNum());
        recoverVehicleVO.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
        recoverVehicleVO.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
        recoverVehicleVO.setRecoverVehicleTime(recoverVehicleDTO.getRecoverVehicleTime());
        recoverDetailVO.setRecoverVehicleVO(recoverVehicleVO);

        String customerIDCardOrgSaleName = recoverDetailVO.getCustomerName();

        CustomerEnterpriseNcInfoDTO customerEnterpriseNcInfoDTO = ResultDataUtils.getInstance(customerAggregateRootApi.getCustomerEnterpriseNcInfoDTOByCustomerId(serveDTO.getCustomerId())).getDataOrNull();
        if (customerEnterpriseNcInfoDTO != null) {
            String creditCode = customerEnterpriseNcInfoDTO.getCreditCode();
            if (StrUtil.isNotEmpty(creditCode) && creditCode.length() >= 6) {
                customerIDCardOrgSaleName += "(**" + creditCode.substring(creditCode.length() - 6, creditCode.length()) + ")";
            }
        }

        SysOfficeDto sysOfficeDto = ResultDataUtils.getInstance(officeAggregateRootApi.getOfficeDataById(customerVO.getOrgId())).getDataOrNull();
        if (ObjectUtil.isNotEmpty(sysOfficeDto)) {
            customerIDCardOrgSaleName += "-" + sysOfficeDto.getName();
        }


        String saleName = "";
        UserListByEmployeeIdsQry userListByEmployeeIdsQry = new UserListByEmployeeIdsQry();
        userListByEmployeeIdsQry.setEmployeeIds(customerVO.getSaleId().toString());

        List<EmployeeDTO> employeeDTOList = ResultDataUtils.getInstance(userAggregateRootApi.getEmployeeListByEmployees(userListByEmployeeIdsQry)).getDataOrNull();
        if (employeeDTOList != null) {
            saleName = employeeDTOList.get(0).getNickName();
            customerIDCardOrgSaleName += "-" + saleName;
        }

        recoverDetailVO.setCustomerIDCardOrgSaleName(customerIDCardOrgSaleName);
        recoverDetailVO.setCustomerName(customerIDCardOrgSaleName);

        return recoverDetailVO;
    }

}
