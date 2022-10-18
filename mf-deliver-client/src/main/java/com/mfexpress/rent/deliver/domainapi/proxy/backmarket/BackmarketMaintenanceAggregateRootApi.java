package com.mfexpress.rent.deliver.domainapi.proxy.backmarket;

import com.hx.backmarket.maintain.data.cmd.maintenance.*;
import com.hx.backmarket.maintain.data.cmd.maintenanceshop.MaintenanceShopQryCmd;
import com.hx.backmarket.maintain.data.cmd.quotation.CreateQuotationCmd;
import com.hx.backmarket.maintain.data.cmd.quotation.OAUpdateQuotationCmd;
import com.hx.backmarket.maintain.data.cmd.quotation.QuotationCancelCmd;
import com.hx.backmarket.maintain.data.cmd.quotation.StagingQuotationDomainCmd;
import com.hx.backmarket.maintain.data.cmd.sendrepair.MaintenanceSendRepairAcceptCmd;
import com.hx.backmarket.maintain.data.cmd.sendrepair.MaintenanceSendRepairAdmittanceCmd;
import com.hx.backmarket.maintain.data.cmd.sendrepair.MaintenanceSendRepairCancelCmd;
import com.hx.backmarket.maintain.data.cmd.sendrepair.MaintenanceSendRepairCreateCmd;
import com.hx.backmarket.maintain.data.cmd.serviceorder.*;
import com.hx.backmarket.maintain.data.dto.MaintenanceDTO;
import com.hx.backmarket.maintain.data.dto.MaintenanceReplaceVehicleDTO;
import com.hx.backmarket.maintain.data.dto.MaintenanceShopDTO;
import com.hx.backmarket.maintain.data.dto.conservation.MaintenanceConservationDTO;
import com.hx.backmarket.maintain.data.dto.fetchrepair.*;
import com.hx.backmarket.maintain.data.dto.product.ProductItemDTO;
import com.hx.backmarket.maintain.data.dto.quotation.*;
import com.hx.backmarket.maintain.data.dto.sendrepair.MaintenanceSendRepairDTO;
import com.hx.backmarket.maintain.data.dto.serviceorder.MaintenanceServiceOrderDTO;
import com.hx.backmarket.maintain.data.dto.serviceorder.MaintenanceServiceOrderDetailDTO;
import com.hx.backmarket.maintain.data.dto.serviceorder.MaintenanceServiceOrderPageDTO;
import com.hx.backmarket.maintain.data.qry.conservation.MaintenanceConservationPageQry;
import com.hx.backmarket.maintain.data.qry.maintenance.MaintenanceIdsQry;
import com.hx.backmarket.maintain.data.qry.maintenance.MaintenanceListQry;
import com.hx.backmarket.maintain.data.qry.maintenance.MaintenancePageQry;
import com.hx.backmarket.maintain.data.qry.maintenanceshop.MaintenanceShopListQry;
import com.hx.backmarket.maintain.data.qry.product.ProductItemQry;
import com.hx.backmarket.maintain.data.qry.quotation.AddConservationQry;
import com.hx.backmarket.maintain.data.qry.quotation.EchoQuotationSubQry;
import com.hx.backmarket.maintain.data.qry.quotation.QuotationListQry;
import com.hx.backmarket.maintain.data.qry.quotation.QuotationQry;
import com.hx.backmarket.maintain.data.qry.sendrepair.MaintenanceSendRepairPageH5Qry;
import com.hx.backmarket.maintain.data.qry.sendrepair.MaintenanceSendRepairPageWebQry;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;

@FeignClient(name = "backmarket-maintain", contextId = "maintenance-aggregate-root-api", path = "/domain/maintenance", url = "${gateway.backmarket}")
public interface BackmarketMaintenanceAggregateRootApi {

/*********************** 维修工单 start ***********************/
    /**
     * <p>根据maintenanceId获取维修工单</p>
     * <descript></descript>
     *
     * @param cmd
     * @return com.mfexpress.component.response.Result<com.hx.backmarket.maintain.data.dto.MaintenanceDTO>
     */
    @PostMapping(value = "/v3/maintenance/one/maintenanceId")
    Result<MaintenanceDTO> getOne(@RequestBody MaintenanceIdCmd cmd);

