package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.*;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.VehicleViolationDeliverInfoDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.UndoReactiveServeCmd;
import com.mfexpress.rent.deliver.entity.DeliverEntity;

import java.util.List;

public interface DeliverEntityApi {

    List<DeliverDTO> getDeliverDTOListByServeNoList(List<String> serveNoList);

    void toHistory(ReactivateServeCmd cmd);

    DeliverDTO getDeliverDTOByCarId(Integer carId);

    List<DeliverDTO> getValidDeliverByCarIdList(List<Integer> vehicleId);

    /**
     * 根据服务单编号查询未完成（存在费用未处理）得交付单
     *
     * @param serveNoList 服务单编号
     * @return 交付单
     */
    List<DeliverDTO> getDeliverNotComplete(List<String> serveNoList);

    DeliverDTO getDeliverByDeliverNo(String deliverNo);

    void cancelDeliver(DeliverCancelCmd cmd);

    /**
     * 完成发车(修改deliverStatus=2)
     *
     * @param cmd
     */
    void completedDeliver(DeliverCompletedCmd cmd);

    Integer insureByCompany(DeliverInsureCmd cmd);

    Integer insureByCustomer(DeliverInsureByCustomerCmd cmd);

    Integer insureComplete(InsureCompleteCmd cmd);

    List<InsuranceApplyDTO> getInsuranceApplyListByDeliverNoList(List<String> deliverNoList);

    Integer cancelSelectedByDeliver(CancelPreSelectedCmd cmd);

    Integer backInsure(RecoverBackInsureByDeliverCmd cmd);

    InsuranceApplyDTO getInsuranceApply(InsureApplyQry qry);

    List<DeliverDTO> getDeliverDTOListByDeliverNoList(List<String> deliverNoList);

    Integer preSelectedSupplyInsurance(List<DeliverDTO> list);

    Integer undoHistory(UndoReactiveServeCmd cmd);

    List<DeliverDTO> getLeaseDeliverByCarId(List<Integer> carIdList);

    List<DeliverDTO> getDeliverListByCarId(Integer vehicleId);


    VehicleViolationDeliverInfoDTO getVehicleViolationDeliverInfoByDeliverId(Long deliverId);

    List<DeliverDTO> getDeliverListByCarIdList(List<Integer> vehicleIdList);
    Integer updateDeliverByServeNoList(List<String> serveNoList, DeliverEntity deliver);

}
