package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecRecoverContractVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.GroupPhotoVO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class ContractRecoverQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private RedisTools redisTools;

    public ElecRecoverContractVO execute(ContractQry qry, TokenInfo tokenInfo) {
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(qry.getContractId());
        if (!ResultErrorEnum.SUCCESSED.getCode().equals(contractDTOResult.getCode())) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
        }
        if (null == contractDTOResult.getData()) {
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), ResultErrorEnum.NOT_FOUND.getName());
        }
        ElecContractDTO contractDTO = contractDTOResult.getData();

        ElecRecoverContractVO contractVO = new ElecRecoverContractVO();
        contractVO.setElecContractId(contractDTO.getContractId());
        contractVO.setElecContractNo(contractDTO.getContractForeignNo());
        contractVO.setElecContractStatus(contractDTO.getStatus());
        contractVO.setElecContractFailureReason(contractDTO.getFailureReason());

        RecoverInfo recoverInfo = new RecoverInfo();
        BeanUtils.copyProperties(contractDTO, recoverInfo);
        recoverInfo.setDamageFee(contractDTO.getRecoverDamageFee());
        recoverInfo.setParkFee(contractDTO.getRecoverParkFee());

        Result<WarehouseDto> warehouseDtoResult = warehouseAggregateRootApi.getWarehouseById(contractDTO.getRecoverWareHouseId());
        WarehouseDto warehouseDto = warehouseDtoResult.getData();
        recoverInfo.setWareHouseDisplay(warehouseDto == null ? "" : warehouseDto.getName());
        contractVO.setRecoverInfo(recoverInfo);
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        contractVO.setGroupPhotoVOS(groupPhotoVOS);

        // 短信是否可发送判断，目前是一天可以发一条
        // 签署中的合同才可发送短信
        // 先判断日期是否是今天
        if(ElecHandoverContractStatus.SIGNING.getCode() == contractDTO.getStatus()){
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
        }

        return contractVO;
    }

}
