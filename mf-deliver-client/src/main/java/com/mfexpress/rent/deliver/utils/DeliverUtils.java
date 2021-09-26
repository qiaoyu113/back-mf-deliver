package com.mfexpress.rent.deliver.utils;

import com.mfexpress.rent.deliver.constant.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeliverUtils {
    private static DeliverUtils utils;

    private static final String YYYYMMDD = "yyyyMMdd";

    @Value("${spring.profiles}")
    private String envVariable;

    private Map<String, String> numMap = new HashMap<>();

    @PostConstruct
    public void init() {
        utils = this;
        numMap.put(Constants.REDIS_DELIVER_KEY, "JFD");
        numMap.put(Constants.REDIS_SERVE_KEY, "FWD");
        numMap.put(Constants.REDIS_DELIVER_VEHICLE_KEY, "JCD");
        numMap.put(Constants.REDIS_RECOVER_VEHICLE_KEY, "SCD");
    }

    public static String getNo(String tag, long seq) {
        return utils.numMap.get(tag) + getDateByYYMMDD(new Date()) + String.format("%05d", seq);
    }


    /**
     * 将日期格式解析成yyyy-MM-dd格式的字符串
     *
     * @param date
     * @return
     */
    public static String getDateByYYMMDD(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat dateFormatYYMMDD = new SimpleDateFormat(
                DeliverUtils.YYYYMMDD);
        return dateFormatYYMMDD.format(date);
    }


    public static String getEnvVariable(String tag) {
        if (StringUtils.isEmpty(tag)) {
            return "";
        }
        if (tag.equals(Constants.ES_DELIVER_INDEX)) {
            return utils.envVariable + "-" + tag;
        } else if (!StringUtils.isEmpty(utils.numMap.get(tag))) {
            return utils.envVariable + ":" + tag;
        } else {
            return utils.envVariable + "_" + tag;
        }
    }


}
