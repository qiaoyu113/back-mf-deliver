package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
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
            return Result.getInstance(serveDTO).success();
        }

        return Result.getInstance((ServeDTO) null).success();
    }

    @Override
    @PostMapping("/addServe")
    public Result<String> addServe(@RequestBody ServeAddDTO serveAddDTO) {
        List<Serve> serveList = new LinkedList<>();
        List<ServeVehicleDTO> vehicleDTOList = serveAddDTO.getVehicleDTOList();
        if (vehicleDTOList == null) {
            return Result.getInstance("").fail(-1, "车辆信息为空");
        }
        //订单号放入redis
        redisTools.set(serveAddDTO.getOrderId().toString(), serveAddDTO.getOrderId(), 10000);
        for (ServeVehicleDTO serveVehicleDTO : vehicleDTOList) {
            Integer num = serveVehicleDTO.getNum();
            for (int i = 0; i < num; i++) {
                Serve serve = new Serve();
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_SERVE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String serveNo = DeliverUtils.getNo(Constants.REDIS_SERVE_KEY, incr);
                Long bizId = redisTools.getBizId(Constants.REDIS_BIZ_ID_SERVER);
                serve.setServeId(bizId);
                //创建服务单订单传orgId
                serve.setOrgId(serveAddDTO.getOrgId());
                serve.setSaleId(serveAddDTO.getSaleId());
                serve.setServeNo(serveNo);
                serve.setCreateId(serveAddDTO.getCreateId());
                serve.setOrderId(serveAddDTO.getOrderId());
                serve.setCustomerId(serveAddDTO.getCustomerId());
                serve.setCarModelId(serveVehicleDTO.getCarModelId());
                serve.setBrandId(serveVehicleDTO.getBrandId());
                serve.setLeaseModelId(serveVehicleDTO.getLeaseModelId());
                serve.setCreateTime(new Date());
                serve.setUpdateTime(new Date());
                serve.setCarServiceId(0);
                serve.setCityId(0);
                serve.setUpdateId(0);
                serve.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
                serve.setRemark("");
                serveList.add(serve);
            }
        }
        try {
            serveGateway.addServeList(serveList);
        } catch (Exception e) {
            return Result.getInstance(serveAddDTO.getOrderId().toString()).fail(-1, "服务单生成失败");
        }

        return Result.getInstance("服务单生成成功").success();
    }

    @Override
    @PostMapping("/toPreselected")
    public Result<String> toPreselected(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("预选成功").success() : Result.getInstance("预选失败").fail(-1, "预选失败");

    }

    @Override
    @PostMapping("/toReplace")
    public Result<String> toReplace(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);
        return i > 0 ? Result.getInstance("更换成功").success() : Result.getInstance("更换失败").fail(-1, "更换失败");
    }

    @Override
    @PostMapping("/deliver")
    public Result<String> deliver(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.DELIVER.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("发车成功").success() : Result.getInstance("发车失败").fail(-1, "发车失败");
    }

    @Override
    @PostMapping("/recover")
    public Result<String> recover(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.RECOVER.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("收车完成").success() : Result.getInstance("收车失败").fail(-1, "收车失败");
    }

    @Override
    @PostMapping("/completed")
    public Result<String> completed(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.COMPLETED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);
        return i > 0 ? Result.getInstance("处理违章完成").success() : Result.getInstance("处理违章失败").fail(-1, "处理违章失败");
    }

    @Override
    @PostMapping("/completedList")
    public Result<String> completedList(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.COMPLETED.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("退保完成").success() : Result.getInstance("退保失败").fail(-1, "退保失败");

    }

    @PostMapping("/getServePreselectedDTO")
    @Override
    public Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId) {
        return Result.getInstance(serveGateway.getServePreselectedByOrderId(orderId)).success();
    }

    @Override
    @PostMapping("/cancelSelected")
    public Result<String> cancelSelected(@RequestParam("serveNo") String serveNo) {

        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);

        return i > 0 ? Result.getInstance("取消预选成功").success() : Result.getInstance("取消预选失败").fail(-1, "取消预选失败");
    }

    @Override
    @PostMapping("/cancelSelectedList")
    public Result<String> cancelSelectedList(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        serveGateway.updateServeByServeNoList(serveNoList, serve);
        return Result.getInstance("取消预选成功").success();
    }


}
