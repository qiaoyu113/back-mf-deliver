package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCompletedCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeCancelCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServePaidInDepositUpdateCmd;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/serve", contextId = "mf-deliver-serve-aggregate-root-api")
public interface ServeAggregateRootApi {

    @PostMapping("/getServeDtoByServeNo")
    Result<ServeDTO> getServeDtoByServeNo(@RequestParam("serveNo") String serveNo);

    @PostMapping("/addServe")
    Result<String> addServe(@RequestBody ServeAddDTO serveAddDTO);


    @PostMapping("/toPreselected")
    Result<String> toPreselected(@RequestBody List<String> serveNoList);

    @PostMapping("/toReplace")
    Result<String> toReplace(@RequestParam("serveNo") String serveNo);

    @PostMapping("/deliver")
    Result<String> deliver(@RequestBody List<String> serveNoList);

    @PostMapping("/recover")
    Result<String> recover(@RequestBody List<String> serveNoList);

    @PostMapping("/completed")
    Result<String> completed(@RequestParam("serveNo") String serveNo);

    @PostMapping("/getServePreselectedDTO")
    Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId);

    @PostMapping("/cancelSelected")
    Result<String> cancelSelected(@RequestParam("serveNo") String serveNo);

    @PostMapping("/cancelSelectedList")
    Result<String> cancelSelectedList(@RequestBody List<String> serveNoList);


    @PostMapping("/getServeNoListAll")
    Result<List<String>> getServeNoListAll();

    @PostMapping("/getServeDailyDTO")
    Result<PagePagination<ServeDailyDTO>> getServeDailyDTO(@RequestBody ListQry listQry);

    /**
     * @param serveNoList 服务单编号
     * @deprecated 使用 getServeDTOByServeNoList
     */
    @PostMapping("/getServeMapByServeNoList")
    Result<Map<String, Serve>> getServeMapByServeNoList(@RequestBody List<String> serveNoList);

    @PostMapping("/getServeDTOByServeNoList")
    Result<List<ServeDTO>> getServeDTOByServeNoList(@RequestBody List<String> serveNoList);

    @PostMapping("/getCycleServe")
    Result<PagePagination<ServeDTO>> getCycleServe(@RequestBody ServeCycleQryCmd serveCycleQryCmd);

    @PostMapping("/toRepair")
    Result<String> toRepair(@RequestParam("serveNo") String serveNo);

    @PostMapping("/cancelOrCompleteRepair")
    Result<String> cancelOrCompleteRepair(@RequestParam("serveNo") String serveNo);

    @PostMapping("/addServeForReplaceVehicle")
    Result<String> addServeForReplaceVehicle(@RequestBody ServeReplaceVehicleAddDTO serveAddDTO);

    @PostMapping("/getServeListByOrderIds")
    Result<List<ServeDTO>> getServeListByOrderIds(@RequestBody List<Long> orderIds);

    @PostMapping("/renewalServe")
    Result<Integer> renewalServe(@RequestBody @Validated RenewalCmd cmd);

    @PostMapping("/renewalReplaceServe")
    Result<Integer> renewalReplaceServe(@RequestBody @Validated RenewalReplaceServeCmd cmd);

    @PostMapping("/passiveRenewalServe")
    Result<Integer> passiveRenewalServe(@RequestBody @Validated PassiveRenewalServeCmd cmd);

    @PostMapping("/getServeChangeRecordList")
    Result<List<ServeChangeRecordDTO>> getServeChangeRecordList(@RequestParam("serveNo") String serveNo);

    /**
     * 根据客户查询租赁中的服务单
     *
     * @param customerIdList 客户
     * @return 服务单
     */
    @PostMapping("/getServeByCustomerIdAndDeliver")
    Result<List<ServeDTO>> getServeByCustomerIdAndDeliver(@RequestBody List<Integer> customerIdList);

    @PostMapping("/getServeByCustomerIdAndRecover")
    Result<List<ServeDTO>> getServeByCustomerIdAndRecover(@RequestBody List<Integer> customerIdList);

    @PostMapping("/getCountByQry")
    Result<Long> getCountByQry(@RequestBody ServeListQry qry);

    @PostMapping("/getPageServeByQry")
    Result<PagePagination<Serve>> getPageServeByQry(@RequestBody ServeListQry qry);

    /**
     * 获取客户押金列表
     *
     * @param customerDepositLisDTO
     * @return
     */
    @PostMapping("/getPageServeDepositList")
    Result<PagePagination<ServeDepositDTO>> getPageServeDepositList(@RequestBody CustomerDepositListDTO customerDepositLisDTO);

    /**
     * 解锁押金
     *
     * @param serveNoList 服务单编号
     */
    @PostMapping("/unLockDeposit")
    Result<Boolean> unLockDeposit(@RequestParam("serveNoList") List<String> serveNoList, @RequestParam("creatorId") Integer creatorId);

    /**
     * 获取批量锁定数据页面
     *
     * @param serveNoList 选中服务单号
     * @return 锁定押金数据
     */
    @PostMapping("/getCustomerDepositLockList")
    Result<List<CustomerDepositLockListDTO>> getCustomerDepositLockList(@RequestBody List<String> serveNoList);

    /**
     * 锁定押金
     * @param confirmDTOList 锁定押金列表
     * @return
     */
    @PostMapping("/lockDeposit")
    Result <Boolean> lockDeposit(@RequestBody List<CustomerDepositLockConfirmDTO> confirmDTOList);

    @PostMapping("/reactiveServe")
    Result<Integer> reactiveServe(@RequestBody ReactivateServeCmd cmd);

    @PostMapping("/getServeNoListByPage")
    Result<PagePagination<String>> getServeNoListByPage(@RequestBody ListQry listQry);

    @PostMapping("/getReplaceNumByCustomerIds")
    Result<Map<Integer,Integer>> getReplaceNumByCustomerIds(@RequestBody List<Integer> customerIds);

    @PostMapping("/getRentingServeNumByCustomerId")
    Result<Integer> getRentingServeNumByCustomerId(@RequestParam("customerId") Integer customerId);

    @PostMapping(value = "/cancel")
    Result<Integer> cancelServe(@RequestBody ServeCancelCmd cmd);

    /**
     * 判断是否可以收车验车
     *
     * @param cmd
     * @return
     */
    @PostMapping(value = "/recover/check/judge")
    Result<Integer> recoverCheckJudge(@RequestBody RecoverCheckJudgeCmd cmd);

    /**
     * 查询服务单调整记录
     *
     * @param qry
     * @return
     */
    @PostMapping(value = "/serve/adjust/wo")
    Result<ServeAdjustDTO> getServeAdjust(@RequestBody ServeAdjustQry qry);

    /**
     * 替换车服务单调整
     * @param cmd
     */
    @PostMapping(value = "/serve/adjust")
    Result<Integer> serveAdjustment(@RequestBody ServeAdjustCmd cmd);

    @PostMapping(value = "/serve/adjust/start/billing")
    Result<Integer> serveAdjustStartBilling(@RequestBody ServeAdjustStartBillingCmd cmd);

    @PostMapping(value = "/serve/adjust/completed")
    Result<Integer> serveAdjustCompleted(@RequestBody ServeAdjustCompletedCmd cmd);

    @PostMapping(value = "/serve/paid-in-depost/update")
    Result<Integer> updateServePaidInDeposit(@RequestBody ServePaidInDepositUpdateCmd cmd);

    @GetMapping("/getServeReplaceVehicleList")
    Result<List<ServeReplaceVehicleDTO>> getServeReplaceVehicleList(@RequestParam("serveId") Long serveId);
}
