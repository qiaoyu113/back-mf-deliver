package com.mfexpress.rent.deliver.consumer.sync;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.builder.ESMappingBuilder;
import com.mfexpress.component.starter.elasticsearch.setting.ESIndexSettingEnum;
import com.mfexpress.component.starter.mq.relation.binlog.EsBatchSyncHandlerI;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogRelation;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogTableFullName;
import com.mfexpress.component.starter.tools.es.ESBatchSyncTools;
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.component.starter.tools.es.IdListPageQry;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverQry;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.es.DeliverES;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("deliverSyncServiceImpl")
@Slf4j
@MFMqBinlogRelation
public class DeliverSyncServiceImpl implements EsBatchSyncHandlerI {

    @Resource
    private ElasticsearchTools elasticsearchTools;

    private ESBatchSyncTools esBatchSyncTools;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;


    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    private int syncThreadNum = 5;

    private int limit = 100;

    @Override
    public boolean execAll(String indexVersionName) {
        // 创建索引
        indexVersionName = createIndex(indexVersionName);

        // 获取数据页数
        DeliverQry qry = new DeliverQry();
        qry.setPage(1);
        qry.setLimit(limit);
        Result<PagePagination<String>> deliverNoListPageResult = deliverAggregateRootApi.getDeliverNoListByPage(qry);
        PagePagination<String> pagePagination = ResultDataUtils.getInstance(deliverNoListPageResult).getDataOrException();
        int totalPages = pagePagination.getPagination().getTotalPages();

        // 执行批量同步
        esBatchSyncTools = new ESBatchSyncTools(elasticsearchTools, this, syncThreadNum, totalPages, limit, indexVersionName, Constants.ES_DELIVER_TYPE);
        esBatchSyncTools.execute();
        esBatchSyncTools.syncClose();

        return true;
    }

    private String createIndex(String indexVersionName) {
        if (StringUtils.isEmpty(indexVersionName)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "indexName不能为空");
        }
        // 拼接环境信息到indexName中
        indexVersionName = DeliverUtils.getEnvVariable(indexVersionName);
        boolean exist = elasticsearchTools.existIndex(indexVersionName);
        if (exist) {
            return indexVersionName;
        }

        Map<String, String> indexMappingMap = null;
        try {
            indexMappingMap = ESMappingBuilder.getInstance().buildMappingAsString(ServeES.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建index失败，解析es对象出错");
        }
        String mapping = indexMappingMap.get(Constants.ES_SERVE_TYPE);
        Settings.Builder setting = Settings.builder()
                .put(ESIndexSettingEnum.NUMBER_OF_SHARDS.getKey(), (int) ESIndexSettingEnum.NUMBER_OF_SHARDS.getDefaultValue())
                .put(ESIndexSettingEnum.NUMBER_OF_REPLICAS.getKey(), (int) ESIndexSettingEnum.NUMBER_OF_REPLICAS.getDefaultValue());
        boolean result = elasticsearchTools.createIndexWithMappingAndSetting(indexVersionName, Constants.ES_DELIVER_TYPE, mapping, setting);
        if (!result) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建index失败，详情请查看日志");
        }

