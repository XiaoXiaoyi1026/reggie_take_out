package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.Employee;
import com.xiaoxiaoyi.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1. 清除session
        request.getSession().removeAttribute("employee");
        // 2. 返回退出成功
        return R.success("退出成功！");
    }

    /**
     * 添加员工
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        // 设置初始密码123456，但是需要MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获取当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        log.info("新增员工：{}", employee.toString());
        employeeService.save(employee);
        return R.success("新增员工成功！");
    }

    /**
     * 员工列表分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize, @RequestParam(value = "name", required = false) String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        // 1. 构造分页对象
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 2. 构造条件对象
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 当name不为null时添加like条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件，按照updateTime降序排列
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 3. 执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
}