    /**
     * 依据申请创建维修工单
     *
     * @param cmd
     * @return
     */
    @PostMapping(value = "/v3/maintenance/create/basis/apply")
    Result<MaintenanceDTO> createBasisApply(@RequestBody MaintenanceCreateBasisApplyCmd cmd);

    @PostMapping("checkRepairComplete")
    @ApiOperation("校验是否维修完成")
    Result<Boolean> checkRepairComplete(@RequestParam("maintenanceId") Long maintenanceId);

    /**
     * <p>入场</p>
     * <descript>维修工单 增加入场时间</descript>
     *
     * @param cmd
     * @return com.mfexpress.component.response.Result<java.lang.Integer>
     */
    @PostMapping(value = "/v3/maintenance/admission")
    Result<Integer> admission(@RequestBody MaintenanceAdmissionCmd cmd);

    /**
     * <p>报价单审批通过</p>
     * <descript>修改维修工单状态为维修中，并增加维修开始时间</descript>
     *
     * @param cmd
     * @return java.lang.Integer
     */
    @PostMapping(value = "/v3/maintenance/quotation/pass")
    Result<Integer> quotationPass(@RequestBody MaintenanceQuotationPassCmd cmd);

    /**
     * <p>维修完成</p>
     * <descript>维修工单状态改为维修完成，并增加维修结束时间</descript>
     *
     * @param cmd
     * @return com.mfexpress.component.response.Result<java.lang.Integer>
     */
    @PostMapping(value = "/v3/maintenance/repair/finish")
    Result<Integer> finishRepair(@RequestBody MaintenanceFinishRepairCmd cmd);

    /**
     * <p>维修工单完成</p>
     * <descript>维修工单状态改为已完成，并增加完成时间</descript>
     *
     * @param cmd
     * @return com.mfexpress.component.response.Result<java.lang.Integer>
     */
    @PostMapping(value = "/v3/maintenance/finish")
    Result<Integer> finish(@RequestBody MaintenanceIdCmd cmd);

    /**
     * 维修工单分页
     *
     * @param qry
     * @return
     */
    @PostMapping(value = "/v3/maintenance/page")
    Result<PagePagination<MaintenanceDTO>> page(@RequestBody MaintenancePageQry qry);

    @PostMapping(value = "/v3/pageH5")
    Result<PagePagination<MaintenanceDTO>> pageH5(@RequestBody MaintenancePageQry qryCmd);

    /**
     * 维修工单LISt
     *
     * @param qry
     * @return
     */
    @PostMapping(value = "/v3/maintenance/list")
    Result<List<MaintenanceDTO>> list(@RequestBody MaintenanceListQry qry);

    @PostMapping(value = "/v3/maintenance/list/ids")
    Result<List<MaintenanceDTO>> list(@RequestBody MaintenanceIdsQry qry);

    /*********************** 维修工单 end ***********************/


    //************************  维修服务单 start  ***********************
    @ApiOperation("获取维修服务单分页列表")
    @PostMapping("/serviceOrder/querypage")
    Result<PagePagination<MaintenanceServiceOrderPageDTO>> queryServiceOrderPage(@RequestBody MaintenanceServiceOrderPageQry maintenanceServiceOrderPageQry);

    @ApiOperation("批量获取维修服务单")
    @PostMapping("/serviceOrder/querylist")
    Result<List<MaintenanceServiceOrderDTO>> queryServiceOrderList(@RequestBody MaintenanceServiceOrderListQry maintenanceServiceOrderListQry);

    @ApiOperation("根据业务id获取维修服务单")
    @PostMapping("/serviceOrder/queryById")
    Result<MaintenanceServiceOrderDTO> queryServiceOrderById(@RequestParam("maintenanceServiceOrderId") Long maintenanceServiceOrderId);

    @ApiOperation("根据业务id更新")
    @PostMapping("/serviceOrder/updateById")
    Result<Boolean> updateServiceOrderById(@RequestBody MaintenanceServiceOrderDTO maintenanceServiceOrderDTO);

    @ApiOperation("创建维修服务单")
    @PostMapping("/serviceOrder/create")
    Result<Boolean> createServiceOrder(@RequestBody MaintenanceServiceOrderCreateCmd maintenanceServiceOrderCreateCmd);

    @ApiOperation("维修服务单完工暂存")
    @PostMapping("/serviceOrder/saveComplete")
    Result<Boolean> saveServiceOrderCompleted(@RequestBody MaintenanceServiceOrderCompletedCmd maintenanceServiceOrderCompletedCmd);

