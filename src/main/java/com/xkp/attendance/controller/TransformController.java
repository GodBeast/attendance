package com.xkp.attendance.controller;

import com.xkp.attendance.manager.ClockInManager;
import com.xkp.attendance.manager.EmployeeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: attendance1
 * @description:
 * @author: xkp
 * @create: 2019-05-30 20:05
 **/
@RestController
public class TransformController {

    @Autowired
    private ClockInManager clockInManager;
    @Autowired
    private EmployeeManager employeeManager;

    @GetMapping("/transfer")
    public String transfer(){
        clockInManager.transfer();
        return "解析完成";
    }

    @GetMapping("/update-employee-type")
    public String updateEmployeeType(){
        employeeManager.updateType();
        return "解析完成";
    }
}
