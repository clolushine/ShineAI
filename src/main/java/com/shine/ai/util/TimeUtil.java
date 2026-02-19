/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
