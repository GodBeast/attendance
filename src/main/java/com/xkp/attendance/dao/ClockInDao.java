package com.xkp.attendance.dao;

import com.xkp.attendance.CommonUtils;
import com.xkp.attendance.entity.ClockIn;
import com.xkp.attendance.entity.Original;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-19 11:26
 **/
@Repository
@Slf4j
public class ClockInDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void analysis(){
        List<Original> originals = this.queryOriginal();
        if(CollectionUtils.isEmpty(originals)){
            log.info("------------没有原始数据");
            return;
        }
        List<ClockIn> clockIns = originals.stream().parallel().map(x -> {
            ClockIn clockIn = new ClockIn();
            String original = x.getOriginal();
            String type = CommonUtils.getClockInType(original);
            if("B1".equals(type)){
                clockIn.setType(1);
            }else if("B2".equals(type)){
                clockIn.setType(2);
            }else {
                clockIn.setType(0);
            }
            clockIn.setEmpNo(CommonUtils.getEmpNo(original));
            clockIn.setClockInDate(CommonUtils.paseDate(CommonUtils.getClockInTime(original)));
            clockIn.setOriginalId(x.getId());
            return clockIn;
        }).collect(Collectors.toList());

        String insertSql = "insert into clockIn (type, empNo, clockInDate,originalId) values(?,?,?,?)";
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, clockIns.get(i).getType());
                ps.setString(2, clockIns.get(i).getEmpNo());
                ps.setTimestamp(3, new java.sql.Timestamp(clockIns.get(i).getClockInDate().getTime()));
                ps.setInt(4, clockIns.get(i).getOriginalId());
            }

            @Override
            public int getBatchSize() {
                return clockIns.size();
            }
        });


    }

    public List<Original> queryOriginal(Date startTime, Date endTime){
        String sql = "select * from original where  createTime >= ? and createTime <= ?";

        List<Original> list = jdbcTemplate.query(sql, new RowMapper<Original>(){
            Original original = null;
            @Override
            public Original mapRow(ResultSet rs, int i) throws SQLException {
                original = new Original();
                original.setId(rs.getInt("id"));
                original.setOriginal(rs.getString("original"));
                return original;
            }
        }, startTime, endTime);
        return list;
    }

    public List<Original> queryOriginal(){
        String sql = "select * from original";

        List<Original> list = jdbcTemplate.query(sql, new RowMapper<Original>(){
            Original original = null;
            @Override
            public Original mapRow(ResultSet rs, int i) throws SQLException {
                original = new Original();
                original.setId(rs.getInt("id"));
                original.setOriginal(rs.getString("original"));
                return original;
            }
        });
        return list;
    }
}
