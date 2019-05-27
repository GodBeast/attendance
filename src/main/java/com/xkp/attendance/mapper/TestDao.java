package com.xkp.attendance.mapper;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-18 23:23
 **/
public class TestDao {

    public static void main(String[] args) {
        String a = "ANOB2118061712345678901112";
        String type = a.substring(3, 5);
        String dateTime = a.substring(6, 16);
        String num = a.substring(21);
        System.out.println(type);
        System.out.println(dateTime);
        System.out.println(num);
    }
}
