package com.mfexpress.rent.deliver.consumer.sync;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.builder.ESMappingBuilder;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogRelation;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogTableFullName;
import com.mfexpress.component.starter.tools.es.ESBatchSyncTools;
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.component.starter.tools.es.IdListPageQry;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.data.ProductDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("serveSyncServiceImpl")
@Slf4j
@MFMqBinlogRelation
public class ServeSyncServiceImpl implements EsSyncHandlerI {

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
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;
    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    private ESBatchSyncTools esBatchSyncTools;

    private int syncThreadNum = 5;

    private int limit = 100;

    @Override
    public boolean execAll(String indexVersionName) {
        // 创建索引
        indexVersionName = createIndex(indexVersionName);

        // 获取数据页数
        ListQry listQry = new ListQry();
        listQry.setPage(1);
        listQry.setLimit(limit);
        Result<PagePagination<String>> serveNoListPageResult = serveAggregateRootApi.getServeNoListByPage(listQry);
        PagePagination<String> pagePagination = ResultDataUtils.getInstance(serveNoListPageResult).getDataOrException();
        int totalPages = pagePagination.getPagination().getTotalPages();

        // 执行批量同步
        esBatchSyncTools = new ESBatchSyncTools(elasticsearchTools, this, syncThreadNum, totalPages, limit, indexVersionName, Constants.ES_SERVE_TYPE);
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

        // 创建index并设置mapping和setting
        Map<String, String> indexMappingMap = null;
        try {
            indexMappingMap = ESMappingBuilder.getInstance().buildMappingAsString(ServeES.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建index失败，解析es对象出错");
        }
        String mapping = indexMappingMap.get(Constants.ES_SERVE_TYPE);
        // mapping = "{\"properties\":{\"brandId\":{\"type\":\"long\"},\"brandModelDisplay\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"carId\":{\"type\":\"long\"},\"carModelId\":{\"type\":\"long\"},\"carModelVOList\":{\"properties\":{\"brandId\":{\"type\":\"long\"},\"brandModelDisplay\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"carModelId\":{\"type\":\"long\"},\"num\":{\"type\":\"long\"}}},\"carNum\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"carServiceId\":{\"type\":\"long\"},\"cityId\":{\"type\":\"long\"},\"contractNo\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"customerId\":{\"type\":\"long\"},\"customerName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"customerPhone\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"deliverContractStatus\":{\"type\":\"long\"},\"deliverNo\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"deliverStatus\":{\"type\":\"long\"},\"deliverVehicleTime\":{\"type\":\"long\"},\"deposit\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"expectRecoverDate\":{\"type\":\"long\"},\"expectRecoverTime\":{\"type\":\"long\"},\"extractVehicleTime\":{\"type\":\"long\"},\"frameNum\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"goodsId\":{\"type\":\"long\"},\"isCheck\":{\"type\":\"long\"},\"isDeduction\":{\"type\":\"long\"},\"isInsurance\":{\"type\":\"long\"},\"isPreselected\":{\"type\":\"long\"},\"leaseEndDate\":{\"type\":\"long\"},\"leaseModelDisplay\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"leaseModelId\":{\"type\":\"long\"},\"mileage\":{\"type\":\"float\"},\"orderId\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"orgId\":{\"type\":\"long\"},\"recoverAbnormalFlag\":{\"type\":\"long\"},\"recoverContractStatus\":{\"type\":\"long\"},\"recoverVehicleTime\":{\"type\":\"long\"},\"renewalType\":{\"type\":\"long\"},\"rent\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"replaceFlag\":{\"type\":\"long\"},\"saleId\":{\"type\":\"long\"},\"serveNo\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"serveStatus\":{\"type\":\"long\"},\"sort\":{\"type\":\"long\"},\"updateTime\":{\"type\":\"long\"},\"vehicleAge\":{\"type\":\"float\"}}}";
        Settings.Builder setting = Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 1);
        boolean result = elasticsearchTools.createIndexWithMappingAndSetting(indexVersionName, Constants.ES_SERVE_TYPE, mapping, setting);
        if (!result) {
            log.error("创建别名失败");
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "创建index失败，详情请查看日志");
        }

