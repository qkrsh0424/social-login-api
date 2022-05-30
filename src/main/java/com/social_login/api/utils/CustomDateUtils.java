package com.social_login.api.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class CustomDateUtils {
    private static Integer NINE_HOUR = 9;

    public static LocalDateTime getCurrentDateTime(){
        return LocalDateTime.now();
    }

    public static String getCurrentKRDate2yyyyMMddHHmmss_SSS(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        String result = currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss_SSS"));
        return result;
    }

    public static Date getCurrentDate(){
        Date date = Calendar.getInstance().getTime();
        return date;
    }

    public static Date getCurrentDate2(){
        Date date = Calendar.getInstance().getTime();
        return date;
    }

    // yyyy-MM-dd HH:mm:ss
    public static String getLocalDateTimeToyyyyMMddHHmmss(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // createdAt, salesAt, releaseAt 시간은 +9시간 세팅
    public static String getLocalDateTimeToDownloadFormat(LocalDateTime localDateTime) {
        localDateTime = localDateTime.plusHours(NINE_HOUR);
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
