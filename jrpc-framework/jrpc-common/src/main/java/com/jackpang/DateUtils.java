package com.jackpang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description: DateUtils
 * date: 11/5/23 4:09 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class DateUtils {
    public static Date get(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
