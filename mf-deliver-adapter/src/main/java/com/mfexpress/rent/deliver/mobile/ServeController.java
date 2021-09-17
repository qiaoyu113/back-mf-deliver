package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/deliver/v3/serve")
@Api(tags = "api--交付--1.1租赁服务单", value = "ServeController")
@ApiSort(1)
public class ServeController {

    @Resource
    private ServeServiceI serveServiceI;


    //====================租赁服务单生成===============//

    @ApiOperation("生成租赁服务单")
    @PostMapping("/addServe")
    public Result<String> addServe(@RequestBody ServeAddCmd serveAddCmd) {

        return Result.getInstance(serveServiceI.addServe(serveAddCmd));
    }
    //====================发车任务=======================//

    @PostMapping("/getServeDeliverTaskListVO")
    @ApiOperation("发车任务列表")
    public Result<ServeDeliverTaskListVO> getServeStayDeliverTaskListVO(@RequestBody ServeDeliverTaskQryCmd serveDeliverTaskQryCmd) {

        return Result.getInstance(serveServiceI.getServeDeliverTaskListVO(serveDeliverTaskQryCmd));
    }


    @PostMapping("/getServeFastPreselectedVO")
    @ApiOperation("订单快速预选页")
    public Result<ServeFastPreselectedListVO> getServeFastPreselectedVO(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeFastPreselectedVO(serveQryListCmd)).success();
    }

    //===================发车操作列表======================//


    @Valid
    @PostMapping("/getServeListVoByOrderNoAll")
    @ApiOperation("全部发车操作列表")
    public Result<ServeListVO> getServeListVoByOrderNoAll(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoByOrderNoAll(serveQryListCmd)).success();
    }


    @Valid
    @PostMapping("/getServeListVoPreselected")
    @ApiOperation("发车操作待预选列表")
    public Result<ServePreselectedListVO> getServeListVoPreselected(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoPreselected(serveQryListCmd)).success();
    }


    @Valid
    @PostMapping("/getServeListVoInsure")
    @ApiOperation("发车操作待投保列表")
    public Result<ServeListVO> getServeListVoInsure(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoInsure(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoCheck")
    @ApiOperation("发车操作待验车列表")
    public Result<ServeListVO> getServeListVoCheck(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoCheck(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoDeliver")
    @ApiOperation("发车操作待发车列表")
    public Result<ServeListVO> getServeListVoDeliver(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoDeliver(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoCompleted")
    @ApiOperation("发车操作已完成列表")
    public Result<ServeListVO> getServeListVoCompleted(@RequestBody ServeQryListCmd serveQryListCmd) {
        return Result.getInstance(serveServiceI.getServeListVoCompleted(serveQryListCmd)).success();
    }


}