        return indexVersionName;
    }

    @Override
    public List<String> getIdList(IdListPageQry idListPageQry) {
        ListQry listQry = new ListQry();
        listQry.setLimit(idListPageQry.getLimit());
        listQry.setPage(idListPageQry.getPage());

        Result<PagePagination<String>> serveNoListPageResult = serveAggregateRootApi.getServeNoListByPage(listQry);
        PagePagination<String> pagePagination = ResultDataUtils.getInstance(serveNoListPageResult).getDataOrNull();
        if (null == pagePagination) {
            return null;
        }
        return pagePagination.getList();
    }

    @Override
    /**
     * @description: 别名切换
     * @param alias 别名
     * @param newIndexVersionName 实际的indexName
     */
    public boolean switchAliasIndex(String alias, String newIndexVersionName) {
        return elasticsearchTools.switchAliasIndex(DeliverUtils.getEnvVariable(alias), DeliverUtils.getEnvVariable(newIndexVersionName));
    }

    @Override
    @MFMqBinlogTableFullName({"mf-deliver.deliver", "mf-deliver.serve", "mf-deliver.deliver_vehicle", "mf-deliver.recover_vehicle"})
    public boolean execOne(Map<String, String> data) {
        ServeES serveES = (ServeES) assembleEsData(data);
        if (null != serveES) {
            sendRequest(serveES);
        }
        return true;
    }

    @Override
    public void sendRequest(Object object) {
        ServeES serveES = (ServeES) object;
        // 此处可加删除逻辑
        /*if(JudgeEnum.NO.getCode().equals(serveES.getDelFlag())){
            elasticsearchTools.saveByEntity(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo(), serveES);
        }else if (JudgeEnum.YES.getCode().equals(serveES.getDelFlag())){
            elasticsearchTools.deleteById(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
        }*/
        elasticsearchTools.saveByJson(DeliverUtils.getEnvVariable(Constants.ES_SERVE_INDEX), Constants.ES_SERVE_INDEX, serveES.getServeNo(), JSONUtil.toJsonStr(serveES));
    }

    @Override
    public List<Object> assembleEsDataList(List<String> idList) {
        Map<String, String> map = new HashMap<>();
        return idList.stream().map(id -> {
            map.put("serve_no", id);
            return assembleEsData(map);
        }).collect(Collectors.toList());
    }

    @Override
    public Object assembleEsData(Map<String, String> map) {
        ServeES serveEs = new ServeES();
        String serveNo = map.get("serve_no");
        if (StringUtils.isEmpty(serveNo)) {
            return false;
        }
        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if (serveResult.getData() == null) {
            return false;
        }
        ServeDTO serveDTO = serveResult.getData();
        BeanUtils.copyProperties(serveDTO, serveEs);
        serveEs.setContractNo(serveDTO.getOaContractCode());
        serveEs.setServeStatus(serveDTO.getStatus());
        serveEs.setServeStatusSort(getServeStatusSort(serveEs));
        serveEs.setOrderId(serveDTO.getOrderId().toString());
        serveEs.setRent(serveDTO.getRent().toString());
        serveEs.setDeposit(serveDTO.getDeposit().toString());
        serveEs.setLeaseEndDate(serveDTO.getLeaseEndDate());
        serveEs.setRenewalType(serveDTO.getRenewalType());
        if (!StringUtils.isEmpty(serveDTO.getExpectRecoverDate())) {
            serveEs.setExpectRecoverDate(DateUtil.parseDate(serveDTO.getExpectRecoverDate()));
        }
        //租赁方式
        serveEs.setLeaseModelDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.DELIVER_LEASE_MODE), serveEs.getLeaseModelId().toString()));
        serveEs.setExtractVehicleTime(serveDTO.getLeaseBeginDate());

        //品牌车型描述
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveEs.getCarModelId());
        serveEs.setBrandModelDisplay(carModelResult.getData());
        //调用订单查询订单信息
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(serveEs.getOrderId());
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (orderResult.getCode() == 0 && orderResult.getData() != null) {
            OrderDTO order = orderResult.getData();
            if (StringUtils.isEmpty(serveEs.getContractNo())) {
                serveEs.setContractNo(order.getOaContractCode());
            }
            Result<CustomerVO> customerResult = customerAggregateRootApi.getById(order.getCustomerId());
            if (customerResult.getCode() == 0 && customerResult.getData() != null) {
                serveEs.setCustomerName(customerResult.getData().getName());
                serveEs.setCustomerPhone(customerResult.getData().getPhone());
            }
            serveEs.setExtractVehicleTime(order.getDeliveryDate());
            List<OrderCarModelVO> carModelList = new LinkedList<>();
            List<ProductDTO> productList = order.getProductList();
            if (!CollUtil.isEmpty(productList)) {
                List<Integer> modelsIdList = productList.stream().map(ProductDTO::getModelsId).collect(Collectors.toList());
                Result<Map<Integer, String>> vehicleBrandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeListById(modelsIdList);
                Map<Integer, String> brandTypeMap = vehicleBrandTypeResult.getData();
                for (ProductDTO productDTO : productList) {
                    OrderCarModelVO orderCarModelVO = new OrderCarModelVO();
                    orderCarModelVO.setBrandId(productDTO.getBrandId());
                    orderCarModelVO.setCarModelId(productDTO.getModelsId());
                    orderCarModelVO.setBrandModelDisplay(brandTypeMap.get(productDTO.getModelsId()));
                    orderCarModelVO.setNum(productDTO.getProductNum());
                    carModelList.add(orderCarModelVO);
                }
                serveEs.setCarModelVOList(carModelList);
            }
        }


        //预选状态下存在交付单
        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if (deliverResult.getData() != null) {
            serveEs.setIsPreselected(ServeEnum.PRESELECTED.getCode());
            DeliverDTO deliverDTO = deliverResult.getData();
            serveEs.setDeliverNo(deliverDTO.getDeliverNo());
            serveEs.setDeliverStatus(deliverDTO.getDeliverStatus());
            serveEs.setIsInsurance(deliverDTO.getIsInsurance());
            serveEs.setIsCheck(deliverDTO.getIsCheck());
            serveEs.setIsDeduction(deliverDTO.getIsDeduction());
            serveEs.setCarId(deliverDTO.getCarId());
            serveEs.setCarNum(deliverDTO.getCarNum());
            serveEs.setFrameNum(deliverDTO.getFrameNum());
            serveEs.setMileage(deliverDTO.getMileage());
            serveEs.setVehicleAge(deliverDTO.getVehicleAge());
            serveEs.setCarServiceId(deliverDTO.getCarServiceId());
            serveEs.setUpdateTime(deliverDTO.getUpdateTime());
            serveEs.setDeliverContractStatus(deliverDTO.getDeliverContractStatus());
            serveEs.setRecoverContractStatus(deliverDTO.getRecoverContractStatus());
            serveEs.setRecoverAbnormalFlag(deliverDTO.getRecoverAbnormalFlag());

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
            serveEs.setIsPreselected(JudgeEnum.NO.getCode());
            serveEs.setDeliverStatus(ServeEnum.NOT_PRESELECTED.getCode());
            serveEs.setIsCheck(0);
            serveEs.setIsInsurance(0);
            serveEs.setIsDeduction(0);
            serveEs.setSort(DeliverSortEnum.TWO.getSort());
        }
        return serveEs;
    }

    @Override
    public void addRequest(Object obj, String indexName, String typeName) {
        ServeES serveES = (ServeES) obj;
        /*if(JudgeEnum.NO.getCode().equals(serveES.getDelFlag())){
            IndexRequest indexRequest = new IndexRequest(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
            indexRequest.source(JSON.toJSONString(serveES), XContentType.JSON);
            esBatchSyncTools.addIndexRequest(indexRequest);
        }else if (JudgeEnum.YES.getCode().equals(serveES.getDelFlag())){
            DeleteRequest deleteRequest = new DeleteRequest(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveES.getServeNo());
            esBatchSyncTools.addDeleteRequest(deleteRequest);
        }*/
        IndexRequest indexRequest = new IndexRequest(indexName, typeName, serveES.getServeNo());
        indexRequest.source(JSON.toJSONString(serveES), XContentType.JSON);
        esBatchSyncTools.addIndexRequest(indexRequest);
    }

    private Integer getServeStatusSort(ServeES serveES) {
        // 待预选＞已预选＞租赁中＞维修中＞已收车＞已完成
        if (ServeEnum.NOT_PRESELECTED.getCode().equals(serveES.getServeStatus())) {
            // 待预选
            return 1;
        } else if (ServeEnum.PRESELECTED.getCode().equals(serveES.getServeStatus())) {
            // 已预选
            return 2;
        } else if (ServeEnum.DELIVER.getCode().equals(serveES.getServeStatus())) {
            // 租赁中
            return 3;
        } else if (ServeEnum.REPAIR.getCode().equals(serveES.getServeStatus())) {
            // 维修中
            return 4;
        } else if (ServeEnum.RECOVER.getCode().equals(serveES.getServeStatus())) {
            // 已收车
            return 5;
        } else if (ServeEnum.COMPLETED.getCode().equals(serveES.getServeStatus())) {
            // 已完成
            return 6;
        } else {
            // 意外情况
            return 0;
        }
    }

    private Integer getSort(ServeES serveEs) {
        int sort = DeliverSortEnum.ZERO.getSort();
        boolean deliverFlag = serveEs.getIsCheck().equals(JudgeEnum.NO.getCode()) || serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode());
        boolean recoverFlag = serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode()) || serveEs.getIsDeduction().equals(JudgeEnum.NO.getCode());
        if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            if (DeliverContractStatusEnum.NOSIGN.getCode() == serveEs.getDeliverContractStatus()) {
                // 待发车
                sort = DeliverSortEnum.ONE.getSort();
            } else {
                // 签署中
                sort = DeliverSortEnum.TWO.getSort();
            }
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && deliverFlag) {
            // 待预选
            sort = DeliverSortEnum.THREE.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && JudgeEnum.NO.getCode().equals(serveEs.getIsCheck())) {
            // 待验车
            sort = DeliverSortEnum.FOUR.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && JudgeEnum.YES.getCode().equals(serveEs.getIsCheck())) {
            // 待投保
            sort = DeliverSortEnum.FIVE.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.DELIVER.getCode()) && serveEs.getDeliverStatus().equals(DeliverEnum.DELIVER.getCode())) {
            // 发车已完成
            sort = DeliverSortEnum.SIX.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.REPAIR.getCode()) && serveEs.getDeliverStatus().equals(DeliverEnum.DELIVER.getCode())) {
            // 发车已完成
            sort = DeliverSortEnum.SEVEN.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.COMPLETED.getCode())) {
            // 服务单已完成
            sort = DeliverSortEnum.SIXTEEN.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && serveEs.getIsCheck().equals(JudgeEnum.NO.getCode())) {
            //收车中 待验车
            sort = DeliverSortEnum.TEN.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.NOSIGN.getCode()) {
            // 收车中 待收车
            sort = DeliverSortEnum.ELEVEN.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && (serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.GENERATING.getCode() || serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.SIGNING.getCode())) {
            // 收车中 签署中
            sort = DeliverSortEnum.TWELVE.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.RECOVER.getCode()) && DeliverEnum.RECOVER.getCode().equals(serveEs.getDeliverStatus()) && JudgeEnum.NO.getCode().equals(serveEs.getIsInsurance())) {
            // 已收车  待退保
            sort = DeliverSortEnum.THIRTEEN.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.RECOVER.getCode()) && DeliverEnum.RECOVER.getCode().equals(serveEs.getDeliverStatus()) && JudgeEnum.YES.getCode().equals(serveEs.getIsInsurance())) {
            // 已收车  待处理违章
            sort = DeliverSortEnum.FOURTEEN.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.RECOVER.getCode()) && DeliverEnum.COMPLETED.getCode().equals(serveEs.getDeliverStatus())) {
            // 已收车  待处理违章
            sort = DeliverSortEnum.FIFTEEN.getSort();
        }

        return sort;

    }

    private Map<String, DictDataDTO> getDictDataDtoMapByDictType(String dictType) {
        DictTypeDTO dictTypeDTO = new DictTypeDTO();
        dictTypeDTO.setDictType(dictType);
        Result<List<DictDataDTO>> dictDataResult = dictAggregateRootApi.getDictDataByType(dictTypeDTO);
        if (dictDataResult.getCode() == 0) {
            List<DictDataDTO> dictDataDTOList = dictDataResult.getData();
            if (dictDataDTOList == null || dictDataDTOList.isEmpty()) {
                return new HashMap<>(16);
            }
            Map<String, DictDataDTO> dictDataDTOMap = dictDataDTOList.stream().collect(Collectors.toMap(DictDataDTO::getDictValue, Function.identity(), (key1, key2) -> key1));
            return dictDataDTOMap;
        }
        return null;
    }

    /**
     * 字典值
     */
    private String getDictDataDtoLabelByValue(Map<String, DictDataDTO> dictDataDtoMap, String value) {
        if (dictDataDtoMap == null) {
            return "";
        }

        DictDataDTO dictDataDTO = dictDataDtoMap.get(value);
        if (dictDataDTO != null) {
            return dictDataDTO.getDictLabel();
        }
        return "";
    }



    /**
     * 根据deliverNo查询客户信息 收车
     * @param deliverNo
     * @return
     */
    public Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(String deliverNo){
        return recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverNo);
    }
}
