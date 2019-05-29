package com.xkp.attendance.VO;

/**
 * 表头的实体类： 在具体的项目里，可以是你从数据库里查询出来的数据
 */
public class TitleEntity {
    public  String t_id;
    public  String t_pid;
    public  String t_content;
    public  String t_fielName;
    public TitleEntity(){}
    public TitleEntity(String t_id, String t_pid, String t_content, String t_fielName) {
        this.t_id = t_id;
        this.t_pid = t_pid;
        this.t_content = t_content;
        this.t_fielName = t_fielName;
    }

    public String getT_id() {
        return t_id;
    }

    public void setT_id(String t_id) {
        this.t_id = t_id;
    }

    public String getT_pid() {
        return t_pid;
    }

    public void setT_pid(String t_pid) {
        this.t_pid = t_pid;
    }

    public String getT_content() {
        return t_content;
    }

    public void setT_content(String t_content) {
        this.t_content = t_content;
    }

    public String getT_fielName() {
        return t_fielName;
    }

    public void setT_fielName(String t_fielName) {
        this.t_fielName = t_fielName;
    }
}