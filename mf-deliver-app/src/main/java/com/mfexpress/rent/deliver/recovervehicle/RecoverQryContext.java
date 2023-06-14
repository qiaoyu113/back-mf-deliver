package com.mfexpress.rent.deliver.recovervehicle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mfexpress.common.app.userCentre.dto.EmployeeDTO;
import com.mfexpress.common.app.userCentre.dto.qry.UserListByEmployeeIdsQry;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.api.UserAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.api.RentalCustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerEnterpriseNcInfoDTO;
import com.mfexpress.transportation.customer.dto.rent.RentalCustomerDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecoverQryContext {


    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private RentalCustomerAggregateRootApi rentalCustomerAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private UserAggregateRootApi userAggregateRootApi;

    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        RecoverQryServiceI bean = (RecoverQryServiceI) applicationContext.getBean(RecoverEnum.getServiceName(recoverQryListCmd.getTag()) + "QryExe");
        RecoverTaskListVO recoverTaskListVO = bean.execute(recoverQryListCmd, tokenInfo);
        List<RecoverVehicleVO> recoverVehicleVOList = recoverTaskListVO.getRecoverVehicleVOList();

        if (CollUtil.isEmpty(recoverVehicleVOList)) {
            return recoverTaskListVO;
        }

        List<SysOfficeDto> sysOfficeDtoList = ResultDataUtils.getInstance(officeAggregateRootApi.getOfficeCityListAll()).getDataOrNull();
        Map<Integer, SysOfficeDto> sysOfficeDtoMap = sysOfficeDtoList.stream().collect(Collectors.toMap(SysOfficeDto::getId, v -> v));

        List<Integer> customerIdList = recoverVehicleVOList.stream().map(RecoverVehicleVO::getCustomerId).collect(Collectors.toList());
        List<RentalCustomerDTO> rentalCustomerDTOList = ResultDataUtils.getInstance(rentalCustomerAggregateRootApi.getRentalCustomerByCustomerIdList(customerIdList)).getDataOrNull();
        Map<Integer, RentalCustomerDTO> rentalCustomerDTOMap = CollUtil.isNotEmpty(rentalCustomerDTOList)
                ? rentalCustomerDTOList.stream().collect(Collectors.toMap(RentalCustomerDTO::getId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        List<CustomerEnterpriseNcInfoDTO> customerEnterpriseNcInfoDTOList = ResultDataUtils.getInstance(customerAggregateRootApi.getCustomerEnterpriseNcInfoDTOListByCustomerIdList(customerIdList)).getDataOrNull();
        Map<Integer, CustomerEnterpriseNcInfoDTO> customerEnterpriseNcInfoDTOMap = CollUtil.isNotEmpty(customerEnterpriseNcInfoDTOList)
                ? customerEnterpriseNcInfoDTOList.stream().collect(Collectors.toMap(CustomerEnterpriseNcInfoDTO::getCustomerId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        String saleIdString = CollUtil.isNotEmpty(rentalCustomerDTOList)
                ? rentalCustomerDTOList.stream().map(RentalCustomerDTO::getSaleId).map(String::valueOf).collect(Collectors.joining(",")) : "";
        UserListByEmployeeIdsQry userListByEmployeeIdsQry = new UserListByEmployeeIdsQry();
        userListByEmployeeIdsQry.setEmployeeIds(saleIdString);
        List<EmployeeDTO> employeeDTOList = ResultDataUtils.getInstance(userAggregateRootApi.getEmployeeListByEmployees(userListByEmployeeIdsQry)).getDataOrNull();
        Map<Integer, EmployeeDTO> employeeDTOMap = CollUtil.isNotEmpty(employeeDTOList)
                ? employeeDTOList.stream().collect(Collectors.toMap(EmployeeDTO::getId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        List<String> serveNoList = recoverVehicleVOList.stream().map(RecoverVehicleVO::getServeNo).collect(Collectors.toList());
        List<ServeDTO> serveDTOList = ResultDataUtils.getInstance(serveAggregateRootApi.getServeDTOByServeNoList(serveNoList)).getDataOrNull();
        Map<String, ServeDTO> serveDTOMap = CollUtil.isNotEmpty(serveDTOList)
                ? serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        for (RecoverVehicleVO v : recoverVehicleVOList) {
            Result<MaintenanceDTO> maintainResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(v.getServeNo());
            if (maintainResult.getData() != null) {
                v.setConfirmDate(maintainResult.getData().getConfirmDate());
            }

            ServeDTO serveDTO = serveDTOMap.getOrDefault(v.getServeNo(), null);
            v.setRent(ObjectUtil.isNotEmpty(serveDTO) ? serveDTO.getRent() : BigDecimal.ZERO);
            v.setDeposit(ObjectUtil.isNotEmpty(serveDTO) ? serveDTO.getDeposit() : BigDecimal.ZERO);

            String customerIDCardOrgSaleName = v.getCustomerName();
            CustomerEnterpriseNcInfoDTO customerEnterpriseNcInfoDTO = customerEnterpriseNcInfoDTOMap.getOrDefault(v.getCustomerId(), null);
            if (customerEnterpriseNcInfoDTO != null) {
                String creditCode = customerEnterpriseNcInfoDTO.getCreditCode();
                if (StrUtil.isNotEmpty(creditCode) && creditCode.length() >= 6) {
                    customerIDCardOrgSaleName += "(**" + creditCode.substring(creditCode.length() - 6, creditCode.length()) + ")";
                }
            }

            SysOfficeDto sysOfficeDto = sysOfficeDtoMap.getOrDefault(v.getOrgId(), null);
            if (sysOfficeDto != null) {
                customerIDCardOrgSaleName += "-" + sysOfficeDto.getName();
            }

            RentalCustomerDTO rentalCustomerDTO = rentalCustomerDTOMap.getOrDefault(v.getCustomerId(), null);
            if (ObjectUtil.isNotEmpty(rentalCustomerDTO)) {
                EmployeeDTO employeeDTO = employeeDTOMap.getOrDefault(rentalCustomerDTO.getSaleId(), null);
                if (ObjectUtil.isNotEmpty(employeeDTO)) {
                    customerIDCardOrgSaleName += "-" + employeeDTO.getNickName();
                }
            }

            v.setCustomerIDCardOrgSaleName(customerIDCardOrgSaleName);
            v.setCustomerName(customerIDCardOrgSaleName);
        }
        return recoverTaskListVO;

    }

}
