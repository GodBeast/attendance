package com.xkp.attendance;

import com.xkp.attendance.entity.Original;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AttendanceApplicationTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void contextLoads() {
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
        list.forEach(x -> System.out.println(x.getOriginal()));
    }

}
