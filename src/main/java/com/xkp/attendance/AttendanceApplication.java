package com.xkp.attendance;

import com.xkp.attendance.manager.EmployeeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@ComponentScan("com.xkp.attendance")
@SpringBootApplication(scanBasePackages = {"com.xkp.attendance"})
@MapperScan("com.xkp.attendance.mapper")
public class AttendanceApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }

    @Autowired
    EmployeeManager employeeManager;

    @Override
    public void run(String... args) throws Exception {
        //解析基础数据
        //clockInManager.transfer();
        //解析员工类型
        employeeManager.updateType();

        //statisticsManager.checkClockInOfOneDay();
    }
}
