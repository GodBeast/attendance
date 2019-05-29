package com.xkp.attendance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-19 15:40
 **/
public class CommonUtils {

    public static String getClockInType(String original){
        return original.substring(3, 5);
    }

    public static String getClockInTime(String original){
        return original.substring(6, 16);
    }

    public static String getEmpNo(String original){
       return original.substring(21);
    }

    public static Date paseDate(String clockInTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        try {
            return sdf.parse(clockInTime);
        }catch (ParseException e){
            return null;
        }

    }

//    public static void main(String[] args) throws ParseException {
//        String a = "ANOB2118061712345678901112";
//        System.out.println(CommonUtils.getClockInTime(a));
//        System.out.println(CommonUtils.paseDate("1806171234"));
//        System.out.println(new java.sql.Timestamp(CommonUtils.paseDate("1806171234").getTime()));
//    }
}
