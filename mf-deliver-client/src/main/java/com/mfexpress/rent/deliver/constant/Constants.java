package com.mfexpress.rent.deliver.constant;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String BINLOG_MQ_DELIVER_DATA_BASE_NAME = "mf-deliver";
    public static final String BINLOG_MQ_DELIVER_TABLE = "deliver";
    public static final String BINLOG_MQ_SERVE_TABLE = "serve";
    public static final String BINLOG_MQ_DELIVER_VEHICLE_TABLE = "deliver_vehicle";
    public static final String BINLOG_MQ_RECOVER_VEHICLE_TABLE = "recover_vehicle";

    public static final String ES_DELIVER_INDEX = "deliver";
    public static final String REDIS_DELIVER_KEY = "mf:deliver";
    public static final String REDIS_SERVE_KEY = "mf:serve";
    public static final String REDIS_DELIVER_VEHICLE_KEY = "mf:deliver:vehicle";
    public static final String REDIS_RECOVER_VEHICLE_KEY = "mf:recover:vehicle";
}
