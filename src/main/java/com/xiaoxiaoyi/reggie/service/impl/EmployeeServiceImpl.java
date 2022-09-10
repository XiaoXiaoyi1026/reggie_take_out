package com.xiaoxiaoyi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoxiaoyi.reggie.entity.Employee;
import com.xiaoxiaoyi.reggie.mapper.EmployeeMapper;
import com.xiaoxiaoyi.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
