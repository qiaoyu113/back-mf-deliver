package com.mfexpress.rent.deliver.constant;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String BINLOG_MQ_DELIVER_DATA_BASE_NAME = "mf-deliver";
    public static final String BINLOG_MQ_DELIVER_TABLE = "deliver";
    public static final String BINLOG_MQ_SERVE_TABLE = "serve";
    public static final String BINLOG_MQ_DELIVER_VEHICLE_TABLE = "deliver_vehicle";
    public static final String BINLOG_MQ_RECOVER_VEHICLE_TABLE = "recover_vehicle";
    public static final Integer REDIS_BIZ_ID_SERVER = 122;
    public static final Integer REDIS_BIZ_ID_DELIVER = 123;
    public static final String DELIVER_LEASE_MODE = "lease_mode";

    public static final String ES_DELIVER_INDEX = "deliver";
    public static final String REDIS_DELIVER_KEY = "mf:deliver";
    public static final String REDIS_SERVE_KEY = "mf:serve";
    public static final String REDIS_DELIVER_VEHICLE_KEY = "mf:deliver:vehicle";
    public static final String REDIS_RECOVER_VEHICLE_KEY = "mf:recover:vehicle";
    public static final String DELIVER_ORDER_TOPIC = "dev2m1_event";
    public static final String DELIVER_ORDER_TAG = "order_payment_finish";
    public static final String DELIVER_VEHICLE_TAG = "vehicle_update";
}
