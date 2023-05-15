package com.cqust.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqust.common.R;
import com.cqust.entity.User;
import com.cqust.service.UserService;
import com.cqust.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
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
    private R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 获取手机号码
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            // 生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            // 调用阿里云提供的短信API完成短信发送
            log.info("验证码为={}", code);
            // 需要将生成的验证码保存到Session
            session.setAttribute(phone, code);
            return R.success(null,"短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    private R<User> login(@RequestBody Map<String, String> map, HttpSession session){
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone");
        // 获取验证码
        String code = map.get("code");
        // 从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        // 进行验证码比对
        if(codeInSession != null && codeInSession.equals(code)){
            // 比对成功，则能登录

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            // 判断当前手机号是否为新用户
            if(user == null){
                // 新用户自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user,"success");
        }
        return R.error("登录失败");
    }

}
