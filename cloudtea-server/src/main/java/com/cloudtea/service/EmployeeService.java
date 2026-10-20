package com.cloudtea.service;

import com.cloudtea.dto.EmployeeDTO;
import com.cloudtea.dto.EmployeeLoginDTO;
import com.cloudtea.dto.EmployeePageQueryDTO;
import com.cloudtea.entity.Employee;
import com.cloudtea.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);
    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);
    /**
     * 启用禁用员工账号
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);
    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);
    /**
     * 更新员工
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