    @ApiOperation("维修服务单完工提交")
    @PostMapping("/serviceOrder/submitComplete")
    Result<Boolean> submitServiceOrderCompleted(@RequestBody MaintenanceServiceOrderCompletedCmd maintenanceServiceOrderCompletedCmd);

    @PostMapping("/serviceOrder/accept")
    @ApiOperation("维修服务单验收")
    Result<Boolean> acceptServiceOrder(@RequestBody MaintenanceServiceOrderAcceptCmd maintenanceServiceOrderAcceptCmd);

    @PostMapping("/serviceOrder/detail")
    @ApiOperation("维修服务单详情查询")
    Result<MaintenanceServiceOrderDetailDTO> queryServiceOrderDetail(@RequestParam("maintenanceServiceOrderId") Long maintenanceServiceOrderId);

    @PostMapping("/serviceOrder/queryByMaintenanceId")
    @ApiOperation("根据维修工单id查询修服务单")
    Result<List<MaintenanceServiceOrderDTO>> queryServiceOrderByMaintenanceId(@RequestBody MaintenanceIdCmd maintenanceIdCmd);

    @PostMapping("/serviceOrder/queryByQuotationId")
    @ApiOperation("根据报价单id查询修服务单")
    Result<MaintenanceServiceOrderDTO> queryServiceOrderByQuotationId(@RequestParam("quotationId") Long quotationId);

    //************************  维修服务单  end   ***********************


    //************************  报价单 start  ***********************

    @ApiOperation("获取报价单列表")
    @PostMapping("quotation/getList")
    Result<PagePagination<QuotationListDTO>> getQuotationList(@RequestBody QuotationListQry quotationListQry);

    @ApiOperation("回显子报价单")
    @PostMapping("quotation/echoQuotationSub")
    Result<EchoQuotationSubDTO> echoQuotationSub(@RequestBody @Validated EchoQuotationSubQry echoQuotationSubQry);

    @ApiOperation("过去报价单详情")
    @PostMapping("quotation/getQuotationSub")
    Result<QuotationDetailedDTO> getQuotation(@RequestBody @Validated QuotationQry quotationQry);

    @ApiOperation("暂存报价单信息")
    @PostMapping("quotation/stagingQuotation")
    Result<String> stagingQuotation(@RequestBody StagingQuotationDomainCmd stagingQuotationDomainCmd);

    @ApiOperation("作废报价单")
    @PostMapping("quotation/quotationCancel")
    Result<String> quotationCancel(@RequestBody @Validated QuotationCancelCmd quotationCancelCmd);

    @ApiOperation("创建报价单")
    @PostMapping("quotation/createQuotation")
    Result<String> createQuotation(@RequestBody @Validated CreateQuotationCmd createQuotationCmd);

    @ApiOperation("能否创建保养单")
    @PostMapping("quotation/canAddConservation")
    Result<CanAddConservationDTO> canAddConservation(@RequestBody @Validated AddConservationQry addConservationQry);

    @ApiOperation("提交报价单")
    @PostMapping("quotation/submitQuotation")
    Result<String> submitQuotation(@RequestBody @Validated StagingQuotationDomainCmd stagingQuotationCmd);

    @ApiOperation("oa审核通过")
    @PostMapping("quotation/oaApproved")
    Result<String> oaApproved(@RequestBody @Validated OAUpdateQuotationCmd OAUpdateQuotationCmd);

    @ApiOperation("oa审核驳回")
    @PostMapping("quotation/oaReject")
    Result<String> oaReject(@RequestBody @Validated OAUpdateQuotationCmd OAUpdateQuotationCmd);

    @PostMapping("quotation/getQuotationAndSubDTO")
    @ApiOperation("查询维修工单下所有报价单")
    Result<List<QuotationAndSubDTO>> getQuotationAndSubDTO(@RequestBody Long maintenanceId);

    @ApiOperation("根据子报价单id查询子报价单基础信息")
    @PostMapping("quotation/getQuotationById")
    Result<QuotationRoughDTO> getQuotationById(@RequestBody @NotNull(message = "报价单id不能为空") Long quotationId);

    //************************  报价单  end   ***********************


    //*********************** 取修任务 start *************************//

