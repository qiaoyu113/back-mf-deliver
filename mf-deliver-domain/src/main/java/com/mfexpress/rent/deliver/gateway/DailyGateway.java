package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.Daily;

import java.util.List;

public interface DailyGateway {

    List<Daily> getDailyByServeNoListAndRentDate(List<String>serveNoList,String rentDate);

    void addDailyList(List<Daily>dailyList);

    void deleteDailyByServeNoAndRentDate(String serveNo,String rentDate);
}
