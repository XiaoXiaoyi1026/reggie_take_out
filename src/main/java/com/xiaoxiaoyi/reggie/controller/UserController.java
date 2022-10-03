package com.xiaoxiaoyi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoxiaoyi.reggie.common.R;
import com.xiaoxiaoyi.reggie.entity.User;
import com.xiaoxiaoyi.reggie.service.MailService;
import com.xiaoxiaoyi.reggie.service.UserService;
import com.xiaoxiaoyi.reggie.utils.SMSUtils;
import com.xiaoxiaoyi.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RequestMapping("/user")
@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    /**
     * 发送验证码短信(模拟)
     *
     * @param user 用户对象，接收phone
     * @return 信息
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {
        log.info(user.getPhone());
        // 1. 获取手机号(其实是邮箱地址)
        String phoneNumber = user.getPhone();

        // 判断手机号是否为空
        if (StringUtils.isNotEmpty(phoneNumber)) {
            // 2. 生成随机4位验证码(ValidateCodeUtils)
            String code = String.valueOf(ValidateCodeUtils.generateValidateCode(4));
            // 3. 调用阿里云的短信服务Api发送短信(SMSUtils)
            //                      签名      模板code  电话号码  验证码
            // SMSUtils.sendMessage("瑞吉外卖", "", phoneNumber, code);
            // 使用邮箱查看验证码            目标邮箱             邮件主题                     内容
            try {
                mailService.sendSampleMail(phoneNumber, "Check Code", "本次登录验证码为：" + code);
            } catch (Exception e) {
                return R.error("发送验证码失败!请检查邮箱是否正确!!");
            }
            // 模拟查看验证码
            log.info("code: {}", code);
            // 4. 验证码存入session，以便用户点击登录时进行校验，根据用户的手机号码进行划分
            httpSession.setAttribute(phoneNumber, code);
            return R.success("验证码发送成功！");
        }

        return R.error("验证码发送失败！");
    }

    /**
     * 移动端用户点击登录后逻辑
     *
     * @param map         前端参数
     * @param httpSession session
     * @return 返回当前登录的用户信息
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession httpSession) {

        // 检查参数
        log.info(map.toString());

        // 1. 从map中获取手机号和验证码(前端页面提交)
        String phone = map.get("phone");
        String code = map.get("code");

        // 2. 从session根据手机号取出保存的验证码
        Object codeInSession = httpSession.getAttribute(phone);

        // 3. 进行比对(比对正确登陆成功)

        if (codeInSession != null && codeInSession.equals(code)) {
            // 比对成功 登陆成功

            // 4. 查询数据库，确认是否为新用户，如果为新用户则直接注册(存入数据库)

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);

            if (user == null) {
                // 新用户，进行注册
                user = new User();
                user.setPhone(phone);
                // 启用
                user.setStatus(1);
                userService.save(user);
            }
            // 将userId存入session，以通过前端filter校验
            httpSession.setAttribute("user", user.getId());
            // 向前端返回登录的用户信息
            return R.success(user);
        }

        return R.error("登录失败! ");
    }

}
