package com.mfexpress.rent.deliver.constant;

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

    public static TimeRange RECOVER_TIMERANGE;
    public static TimeRange DELIVER_TIMERANGE;

    @Override
    public void afterPropertiesSet() throws Exception {
        RECOVER_TIMERANGE = recoverTimeRange;
        DELIVER_TIMERANGE = deliverTimeRange;
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
