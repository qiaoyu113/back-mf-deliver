package com.mfexpress.rent.deliver.daily;

import com.mfexpress.rent.deliver.daily.repository.DailyMapper;
import com.mfexpress.rent.deliver.entity.Daily;
import com.mfexpress.rent.deliver.gateway.DailyGateway;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DailyGatewayImpl implements DailyGateway {

    @Resource
    private DailyMapper dailyMapper;

    @Override
    public List<Daily> getDailyByServeNoListAndRentDate(List<String> serveNoList, String rentDate) {
        Example example = new Example(Daily.class);
        example.createCriteria().andIn("serveNo", serveNoList)
                .andEqualTo("rentDate", rentDate)
                .andEqualTo("delFlag", 0);
        return dailyMapper.selectByExample(example);
    }

    @Override
    public void addDailyList(List<Daily> dailyList) {
        dailyMapper.insertList(dailyList);
    }

    @Override
    public void deleteDailyByServeNoAndRentDate(String serveNo, String rentDate) {
        Example example = new Example(Daily.class);
        example.createCriteria().andEqualTo("serveNo", serveNo)
                .andGreaterThan("rentDate", rentDate)
                .andEqualTo("delFlag", 0);
        Daily daily = new Daily();
        daily.setDelFlag(1);
        dailyMapper.updateByExample(daily, example);
    }

    @Override
    public void updateDailyRepairFlagByServeNoAndGteRentDate(String serveNo, String rentDate, Integer repairFlag) {
        Example example = new Example(Daily.class);
        example.createCriteria().andEqualTo("serveNo").andGreaterThanOrEqualTo("rentDate", rentDate);
        Daily daily = new Daily();
        daily.setReplaceFlag(repairFlag);
        dailyMapper.updateByExample(daily, example);
    }
}