    @ApiOperation("列表查询")
    @PostMapping("pageFetchRepair")
    Result<PagePagination<FetchRepairListDTO>> pageFetchRepair(@RequestBody FetchRepairListQryDTO fetchRepairListQryDTO);

    @ApiOperation("获取去修任务详情")
    @PostMapping("getFetchRepairInfo")
    Result<FetchRepairDetailDTO> getFetchRepairInfo(@RequestParam("fetchRepairId") Long fetchRepairId);

    @PostMapping("fetchSubmit")
    @ApiOperation("取修提交")
    Result<Boolean> fetchSubmit(@RequestBody FetchSubmitDTO fetchSubmitDTO);

    @PostMapping("liquidation")
    @ApiOperation("款项清理")
    Result<List<MaintenanceFeeDTO>> liquidation(@RequestBody MaintainLiquidationCreateDTO maintainLiquidationCreateDTO);

    @ApiOperation("查询维修费用")
    @PostMapping("getMaintenanceFee")
    Result<List<MaintenanceFeeDTO>> getMaintenanceFee(@RequestParam("maintenanceId") Long maintenanceId);

    @ApiOperation("根据维修工单获取")
    @PostMapping("getFetchRepairByMaintenanceId")
    Result<FetchRepairDTO> getFetchRepairByMaintenanceId(@RequestParam("maintenanceId") Long maintenanceId);


    //*********************** 取修任务 end *************************//

    @ApiOperation("验车完成")
    @PostMapping("confirmFetch")
    Result<Boolean> confirmFetch(@RequestBody ConfirmFetchDTO confirmFetchDTO);

    /**
     * ********************  送修单 start
     ********************/

    @ApiOperation("查询h5列表")
    @PostMapping("/sendrepair/queryListH5")
    Result<PagePagination<MaintenanceSendRepairDTO>> querySendRepairH5Page(@RequestBody MaintenanceSendRepairPageH5Qry maintenanceSendRepairPageH5Qry);

    @ApiOperation("查询web列表")
    @PostMapping("/sendrepair/queryListWeb")
    Result<PagePagination<MaintenanceSendRepairDTO>> querySendRepairWebPage(@RequestBody MaintenanceSendRepairPageWebQry maintenanceSendRepairPageWebQry);

    @ApiOperation("查询h5详情")
    @PostMapping("/sendrepair/queryByIdH5")
    Result<MaintenanceSendRepairDTO> querySendRepairH5ById(@RequestParam("maintenanceSendRepairId") Long maintenanceSendRepairId);

    @ApiOperation("查询web详情")
    @PostMapping("/sendrepair/queryByIdWeb")
    Result<MaintenanceSendRepairDTO> querySendRepairWebById(@RequestParam("maintenanceSendRepairId") Long maintenanceSendRepairId);

    @ApiOperation("查询h5送修单数量")
    @PostMapping("/sendrepair/queryNum")
    Result<Integer> querySendRepairNum();

    @ApiOperation("受理")
    @PostMapping("/sendrepair/accept")
    Result<Long> acceptSendRepair(@RequestBody MaintenanceSendRepairAcceptCmd maintenanceSendRepairAcceptCmd);

    @ApiOperation("取消受理")
    @PostMapping("/sendrepair/cancel")
    Result<Long> cancelSendRepair(@RequestBody MaintenanceSendRepairCancelCmd maintenanceSendRepairCancelCmd);

    @ApiOperation("入场")
    @PostMapping("/sendrepair/admittance")
    Result<Long> admittanceSendRepair(@RequestBody MaintenanceSendRepairAdmittanceCmd maintenanceSendRepairAdmittanceCmd);

    @ApiOperation("创建")
    @PostMapping("/sendrepair/create")
    Result<Long> createSendRepair(@RequestBody MaintenanceSendRepairCreateCmd maintenanceSendRepairCreateCmd);

    @ApiOperation("根据工单id查询送修详情")
    @PostMapping("/querySendRepairByMaintenanceId")
    Result<MaintenanceSendRepairDTO> querySendRepairByMaintenanceId(@RequestParam("maintenanceId") Long maintenanceId);

    /** *********************  送修单 end  ***********************/

    /**
     * ********************  保养 start
     *************************/

    @ApiOperation("查询保养列表")
    @PostMapping("/conservation/queryPage")
    Result<PagePagination<MaintenanceConservationDTO>> queryConservationPage(@RequestBody MaintenanceConservationPageQry maintenanceConservationPageQry);

