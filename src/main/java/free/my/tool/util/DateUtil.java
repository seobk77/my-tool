package free.my.tool.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {
    private static String defaultDatetimeFormat = "yyyy-MM-dd' 'HH:mm:ss";
    private static String defaultDateFormat = "yyyy-MM-dd";
    private static String defaultDateHourFormat = "yyyyMMddHH";
    private static String imDateFormat = "yyyyMMddHHmmss";
    private static Pattern number = Pattern.compile("[0-9]+");

    public static String format(Long time) {
        return format(time, defaultDatetimeFormat);
    }

    public static String format(Long time, String strFormat) {
        return new SimpleDateFormat(strFormat).format(time);
    }

    public static Date createDateWithDeltaHour(int startHour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.HOUR, startHour);

        return cal.getTime();
    }

    public static Date createDate(long millisecond) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millisecond);
        return cal.getTime();
    }

    public static Date[] beforeDateRange(String date, int day) {
        Date[] ret = new Date[3];
        Date d = createDate(date);
        ret[0] = d;
        ret[1] = new Date(d.getTime() - 86400000l * (day-1));
        ret[2] = new Date(d.getTime() + 86400000l * 1);

        return ret;
    }

    public static String[] createRange(String date, int day) {
        Date[] range = beforeDateRange(date, day);
        return createRange(range);
    }

    public static String[] createRange(Date[] range) {
        Long[] ret = new Long[range.length];
        int index = 0;
        for(Date r : range) {
            ret[index++] = r.getTime();
        }
        return createRange(ret);
    }

    public static String[] createRange(Long[] range) {
        String[] ret = new String[range.length];
        for(int i=0; i<range.length; i++) {
            ret[i] = Long.toString(range[i]);
        }
        return ret;
    }

    public static String beforeDay(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * i);
        return createDate(cal.getTime());
    }

    public static Date createDate(String date) {
        try {
            return new SimpleDateFormat(defaultDateFormat).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("wrong date format. format:" + defaultDateFormat);
        }
    }

    public static Date createDateHour(String date) {
        try {
            return new SimpleDateFormat(defaultDateHourFormat).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("wrong date-hour format. format:" + defaultDateHourFormat);
        }
    }

    public static String createDate(Date date) {
        return new SimpleDateFormat(defaultDateFormat).format(date);
    }

    public static Date setMillisecond(Date date, int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, i);
        return cal.getTime();
    }

    public static String[] imDateToTimeStamp(String[] candidateValues) {
        String[] retStrings = new String[candidateValues.length];
        try {
            for(int i=0; i<candidateValues.length; i++) {
                String c = candidateValues[i];
                Matcher m = number.matcher(c);
                StringBuffer sb = new StringBuffer();
                while(m.find()) {
                    m.appendReplacement(sb, Long.toString(new SimpleDateFormat(imDateFormat).parse(m.group()).getTime()));
                }
                retStrings[i] = sb.toString();
            }
        } catch (ParseException e) {
            throw new RuntimeException("wrong date-hour format. format:" + imDateFormat);
        }

        return retStrings;
    }
}
