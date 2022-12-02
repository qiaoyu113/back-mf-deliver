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
    public static final String VEHICLE_BUSINESS_MODE = "vehicle_business_mode";
    public static final String TRAFFIC_PECCANCY_DEALING_METHOD = "traffic_peccancy_dealing_method";
    public static final String REASONS_FOR_NOT_INSURANCE_RETURN = "reasons_for_not_insurance_return";

    public static final String ES_SERVE_INDEX = "serve";
    public static final String ES_SERVE_TYPE = "serve";
    public static final String ES_DELIVER_INDEX = "deliver";
    public static final String ES_DELIVER_TYPE = "deliver";
    public static final String REDIS_DELIVER_KEY = "mf:deliver";
    public static final String REDIS_DELIVER_CONTRACT_KEY = "mf:deliver:contract";
    public static final String REDIS_SERVE_KEY = "mf:serve";
    public static final String REDIS_DELIVER_VEHICLE_KEY = "mf:deliver:vehicle";
    public static final String REDIS_RECOVER_VEHICLE_KEY = "mf:recover:vehicle";
    public static final String DELIVER_ORDER_TOPIC = "dev2m1_event";
    public static final String DELIVER_ORDER_TAG = "order_payment_finish";
    public static final String DELIVER_VEHICLE_TAG = "vehicle_update";

    public static final String THIRD_PARTY_ELEC_CONTRACT_STATUS_TAG = "deliver_tag";

    public static final String RECOVER_VEHICLE_CHECK_INFO_CACHE_KEY = "recoverVehicleCheckInfoCache";

    public static final String ELEC_CONTRACT_LAST_TIME_SEND_SMS_KEY = "elec_contract_last_time_send_sms_key";

    // 电子交接合同每天可发送几次催签短信
    public static final int EVERY_DAY_ENABLE_SEND_SMS_COUNT = 1;

    public static final String VEHICLE_INSURANCE_TAKE_EFFECT_EVENT_TAG = "insurance_effect";

    public static final String PROPERTIES_PREFIX = "project.properties";

}
