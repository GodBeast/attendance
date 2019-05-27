package com.xkp.attendance.manager;

import com.xkp.attendance.entity.Employee;
import com.xkp.attendance.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: attendance
 * @description:
 * @author: xkp
 * @create: 2019-05-23 18:20
 **/
@Component
@Slf4j
public class EmployeeManager {
    @Autowired
    private EmployeeMapper employeeMapper;

    public void updateType(){
        List<Employee> employees = employeeMapper.selectAll();

        employees.parallelStream().forEach(x -> {
            String code = String.valueOf(x.getUserCode());
            if(code.startsWith("1")){
                x.setType(2);
            }else if (code.startsWith("3")){
                x.setType(3);
            }else {
                x.setType(0);
            }
            int update = employeeMapper.updateByPrimaryKeySelective(x);
            if(update <= 0){
                log.info(x.getUserCode()+":"+x.getUsername()+"  update type error!");
            }
        });
    }
}
