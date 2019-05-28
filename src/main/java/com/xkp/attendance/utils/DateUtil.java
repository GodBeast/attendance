package com.xkp.attendance.utils;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 有关日期处理的工具类。
 */
public abstract class DateUtil {

    public static final String DEFAULT_PATTERN = "yyyyMMdd";

    public static final String FULL_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期类型的默认值
     */
    public static final Date DEFAULT = parseDate("1970-01-01 00:00:00");

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static Date getCurrentDatetime() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 返回去除时分秒的日期对象
     */
    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * 返回当前年份
     */

    public static String getYear() {
        return formatDate("yyyy");
    }

    /**
     * 获取月份
     * @return
     */
    public static String getMonth(Date date) {
        return formatDate(date, "MM");
    }

    /**
     * 返回 yyyy-MM-dd HH:mm:ss 格式的当前日期
     *
     * @return [yyyy-MM-dd HH:mm:ss]
     */
    public static String getFullPatternNow() {
        return formatDate(FULL_FORMAT_PATTERN);
    }

    /**
     * 格式化日期 yyyy-MM-dd HH:mm:ss
     *
     * @param date 日期 @see Date
     * @return [yyyy-MM-dd HH:mm:ss]
     */
    public static String getFullPatternDate(Date date) {
        return formatDate(date, FULL_FORMAT_PATTERN);
    }

    /**
     * 将日期转换为 <code>yyyyMMdd</code> 的字符串格式
     *
     * @param date 日期 @see Date
     * @return 格式化后的日期字符串
     */
    public static String formatDate(final Date date) {
        return formatDate(date, DEFAULT_PATTERN);
    }

    /**
     * 将日期转换为指定的字符串格式
     *
     * @param date   日期 @see Date
     * @param format 日期格式
     * @return 格式化后的日期字符串，如果<code>date</code>为<code>null</code>或者 <code>format</code>为空，则返回<code>null</code>。
     */
    public static String formatDate(final Date date, String format) {
        if (null == date || StringUtils.isEmpty(format)) {
            return null;
        }

        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 将当前日期转换为指定的字符串格式
     *
     * @param format 日期格式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(String format) {
        return formatDate(new Date(), format);
    }

    /**
     * 将<code>yyyyMMdd<code>格式的字符串转变为日期对象
     *
     * @param sDate 日期字符串
     * @return 日期
     */
    public static Date parseDate(String sDate) {
        return parseDate(sDate, DEFAULT_PATTERN, null);
    }

    /**
     * 将字符串转换撑日期对象
     *
     * @param sDate  日期字符串
     * @param format 日期格式 @see DateFormat
     * @return 日期对象 @see Date
     */
    public static Date parseDate(String sDate, String format) {
        return parseDate(sDate, format, null);
    }

    /**
     * 将字符串转换成日期对象
     *
     * @param sDate        日期字符串
     * @param format       日期格式 @see DateFormat
     * @param defaultValue 默认值
     * @return 日期对象，如果格式化失败则返回默认值<code>defaultValue</code>
     */
    public static Date parseDate(String sDate, String format, Date defaultValue) {
        if (StringUtils.isEmpty(sDate) || StringUtils.isEmpty(format)) {
            return defaultValue;
        }

        DateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(sDate);
        } catch (ParseException e) {
            return defaultValue;
        }

    }

