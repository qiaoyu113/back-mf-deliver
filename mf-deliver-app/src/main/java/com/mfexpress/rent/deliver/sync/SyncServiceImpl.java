package com.mfexpress.rent.deliver.sync;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.component.starter.utils.MqTools;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class SyncServiceImpl implements SyncServiceI {

    @Resource
    private MqTools mqTools;

    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;
    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Value("${rocketmq.listenBinlogTopic}")
    private String listenBinlogTopic;

    @PostConstruct
    public void init() {
        DeliverBinlogDispatch deliverBinlogDispatch = new DeliverBinlogDispatch();
        deliverBinlogDispatch.setServiceI(this);
        mqTools.addBinlogCommand(listenBinlogTopic, deliverBinlogDispatch);

    }

    @Override
    public void execOne(String serveNo) {
        ServeES serveEs = new ServeES();
        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if (serveResult.getData() == null) {
            return;
        }
        ServeDTO serveDTO = serveResult.getData();
        BeanUtils.copyProperties(serveDTO, serveEs);
        serveEs.setServeStatus(serveDTO.getStatus());
        //todo 调用订单查询订单信息

        //预选状态下存在交付单
        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if (deliverResult.getData() != null) {
            serveEs.setIsPreselected(ServeEnum.PRESELECTED.getCode());
            DeliverDTO deliverDTO = deliverResult.getData();
            BeanUtils.copyProperties(deliverDTO, serveEs);
            //排序规则
            Integer sort = getSort(serveEs);
            serveEs.setSort(sort);

            Result<DeliverVehicleDTO> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
            if (deliverVehicleResult.getData() != null) {
                DeliverVehicleDTO deliverVehicleDTO = deliverVehicleResult.getData();
                serveEs.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
            }
            Result<RecoverVehicleDTO> recoverVehicleResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
            if (recoverVehicleResult.getData() != null) {
                RecoverVehicleDTO recoverVehicleDTO = recoverVehicleResult.getData();
                serveEs.setRecoverVehicleTime(recoverVehicleDTO.getRecoverVehicleTime());
                serveEs.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
            }
        } else {
            serveEs.setIsPreselected(ServeEnum.NOT_PRESELECTED.getCode());
            serveEs.setDeliverStatus(ServeEnum.NOT_PRESELECTED.getCode());
            serveEs.setIsCheck(0);
            serveEs.setIsInsurance(0);
            serveEs.setIsDeduction(0);
            serveEs.setSort(DeliverSortEnum.TWO.getSort());
        }
        elasticsearchTools.saveByEntity(Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveNo, serveEs);

    }

    private Integer getSort(ServeES serveEs) {
        int sort = DeliverSortEnum.ZERO.getSort();
        boolean deliverFlag = serveEs.getIsCheck().equals(JudgeEnum.NO.getCode()) || serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode());
        boolean recoverFlag = serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode()) || serveEs.getIsDeduction().equals(JudgeEnum.NO.getCode());
        //待发车
        if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            sort = DeliverSortEnum.ONE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && deliverFlag) {
            //待验车、待投保
            sort = DeliverSortEnum.THREE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.DELIVER.getCode())) {
            //已发车
            sort = DeliverSortEnum.FOUR.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && serveEs.getIsCheck().equals(JudgeEnum.NO.getCode())) {
            //收车中 待验车
            sort = DeliverSortEnum.ONE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && recoverFlag) {
            //收车中  待退保、待处理违章
            sort = DeliverSortEnum.TWO.getSort();
        } else if (serveEs.getDeliverStatus().equals(ServeEnum.COMPLETED.getCode())) {
            //已完成
            sort = DeliverSortEnum.THREE.getSort();
        }

        return sort;

    }
}
