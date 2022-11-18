package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Transactional
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {
    @Autowired
    public EmployeeService employeeService;
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * ①. 将页面提交的密码password进行md5加密处理, 得到加密后的字符串*/
        String password= employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        /**
         * ②. 根据页面提交的用户名username查询数据库中员工数据信息*/
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        /**
         * ③. 如果没有查询到, 则返回登录失败结果*/
        if(emp==null){
            return Result.error("login failed,user does not exist");
        }
        /**
         * ④. 密码比对，如果不一致, 则返回登录失败结果*/
        if(!emp.getPassword().equals(password)){
            return Result.error("login failed, wrong password");
        }
        /**
         * ⑤. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果*/
        if(emp.getStatus()==0){
            return Result.error("user banned");
        }
        /**
         * ⑥. 登录成功，将员工id存入Session, 并返回登录成功结果
         * */
        request.getSession().setAttribute("employee",emp.getId());
        System.out.println(request.getSession().getAttribute("employee"));
        return Result.success(emp);
    }
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        /**
         * ①. 登出成功，将员工id从Session中移除, 并返回登出成功结果
         * */
        request.getSession().removeAttribute("employee");
        return Result.success("logout success");
    }
    @PostMapping
    public Result<String> save(HttpServletRequest request,@RequestBody Employee emp) {
        System.out.println(emp);
        log.info("新增员工，员工信息：{}",emp.toString());
        /**这里想封装时间戳，但不会看时间*/
        Long userId = (Long) request.getSession().getAttribute("employee");
        System.out.println(userId);
        /**这里错了，用户并没有在这里传入明文密码，业务逻辑没理解清楚*/
        emp.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        emp.setCreateUser(userId);
        emp.setUpdateUser(userId);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        employeeService.save(emp);
        return Result.success("successfully save");
    }
    @GetMapping("/page")
    public Result<Page> search(Integer page, Integer pageSize, String name){
        /**需要一个lqw/qw查询，得到一个符合的list，分页用MP的interceptor类*/
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper();
        lqw.like(null!=name,Employee::getName,name);
        lqw.orderByDesc(Employee::getUpdateTime);
        Page pageInfo=new Page(page,pageSize);
        employeeService.page(pageInfo,lqw);
         /** 通过看index主页，可以找到对应员工的显示页面在page/member/list.html，
          * 找到list.html可以看到Vue部分写了，data(){}包含的return中，写到了page和pageSize有初始值1和10，
          * tableData为空，而tableData为如果成功查询返回的数据：this.tableData = res.data.records
          * 因此res的data前端要用，需要传入返回的结果，因此结果类的T应该是List；失败的话data可以为null*/
         /** 写一个方法，对应路径为/page,简单@GetMapping即可*/
        /** 方法需能接收：page；pageSize；name 3个参数*/
        /** 调用对应service的方法进行模糊+分页查询，*/
        /** 对于查到的集合，在controller中返回成功的Result；如果查询结果为空，返回R.error并提示信息。*/
        return Result.success(pageInfo);
    }
    /**启用/禁用员工账号
     * 1.@PutMapping，无路径（路径employee已经写在类中），接受参数为@RequestBody Employee
     * 2.返回结果为R<String>*/
    @PutMapping
    public Result<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //Long updateId=(Long) request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(updateId);
        employeeService.save(employee);
        return Result.success("successfully updates");
    }
    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id){
        Employee emp=employeeService.getById(id);
        return Result.success(emp);
    }
}
