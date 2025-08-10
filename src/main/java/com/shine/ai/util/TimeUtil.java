/*
 * ShineAI - An IntelliJ IDEA plugin for AI services.
 * Copyright (C) 2025 Shine Zhong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (usually in the LICENSE file).
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
