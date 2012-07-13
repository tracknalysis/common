/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.common.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author David Valeri
 */
public final class TimeUtil {

    public static final long MS_IN_SECOND = 1000;
    public static final long MS_IN_MINUTE = MS_IN_SECOND * 60;
    public static final long MS_IN_HOUR = MS_IN_MINUTE * 60;
    public static final long MS_IN_DAY = MS_IN_HOUR * 24;
    
    private TimeUtil() {
        // Hidden in utility class.
    }
    
    public static long getCurrentMillisecondInDay() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return getMillisecondInDay(cal);
    }
    
    public static long getMillisecondInDay(GregorianCalendar cal) {
        int hour = cal.get(GregorianCalendar.HOUR);
        if (cal.get(GregorianCalendar.AM_PM) == GregorianCalendar.PM) {
            hour += 12; 
        }
        int minute = cal.get(GregorianCalendar.MINUTE);
        int second = cal.get(GregorianCalendar.SECOND);
        
        long timeInDay = (hour * MS_IN_HOUR) + (minute * MS_IN_MINUTE)
                + (second * MS_IN_SECOND) + cal.get(GregorianCalendar.MILLISECOND);
        
        return timeInDay;
    }
    
    public static long getTimeBeforeTodayInMilliseconds() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        
        long timeInDay = getMillisecondInDay(cal);
        
        return cal.getTimeInMillis() - timeInDay;
    }
    
    public static String formatDuration(long duration, boolean useHours, boolean includeFractionalSeconds) {
        
        long absDuration = Math.abs(duration);
        
        long hours = absDuration / TimeUtil.MS_IN_HOUR;
        long minutes = (absDuration % TimeUtil.MS_IN_HOUR) / TimeUtil.MS_IN_MINUTE;
        long seconds = (absDuration % TimeUtil.MS_IN_HOUR % TimeUtil.MS_IN_MINUTE) / TimeUtil.MS_IN_SECOND;
        long ms = (absDuration % TimeUtil.MS_IN_HOUR % TimeUtil.MS_IN_MINUTE % TimeUtil.MS_IN_SECOND);
        
        StringBuilder builder = new StringBuilder();
        
        if (duration < 0) {
            builder.append("-");
        }
        
        if (hours != 0 && useHours) {
            builder.append(hours).append(":");
        } else {
            minutes += hours * 60;
        }
        
        String minuteFormat;
        if (hours != 0) {
            minuteFormat = "%02d";
        } else {
            minuteFormat = "%d";
        }
        
        builder.append(String.format(minuteFormat, minutes)).append(":")
                .append(String.format("%02d", seconds));
        
        if (includeFractionalSeconds) {
            builder.append(".").append(String.format("%03d", ms));
        }
        
        return builder.toString();
    }
}
