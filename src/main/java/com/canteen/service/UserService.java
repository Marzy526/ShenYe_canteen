package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.User;

public interface UserService extends IService<User> {
    void sendMsg(String phone, String subject, String context);
}