    @ApiOperation("查询保养详情")
    @PostMapping("/conservation/queryById")
    Result<MaintenanceConservationDTO> queryConservationById(@RequestParam("maintenanceConservationId") Long maintenanceConservationId);

    @ApiOperation("根据车辆id查询所有保养信息")
    @PostMapping("/conservation/queryConservationListByVehicleId")
    Result<List<MaintenanceConservationDTO>> queryConservationListByVehicleId(@RequestParam("vehicleId") Integer vehicleId);

//    @ApiOperation("根据子报价单查询上次保养信息")
//    @PostMapping("/conservation/queryConservationBySubId")
//    Result<MaintenanceConservationDTO> queryConservationBySubId(@RequestParam("quotationId") Long quotationId);

//    @ApiOperation("根据车辆id查询最新保养信息")
//    @PostMapping("/conservation/queryConservationByVehicleId")
//    Result<MaintenanceConservationDTO> queryConservationByVehicleId(@RequestParam("vehicleId") Integer vehicleId);
//
//    @ApiOperation("根据车辆id查询上次保养信息")
//    @PostMapping("/conservation/queryLastConservationByVehicleId")
//    Result<MaintenanceConservationDTO> queryLastConservationByVehicleId(@RequestParam("vehicleId") Integer vehicleId);

    @PostMapping("/getMaintenanceReplaceVehicleListByMaintenanceId")
    Result<List<MaintenanceReplaceVehicleDTO>> getMaintenanceReplaceVehicleListByMaintenanceId(@RequestBody @Validated MaintenanceReplaceVehicleQryCmd qryCmd);

    @PostMapping("/saveReplaceVehicle")
    Result<Boolean> saveReplaceVehicle(@RequestBody MaintenanceReplaceVehicleSaveCmd replaceVehicleSaveCmd);


    /**
     * ********************  保养 end
     *************************/


    @PostMapping("getProductItem")
    @ApiOperation("产品项查询")
    Result<List<ProductItemDTO>> getProductItem(@RequestBody ProductItemQry productItemQry);

    @PostMapping("getProductItemById")
    Result<List<ProductItemDTO>> getProductItemById(@RequestBody List<Integer> idList);


    /**
     * *******************  维修厂 start
     ************************/

    @ApiOperation("维修厂列表")
    @PostMapping("/getMaintenanceShopList")
    Result<List<MaintenanceShopDTO>> getMaintenanceShopList(@RequestBody MaintenanceShopQryCmd qryCmd);

    @ApiOperation("维修厂详情")
    @PostMapping("/getMaintenanceShopInfo")
    Result<MaintenanceShopDTO> getMaintenanceShopInfo(@RequestParam("maintenanceShopId") Integer id);

    @ApiOperation("条件查询维修厂列表")
    @PostMapping("/selectShopList")
    Result<List<MaintenanceShopDTO>> selectShopList(@RequestBody MaintenanceShopListQry qryCmd);


    /**
     * *******************  维修厂 end
     *************************/

    @ApiOperation("款项清理维修信息")
    @PostMapping("getLiquidationMaintenanceFee")
    Result<LiquidationMaintenanceFeeDTO> getLiquidationMaintenanceFee(@RequestParam("maintenanceId") Long maintenanceId);

    @ApiOperation("获取款项清理维修信息列表")
    @PostMapping("getLiquidationMaintenanceFeeList")
    Result<List<LiquidationMaintenanceFeeDTO>> getLiquidationMaintenanceFeeList(@RequestBody List<Long> maintenanceIds);

    @ApiOperation("取消替换车")
    @PostMapping("cancelReplaceVehicle")
    Result<Boolean> cancelReplaceVehicle(@RequestBody MaintenanceReplaceVehicleSaveCmd replaceVehicleSaveCmd);

    @ApiOperation("替换车短期转长期维修工单维修性质改为库存中维修")
    @PostMapping({"/replaceDeliverRecoverVehicle"})
    Result<Boolean> replaceDeliverRecoverVehicle(@RequestParam("sourceServeNo") String sourceServeNo);

    @PostMapping("/getMaintenanceReplaceVehicleList")
    Result<List<MaintenanceReplaceVehicleDTO>> getMaintenanceReplaceVehicleList(@RequestBody @Validated MaintenanceReplaceVehicleQryCmd qryCmd);
}
