package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVehicleDTO;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.Utils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/domain/deliver/v3/serve")
@Api(tags = "domain--交付--1.1租赁服务单聚合")
public class ServeAggregateRootApiImpl implements ServeAggregateRootApi {

    @Resource
    private ServeGateway serveGateway;
    @Resource
    private RedisTools redisTools;

    @Override
    @PostMapping("/getServeDtoByServeNo")
    public Result<ServeDTO> getServeDtoByServeNo(@RequestParam("serveNo") String serveNo) {
        Serve serve = serveGateway.getServeByServeNo(serveNo);
        ServeDTO serveDTO = new ServeDTO();
        if (serve != null) {
            BeanUtil.copyProperties(serve, serveDTO);
        }

        return Result.getInstance(serveDTO).success();
    }

    @Override
    @PostMapping("/addServe")
    public Result<String> addServe(@RequestBody ServeAddDTO serveAddDTO) {
        List<Serve> serveList = new LinkedList<>();
        List<ServeVehicleDTO> vehicleDTOList = serveAddDTO.getVehicleDTOList();
        if (vehicleDTOList == null) {
            return Result.getInstance("").fail(-1, "车辆信息为空");
        }
        for (ServeVehicleDTO serveVehicleDTO : vehicleDTOList) {
            Integer num = serveVehicleDTO.getNum();
            for (int i = 0; i < num; i++) {
                Serve serve = new Serve();
                long incr = redisTools.incr(Utils.getEnvVariable(Constants.REDIS_SERVE_KEY) + Utils.getDateByYYMMDD(new Date()), 1);
                String serveNo = Utils.getNo(Constants.REDIS_SERVE_KEY, incr);
                serve.setServeNo(serveNo);
                serve.setOrderId(serveAddDTO.getOrderId());
                serve.setCarModelId(serveVehicleDTO.getCarModelId());
                serve.setBrandId(serveVehicleDTO.getBrandId());
                serve.setLeaseModelId(serveVehicleDTO.getLeaseModelId());
                serveList.add(serve);
            }
        }
        serveGateway.addServeList(serveList);

        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toPreselected")
    public Result<String> toPreselected(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.PRESELECTED.getCode()).build();
        serveGateway.updateServeByServeNoList(serveNoList, serve);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toReplace")
    public Result<String> toReplace(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        serveGateway.updateServeByServeNo(serveNo, serve);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/deliver")
    public Result<String> deliver(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.DELIVER.getCode()).build();
        serveGateway.updateServeByServeNoList(serveNoList, serve);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/recover")
    public Result<String> recover(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(serveNoList, serve);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/completed")
    public Result<String> completed(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.COMPLETED.getCode()).build();
        serveGateway.updateServeByServeNo(serveNo, serve);
        return Result.getInstance("").success();
    }


}
