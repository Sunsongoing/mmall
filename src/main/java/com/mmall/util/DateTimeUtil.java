package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author Sunsongoing
 * <p>
 * 依赖joda-time,commons
 */

public class DateTimeUtil {
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 字符串转日期
     *
     * @param dateTimeStr
     * @param formatStr
     * @return
     */
    public static Date str2Date(String dateTimeStr, String formatStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 日期转字符串
     *
     * @param date
     * @param formatStr
     * @return
     */
    public static String date2Str(Date date, String formatStr) {
        if (null == date) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime();
        return dateTime.toString(formatStr);
    }


    public static Date str2Date(String str) {
        return str2Date(str, STANDARD_FORMAT);
    }

    public static String date2Str(Date date) {
        return date2Str(date, STANDARD_FORMAT);
    }
}