        return indexVersionName;
    }

    @Override
    public boolean execAll() {
        return false;
    }

    @Override
    @MFMqBinlogTableFullName("mf-deliver.deliver")
    public boolean execOne(Map<String, String> map) {
        DeliverES deliverES = (DeliverES) assembleEsData(map);
        if (null != deliverES) {
            sendRequest(deliverES);
        }
        return true;
    }

    @Override
    public Object assembleEsData(Map<String, String> map) {
        String deliverNo = map.get("deliver_no");
        if (StringUtils.isEmpty(deliverNo)) {
            log.error("assembleEsData方法，从参数中取到的deliverNo为空");
            return null;
        }

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(deliverNo);
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrNull();
        if (null == deliverDTO) {
            log.error("assembleEsData方法，根据deliverNo：{}查到的deliver为空或查询失败", deliverNo);
            return null;
        }

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrNull();
        if (null == serveDTO) {
            log.error("assembleEsData方法，根据serveNo：{}查到的serve为空或查询失败", deliverDTO.getServeNo());
            return null;
        }

        DeliverES deliverES = new DeliverES();
        deliverES.setServeNo(deliverDTO.getServeNo());
        deliverES.setDeliverNo(deliverDTO.getDeliverNo());
        deliverES.setContractNo(serveDTO.getOaContractCode());
        deliverES.setFrameNum(deliverDTO.getFrameNum());
        deliverES.setServeStatus(serveDTO.getStatus());
        deliverES.setDeliverStatus(deliverDTO.getDeliverStatus());
        deliverES.setIsCheck(deliverDTO.getIsCheck());
        deliverES.setIsInsurance(deliverDTO.getIsInsurance());
        deliverES.setIsDeduction(deliverDTO.getIsDeduction());
        deliverES.setRecoverContractStatus(deliverDTO.getRecoverContractStatus());
        deliverES.setRecoverAbnormalFlag(deliverDTO.getRecoverAbnormalFlag());
        deliverES.setUpdateTime(deliverDTO.getUpdateTime());
        deliverES.setOrgId(serveDTO.getOrgId());
        deliverES.setVehicleBusinessMode(deliverDTO.getVehicleBusinessMode());

        // 客户信息补充
        deliverES.setCustomerId(serveDTO.getCustomerId());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(deliverES.getCustomerId());
        CustomerVO customerVO = ResultDataUtils.getInstance(customerResult).getDataOrNull();
        if (null != customerVO) {
            deliverES.setCustomerName(customerVO.getName());
            deliverES.setCustomerPhone(customerVO.getPhone());
        }

        // 车辆信息补充
        deliverES.setCarId(deliverDTO.getCarId());
        deliverES.setCarNum(deliverDTO.getCarNum());
        deliverES.setCarModelId(serveDTO.getCarModelId());
        Result<String> brandDisplayResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getBrandId());
        String brandDisplay = ResultDataUtils.getInstance(brandDisplayResult).getDataOrNull();
        if (!StringUtils.isEmpty(brandDisplay)) {
            deliverES.setBrandModelDisplay(brandDisplay);
        }

        // 发车日期补充
        Integer deliverStatus = deliverDTO.getDeliverStatus();
        if (DeliverEnum.DELIVER.getCode().equals(deliverStatus) || DeliverEnum.IS_RECOVER.getCode().equals(deliverStatus) || DeliverEnum.RECOVER.getCode().equals(deliverStatus)
                || DeliverEnum.COMPLETED.getCode().equals(deliverStatus)) {
            Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverES.getDeliverNo());
            DeliverVehicleDTO deliverVehicleDTO = ResultDataUtils.getInstance(deliverVehicleDTOResult).getDataOrNull();
            if (null != deliverVehicleDTO) {
                deliverES.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
            }
        }

        // 预计还车日期和实际收车日期补充
        if (DeliverEnum.IS_RECOVER.getCode().equals(deliverStatus) || DeliverEnum.RECOVER.getCode().equals(deliverStatus) || DeliverEnum.COMPLETED.getCode().equals(deliverStatus)) {
            Result<RecoverVehicleDTO> recoverVehicleDTOResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverNo);
            RecoverVehicleDTO recoverVehicleDTO = ResultDataUtils.getInstance(recoverVehicleDTOResult).getDataOrException();
            if (null != recoverVehicleDTO) {
                deliverES.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
                deliverES.setRecoverVehicleTime(recoverVehicleDTO.getRecoverVehicleTime());
            }
            if (DeliverEnum.RECOVER.getCode().equals(deliverStatus) || DeliverEnum.COMPLETED.getCode().equals(deliverStatus)) {
                deliverES.setRecoverTypeDisplay(RecoverVehicleType.getEnumValue(deliverDTO.getRecoverAbnormalFlag()));
            }
        }

        deliverES.setSort(getSort(deliverES));

        return deliverES;
    }

    @Override
    public List<Object> assembleEsDataList(List<String> idList) {
        Map<String, String> map = new HashMap<>();
        return idList.stream().map(id -> {
            map.put("deliver_no", id);
            return assembleEsData(map);
        }).collect(Collectors.toList());
    }

    @Override
    public void addRequest(Object obj, String indexName, String typeName) {
        DeliverES deliverES = (DeliverES) obj;
        /*if(JudgeEnum.NO.getCode().equals(serveES.getDelFlag())){
            IndexRequest indexRequest = new IndexRequest(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
            indexRequest.source(JSON.toJSONString(serveES), XContentType.JSON);
            esBatchSyncTools.addIndexRequest(indexRequest);
        }else if (JudgeEnum.YES.getCode().equals(serveES.getDelFlag())){
            DeleteRequest deleteRequest = new DeleteRequest(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
            esBatchSyncTools.addDeleteRequest(deleteRequest);
        }*/
        IndexRequest indexRequest = new IndexRequest(indexName, typeName, deliverES.getDeliverNo());
        indexRequest.source(JSON.toJSONString(deliverES), XContentType.JSON);
        esBatchSyncTools.addIndexRequest(indexRequest);
    }

    @Override
    public boolean switchAliasIndex(String alias, String newIndexVersionName) {
        alias = DeliverUtils.getEnvVariable(alias);
        newIndexVersionName = DeliverUtils.getEnvVariable(newIndexVersionName);
        return elasticsearchTools.switchAliasIndex(alias, newIndexVersionName);
    }

    @Override
    public void sendRequest(Object o) {
        DeliverES deliverES = (DeliverES) o;
        // 此处可加删除逻辑
        /*if(JudgeEnum.NO.getCode().equals(serveES.getDelFlag())){
            elasticsearchTools.saveByEntity(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo(), serveES);
        }else if (JudgeEnum.YES.getCode().equals(serveES.getDelFlag())){
            elasticsearchTools.deleteById(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
        }*/
        elasticsearchTools.saveByJson(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), Constants.ES_DELIVER_TYPE, deliverES.getDeliverNo(), JSONUtil.toJsonStr(deliverES));
    }

    @Override
    public List<String> getIdList(IdListPageQry idListPageQry) {
        DeliverQry qry = new DeliverQry();
        qry.setPage(idListPageQry.getPage());
        qry.setLimit(idListPageQry.getLimit());

        Result<PagePagination<String>> deliverNoListPageResult = deliverAggregateRootApi.getDeliverNoListByPage(qry);
        PagePagination<String> pagePagination = ResultDataUtils.getInstance(deliverNoListPageResult).getDataOrNull();
        if (null == pagePagination) {
            return null;
        }
        return pagePagination.getList();
    }

    // 只给状态为待收车和已收车和已完成的交付单返回排序
    private static Integer getSort(DeliverES deliverES) {
        // 待收车 23
        // 待退保 24
        // 待处理事项 25
        // 已完成 26
        if (!DeliverEnum.IS_RECOVER.getCode().equals(deliverES.getDeliverStatus()) && !DeliverEnum.RECOVER.getCode().equals(deliverES.getDeliverStatus()) && !DeliverEnum.COMPLETED.getCode().equals(deliverES.getDeliverStatus())) {
            return 0;
        }

        if (DeliverEnum.IS_RECOVER.getCode().equals(deliverES.getDeliverStatus())) {
            return DeliverSortEnum.TWENTY_THREE.getSort();
        } else if (DeliverEnum.RECOVER.getCode().equals(deliverES.getDeliverStatus())) {
            if (JudgeEnum.NO.getCode().equals(deliverES.getIsInsurance()) && JudgeEnum.NO.getCode().equals(deliverES.getIsDeduction())) {
                return DeliverSortEnum.TWENTY_FOUR.getSort();
            } else if (JudgeEnum.YES.getCode().equals(deliverES.getIsInsurance()) && JudgeEnum.NO.getCode().equals(deliverES.getIsDeduction())) {
                return DeliverSortEnum.TWENTY_FIVE.getSort();
            } else if (JudgeEnum.YES.getCode().equals(deliverES.getIsInsurance()) && JudgeEnum.YES.getCode().equals(deliverES.getIsDeduction())) {
                return DeliverSortEnum.TWENTY_SIX.getSort();
            } else {
                return 0;
            }
        } else if (DeliverEnum.COMPLETED.getCode().equals(deliverES.getDeliverStatus())) {
            return DeliverSortEnum.TWENTY_SIX.getSort();
        } else {
            return 0;
        }
    }



}
