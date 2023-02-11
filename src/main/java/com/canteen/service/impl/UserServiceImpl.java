package com.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.User;
import com.canteen.mapper.UserMapper;
import com.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{
    @Value("${spring.mail.username}")
    private String from;   // 邮件发送人
    @Resource
    private JavaMailSender javaMailSender;

    @Override
    public void sendMsg(String phone, String subject, String context) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(phone);
        mailMessage.setSubject(subject);
        mailMessage.setText(context);
        // 真正的发送邮件操作，从 from到 to
        javaMailSender.send(mailMessage);
    }
}
