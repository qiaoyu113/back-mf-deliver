package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReactiveServeCheckCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    // 如果服务单为重新激活的服务单，那么当前操作日期不能超过预计收车日期
    public void execute(ReactivateServeCheckCmd cmd) {
        Result<List<ServeDTO>> serveDTOListResult = serveAggregateRootApi.getServeDTOByServeNoList(cmd.getServeNoList());
        List<ServeDTO> serveDTOList = ResultDataUtils.getInstance(serveDTOListResult).getDataOrException();
        if (null == serveDTOList || serveDTOList.isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单查询失败");
        }
        Map<String, ServeDTO> serveDTOMap = serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (v1, v2) -> v1));

        // 默认校验当前操作时间
        List<String> reactiveServeNoList = new ArrayList<>();
        Date nowDate = DateUtil.parse(DateUtil.today());
        for (ServeDTO serveDTO : serveDTOMap.values()) {
            if (Optional.ofNullable(serveDTO).filter(s -> ServeEnum.CANCEL.getCode().equals(s.getStatus())).isPresent()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单已作废,不能继续发车");
            }
            if (JudgeEnum.YES.getCode().equals(serveDTO.getReactiveFlag())) {
                if (nowDate.after(DateUtil.parseDate(serveDTO.getExpectRecoverDate()))) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单：" + serveDTO.getServeNo() + "已过预计收车日期，不可进行操作");
                }
                reactiveServeNoList.add(serveDTO.getServeNo());
            }
        }

        //如果发车日期不为空，校验其与上次收车时间和预计收车日期的关系，上次收车时间<=发车日期<=预计收车日期
        if (null != cmd.getDeliverVehicleTime() && !reactiveServeNoList.isEmpty()) {
            checkDeliverVehicleTime(serveDTOMap, reactiveServeNoList, cmd.getDeliverVehicleTime());
        }
    }

    private void checkDeliverVehicleTime(Map<String, ServeDTO> serveDTOMap, List<String> reactiveServeNoList, Date deliverVehicleTime) {
        Result<Map<String, RecoverVehicleDTO>> recoverVehicleDTOMapResult = recoverVehicleAggregateRootApi.getRecentlyHistoryMapByServeNoList(reactiveServeNoList);
        Map<String, RecoverVehicleDTO> recoverVehicleDTOMap = ResultDataUtils.getInstance(recoverVehicleDTOMapResult).getDataOrException();
        if (null == recoverVehicleDTOMap || recoverVehicleDTOMap.isEmpty()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "历史收车单查询失败");
        }

        deliverVehicleTime = getYmdDate(deliverVehicleTime);
        Date finalDeliverVehicleTime = deliverVehicleTime;
        reactiveServeNoList.forEach(reactiveServeNo -> {
            RecoverVehicleDTO recoverVehicleDTO = recoverVehicleDTOMap.get(reactiveServeNo);
            if (null == recoverVehicleDTO) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "历史收车单获取失败");
            }

            Date recoverVehicleTime = recoverVehicleDTO.getRecoverVehicleTime();
            String expectRecoverDateChar = serveDTOMap.get(reactiveServeNo).getExpectRecoverDate();

            // 判断到天，而不是时分秒
            recoverVehicleTime = getYmdDate(recoverVehicleTime);
            DateTime expectRecoverDate = DateUtil.parseDate(expectRecoverDateChar);
            if (recoverVehicleTime.after(finalDeliverVehicleTime)) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单：" + reactiveServeNo + "发车时间小于上次收车时间，不可进行操作");
            }
            if (expectRecoverDate.before(finalDeliverVehicleTime)) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单：" + reactiveServeNo + "发车时间大于预计收车日期，不可进行操作");
            }
        });
    }

    private Date getYmdDate(Date date) {
        long time = date.getTime();
        return new Date(time - (time % 86400000));
    }
}
