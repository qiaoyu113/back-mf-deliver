package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecDeliverContractVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.GroupPhotoVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ContractDeliverQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public ElecDeliverContractVO execute(ContractQry qry, TokenInfo tokenInfo) {
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(qry.getContractId());
        if(!ResultErrorEnum.SUCCESSED.getCode().equals(contractDTOResult.getCode())){
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
        }
        if(null == contractDTOResult.getData()){
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), ResultErrorEnum.NOT_FOUND.getName());
        }
        ElecContractDTO contractDTO = contractDTOResult.getData();

        ElecDeliverContractVO contractVO = new ElecDeliverContractVO();
        contractVO.setElecContractId(contractDTO.getContractId().toString());
        contractVO.setElecContractNo(contractDTO.getContractForeignNo());
        contractVO.setElecContractStatus(contractDTO.getStatus());
        contractVO.setElecContractFailureReason(contractDTO.getFailureReason());
        DeliverInfo deliverInfo = new DeliverInfo();
        BeanUtils.copyProperties(contractDTO, deliverInfo);
        contractVO.setDeliverInfo(deliverInfo);
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        contractVO.setGroupPhotoVOS(groupPhotoVOS);
        if(!groupPhotoVOS.isEmpty()){
            // 补充订单id
            Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(groupPhotoVOS.get(0).getServeNo());
            ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
            contractVO.setOrderId(serveDTO.getOrderId().toString());
        }

        // 短信是否可发送判断，目前是一天可以发一条
        // 签署中的合同才可发送短信
        // 先判断日期是否是今天
        /*if(ElecHandoverContractStatus.SIGNING.getCode() == contractDTO.getStatus()){
            String sendSmsDate = contractDTO.getSendSmsDate();
            if (StringUtils.isEmpty(sendSmsDate)) {
                contractVO.setSendSmsFlag(JudgeEnum.YES.getCode());
            } else {
                String nowYmd = FormatUtil.ymdFormatDateToString(new Date());
                if (nowYmd.equals(sendSmsDate)) {
                    // 如果是今天，判断次数达到了限制没有
                    if (contractDTO.getSendSmsCount() < Constants.EVERY_DAY_ENABLE_SEND_SMS_COUNT) {
                        contractVO.setSendSmsFlag(JudgeEnum.YES.getCode());
                    } else {
                        contractVO.setSendSmsFlag(JudgeEnum.NO.getCode());
                    }
                } else {
                    // 如果不是今天，可发送
                    contractVO.setSendSmsFlag(JudgeEnum.YES.getCode());
                }
            }

            // 如果短信可发送，查redis，获取短信发送倒计时
            if (JudgeEnum.YES.getCode().equals(contractVO.getSendSmsFlag())) {
                String key = DeliverUtils.concatCacheKey(Constants.ELEC_CONTRACT_LAST_TIME_SEND_SMS_KEY, contractVO.getElecContractId().toString());
                // 取出来的是毫秒值
                Long lastTime = redisTools.get(key);
                if (null != lastTime) {
                    long now = System.currentTimeMillis();
                    int i = (int) (60 - ((now - lastTime) / 1000));
                    if (i >= 0) {
                        contractVO.setSmsCountDown(i);
                    }
                }
            }
        }*/

        return contractVO;
    }

}
