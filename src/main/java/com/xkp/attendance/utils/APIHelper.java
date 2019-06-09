package com.xkp.attendance.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by weinee on 16/3/7.
 */
public class APIHelper {

    private static final String API_KEY = "db6dd4d854a912463898aad522032288";

    /**
     * 判断某天是否为节假日的API
     */
    private static final String GETHOLIDA_API = "http://apis.baidu.com/xiaogg/holiday/holiday";

    /**
     * 进行API请求的最大次数
     */
    private static final int COUNT = 5;

    /**
     * 判断所给日期为什么类型,
     *
     * @param date 日期
     * @return 0 工作日, 1 休息日, 2 节假日, -1 为判断出错
     */
    public static int holidayType(Date date) {
        String dateStr = "";
        Integer result = -1;
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            dateStr = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String httpArg = "d=" + dateStr;
        String jsonResult = request(GETHOLIDA_API, httpArg);
        if (StringUtils.isNumeric(jsonResult)) {
            try {
                result = Integer.parseInt(jsonResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = isWeeked(date);
        }
        return result;
    }



    /**
     * @param httpUrl :请求接口
     * @param httpArg :参数
     * @return 返回结果
     */
    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey", API_KEY);
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 计算是否为周末 周日或周六
     *
     * @param date 传入的时间
     * @return -1有错误; 0不是周末; 1是周末
     */
    public static int isWeeked(Date date) {
        int result = 0;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int week_index = cal.get(Calendar.DAY_OF_WEEK);
            if (week_index == 1 || week_index == 7) {
                result = 1;
            }
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }
        return result;
    }



    /**
     * 获取当月所有节假日
     *
     * @return
     */
    public static String getHolidayOfMonth() {
        String dateStr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, 1);
        int days = calendar.getActualMaximum(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        if (month < 12) {
            month += 1;
        }
        if (month == 12) {
            month = 1;
            year += 1;
        }
        for (int i = 1; i <= days; i++) {
            String day = i + "";
            if (i < 10) {
                day = "0" + i;
            }
            String mon = month + "";
            if (month < 10) {
                mon = "0" + month;
            }
            dateStr += year + mon + day;
            if (i < days) {
                dateStr += ",";
            }
        }
        String httpArg = "d=" + dateStr;
        String jsonResult = request(GETHOLIDA_API, httpArg);
        return jsonResult;
    }
}