    /**
     * 给指定日期增加月份数
     *
     * @param date   指定日期 @see Date
     * @param months 增加的月份数
     * @return 增加月份后的日期
     */
    public static Date addMonths(Date date, int months) {
        if (months == 0) {
            return date;
        }

        if (date == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    /**
     * 给指定日期增加天数
     *
     * @param date 指定日期 @see Date
     * @param days 增加的天数
     * @return 增加天数后的日期
     */
    public static Date addDays(final Date date, int days) {

        return add(date, Calendar.DAY_OF_MONTH, days);
    }

    /**
     * 给指定日期增加分钟
     *
     * @param date 指定日期 @see Date
     * @param mins 增加的分钟
     * @return 增加分钟后的日期
     */
    public static Date addMins(final Date date, int mins) {
        return add(date, Calendar.MINUTE, mins);
    }

    /**
     * @param type {Calendar.MINUTE, Calendar.DAY_OF_MONTH}
     */
    public static Date add(final Date date, int type, int value) {
        if (value == 0) {
            return date;
        }

        if (date == null) {
            return null;
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(type, value);

        return cal.getTime();
    }


    public static boolean between(Date value, Date min, Date max) {
        if (value != null && (min != null || max != null)) {
            return NumberUtil.between(value.getTime(), min == null ? 0 : min.getTime(), max == null ? Long.MAX_VALUE : max.getTime());
        }
        return false;
    }

    /**
     * 生成int类型日期
     */
    public static int toInt(Date date) {
        if (date != null) {
            return Integer.parseInt(formatDate(date, DEFAULT_PATTERN));
        }
        return 0;
    }

    /**
     * 生成java.util.Date类型的对象
     */
    public static Date getDate(int year, int month, int day) {
        GregorianCalendar d = new GregorianCalendar(year, month - 1, day);
        return d.getTime();
    }

    /**
     * 获取时间间隔 秒
     */
    public static int minusSeconds(Date startTime, Date endTime) {
        long value = endTime.getTime() - startTime.getTime();
        return (int) (value / 1000);
    }

    /**
     * 获取当天的结束时间
     * yyyy-MM-dd 23:59:59
     */
    public static Date getCurrentDateEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取当天剩余秒数
     */
    public static int getDailyRemainingSeconds() {
        return minusSeconds(new Date(), getCurrentDateEnd());
    }


    /**
     * 日期比较 thisDate 是否小于 anotherDate
     */
    public static boolean lessThan(Date thisDate, Date anotherDate) {
        if (thisDate == null || anotherDate == null) return false;
        return thisDate.before(anotherDate);
    }

    /**
     * 日期比较 thisDate 是否大于 anotherDate
     */
    public static boolean greaterThan(Date thisDate, Date anotherDate) {
        if (thisDate == null || anotherDate == null) return false;
        return thisDate.after(anotherDate);
    }


    /**
     * 两个时间段是否有交集
     *
     * @return true 有交集
     */
    public static boolean isOverlap(Date leftStartDate, Date leftEndDate, Date rightStartDate, Date rightEndDate) {
        return !lessThan(leftEndDate, rightStartDate) && !lessThan(rightEndDate, leftStartDate);
    }

    /**
     * 默认时间转为空
     */
    public static Date defaultDateToNull(Date date) {
        if (DEFAULT.equals(date)) {
            return null;
        }
        return date;
    }

    /**
     * 获取指定日期 0点时间
     * @param date
     * @return
     */
    public static Date getDateStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定时间的结束时间
     * yyyy-MM-dd 23:59:59
     */
    public static Date getDateEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取当前时间N天前的时间（0点时间）
     * @param i
     * @return
     */
    public static Date getDateDayBefore(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -i);
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(yy, mm, dd).getTime();
    }

    /**
     *
     * @param dateTime
     * @return
     */
    public static Date parseDate(Long dateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(dateTime);
            return format.parse(time);
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    /**
     * 获取两个日期之间的每一天
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    public static List<Date> dateSplit(Date startDate, Date endDate) {
        if (!startDate.before(endDate)){
            return Collections.emptyList();
        }

        Long spi = endDate.getTime() - startDate.getTime();
        Long step = spi / (24 * 60 * 60 * 1000);// 相隔天数

        List<Date> dateList = new ArrayList<Date>();
        dateList.add(startDate);
        for (int i = 1; i <= step; i++) {
            dateList.add(new Date(dateList.get(i-1).getTime()
                    + (24 * 60 * 60 * 1000)));// 比上一天+1
        }
        return dateList;
    }



    public static void main(String args[]){
        //System.out.println(DateUtil.addDays(new Date(), 1));
        DateUtil.dateSplit(DateUtil.parseDate("2019-4-26 00:00:00", FULL_FORMAT_PATTERN), DateUtil.parseDate("2019-05-25 00:00:00", FULL_FORMAT_PATTERN)).forEach(x -> System.out.println(DateUtil.formatDate(x)));
    }
}
