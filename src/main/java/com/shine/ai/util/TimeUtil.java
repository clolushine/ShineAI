package com.shine.ai.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static String timeFormat(long timestamp,String format) {
        long ts = 0;
        String fmt = "";
        if (timestamp == 0) {
            ts = System.currentTimeMillis();
        }else {
            ts = timestamp;
        }
        if (format == null) {
            fmt = "yyyy-MM-dd HH:mm:ss";
        }
        Date date = new Date(ts);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(date);
    }
}
