package com.mfexpress.rent.deliver.elecHandoverContract;

import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.utils.util.MyBatisUtils;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.elecHandoverContract.repository.ElecHandoverContractMapper;
import com.mfexpress.rent.deliver.gateway.ElecHandoverContractGateway;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElecHandoverContractGatewayImpl implements ElecHandoverContractGateway {

    @Resource
    private ElecHandoverContractMapper contractMapper;

    @Override
    public int create(ElectronicHandoverContractPO contractPO) {
        contractMapper.insertSelective(contractPO);
        return contractPO.getId();
    }

    @Override
    public ElectronicHandoverContractPO getContractByContractId(long contractId) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("contractId", contractId)
                .build(), ElectronicHandoverContractPO.class);
        return contractMapper.selectOneByExample(example);
    }

    @Override
    public int updateContractByContractId(ElectronicHandoverContractPO contractPO) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("contractId", contractPO.getContractId())
                .build(), ElectronicHandoverContractPO.class);
        return contractMapper.updateByExampleSelective(contractPO, example);
    }

    @Override
    public int updateContractByContractForeignNo(ElectronicHandoverContractPO contractPO) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("contractForeignNo", contractPO.getContractForeignNo())
                .build(), ElectronicHandoverContractPO.class);
        return contractMapper.updateByExampleSelective(contractPO, example);
    }

    @Override
    public ElectronicHandoverContractPO getContractByContract(ElectronicHandoverContractPO contractPO) {
        return contractMapper.selectOne(contractPO);
    }

    @Override
    public PagePagination<ElectronicHandoverContractPO> getPageContractDTOSByQry(ContractListQry qry) {
        if(qry.getPage() == null){
            qry.setPage(1);
        }
        if(qry.getLimit() == null){
            qry.setLimit(5);
        }
        PageHelper.clearPage();
        PageHelper.startPage(qry.getPage(), qry.getLimit());

        Example example = new Example(ElectronicHandoverContractPO.class);
        example.orderBy("updateTime").desc();
        Example.Criteria criteriaA = example.createCriteria();
        criteriaA.andEqualTo("orderId", qry.getOrderId());
        criteriaA.andEqualTo("deliverType", qry.getDeliverType());

        // 迫不得已，手写sql
        Example.Criteria criteriaB = example.createCriteria();
        criteriaB.andCondition("status = ".concat(String.valueOf(ElecHandoverContractStatus.GENERATING.getCode()).concat(" or status = ").concat(String.valueOf(ElecHandoverContractStatus.SIGNING.getCode())))
                .concat(" or (status = ".concat(String.valueOf(ElecHandoverContractStatus.FAIL.getCode()))
                        .concat(" and failure_reason in (").concat(String.valueOf(ContractFailureReasonEnum.CREATE_FAIL.getCode())).concat(",").concat(String.valueOf(ContractFailureReasonEnum.OVERDUE.getCode())).concat(")")
                        .concat(" and is_show = ").concat(String.valueOf(JudgeEnum.YES.getCode()))
                        .concat(")")));

        example.and(criteriaB);

        List<ElectronicHandoverContractPO> contractPOS = contractMapper.selectByExample(example);
        return PagePagination.getInstance(contractPOS);
    }

    @Override
    public List<ElectronicHandoverContractPO> getContractDTOSByDeliverNosAndDeliverType(List<String> deliverNos, int deliverType) {
        // 此处不做状态限制，即使是失效状态，也能查出来
        Example example = new Example(ElectronicHandoverContractPO.class);
        Example.Criteria criteria = example.createCriteria();
        List<String> collect = deliverNos.stream().map(deliverNo -> JSONUtil.toJsonStr(Collections.singletonList(deliverNo))).collect(Collectors.toList());
        criteria.andIn("deliverNos", collect);
        criteria.andEqualTo("deliverType", deliverType);
        List<ElectronicHandoverContractPO> contractPOS = contractMapper.selectByExample(example);
        return contractPOS;
    }

    @Override
    public ElectronicHandoverContractPO getContractByForeignNo(String foreignNo) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("contractForeignNo", foreignNo)
                .build(), ElectronicHandoverContractPO.class);
        return contractMapper.selectOneByExample(example);
    }

    @Override
    public ElectronicHandoverContractPO getContractDTOByDeliverNoAndDeliverType(String deliverNo, Integer deliverType) {
        Example example = new Example(ElectronicHandoverContractPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("deliverNos", "%".concat(deliverNo).concat("%"));
        criteria.andEqualTo("deliverType", deliverType);
        criteria.andNotEqualTo("status", ElecHandoverContractStatus.FAIL.getCode());
        return contractMapper.selectOneByExample(example);
    }

    @Override
    public List<ElecContractDTO> getContractDTOSByQry(ContractListQry qry) {
        Example example = new Example(ElectronicHandoverContractPO.class);
        example.orderBy("updateTime").desc();
        Example.Criteria criteriaA = example.createCriteria();
        criteriaA.andEqualTo("orderId", qry.getOrderId());
        criteriaA.andEqualTo("deliverType", qry.getDeliverType());

        Example.Criteria criteriaB = example.createCriteria();
        criteriaB.andCondition("status = ".concat(String.valueOf(ElecHandoverContractStatus.GENERATING.getCode()).concat(" or status = ").concat(String.valueOf(ElecHandoverContractStatus.SIGNING.getCode())))
                .concat(" or (status = ".concat(String.valueOf(ElecHandoverContractStatus.FAIL.getCode()))
                        .concat(" and failure_reason in (").concat(String.valueOf(ContractFailureReasonEnum.CREATE_FAIL.getCode())).concat(",").concat(String.valueOf(ContractFailureReasonEnum.OVERDUE.getCode())).concat(")")
                        .concat(" and is_show = ").concat(String.valueOf(JudgeEnum.YES.getCode()))
                        .concat(")")));

        example.and(criteriaB);

        List<ElectronicHandoverContractPO> contractPOS = contractMapper.selectByExample(example);
        List<ElecContractDTO> collect = contractPOS.stream().map(contractPO -> {
            ElecContractDTO contractDTO = new ElecContractDTO();
            BeanUtils.copyProperties(contractPO, contractDTO);
            return contractDTO;
        }).collect(Collectors.toList());
        return collect;
    }

}
