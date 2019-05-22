package com.xkp.attendance;

import com.xkp.attendance.dao.ClockInDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AttendanceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);


    }
    @Autowired
    ClockInDao clockInDao;
    @Override
    public void run(String... args) throws Exception {
        clockInDao.analysis();
    }
}
