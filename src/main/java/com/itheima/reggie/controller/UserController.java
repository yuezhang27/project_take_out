package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session){
        //接受传来的phone
        String phone=user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //调用验证码生成工具类生成验证码
            String code= ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用API方法发送验证码,skip this part need a business registration
            //把验证码存入session中
            session.setAttribute(phone,code);
            return Result.success("send code");
        }
        return Result.error("could not send code");
    }
    @PostMapping("/login")
    public Result<User> userLogin(@RequestBody Map map,HttpSession session){
        //用map接受传过来的phone和code
        String phone=map.get("phone").toString();
        String code=map.get("code").toString();
        //在session中获取code，若对应前端表单post过来的map里的code，则登录成功
        Object realCode=session.getAttribute(phone);
        if(realCode!=null&&code.equals(realCode)){
            //判断phone在user表中是否存在，，若不存在，save一个user数据
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user=userService.getOne(lambdaQueryWrapper);
            if(user==null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //完成登录，在session中存入user和其id便于后续登录验证
            session.setAttribute("user",user.getId());
            return Result.success(user);
        }
        //否则map不上，返回错误结果
        return Result.error("can not login");
    }
}
