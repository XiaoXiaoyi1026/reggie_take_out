package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Employee;
import com.xiaoxiaoyi.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        // 1. 将请求传过来的password进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据username查询用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3. 如果查询不到则返回没有该用户的错误信息
        if (emp == null) {
            return R.error("账号不存在，登录失败！");
        }

        // 4. 如果查到了则进行密码匹配
        // 5. 密码匹配失败则返回密码错误信息
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误，登录失败！");
        }

        // 7. 如果该账号状态为0代表已被禁用，返回账号已被禁用
        if (emp.getStatus() == 0) {
            return R.error("账号已被禁用！");
        }

        // 8. 账号状态为1这登陆成功，将员工id存入session并返回登陆成功信息
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }
}
