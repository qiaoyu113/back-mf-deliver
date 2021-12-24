package com.mfexpress.rent.deliver.utils;

import cn.hutool.core.date.DateUtil;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;

public class FormatUtil {

    // SimpleDateFormat 在被多个线程同时使用时存在安全性问题，因此全局的 SimpleDateFormat 不可取
    public static final String ymdHmsFormatChar = "yyyy-MM-dd HH:mm:ss";
    public static final String ymdFormatChar = "yyyy-MM-dd";

    // 日期取年月日返回为字符串
    public static String ymdFormatDateToString(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat ymdFormat = new SimpleDateFormat(ymdFormatChar);
        return ymdFormat.format(date);
    }

    // 日期取年月日时分秒返回为字符串
    public static String ymdHmsFormatDateToString(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat ymdHmsFormat = new SimpleDateFormat(ymdHmsFormatChar);
        return ymdHmsFormat.format(date);
    }

    public static Date ymdHmsFormatStringToDate(String date) {
        if (StringUtils.isEmpty(date))
            return null;
        SimpleDateFormat ymdHmsFormat = new SimpleDateFormat(ymdHmsFormatChar);
        try {
            return ymdHmsFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
