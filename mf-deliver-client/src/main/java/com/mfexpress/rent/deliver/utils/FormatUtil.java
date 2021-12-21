package com.mfexpress.rent.deliver.utils;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class FormatUtil {

    private static final SimpleDateFormat ymdHmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat ymdWithoutSplitFormat = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat ymdFormatWithSlash = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat hmsFormat = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat hmsFormatWithoutSplit = new SimpleDateFormat("HHmmss");
    public static final SimpleDateFormat ymdHmsSFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String endTime = "23:59:59.999";

    public static final Pattern yyyyMMddPattern = Pattern.compile("\\d{4}/\\d{2}/\\d{1,2}");
    public static final Pattern HHmmssPattern = Pattern.compile("((0?[0-9])|([1][0-9])|([2][0-4])):\\d{2}:\\d{2}");
    public static final Pattern ymdCharIsNumPattern = Pattern.compile("[0-9]{8}");
    public static final Pattern hmsCharIsNumPattern = Pattern.compile("[0-9]{6}");

    public static String retainTwoDigitPrecision(Double d){
        if(null == d){
            return "";
        }
        return String.format("%.2f", d);
    }

    // 形参1取年月日，形参2取时分秒，拼接为一个新的日期
    public static Date concatYmdAndHms(Date paymentYmd, Date paymentHms){
        if(null == paymentYmd || null == paymentHms){
            return null;
        }
        try {
            return ymdHmsFormat.parse(ymdFormat.format(paymentYmd).concat(" ").concat(hmsFormat.format(paymentHms)));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 日期取年月日返回为字符串
    public static String ymdFormatDateToString(Date date) {
        if (date == null)
            return "";
        return ymdFormat.format(date);
    }

    // 日期取年月日没有分隔符返回为字符串
    public static String ymdWithoutSplitFormatDateToString(Date date){
        if (date == null)
            return "";
        return ymdWithoutSplitFormat.format(date);
    }

    // 日期取年月日时分秒返回为字符串
    public static String ymdHmsFormatDateToString(Date date) {
        if (date == null)
            return "";
        return ymdHmsFormat.format(date);
    }

    public static Date ymdHmsFormatStringToDate(String date) {
        if (StringUtils.isEmpty(date))
            return null;
        try {
            return ymdHmsFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 时间只取年月日然后返回
    public static Date ymdFormatDateToDate(Date date) {
        if (StringUtils.isEmpty(date))
            return null;
        try {
            String ymd = ymdFormat.format(date);
            return ymdFormat.parse(ymd);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 时间取年月日然后拼接23:59:59.999后返回
    public static Date ymdFormatDateToDateConcatEndTime(Date date) {
        if (StringUtils.isEmpty(date))
            return null;
        try {
            String ymd = ymdFormat.format(date);
            return ymdHmsSFormat.parse(ymd.concat(" ").concat(endTime));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
