package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;

public interface DailyEntityApi {

    /**
     * 维修修改日报状态
     * @param dailyMaintainDTO 维修通知
     */
    void operateMaintain(DailyMaintainDTO dailyMaintainDTO);
}
