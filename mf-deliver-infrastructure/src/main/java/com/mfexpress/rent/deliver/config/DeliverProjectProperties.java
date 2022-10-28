package com.mfexpress.rent.deliver.config;

import com.mfexpress.rent.deliver.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yj
 * @date 2022/10/24 8:57
 */
@Data
@Component
@ConfigurationProperties(prefix = Constants.PROPERTIES_PREFIX)
@EnableConfigurationProperties
public class DeliverProjectProperties implements InitializingBean {

    private TimeRange recoverTimeRange;
    private TimeRange deliverTimeRange;

    public static TimeRange RECOVER_TIME_RANGE;
    public static TimeRange DELIVER_TIME_RANGE;

    @Override
    public void afterPropertiesSet() throws Exception {
        RECOVER_TIME_RANGE = recoverTimeRange;
        DELIVER_TIME_RANGE = deliverTimeRange;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeRange {

        /**
         * T-
         */
        private Integer pre;
        /**
         * T+
         */
        private Integer suf;

    }
}
