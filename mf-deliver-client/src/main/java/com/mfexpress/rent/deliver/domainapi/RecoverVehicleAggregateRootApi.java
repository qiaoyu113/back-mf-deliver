package com.mfexpress.rent.deliver.domainapi;

import java.util.List;
import java.util.Map;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalQry;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverInvalidCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/recovervehicle", contextId = "mf-deliver-recover-aggregate-root-api")
public interface RecoverVehicleAggregateRootApi {

    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(@RequestParam("deliverNo") String deliverNo);

    @PostMapping("/addRecoverVehicle")
    Result<String> addRecoverVehicle(@RequestBody List<RecoverVehicleDTO> recoverVehicleDTOList);

    @PostMapping("/cancelRecover")
    Result<String> cancelRecover(@RequestBody RecoverVehicleDTO recoverVehicleDTO);

    /*@PostMapping("/toCheck")
    Result<String> toCheck(@RequestBody RecoverVehicleDTO recoverVehicleDTO);*/

    @PostMapping("/toBackInsure")
    Result<List<RecoverVehicleDTO>> toBackInsure(@RequestBody List<String> serveNo);

    /**
     * @deprecated  废弃方法 查询收车单只能使用交付单
     * getRecoverVehicleDtosByDeliverNoList
     */
    @PostMapping("/getRecoverVehicleByServeNo")
    Result<Map<String, RecoverVehicle>> getRecoverVehicleByServeNo(@RequestBody List<String> serveNoList);

    @PostMapping("/abnormalRecover")
    Result<Integer> abnormalRecover(@RequestBody @Validated RecoverAbnormalCmd cmd);

    @PostMapping("/recovered")
    Result<Integer> recovered(@RequestParam("deliverNo") String deliverNo, @RequestParam("foreignNo") String foreignNo);

    @PostMapping("/getRecoverAbnormalByQry")
    Result<RecoverAbnormalDTO> getRecoverAbnormalByQry(@RequestBody RecoverAbnormalQry qry);

    @PostMapping("/updateDeductionFee")
    Result<Integer> updateDeductionFee(@RequestBody RecoverDeductionCmd cmd);

    @PostMapping("/getRecoverVehicleDtosByDeliverNoList")
    Result<List<RecoverVehicleDTO>> getRecoverVehicleDtosByDeliverNoList(@RequestParam("deliverNoList") List<String> deliverNoList);

    @PostMapping("/getRecoverVehicleDTOByDeliverNos")
    Result<List<RecoverVehicleDTO>> getRecoverVehicleDTOByDeliverNos(@RequestParam("deliverNoList") List<String> deliverNoList);

    @PostMapping("/getRecentlyHistoryMapByServeNoList")
    Result<Map<String, RecoverVehicleDTO>> getRecentlyHistoryMapByServeNoList(@RequestParam("reactiveServeNoList") List<String> reactiveServeNoList);

    @PostMapping("/updateDeductionFeeByDeliver")
    Result<Integer> updateDeductionFeeByDeliver(@RequestBody RecoverDeductionByDeliverCmd cmd);

    /**
     * 收车无效
     *
     * @param cmd
     * @return
     */
    @PostMapping(value = "/recover/invalid")
    Result<Integer> invalidRecover(@RequestBody RecoverInvalidCmd cmd);
}
