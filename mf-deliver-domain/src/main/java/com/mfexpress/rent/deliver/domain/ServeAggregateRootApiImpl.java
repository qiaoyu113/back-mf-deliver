package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @PrintParam
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
    @PrintParam
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
                serve.setRent(serveVehicleDTO.getRent());
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
    @PrintParam
    public Result<String> toPreselected(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("预选成功").success() : Result.getInstance("预选失败").fail(-1, "预选失败");

    }

    @Override
    @PostMapping("/toReplace")
    @PrintParam
    public Result<String> toReplace(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);
        return i > 0 ? Result.getInstance("更换成功").success() : Result.getInstance("更换失败").fail(-1, "更换失败");
    }

    @Override
    @PostMapping("/deliver")
    @PrintParam
    public Result<String> deliver(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.DELIVER.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("发车成功").success() : Result.getInstance("发车失败").fail(-1, "发车失败");
    }

    @Override
    @PostMapping("/recover")
    @PrintParam
    public Result<String> recover(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.RECOVER.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("收车完成").success() : Result.getInstance("收车失败").fail(-1, "收车失败");
    }

    @Override
    @PostMapping("/completed")
    @PrintParam
    public Result<String> completed(@RequestParam("serveNo") String serveNo) {
        Serve serve = Serve.builder().status(ServeEnum.COMPLETED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);
        return i > 0 ? Result.getInstance("处理违章完成").success() : Result.getInstance("处理违章失败").fail(-1, "处理违章失败");
    }

    @Override
    @PostMapping("/completedList")
    @PrintParam
    public Result<String> completedList(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.COMPLETED.getCode()).build();
        int i = serveGateway.updateServeByServeNoList(serveNoList, serve);
        return i > 0 ? Result.getInstance("退保完成").success() : Result.getInstance("退保失败").fail(-1, "退保失败");

    }

    @PostMapping("/getServePreselectedDTO")
    @Override
    @PrintParam
    public Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId) {
        return Result.getInstance(serveGateway.getServePreselectedByOrderId(orderId)).success();
    }

    @Override
    @PostMapping("/cancelSelected")
    @PrintParam
    public Result<String> cancelSelected(@RequestParam("serveNo") String serveNo) {

        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        int i = serveGateway.updateServeByServeNo(serveNo, serve);

        return i > 0 ? Result.getInstance("取消预选成功").success() : Result.getInstance("取消预选失败").fail(-1, "取消预选失败");
    }

    @Override
    @PostMapping("/cancelSelectedList")
    @PrintParam
    public Result<String> cancelSelectedList(@RequestBody List<String> serveNoList) {
        Serve serve = Serve.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        serveGateway.updateServeByServeNoList(serveNoList, serve);
        return Result.getInstance("取消预选成功").success();
    }

    @Override
    @PostMapping("/getServeNoListAll")
    @PrintParam
    public Result<List<String>> getServeNoListAll() {
        List<String> serveNoListAll = serveGateway.getServeNoListAll();
        return Result.getInstance(serveNoListAll);
    }

    @Override
    @PostMapping("/getServeDailyDTO")
    public Result<PagePagination<ServeDailyDTO>> getServeDailyDTO(@RequestBody ListQry listQry) {
        try {
            PageHelper.clearPage();
            if (listQry.getLimit() == 0) {
                PageHelper.startPage(1, listQry.getLimit());
            }
            PageHelper.startPage(listQry.getPage(), listQry.getLimit());
            //查询当前状态为已发车或维修中

            List<Serve> serveList = serveGateway.getServeByStatus();
            if (serveList != null) {
                List<ServeDailyDTO> serveDailyDTOList = serveList.stream().map(serve -> {
                    ServeDailyDTO serveDailyDTO = new ServeDailyDTO();
                    serveDailyDTO.setServeNo(serve.getServeNo());
                    serveDailyDTO.setCustomerId(serve.getCustomerId());
                    return serveDailyDTO;
                }).collect(Collectors.toList());
                return Result.getInstance(PagePagination.getInstance(serveDailyDTOList)).success();
            }
            return Result.getInstance((PagePagination<ServeDailyDTO>) null).fail(-1, "");
        } catch (Exception e) {
            return Result.getInstance((PagePagination<ServeDailyDTO>) null).fail(-1, "");
        }
    }

    @Override
    @PostMapping("/getServeMapByServeNoList")
    public Result<Map<String, Serve>> getServeMapByServeNoList(@RequestBody List<String> serveNoList) {
        List<Serve> serveList = serveGateway.getServeByServeNoList(serveNoList);
        Map<String, Serve> map = serveList.stream().collect(Collectors.toMap(Serve::getServeNo, Function.identity()));
        return Result.getInstance(map).success();
    }

    @Override
    @PostMapping("/getCycleServe")
    public Result<PagePagination<ServeDTO>> getCycleServe(@RequestBody ListQry listQry) {
        try {
            PageHelper.clearPage();
            if (listQry.getLimit() == 0) {
                PageHelper.startPage(1, listQry.getLimit());
            }
            PageHelper.startPage(listQry.getPage(), listQry.getLimit());
            //查询当前状态为已发车或维修中

            List<Serve> serveList = serveGateway.getCycleServe();
            if (serveList != null) {
                List<ServeDTO> serveDailyDTOList = serveList.stream().map(serve -> {
                    ServeDTO serveDTO = new ServeDTO();
                    BeanUtils.copyProperties(serve, serveDTO);
                    return serveDTO;
                }).collect(Collectors.toList());
                return Result.getInstance(PagePagination.getInstance(serveDailyDTOList)).success();
            }
            return Result.getInstance((PagePagination<ServeDTO>) null).fail(-1, "");
        } catch (Exception e) {
            return Result.getInstance((PagePagination<ServeDTO>) null).fail(-1, "");
        }

    }

    /* luzheng add */
    @Override
    @PostMapping("/toRepair")
    @PrintParam
    public Result<String> toRepair(@RequestParam("serveNo") String serveNo) {
        Serve serve = serveGateway.getServeByServeNo(serveNo);
        if(null == serve){
            return Result.getInstance("").fail(-1, "服务单不存在");
        }
        if(!ServeEnum.DELIVER.getCode().equals(serve.getStatus())){
            return Result.getInstance("").fail(-1, "服务单状态异常");
        }
        Serve serveToUpdate = new Serve();
        serveToUpdate.setStatus(ServeEnum.REPAIR.getCode());
        int i = serveGateway.updateServeByServeNo(serveNo, serveToUpdate);
        return i > 0 ? Result.getInstance("修改成功").success() : Result.getInstance("修改失败").fail(-1, "修改失败");
    }

    @Override
    @PostMapping("/cancelOrCompleteRepair")
    @PrintParam
    public Result<String> cancelOrCompleteRepair(@RequestParam("serveNo") String serveNo) {
        Serve serve = serveGateway.getServeByServeNo(serveNo);
        if(null == serve){
            return Result.getInstance("").fail(-1, "服务单不存在");
        }
        if(!ServeEnum.REPAIR.getCode().equals(serve.getStatus())){
            return Result.getInstance("").fail(-1, "服务单状态异常");
        }
        Serve serveToUpdate = new Serve();
        serveToUpdate.setStatus(ServeEnum.DELIVER.getCode());
        int i = serveGateway.updateServeByServeNo(serveNo, serveToUpdate);
        return i > 0 ? Result.getInstance("修改成功").success() : Result.getInstance("修改失败").fail(-1, "修改失败");
    }

    @Override
    @PostMapping("/addServeForReplaceVehicle")
    @PrintParam
    public Result<String> addServeForReplaceVehicle(@RequestBody ServeReplaceVehicleAddDTO serveAddDTO) {

        String serveNo = serveAddDTO.getServeNo();
        Serve serve = serveGateway.getServeByServeNo(serveNo);
        if(null == serve){
            return Result.getInstance("").fail(-1, "原服务单不存在");
        }
        if(!ServeEnum.REPAIR.getCode().equals(serve.getStatus())){
            return Result.getInstance("").fail(-1, "原服务单状态异常");
        }

        Long newServeId = redisTools.getBizId(Constants.REDIS_BIZ_ID_SERVER);
        long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_SERVE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
        String newServeNo = DeliverUtils.getNo(Constants.REDIS_SERVE_KEY, incr);

        serve.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
        serve.setCarModelId(serveAddDTO.getModelsId());
        serve.setBrandId(serveAddDTO.getBrandId());
        serve.setCreateId(serveAddDTO.getCreatorId());
        serve.setUpdateId(serveAddDTO.getCreatorId());
        serve.setServeId(newServeId);
        serve.setServeNo(newServeNo);
        serve.setCreateTime(new Date());
        serve.setUpdateTime(new Date());
        serve.setId(null);
        // 替换车申请的服务单 其月租金应为0
        serve.setRent(null);

        try {
            serveGateway.addServeList(Collections.singletonList(serve));
        } catch (Exception e) {
            return Result.getInstance(serveAddDTO.getServeNo()).fail(-1, "替换车服务单生成失败");
        }

        return Result.getInstance(newServeNo).success();
    }

}
